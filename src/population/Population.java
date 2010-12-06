package population;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import siteModels.CodonUtils;
import siteModels.CodonUtils.AminoAcid;
import statistics.Collectible;

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

	ArrayList<Individual> pop; //List of individuals in current population
	Individual root; //Most basal individual.
	RandomEngine rng;   //Base random number generator
	Uniform uniGenerator;  //Generator for uniform random variables
	boolean preserve = false;	//Flag to signal preservation of ancestral genetic data (not always needed, and it saves lots of memory to turn it off)
	int calls = 0;
	int currentGen = 0;	//Counts number of calls to newGen
	
	ArrayList<Individual> preservedIndividuals; //All 'preserved' individuals, which are not (necessarily) in the current generation but are not to be disposed of (yet)

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
		
		preservedIndividuals = new ArrayList<Individual>();
		myPopNumber = getTotalPopCount();
		totalPopCount++;
	}
	
	/**
	 * Creates a new population using the specified randomEngine, the specified initial size, the preserve_ancestral_data flag,
	 * and the specified fitnessProvider 
	 * 
	 */
	public Population(RandomEngine rnger, int size, boolean preserve, FitnessProvider type) {
		this.preserve = preserve;
		uniGenerator = new Uniform(rng);
		preservedIndividuals = new ArrayList<Individual>();
		initialize(rnger, size, type, null);
		myPopNumber = getTotalPopCount();
		totalPopCount++;
	}
	
	public Population(RandomEngine rnger, int size, boolean preserve, FitnessProvider type, Inheritable item) {
		ArrayList<Inheritable> inherits = new ArrayList<Inheritable>();
		inherits.add(item);
		initialize(rnger, size, type, inherits);
		this.preserve = preserve;
		preservedIndividuals = new ArrayList<Individual>();
		uniGenerator = new Uniform(rng);
		myPopNumber = getTotalPopCount();
		totalPopCount++;
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
		return pop.get(0).fitnessData;
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
			for(Individual ind : pop) {
				DNAFitness dnaFitness = (DNAFitness) ind.getFitnessData();
				dnaFitness.setMasterSequence(newMaster);
			}
		}
		catch (ClassCastException cce) {
			ErrorWindow.showErrorWindow(new IllegalStateException("Cannot set the master sequence for individuals with no DNA fitness model"));
		}

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
	public ArrayList<Individual> getList() {
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
		for(Individual ind : pop) {
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
	public ArrayList<Individual> getSample(int sampleSize) {
		ArrayList<Individual> sample = new ArrayList<Individual>();
		
		while(sample.size()<Math.min(pop.size(), sampleSize)) {
			int which = uniGenerator.nextIntFromTo(0, pop.size()-1);
			Individual ind = pop.get(which);
			if (! sample.contains(ind) ) {
				sample.add( ind );
			}
		}
		
		return sample;
	}
	
	/**
	 * Returns the individual with the given id from the list
	 */
	public static Individual findIndByID(List<Individual> inds, long id) {
		for(Individual ind : inds) {
			if (ind.getID()==id)
				return ind;
		}
		return null;
	}
	
	/**
	 * Creates a list containing newly created individuals who are the parents of the 'kids', without repetition (no duplicate parents),
	 * and copying the parent-offspring relations of the kids and their real parents. The new parental generation
	 * is attached to the sampleKid generation via parent-offspring references
	 * @param kids
	 */
	public static void createSampleParents(List<Individual> actualKids, List<Individual> sampleKids) {
		List<Individual> parents = new ArrayList<Individual>(); //The actual parents of the kids (in the persistent pop)
		List<Individual> sampleParents = new ArrayList<Individual>(); //The sampled (copied) parents
		
		for(Individual kid : actualKids) {
			if ( findIndByID(parents, kid.getParent().getID())!=null ) {	//The parent of kid is already in the list of parents, all we have to do is update references  
				Individual sampleParent = findIndByID(sampleParents, kid.getParent().getID());
				Individual sampleKid = findIndByID(sampleKids, kid.getID());
				if (sampleKid==null) 
					System.err.println("2: Uh-oh, could not find sampleKid with id=" + kid.getID()  );
				sampleParent.addOffspring( sampleKid );
				sampleKid.setParent( sampleParent);
			}
			else {	//The parent of kid is not in the list of sampled parents, so we must create a copy of the parent and add it to sampleParents
				parents.add( kid.getParent() );
				Individual sampleParent = kid.getParent().getDataCopy();
				sampleParent.setID( kid.getParent().getID() );
				
				Individual sampleKid = findIndByID(sampleKids, kid.getID());
				if (sampleKid==null) 
					System.err.println("1: Uh-oh, could not find sampleKid with id=" + kid.getID()  );
				sampleParent.addOffspring(sampleKid);
				sampleKid.setParent( sampleParent );
				
				sampleParents.add( sampleParent );
			}
		}
		

		sampleKids.clear();
		sampleKids.addAll(sampleParents);
		actualKids.clear();
		actualKids.addAll(parents);
	}
	

	
	/**
	 * Returns a sample of size sampleSize - and re-creates the whole tree structure of their ancestors
	 * 
	 * @param sampleSize
	 * @return The root of the sampled tree 
	 */
	public Individual getSampleTree(int sampleSize) {
		ArrayList<Individual> actualKids = getSample(sampleSize);
		ArrayList<Individual> sampleKids = new ArrayList<Individual>();

		
		for(Individual kid : actualKids) {
			Individual sampleKid = kid.getDataCopy();
			sampleKid.setID( kid.getID() );
			sampleKids.add( sampleKid );
		}
		
		int iteration = 0;
		while(iteration < 1000000 && sampleKids.size()>1) {
			iteration++;
			//System.out.println("Iteration : " + iteration + " actual kids: " + actualKids.size() + " sample kids : " + sampleKids.size());
			createSampleParents(actualKids, sampleKids);
		}
		
		if (iteration == 1000000) {
			System.err.println("Uh-oh, could not find a common ancestor for this sample of individuals (at least, not in 10000K generations)");
			return null;
		}
		
		return sampleKids.get(0);
	}
	
	/**
	 * Returns the Individual at index 'which' in the population
	 * @param which
	 * @return
	 */
	public Individual getInd(int which) {
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
		ArrayList<Individual> newPop = new ArrayList<Individual>();
		
		if (pop==null) {
			throw new IllegalStateException("List is null, for pop #" + myPopNumber);
		}
		
		while(newPop.size() < newSize) {
			int who = uniGenerator.nextIntFromTo(0, pop.size()-1);
			Individual parent = pop.get( who );
			
			if (uniGenerator.nextDouble() < 0.5*parent.getRelFitness() ) {		
				Individual kid = new Individual(rng);
			
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
		for(Individual ind : newPop) {
			ind.mutate();
			newMeanW += ind.getFitness();
		}
		newMeanW /= (double)newPop.size();
		
		boolean qgen = newPop.get(0).getFitnessData() instanceof QGenFitness;
		
		
		//Now set Individual relative fitnesses
		for(Individual ind : newPop) {
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
		for(Individual ind : pop) {
			if (ind.isPreserve()) {
				preservedIndividuals.add(ind);
				newlyPreserved++;
			}
			else {
				if (ind.numOffspring()==0 && !ind.isPreserve())
					ind.removeFromPop();
			}
		}
		
//		if (newlyPreserved>0)
//			System.out.println("Preserving " + newlyPreserved + " new individuals; total is now " + preservedIndividuals.size() );
		
		 pop = newPop;
		 
		 if (autoShortenRoot)
			 shortenRoot();
		 
//		 if (calls % 100 == 0) {
//			 for(Individual ind : pop) {
//				 DNASequence master = ((DNAFitness) ind.getFitnessData()).getMaster();
//				 ((DNAFitness)ind.getFitnessData()).verifyFitness(master);
//			 }
//		 }
		 
		 calls++;
	}
	
	/**
	 * Move the root toward the tips while root has only a single offspring
	 */
	public void shortenRoot() {
		 while(root.numOffspring()==1) {
			 root.setParent(null);
			 root = root.getOffspring(0);
		 }
	}
	
	/**
	 * Called to dispose of all preserved individuals. If there are preserved inds and this is not called, the tree
	 * will grow in size until the heap gets too big, and then the program will crash.
	 */
	public void releasePreservedInds() {
		
		//There's a chance that pop contains some individuals which have just now (since the last call to newGen) been
		//preserved, in this case these individuals will not be in the preservedInds list. We do a quick check here
		//to make sure see if there are any newly flagged inds that are not in the last
		for(Individual ind : pop) {
			if (ind.isPreserve() && !preservedIndividuals.contains(ind))
				preservedIndividuals.add(ind);
		}
		
		for(Individual ind : preservedIndividuals) {
			ind.setPreserve(false);
		}
		
		//System.out.println("Releasing " + preservedIndividuals.size() + " preserved inds ");
		for(Individual ind : preservedIndividuals) {
			if (ind.numOffspring()==0 && !pop.contains(ind)) { //Remove all inds not in the current generation
				ind.removeFromPop();
			}		
		}
		
		preservedIndividuals.clear();
		shortenRoot();
	}

	/**
	 * Created the initial state of the population. One individual, the 'root', is the parent of all individuals 
	 * in the created generation. Mutate() is also called once on all Individuals. 
	 * @param N
	 * @param type
	 * @param inheritables
	 * @return the root individual of this population. If autoShortenRoot is on (which it is by default), the root will change over time. 
	 */
	public Individual initialize(RandomEngine rnger, int N, FitnessProvider type, ArrayList<Inheritable> inheritables) {
		this.rng = rnger;
		uniGenerator = new Uniform(rng);
		pop = new ArrayList<Individual>();
		
		root = new Individual(rng); 
		root.setParent(null);
		root.setFitnessProvider( type );
		if (inheritables != null)
			for(Inheritable item : inheritables)
				root.addInheritable(item);
		
		
		for(int i=0; i<N; i++) { 
			Individual ind = new Individual(rng);
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
		
		List<Individual> founders = source.getSample(N);
		pop = new ArrayList<Individual>(founders.size());
		
		for(Individual founder : founders) {
			Individual ind = new Individual(rng);
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
	 * Return the root individual of this population, which is the ancestor of all inds in this pop. Note that this may be
	 * meaningless, for instance, in populations that are part of multi-population demo. models there is no single root 
	 * individual tracked by individual populations. In those situations the root is tracked by the demographic model 
	 * (typically in by the globalRoot individual in the MultiPopDemoModel class)
	 * @return
	 */
	public Individual getRoot() {
		return root;
	}
	
	/**
	 * Remove num individuals from this population and return them in a list
	 * @param num The number of individuals to remove
	 * @return A list of Individuals removed
	 */
	public List<Individual> removeIndividuals(int num) {
		List<Individual> sample = new ArrayList<Individual>(num);
		if (num > pop.size()) {
			throw new IllegalArgumentException("Cannot sample " + num + " individuals from a population of size : " + pop.size());
		}
		
		while(sample.size()<num) {
			int which = uniGenerator.nextIntFromTo(0, pop.size()-1);
			Individual ind = pop.get(which);
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
	public void addIndividuals(List<Individual> migrants) {
		for(Individual ind : migrants) {
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
