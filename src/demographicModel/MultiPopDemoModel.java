package demographicModel;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.RandomEngine;
import fitnessProviders.FitnessProvider;
import population.Locus;
import population.Population;
import statistics.Collectible;

/**
 * An abstract base class for demographic models that have multiple populations stored in a list (popList) 
 * @author brendan
 *
 */
public abstract class MultiPopDemoModel extends DemographicModel {

	//A single individual that is the ancestor of all others. This moves when we call shortenGlobalRoot 
	//Locus globalRoot = null;
	
	//A list of all populations in the world
	List<Population> popList = new ArrayList<Population>();
	
	//Some definitions for the sampling strategy
	MultiPopCollectible.Strategy samplingStrategy;
	int singlePopNum;
	
	public MultiPopDemoModel(String blockName) {
		super(blockName);
	}
	
	public void setSamplingStrategy(MultiPopCollectible.Strategy strat, int popNum) {
		this.samplingStrategy = strat;
		this.singlePopNum = popNum;
	}
	
	
	/**
	 * Initialize all populations in this model and attaches the population roots to the globalRoot
	 */
	public void initializePopulations(RandomEngine rng, FitnessProvider fitnessModel) {
		Locus globalRoot = new Locus(rng);
		
		for(Population pop : getPopList()) {
			Locus popRoot = pop.initialize(rng, 1, fitnessModel);
			pop.setAutoShortenRoot(false); //Must turn off auto root shortening for individual populations
			globalRoot.addOffspring(popRoot);
			popRoot.setParent(globalRoot);
		}
	}
	
	
	
	/**
	 * Get the total number of Populations in this model
	 */
	@Override
	public int getPopulationCount() {
		return popList.size();
	}

	
	/**
	 * Get the Population at index which from the list of Populations
	 */
	@Override
	public Population getPop(int which) {
		return popList.get(which);
	}

	
	/**
	 * Obtain a collectible that wraps all populations in this model. 
	 */
	@Override
	public Collectible getCollectible() {
		if (rng==null)
			System.err.println("RNG is null in mpd.getCollectible");
		MultiPopCollectible collector = new MultiPopCollectible(rng, popList);
		collector.setSamplingStrategy(samplingStrategy, singlePopNum);
		return collector;
	}

	
	/**
	 * Obtain the list of all current populations
	 */
	@Override
	public List<Population> getPopList() {
		return popList;
	}
	
	/**
	 * Move the global root toward the tips until it has >1 offspring, and release memory behind (basal to) it. 
	 */
//	protected void shortenGlobalRoot() {
//		int steps = 0;
//		while (globalRoot.numOffspring()==1) {
//			if (globalRoot.getParent()!=null)
//				globalRoot.getParent().removeOffspring(globalRoot);
//			globalRoot.setParent(null);
//			globalRoot = globalRoot.getOffspring(0);
//			steps++;
//		}
//
//	}
}
