package demographicModel;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.RandomEngine;
import fitnessProviders.FitnessProvider;

import population.Population;
import statistics.Collectible;
import treesimj.TreesimJView;

/**
 * A convenient base class for Demographic Models that contain only a single population, and therefore just specify 
 * how population size changes over time. 
 * @author brendan
 *
 */

public abstract class SimpleDemographicModel extends DemographicModel {
	
	protected Population pop;
	protected List<Population> popList = new ArrayList<Population>(1);
	
	public SimpleDemographicModel(String blockName) {
		super(blockName);
		this.pop = new Population();
		popList.add(pop);
	}
	
	
	public void initializePopulations(RandomEngine rng, FitnessProvider fitnessModel) {
		
		for(Population pop : getPopList()) {
			pop.initialize(rng, getN(0), fitnessModel);
		}
	}
	
	
	/**
	 * We just return the population itself here. 
	 */
	public Collectible getCollectible() {
		return pop;
	}
	
	/**
	 * Returns the target size of the population on generation 'gen' - this determines
	 * what the size of the pop will be after reproduction. 
	 * @param gen
	 * @return The new population size
	 */
	protected abstract int getN(int gen);
	
	public Population getPop(int which) {
		if (which>0) 
			throw new IllegalArgumentException("Cannot get population #" + which + " for this demographic model, since it only has " + getPopulationCount() + " populations");
		return pop;
	}
	
	/**
	 * Calls newGen(size) on the population, with size determined by getN( );
	 */
	public void reproduce(int i) {
		int newSize = getN( pop.getCurrentGenNumber()+1 );
		pop.newGen(newSize);
	}
	
	public int getPopulationCount() {
		return 1;
	}
	
	public List<Population> getPopList() {
		return popList;
	}

	
}
