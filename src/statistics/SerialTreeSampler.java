package statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import population.Locus;
import population.Population;

import tree.DiscreteGenTree;

/**
 * This class collects trees in which not all individuals are members of the same generation. Experimental!
 * 
 * It taken a certain number of generations for this thing to generate a new tree, and therefore it cannot
 * immediately be called on to sample a tree and then give it to some TreeCollectionListener. The current
 * scheme is to override sampleFrequency so that .collect() is called every generation, and to fire new tree
 * collection events only when a new tree is finally generated. This means that we ignore requestCollection()
 * collectAndFire() and collectWithoutFiring() - only this thing gets to decide when to collect trees.  
 * 
 * @author brendan
 *
 */
public class SerialTreeSampler extends TreeSampler {

	int numSamplingPeriods; //The number of times at which samples are taken
	int sampleSize;	//The number of individuals sampled at EACH time (not the total number of tips)
	int sampleDelay; //The delay between sampling periods
	int collectionFrequency; //How often to start generating a new tree
	
	int collectionOffset = 0;
	
	Map<Long, Locus> indsInTree; //Map of all individuals so far added to the tree, indexed by id for speedy lookup
	
	ArrayList<Locus> tips; //List of individuals sampled thus far
	
	protected boolean collecting = false;
	int startGen;
	
	int lastGenCollected = -1; //Kinda klugy, but if we get called multiple times in the same generation it could result in
								//multiple samples from the same gen. So we check to make sure we only get called once per gen
							  //gen by keeping track of the last time collect() was called. 
	
	int firstGenCalled = -1;
	
	DiscreteGenTree lastTree;
	
	
	private int nextSampleIndex = 0;
	int[] samplingTimes = null;
	int[] sampleSizes = null;
	
	public SerialTreeSampler(int[] samplingTimes, int[] sampleSizes) {
		this.samplingTimes = samplingTimes;
		this.sampleSizes = sampleSizes;
		this.collectionFrequency = samplingTimes[ samplingTimes.length-1]*5;
		System.out.println("Initiating new serial tree sampler with collection frequency " + collectionFrequency);
		if (samplingTimes.length != (sampleSizes.length)) {
			throw new IllegalArgumentException("There must be the same number of sampling times than sample sizes!");
		}
		indsInTree = new HashMap<Long, Locus>();
	}
	
	public SerialTreeSampler(int samplingPeriods, int sampleSize, int delay) {
		this.numSamplingPeriods = samplingPeriods;
		this.sampleSize = sampleSize;
		this.sampleDelay = delay;
		this.collectionFrequency = samplingPeriods*delay*5;
		System.out.println("SerialTreeSampler collection frequency : " + collectionFrequency);
		System.out.println("Constructing serial tree sampler with " + samplingPeriods + " sampling periods, each with sample size " + sampleSize + " and time between periods " + delay);
		indsInTree = new HashMap<Long, Locus>();
	}
	
	public void setSampleFrequency(int freq) {
		this.collectionFrequency = freq;
	}
	
	public int getNumSamplingPeriods() {
		return numSamplingPeriods;
	}
	
	/**
	 * This collect method works a bit differently than other Statistics. It is called every generation, and on 
	 * certain generations marks individuals in the persistent Population for 'preservation', meaning that they
	 * (and all of their ancestors) will not be removed from the population. After a number of sampling periods, 
	 * a sample tree is constructed from all of the serially-sampled individuals and put in lastTree, which is
	 * accessible through getLastTree() 
	 */
	public void collect(Collectible pop) {
		//System.out.println("Serial tree sampler is collecting");
		
		if (pop.getCurrentGenNumber()==lastGenCollected) {
			//System.out.println("Returning since last gen collected = " + pop.getCurrentGenNumber());
			return;
		}
		
		if (firstGenCalled<0)
			firstGenCalled = pop.getCurrentGenNumber();
		
		//System.out.println("Not returning. on gen " + pop.getCurrentGenNumber());
		lastGenCollected = pop.getCurrentGenNumber();
		
		if (firstGenCalled==pop.getCurrentGenNumber() || (pop.getCurrentGenNumber()-firstGenCalled)%collectionFrequency==0) {
			if (collecting) {
				//System.out.println("We're still collecting but we're trying to initiate a new collection period");
				return;
			}
			System.out.println("Gen : " + pop.getCurrentGenNumber() + " starting new serial data collection, N=" + pop.size());
			tips = new ArrayList<Locus>();
			startGen = pop.getCurrentGenNumber();
			collecting = true;
		} 
		
		
		if (collecting) { 
			int elapsedGens = pop.getCurrentGenNumber()-startGen;
			
			if ( samplingTimes[nextSampleIndex]==elapsedGens) { 
				List<Locus> sample = pop.getSample(sampleSizes[nextSampleIndex]);
				
				System.out.println("Gen : " + pop.getCurrentGenNumber() + " Flagging " + sample.size() + " new individuals for pres");
				for(Locus ind : sample) {
					ind.setPreserve(true);
					tips.add(ind); 
				}
				
				nextSampleIndex++;
			}

			
			if (nextSampleIndex==samplingTimes.length ) {
				constructTreeFromTips();
				pop.releasePreservedInds();
				collecting = false;
				indsInTree.clear();
				tips.clear();	//It's probably important that we don't retain references to any individuals in the persistent pop
				addDepthLabelsToTree();
				System.out.println("Serial tree sampler has finished another tree, N=" + pop.size());
				fireNewTreeEvent(lastTree);
				nextSampleIndex = 0;
			}
			
		}	
		
	}

	/**
	 * Don't sample these trees during the burn in period
	 */
	public boolean collectDuringBurnin() {
		return false;
	}
	
	
	/**
	 * Asks this treeSampler to generate a tree and have a given listener collect the data from it.
	 * @param pop
	 * @param listener
	 */
	public void requestCollection(Population pop, TreeCollectionListener listener) {	}
	
	/**
	 * Has no effect, we can't collect on command. 
	 * @param pop
	 */
	public void collectAndFire(Population pop) {	}
	
	
	/**
	 * Has no effect, we can't collect these on command
	 * @param pop
	 */
	public void collectWithoutFiring(Population pop) {	}
	
	/**
	 * Using the serially-sampled individuals in tips, we clone each individual, and add it and all of its ancestors
	 * to the tree
	 */
	private void constructTreeFromTips() {
		if (tips==null || tips.size()==0) {
			System.err.println("Cannot construct tree, there are no tips");
		}
		
		Locus root = addAncestorsToTree(tips.get(0));
		
		for(int i=1; i<tips.size(); i++) {
			addAncestorsToTree(tips.get(i));
		}
		
		
		//Bring root 'up' (tipward) until it has more than one offspring
		while(root.numOffspring()==1) {
			Locus tail = root;
			root = root.getOffspring(0);
			root.setParent(null);
			tail.removeOffspring(root);
		}
		
		lastTree = new DiscreteGenTree(root, (List<Locus>)tips.clone());
	}
	
	public DiscreteGenTree getLastTree() {
		return lastTree;
	}
	
	/**
	 * Sets the depth field of all tips to the correct value
	 */
	private void addDepthLabelsToTree() {
		int height = lastTree.getMaxHeight();
		List<Locus> tips = lastTree.getTips();
		
		for(Locus tip : tips) {
			int dist = tip.distToRoot();
			tip.setDepth(height-dist);
		}
	}
	
	/**
	 * Adds cloned ancestors of popInd to a sample tree until either the root of the tree is reached, or 
	 * the parent of the growing lineage is already in the tree, in which case we just attach to the existing
	 * parent and end there. 
	 * @param popInd An individual from the persistent population
	 */
	private Locus addAncestorsToTree(Locus popInd) {
		int count = 0;
				
		//There's a chance that popInd is already in the tree, if so, we're done
		if (indsInTree.containsKey(popInd.getID())) {
			return null;
		}
		
		Locus sampleInd = popInd.getDataCopy();
		sampleInd.setID(popInd.getID());
		
		Locus actualParent = popInd.getParent();

		Locus sampleParent = indsInTree.get(actualParent.getID());
		
		while (sampleParent==null && actualParent!=null) {
			
			sampleParent = actualParent.getDataCopy();
			sampleParent.setID(actualParent.getID());
			
			sampleParent.addOffspring(sampleInd);
			sampleInd.setParent(sampleParent);
			
			indsInTree.put(sampleParent.getID(), sampleParent);
			
			sampleInd = sampleParent;
			actualParent = actualParent.getParent();
			
			if (actualParent!=null) //We may be at the root
				sampleParent = indsInTree.get(actualParent.getID());
			
			count++;
		}
		
		//System.out.println("Added " + count + " individuals to tree");
		
		if (actualParent==null) {
			//System.out.println("Actual parent is null, must've gotten back to the root. Root has id: " + sampleInd.getReadableID());
			return sampleInd;
		}
		if (sampleParent!=null) {
			//System.out.println("Sample parent was found in tree, has id : " + sampleParent.getReadableID());
			sampleParent.addOffspring(sampleInd);
			sampleInd.setParent(sampleParent);
			return sampleParent;
		}
	
		System.err.println("Hmm actual parent was not null, but sample parent is null, we should not get here.");
		return null;
	}

	public boolean useDefaultCollectionFrequency() {
		return false;
	}
	
	/**
	 * We want .collect to get called every generation 
	 * @return
	 */
	public int getSampleFrequency() {
		return 1;
	}
	

	public String getDescription() {
		return "Generates serially sampled trees";
	}


	public String getIdentifier() {
		return "Serial tree sampler";
	}


	public Statistic getNew(Options ops) {
		// TODO Auto-generated method stub
		return null;
	}

}
