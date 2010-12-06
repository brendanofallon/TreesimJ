package statistics;

import java.util.ArrayList;
import java.util.List;

import population.Individual;

/**
 * An interface for those items from which groups of Individuals may be sampled. These types of objects are passed
 * to Statistics, which generally need to sample Individuals to do their job. Populations are one type of Collectible,
 * but so are groups of Populations (for instance, in a multi-population model). 
 * @author brendan
 *
 */
public interface Collectible {

	public Individual getInd(int which);

	public List<Individual> getSample(int sampleSize);
	
	public int getCurrentGenNumber();
	
	public int size();
	
	public List<Individual> getList();
	
	public Individual getSampleTree(int sampleSize);
	
	public void releasePreservedInds();
	
}
