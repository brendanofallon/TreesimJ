package population;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import siteModels.CodonUtils;
import siteModels.CodonUtils.AminoAcid;
import statistics.Collectible;
import tree.DiscreteGenTree;

import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import dnaModels.DNASequence;
import errorHandling.ErrorWindow;
import fitnessProviders.DNAFitness;
import fitnessProviders.FitnessProvider;
import fitnessProviders.NeutralFitness;
import fitnessProviders.QGenFitness;


/**
 * Describes a population of Individuals and their ancestral relationships.  This class provides the function for creating
 *  a new generation of the population, as well as sampling a random tree from the population. 
 * @author brendan
 *
 */
public class Population implements Serializable, Collectible {

	ArrayList<Locus> pop; 	//List of individuals in current population
	Locus root; 			//Most basal individual.
	RandomEngine rng;   	//Base random number generator
	Uniform uniGenerator;   //Generator for uniform random variables
	Poisson poissonGenerator; //Generator for poisson random vars
	boolean preserve = false;	//Flag to signal preservation of ancestral genetic data (not always needed, and it saves lots of memory to turn it off)
	int calls = 0;
	int currentGen = 0;	//Counts number of calls to newGen
	
	ArrayList<Locus> preservedIndividuals; //All 'preserved' individuals, which are not (necessarily) in the current generation but are not to be disposed of (yet)

	private static int totalPopCount = 0;
	
	int myPopNumber;
	
	//Automatically walk the root up the population tree as much as possible each generation. If we only have
	//a single population this is desireably since individuals more basal than the root can be garbage collected. However
	// in multiple population scenarios individual pops can't generally do their own shortening, since there's a global
	//root that unites the roots of all single populations. 
	boolean autoShortenRoot = true;
	
	/**
	 * Create a population but do not initialize it (that is, do not create the individuals yet)
	 * @param rnger
	 */
	public Population() {
		preservedIndividuals = new ArrayList<Locus>();
		myPopNumber = getTotalPopCount();
		totalPopCount++;
		poissonGenerator = new Poisson(1.0, rng); //The mean gets set later
	}
	
	/**
	 * Creates a new population using the specified randomEngine, the specified initial size, the preserve_ancestral_data flag,
	 * and the specified fitnessProvider 
	 * 
	 */
	public Population(RandomEngine rnger, int size, boolean preserve, FitnessProvider type) {
		this.preserve = preserve;
		uniGenerator = new Uniform(rng);
		poissonGenerator = new Poisson(1.0, rng); //The mean gets set later
		preservedIndividuals = new ArrayList<Locus>();
		initialize(rnger, size, type);
		myPopNumber = getTotalPopCount();
		totalPopCount++;
	}
	
	
	/**
	 * Created the initial state of the population. One individual, the 'root', is the parent of all individuals 
	 * in the created generation. Mutate() is also called once on all Individuals. 
	 * @param N
	 * @param type
	 * @param inheritables
	 * @return the root individual of this population. If autoShortenRoot is on (which it is by default), the root will change over time. 
	 */
	public Locus initialize(RandomEngine rnger, int N, FitnessProvider type) {
		this.rng = rnger;
		uniGenerator = new Uniform(rng);
		poissonGenerator = new Poisson(1.0, rng); //The mean gets set later
		pop = new ArrayList<Locus>();
		
		root = new Locus(rng); 
		root.setParent(null);
		root.setFitnessProvider( type );
		
		for(int i=0; i<N; i++) { 
			Locus ind = new Locus(rng);
			ind.copyDataFrom(root);
			root.addOffspring(ind);
			ind.setParent(root);
			ind.mutate();
			pop.add( ind );
		}

		return root;
	}

	/**
	 * Initialize this population by taking N individuals from the given source Population
	 * @param rnger
	 * @param source
	 * @param N Number of founder individuals in this population
	 */
	public void initialize(RandomEngine rnger, Population source, int N ) {
		this.rng = rnger;
		uniGenerator = new Uniform(rng);
		poissonGenerator = new Poisson(1.0, rng); //The mean gets set later
		List<Locus> founders = source.getSample(N);
		pop = new ArrayList<Locus>(founders.size());
		
		for(Locus founder : founders) {
			Locus ind = new Locus(rng);
			ind.setParent(founder);
			founder.addOffspring(ind);
			ind.copyDataFrom(founder);
			
			if (totalPopCount>1) {
				ind.setOriginPopulation(myPopNumber);
			}
			pop.add(ind);
		}
		currentGen = source.getCurrentGenNumber();
	
		this.preserve = source.getPreservesAncestralData();
		root = source.getRoot();
	}
	
	/**
	 * Turns on / off automatic moving of population root up the tree each round. 
	 * @param autoShorten
	 */
	public void setAutoShortenRoot(boolean autoShorten) {
		this.autoShortenRoot = autoShorten;
	}
	
	/**
	 * Setting this to true causes all ancestral genetic information to be retained, instead of just fitness. This means we can go back in time
	 * and look at DNA sequences as they were many generations ago, but it's both slow (since sequences need to be copied more frequently) and
	 * takes a lot more memory. The default here is false. 
	 * @param preserve
	 */
	public void setPreserveData(boolean preserve) {
		this.preserve = preserve;
	}
	
	/**
	 * The fitnessProvider currently in use (since we don't support multiple fitness models being used in the same population, we can
	 * just take the fitness model from a random individual, say 0).
	 * @return
	 */
	public FitnessProvider getFitnessModel() {
		return pop.get(0).getFitnessData();
	}
	
	/**
	 * Total number of times newGen has been called
	 * @return
	 */
	public int getCurrentGenNumber() {
		return currentGen;
	}
	
	/**
	 * Set the current generation number value
	 * @param gen
	 */
	public void setCurrentGenNumber(int gen) {
		this.currentGen = gen;
	}
	
	/**
	 * Change the master sequence for all individuals. Probably not ready for production use. 
	 * @param newMaster The new master sequence
	 */
	public void changeMasterSequence(DNASequence newMaster) {
		try {
			for(Locus ind : pop) {
				DNAFitness dnaFitness = (DNAFitness) ind.getFitnessData();
				dnaFitness.setMasterSequence(newMaster);
			}
		}
		catch (ClassCastException cce) {
			ErrorWindow.showErrorWindow(new IllegalStateException("Cannot set the master sequence for individuals with no DNA fitness model"));
		}

	}
	
	
	/**
	 * Find the most recent common ancestor that is the parent of all individuals in the given sample. This method is 
	 * recombination aware.  
	 * @param sample
	 */
	public Locus findFC(List<Locus> sample) {
		int depth = 0;
		
		Set<Locus> parentSet = new HashSet<Locus>();
		
		int nodeTotal = 0;
		//System.out.println("Finding fc...");
		while(sample.size()>1) {
//			if (getCurrentGenNumber()%100==0 && depth%10==0)
//				System.out.println("Ancestors at depth " + depth + " : " + sample.size());
			depth++;
			nodeTotal += sample.size();
			List<Locus> parents = new ArrayList<Locus>(pop.size());
			parents.clear();
			parentSet.clear();
			
			for(int i=0; i<sample.size(); i++) {
				//If the parent is not already in the list, add it
				if (! parentSet.contains(sample.get(i).getParent())) {
					parents.add(sample.get(i).getParent());
					parentSet.add(sample.get(i).getParent());
				}
				
				//If this sample has a recombination, then we also add it's recomb. partner's parent. 
				if (sample.get(i).hasRecombination()) {
					Locus partner = sample.get(i).getRecombinationPartner();
					if (partner == null) {
						System.out.println("Yikes! Recomb. partner is null!");
					}
					if (partner.getParent()==null) {
						System.out.println("This node: " + sample.get(i).getReadableID() + " partner: " + partner.getReadableID());
						System.out.println("Yikes, recomb. partner's parent is null!");
					}
					if (! parentSet.contains( sample.get(i).getRecombinationPartner().getParent())) {
						parents.add( sample.get(i).getRecombinationPartner().getParent() );
						parentSet.add( sample.get(i).getRecombinationPartner().getParent() );
					}
				}
			}
			//System.out.println("tree size at depth " + depth + " : " + sample.size() );
			sample = parents;
		}
		
		//System.out.println("..done");
		
		if (getCurrentGenNumber()%50==0) {
			System.out.println("Current gen: "+ getCurrentGenNumber() + " FC depth : " + depth + " total nodes: " + nodeTotal);
		}
		return sample.get(0);
	}
	
	

	

	
	/**
	 * Whether or not the DNA state of all ancestral individuals (those not in the current gen) should be preserved. Setting
	 * this to true allows for some additional data to be collected, but uses a lot more memory and slows things down some
	 * @return
	 */
	public boolean getPreservesAncestralData() {
		return preserve;
	}
	
	/**
	 * Set the random number generator (rng) and generate a new uniform random number engine based on the rng as well
	 * @param rng
	 */
	public void setRandomEngine(RandomEngine rng) {
		this.rng = rng;
		uniGenerator = new Uniform(rng);
	}

	/**
	 * Returns the current gen (with references to ancestral pop)
	 * @return
	 */
	public ArrayList<Locus> getList() {
		return pop;
	}
	
	/**
	 * Returns current population size
	 * @return
	 */
	public int size() {
		return pop.size();
	}
	
	/**
	 * Returns true if all individuals in the pop have the same distance to the root
	 * @return
	 */
	public boolean isSane() {
		int d1 = pop.get(0).distToRoot();
		boolean is = true;
		for(Locus ind : pop) {
			if (ind.distToRoot() != d1) {
				is = false;
				break;
			}
		}
		return is;
	}
	
	/**
	 * Returns a sample of size sampleSize from the current population. This does not clone individuals, it just returns
	 * references to individuals in this population. 
	 * @param sampleSize
	 * @return
	 */
	public ArrayList<Locus> getSample(int sampleSize) {
		ArrayList<Locus> sample = new ArrayList<Locus>();
		
		while(sample.size()<Math.min(pop.size(), sampleSize)) {
			int which = uniGenerator.nextIntFromTo(0, pop.size()-1);
			Locus ind = pop.get(which);
			if (! sample.contains(ind) ) {
				sample.add( ind );
			}
		}
		
		return sample;
	}
	
	/**
	 * Returns the individual with the given id from the list
	 */
	public static Locus findIndByID(List<Locus> inds, long id) {
		for(Locus ind : inds) {
			if (ind.getID()==id)
				return ind;
		}
		return null;
	}
	

	

	/**
	 * For each individual in sample list, find the individual in the current population with matching id, and return them
	 * all in a list.
	 * @param sample
	 * @return List of individuals from current population whose ID's match the sample individual id's
	 */
	public ArrayList<Locus> findPopulationIndsForSample(List<Locus> sample) {
		ArrayList<Locus> popInds = new ArrayList<Locus>();
		
		for(Locus ind : sample) {
			Locus popInd = findIndByID(pop, ind.getID());
			popInds.add(popInd);
		}
		
		return popInds;
	}
	
	/**
	 * Performs a few checks for the validity of the tree, including making sure individuals have a parent and
	 * that the parent has a link back to the offspring, etc 
	 * @return
	 */
	public boolean checkSanity() {
		boolean sane = true;
		int depth = 0;
		
		for(Locus ind : pop) {
			while(ind.getParent() != root) {
				if (ind.getParent()==null) {
					sane = false;
					System.err.println("Individual " + ind.getID() + " has a null parent at depth: " + depth);
				}
				else {
					Locus parent = ind.getParent();
					if (! parent.getOffspring().contains(ind)) {
						System.err.println("Parent/offspring links are inconsistent for parent: " + parent.getID() + " at depth: " + depth);
						sane = false;
					}
				}

				if (ind.hasRecombination()) {
					if (ind != ind.getRecombinationPartner().getRecombinationPartner()) {
						System.err.println("Recombination links are inconsistent for ind: " + ind.getID() + " at depth: " + depth);
						sane = false;
					}
				}

				depth++;
				ind = ind.getParent();
			}
		}
		
		return sane;
	}
	
	/**
	 * This important method is called repeatedly by getSampleTree to create a cloned genealogy, with each call creating 
	 * one new 'generation' of the genealogy, in backward time, using the relationships and data in actualKids as the 
	 * structure to clone.  
	 * ActualKids are assumed to represent Loci in a 'real' Population, and thus they have parents with all appropriate
	 * references intact. sampleKids represents the growing sample, this method examines the parents and recombination partners
	 * of the actualKids, and creates parents and recombination partners for the sampleKids with identical data, ID's, and 
	 * structure. After doing this, everyone in sampleKids is replaced by the sample parents, and everyone in actualKids is 
	 * replaced by their parents, so repeated calls to this method continually add new generations to the growing sample. 
	 * Typical usage involves calling this repeatedly until actualKids and sampleKids contain one locus, which represents the
	 * MRCA of the sample.  
	 * @param actualKids
	 * @param sampleKids
	 */
	public static void createSampleParents(List<Locus> actualKids, List<Locus> sampleKids) {
		ArrayList<Locus> parents = new ArrayList<Locus>(); 			//The actual parents of the kids (in the persistent pop)
		ArrayList<Locus> sampleParents = new ArrayList<Locus>();    //The sampled (copied) parents
		
		if (actualKids.size() != sampleKids.size()) {
			throw new RuntimeException("ActualKids and sampleKids are not the same size.");
		}
		
		//Iterate over all actual kids, examining who their parents are, and constructing the exact same
		//relationship among the sampleKids 
		for(Locus actualKid : actualKids) {
			Locus actualParent = actualKid.getParent();
			Locus sampleKid = findIndByID(sampleKids, actualKid.getID());
			Locus sampleParent = findIndByID(sampleParents, actualParent.getID());
			
			if (! parents.contains(actualParent)) {
				//If parents already contains actualParent, then sampleParents should contain an ind with that ID as well...
				if ( sampleParent != null) {
					throw new RuntimeException("createSampleParents is not working, sampleParents and actualParents do not match");
				}
				sampleParent = actualParent.getDataCopy();
				sampleParent.setID( actualParent.getID());
				sampleParents.add(sampleParent);
				parents.add(actualParent);
			}
			
			sampleParent.addOffspring(sampleKid);
			sampleKid.setParent(sampleParent);
			
			//If the kid has a recombination, then find its partner, and create a new locus that is the corresponding sample partner.
			//Also, see if its partner's parent is already in the parents list. If so, just find it. If not, create a second
			//new sample locus that represents the partner's parent, and add the actual partner parent and sample partner parent
			//to their respective parent lists. 
			if (actualKid.hasRecombination()) {
				Locus actualPartner = actualKid.getRecombinationPartner();
				Locus samplePartner = actualPartner.getDataCopy();
				samplePartner.setID( actualPartner.getID());
				sampleKid.setRecombinationPartner(actualKid.getBreakPointMin(), actualKid.getBreakPointMax(), samplePartner);
				samplePartner.setRecombinationPartner(actualPartner.getBreakPointMin(), actualPartner.getBreakPointMax(), sampleKid);
				
				Locus actualPartnerParent = findIndByID(parents, actualPartner.getParent().getID());
				Locus samplePartnerParent = findIndByID(sampleParents, actualPartner.getParent().getID());
				
				//Error checking
				if ((actualPartnerParent == null && samplePartnerParent != null) || (actualPartnerParent != null && samplePartnerParent == null)) {
					throw new RuntimeException("Sample / actual parent lists invalid");
				}
				
				//If partner's parent not already in list of parents, then we must create a new parent for the sample partner,
				//and add both the actual partner parent and sample partner parent to the list of parents
				if ( actualPartnerParent==null) {
					actualPartnerParent = actualPartner.getParent();
					samplePartnerParent = actualPartnerParent.getDataCopy();
					samplePartnerParent.setID( actualPartnerParent.getID());
					sampleParents.add(samplePartnerParent);
					parents.add(actualPartnerParent);
				}
				
				//Always connect the sample partner to its parent
				samplePartnerParent.addOffspring(samplePartner);
				samplePartner.setParent(samplePartnerParent);
			}
		}
		
		actualKids.clear();
		actualKids.addAll(parents);
		sampleKids.clear();
		sampleKids.addAll(sampleParents);
	}
	
	/**
	 * Construct the genealogy of the given sample of individuals. All individuals in actualKids are cloned to produce the genealogy 
	 * so they are not actually members of the newly created tree (although they will have the same ID & data as those individuals 
	 * in the tips of the tree). 
	 * 
	 * The reason this is currently private is that we need to set some information in the sampleKids prior to cloning - namely their full ID,
	 * but also some debugging stuff like 'population'. This is done in getSampleTree(int) but not here since we don't
	 * want to do it twice... so for now we only support sampling trees from randomly selected individuals
	 * 
	 * @param sample A list of individuals,  whose genealogy is to be constructed.
	 * @return The root of the newly created genealogy
	 */
	private DiscreteGenTree getSampleTree(ArrayList<Locus> sample) {
		ArrayList<Locus> actualKids = findPopulationIndsForSample(sample);
		
		ArrayList<Locus> sampleKids = new ArrayList<Locus>();
		sampleKids.addAll(sample);
		
		boolean sane = checkSanity();
		if (!sane) {
			throw new IllegalStateException("Population is not sane!");
		}
		
		int iteration = 0;
		while(iteration < 5000000 && sampleKids.size()>1) {
			iteration++;
			//System.out.println("Iteration : " + iteration + " actual kids: " + actualKids.size() + " sample kids : " + sampleKids.size());
			
//			System.out.print("Depth: " + iteration + " : ");
//			for(Locus sampKid : sampleKids)
//				System.out.print(sampKid.getID() + "\t");
//			System.out.println();
			
			createSampleParents(actualKids, sampleKids);
			
			if (actualKids.size() != sampleKids.size() ) {
				throw new RuntimeException("Yikes! actual kids and samplekids do not have same size in sample tree creation!");
			}

			if (actualKids.size()>1) {
				for(Locus actualKid : actualKids) {
					if (actualKid.getParent()==null) {
						System.out.println("Uh-oh, an actual kid in the genealogy had a null parent (at depth " + iteration + "). Sample size : " + actualKids.size());
						if (actualKid.hasRecombination()) {
							System.out.println("Kid has recombination at breakpoints " + actualKid.getBreakPointMin() + "-" + actualKid.getBreakPointMax());
						}
						else{
							System.out.println("Kid has no recombination.");
						}
					}
				}
			}
		}
		
		if (iteration == 5000000) {
			System.err.println("Uh-oh, could not find a common ancestor for this sample of individuals (at least, not in 5M generations)");
			return null;
		}
		
		System.out.println("Found sample MRCA on iteration " + iteration);
		return new DiscreteGenTree(sampleKids.get(0), sample);
	}
	
	
	/**
	 * Randomly selects sampleSize individuals from the current generation of the population, clones them all (via a call to getDataCopy), and
	 * then reconstructs the genealogy of the individuals and returns the root. 
	 * 
	 * @param sampleSize
	 * @return The root of the sampled tree 
	 */
	public DiscreteGenTree getSampleTree(int sampleSize) {
		ArrayList<Locus> actualKids = getSample(sampleSize);
		ArrayList<Locus> sampleKids = new ArrayList<Locus>();

		for(Locus kid : actualKids) {
			Locus sampleKid = kid.getDataCopy();
			sampleKid.setPop("sample");
			sampleKid.setID( kid.getID() );
			sampleKids.add( sampleKid );
		}
	
		return getSampleTree(sampleKids);
	}
	
	/**
	 * Returns the Individual at index 'which' in the population
	 * @param which
	 * @return
	 */
	public Locus getInd(int which) {
		if (which > pop.size())
			return null;
		else
			return pop.get(which);
	}
	
	/**
	 * Creates a new generation with the same size as the previous generation
	 */
	public void newGen() {
		newGen(pop.size());
	}
	
	
	
	/**
	 * Creates a new generation with population size given by newSize
	 */
	public void newGen(int newSize) {
		currentGen++;
		ArrayList<Locus> newPop = new ArrayList<Locus>();
		
		if (pop==null) {
			throw new IllegalStateException("List is null, for pop #" + myPopNumber);
		}
		
		if (currentGen % 10 ==0) {
			root = findFC(pop);
			root.setParent(null);
		}
		shortenRoot();
		
		while(newPop.size() < newSize) {
			int who = uniGenerator.nextIntFromTo(0, pop.size()-1);
			Locus parent = pop.get( who );
			
			if (uniGenerator.nextDouble() < 0.5*parent.getRelFitness() ) {		
				Locus kid = new Locus(rng);
			
				kid.setParent( parent ) ;
				parent.addOffspring(kid);

				//If we preserve ancestral data, then always copy all data from parent to offspring
				if (preserve) {
					kid.copyDataFrom( parent );
				}
				else { //If not preserving data, just pass a reference from parent to offspring
					if (parent.numOffspring()==1)
						kid.inheritFrom(parent);
					else
						kid.copyDataFrom(parent);	
				}

				if (totalPopCount>1) {
					kid.setOriginPopulation(myPopNumber);
				}
				
				newPop.add(kid);
			}
		}
		
		double newMeanW = 0;
		for(Locus ind : newPop) {
			ind.mutate();
			newMeanW += ind.getFitness();
		}
		newMeanW /= (double)newPop.size();
		
		boolean qgen = newPop.get(0).getFitnessData() instanceof QGenFitness;
		
		
		//Now set Individual relative fitnesses
		for(Locus ind : newPop) {
			if (qgen) {
				ind.getFitnessData().setFitness( ind.getFitness()/newMeanW ); //Most fitness models will ignore this,
				ind.setRelFitness( ind.getFitness() );
			}
			else {
				ind.setRelFitness( ind.getFitness()/newMeanW );
			}
		}

		
		//Remove references from those individuals in the parental population that had
		//zero offspring. This allows the garbage collector to collect these items
		//At some point it may be more efficient to return these to a pool...
		int newlyPreserved = 0;
		for(Locus ind : pop) {
			if (ind.isPreserve()) {
				preservedIndividuals.add(ind);
				newlyPreserved++;
			}
			else {
				if (ind.numOffspring()==0 && !ind.isPreserve())
					releaseIndividual(ind);
			}
		}
		
//		if (newlyPreserved>0)
//			System.out.println("Preserving " + newlyPreserved + " new individuals; total is now " + preservedIndividuals.size() );
		
		 pop = newPop;
		 
		 if (autoShortenRoot)
			 shortenRoot();
		 
		 if (calls % 500 == 0) {
			 for(Locus ind : pop) {
				 DNASequence master = ((DNAFitness) ind.getFitnessData()).getMaster();
				 ((DNAFitness)ind.getFitnessData()).verifyFitness(master);
			 }
		 }
		 
		 recombine();
		 calls++;
	}
	
	/**
	 * Move the root toward the tips while root has only a single offspring and no recombinations 
	 */
	public void shortenRoot() {
		 while(root.numOffspring()==1 && !root.hasRecombination()) {
			 root.setParent(null);
			 root = root.getOffspring(0);
		 }
	}
	
	
	/**
	 * Pick some individuals from the population to recombine, and then recombine them. The 'rate' is the
	 * individual rate of recombination, such that the total expected number of individuals who recombine this
	 * generation is rate*N. 
	 * @param rate Recombination rate, such that expected number of recombinations is N*rate
	 */
	protected void recombine() {
		int count = 0;
		FitnessProvider fitnessModel = pop.get(0).getFitnessData();
		if (! (fitnessModel instanceof DNAFitness)) {
			return;
		}
		DNAFitness dnaFitness = (DNAFitness)fitnessModel;
		double rate = dnaFitness.getMutationModel().getRecombinationRate();
		poissonGenerator.setMean(rate / 2.0 * pop.size()); //Since each recombination event involves two individuals, rate/2 is the pairwise rate
		
		int recombiningPairs = poissonGenerator.nextInt();
		//System.out.println("Recombining with rate : " + (rate / 2.0 * pop.size()) + " actual number of recombining pairs: " + recombiningPairs);
		for(int i=0; i<recombiningPairs; i++) {
			Locus one = pop.get(uniGenerator.nextIntFromTo(0, pop.size()-1));
			while( one.hasRecombination() && count < 1000) {
				one = pop.get(uniGenerator.nextIntFromTo(0, pop.size()-1));
				count++;
			}
			
			if (count==1000)
				return;
			count = 0;
			Locus two = pop.get(uniGenerator.nextIntFromTo(0, pop.size()-1));
			while( (two.hasRecombination() || two==one) && count < 1000) {
				two = pop.get(uniGenerator.nextIntFromTo(0, pop.size()-1));
				count++;
			}
			
			if (count==1000)
				return;
			
			//System.out.println("Recombining " + ((Individual) one).getReadableID() + " and "+  ((Individual) two).getReadableID() );
			Locus.recombine(one, two); //Actually a static method, but since it's from an interfarce it can't actually be static
		}
	}
	
	/**
	 * Remove this specific locus from the population and sets all references to and from this locus to null
	 * @param loc
	 */
	private void clearLocus(Locus loc) {
		//System.out.println("clearing locus " + loc.getID() + " now");
		if (loc.hasRecombination()) {
			Locus partner = loc.getRecombinationPartner();
			if (partner.numOffspring()>0){
				System.out.println("Hmm...we're clearing an ind. that has recomb. and a partner with > 0 offspring");
			}
		}
		loc.getParent().removeOffspring(loc);
		loc.clearReferences();
	}
	
	/**
	 * Attempt to remove the given individual from the population and clear it's links so that it can be garbage collected.
	 * This is done in a recombination-aware manner, and only releases individuals if they have zero offspring and either
	 * are have no recombination partner or a recombination partner also with zero offspring. 
	 * 
	 * In general, it's not safe to call this haphazardly on just a few individuals from some generation, because it may
	 * create individuals with null parents. To avoid this, it must be called repeatedly for every releaseable individual
	 * in a current gerneration.  
	 * @param loc
	 * @return
	 */
	private int releaseIndividual(Locus loc) {
		if (loc.numOffspring()>0)
			return 0;
		
		if (loc.getParent()==null) {
			//This is either the (or a) root or a locus whose recombination partner released it's parent on a previous
			//call to releaseLocus().. this is valid since loc has no offspring and hence can never be referenced, but, since loc
			//still exists in the pop list (the vector of parents) this function may be called on it. In this case we can safely ignore
			//this call, since this locus has already been 'released'
			return 0;
		}
		
		Stack<Locus> relInds = new Stack<Locus>();
		Set<Locus> relIndSet = new HashSet<Locus>(); //Another buffer used to quickly look up which parents are already in the stack
		
		int tot = 0;
		Locus locRef;
	
		//A stack of loci that are definitely going to be removed.. they're added only if
		//they have an offspring ref count == 0
		
		relInds.clear();
		relIndSet.clear();
		relInds.add(loc);
		relIndSet.add(loc);
		
		while(relInds.size()>0) {
			locRef = relInds.pop();

			if (locRef.numOffspring()!=0) {
				System.err.println("Hmm, locRef ind has nonzero offspring count");
			}
			
			if (locRef.hasRecombination()) {
				Locus partner = locRef.getRecombinationPartner();
				
				if (partner.numOffspring()==0) {
					Locus parent = locRef.getParent();
					
					clearLocus(locRef);
					if (parent.numOffspring()==0 && (!relIndSet.contains(parent))) {
						relInds.add(parent);
						relIndSet.add(parent);
					}
					
					//We only add if the partner's recombination partner isn't null.. this is because if we already
					//released the partner it's recomb. ref will be set to null, otherwise we'll end up adding it
					//to the stack again
					if (!relIndSet.contains(partner) && partner.getRecombinationPartner()!=null) {
						relInds.add(partner);
						relIndSet.add(partner);
					}
				}
			}
			else {
				//Only one parent here since there's no recombinations
				Locus parent = locRef.getParent();
				
				clearLocus(locRef);
				if (parent.numOffspring()==0 && (!relIndSet.contains(parent))) {
					relInds.add(parent);
					relIndSet.add(parent);
				}
			}
		}
		return tot;
	}
	
	/**
	 * Called to dispose of all preserved individuals. If there are preserved inds and this is not called, the tree
	 * will grow in size until the heap gets too big, and then the program will crash.
	 */
	public void releasePreservedInds() {
		
		//There's a chance that pop contains some individuals which have just now (since the last call to newGen) been
		//preserved, in this case these individuals will not be in the preservedInds list. We do a quick check here
		//to make sure see if there are any newly flagged inds that are not in the last
		for(Locus ind : pop) {
			if (ind.isPreserve() && !preservedIndividuals.contains(ind))
				preservedIndividuals.add(ind);
		}
		
		for(Locus ind : preservedIndividuals) {
			ind.setPreserve(false);
		}
		
		//System.out.println("Releasing " + preservedIndividuals.size() + " preserved inds ");
		for(Locus ind : preservedIndividuals) {
			if (ind.numOffspring()==0 && !pop.contains(ind)) { //Remove all inds not in the current generation
				releaseIndividual(ind);
			}		
		}
		
		preservedIndividuals.clear();
		shortenRoot();
	}


	
	/**
	 * Return the root individual of this population, which is the ancestor of all inds in this pop. Note that this may be
	 * meaningless, for instance, in populations that are part of multi-population demo. models there is no single root 
	 * individual tracked by individual populations. In those situations the root is tracked by the demographic model 
	 * (typically in by the globalRoot individual in the MultiPopDemoModel class)
	 * @return
	 */
	public Locus getRoot() {
		return root;
	}
	
	/**
	 * Remove num individuals from this population and return them in a list
	 * @param num The number of individuals to remove
	 * @return A list of Individuals removed
	 */
	public List<Locus> removeIndividuals(int num) {
		List<Locus> sample = new ArrayList<Locus>(num);
		if (num > pop.size()) {
			throw new IllegalArgumentException("Cannot sample " + num + " individuals from a population of size : " + pop.size());
		}
		
		while(sample.size()<num) {
			int which = uniGenerator.nextIntFromTo(0, pop.size()-1);
			Locus ind = pop.get(which);
			if (! sample.contains(ind) ) {
				sample.add( ind );
				pop.remove(ind);
			}
		}
		
		return sample;
	}

	/**
	 * Add the sample of individuals to the current population. 
	 * @param migrants
	 */
	public void addIndividuals(List<Locus> migrants) {
		for(Locus ind : migrants) {
			pop.add(ind);
		}
	}

	/**
	 * Set the total population count back to zero
	 */
	public static void resetTotalPopCount() {
		Population.totalPopCount = 0;
	}

	/**
	 * Get the total number of populations currently tracked
	 * @return
	 */
	public static int getTotalPopCount() {
		return totalPopCount;
	}

	
}
