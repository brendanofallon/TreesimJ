package demographicModel;

import java.util.ArrayList;
import java.util.List;

import population.Individual;
import population.Population;
import statistics.Collectible;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

/**
 * Class to wrap multiple populations and make them appear as a single Collectible, suitable for handing to Statistics. 
 * DemographicModels that house multiple populations will likely make use of these to facilitate collecting of statistics. 
 * 
 * @author brendan
 *
 */
public class MultiPopCollectible implements Collectible {

	List<Population> popList;
	Uniform uniGen;
	
	//This enum defines different possible sampling strategies. 
	//RANDOM denotes taking individuals at random from all populations (all inds are sampled with equal probability)
	//EVEN attempts to divide the sample evenly among all populations. 
	//SINGLE samples all individuals from one population, which is given by the integer singlePop
	public enum Strategy {RANDOM, EVEN, SINGLE};
	int singlePop = -1;
	
	Strategy samplingStrategy = Strategy.RANDOM;
	
	
	public MultiPopCollectible(RandomEngine rng, List<Population> pops) {
		popList = pops;
		uniGen = new Uniform(rng);
		if (rng==null) {
			System.err.println("Got a null RNG in MultiPopCollectible");
		}
	}
	 
	/**
	 * Set the sampling strategy to the given value. SinglePopNum is used only if that strategy is SINGLE, in which
	 * case it denotes the single population to obtain samples from. 
	 * @param strat
	 * @param singlePopNum
	 */
	public void setSamplingStrategy(Strategy strat, int singlePopNum) {
		samplingStrategy = strat;
		singlePop = singlePopNum;
	}
	
	
	public Individual getInd(int which) {
		int initNum = which;
		int popNum = 0;
		
		while(which >= popList.get(popNum).size()) {
			which -= popList.get(popNum).size();
			popNum++;
			if (popNum == popList.size()) {
				System.out.println("Error, attempted to get individual #" + initNum + " and somehow ended up reading off end beyond all pops...");
			}
		}
		
		//System.out.println("Getting ind #" + which + " from population " + popNum);
		return popList.get(popNum).getInd(which);
	}

	
	public List<Individual> getSample(int sampleSize) {
		List<Individual> sample = new ArrayList<Individual>();
		
		if (samplingStrategy == Strategy.RANDOM) {
			int maxInds = size();
			while(sample.size() < sampleSize) {
				Individual ind = getInd(uniGen.nextIntFromTo(0, maxInds-1));
				if (!sample.contains(ind))
					sample.add(ind);

			}
		}
		
		if (samplingStrategy == Strategy.SINGLE) {
			Population pop = popList.get(singlePop);
			while(sample.size() < sampleSize) {
				Individual ind = pop.getInd(uniGen.nextIntFromTo(0, pop.size()-1));
				if (!sample.contains(ind))
					sample.add(ind);

			}
			//System.out.println("Strategy is single, collecting inds from pop #" + singlePop + " obtained sample of size: " + sample.size());
		}
		
		if (samplingStrategy == Strategy.EVEN) {
			int singleSize = (int)Math.round( (double)sampleSize / (double)popList.size());
			for(Population pop : popList) {
				List<Individual> subsample = new ArrayList<Individual>();
				while(subsample.size() < singleSize) {
					Individual ind = pop.getInd(uniGen.nextIntFromTo(0, pop.size()-1));
					if (!subsample.contains(ind))
						subsample.add(ind);

				}
				sample.addAll(subsample);
			}
		}
		
		
		return sample;
	}

	
	public int getCurrentGenNumber() {
		return popList.get(0).getCurrentGenNumber();
	}

	
	public int size() {
		int sum = 0;
		for(Population pop : popList) {
			sum += pop.size();
		}
		return sum;
	}

	
	public List<Individual> getList() {
		List<Individual> everyone = new ArrayList<Individual>(popList.get(0).size());
		for(Population pop : popList) {
			everyone.addAll(pop.getList());
		}
		return everyone;
	}


	
	
	public Individual getSampleTree(int sampleSize) {
		List<Individual> actualKids = getSample(sampleSize);
		List<Individual> sampleKids = new ArrayList<Individual>();

		
		for(Individual kid : actualKids) {
			Individual sampleKid = kid.getDataCopy();
			sampleKid.setID( kid.getID() );
			sampleKids.add( sampleKid );
		}
		
		int iteration = 0;
		while(iteration < 1000000 && sampleKids.size()>1) {
			iteration++;
			//System.out.println("Iteration : " + iteration + " actual kids: " + actualKids.size() + " sample kids : " + sampleKids.size());
			Population.createSampleParents(actualKids, sampleKids);
		}
		
		if (iteration == 1000000) {
			System.err.println("Uh-oh, could not find a common ancestor for this sample of individuals (at least, not in 10000K generations)");
			return null;
		}
		
		return sampleKids.get(0);
	}

	
	public void releasePreservedInds() {
		for(Population pop : popList) {
			pop.releasePreservedInds();
		}
	}
	
}
