package demographicModel;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.RandomEngine;

import fitnessProviders.FitnessProvider;

import population.Population;
import statistics.Collectible;

import xml.XMLParseable;

public abstract class DemographicModel extends XMLParseable {

	RandomEngine rng = null;
		
	public DemographicModel(String blockName) {
		super(blockName);
	}

	public void setRng(RandomEngine rng) {
		this.rng = rng;
	}
	
	/**
	 * Causes the population at index popNum to undergo one Wright-Fisher generation. It is up 
	 * to various subclasses to determine what the size of the population after
	 * reproduction should be. 
	 * @param popNum Index of the population to reproduce
	 */
	public abstract void reproduce(int popNum);
	
	
	/**
	 * Calls reproduce on all populations.
	 */
	public void reproduceAll() {
		for(int i=0; i<getPopulationCount(); i++) {
			reproduce(i);
		}
	}
	
	/**
	 * Initialize all populations with the given random engine and fitness model. This must be called
	 * prior to reproduce()
	 * @param rng
	 * @param fitnessModel
	 */
	public void initializePopulations(RandomEngine rng, FitnessProvider fitnessModel) {
		for(Population pop : getPopList()) {
			pop.initialize(rng, 1, fitnessModel);
		}
	}
	
	/**
	 * A convenience method for retreiving the current generation number, this just gets it from pop(0). 
	 * @return
	 */
	public int getCurrentGenNumber() {
		return getPop(0).getCurrentGenNumber();
	}
	
	/**
	 * Obtain a user-readable description of this model
	 * @return
	 */
	public abstract String getDescription(); 
	
	/**
	 * Get the number of populations
	 * @return
	 */
	public abstract int getPopulationCount();
	
	public abstract Population getPop(int which);
	
	/**
	 * Obtain a collectible item suitable for handing to a Statistic. Typically this will be either a single population, or several
	 * populations wrapped in a MultiPopCollectible
	 * @return
	 */
	public abstract Collectible getCollectible();
	
	/**
	 * Obtain a list of all current populations
	 * @return
	 */
	public abstract List<Population> getPopList();
	
}
