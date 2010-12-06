package statistics;

import population.Population;
import tree.DiscreteGenTree;

/**
 * The base class of statistics that collect their values from trees, not populations. Population 
 * @author brendan
 *
 */
public abstract class TreeStatistic extends Statistic implements TreeCollectionListener {

	protected Collectible pop; //We maintain a reference to the population so tree stats. can query it for population size, etc. 
	
	public final void collect(Collectible pop) {
		this.pop = pop;
	}
	
	public void setTreeSampler(TreeSampler sampler) {
		sampler.addTreeCollectionListener(this);
	}
	
	/**
	 * Indicated whether or not this statistic requires genealogical information - if it ever references parents, then it does. 
	 * This is generally true for all tree stats, of coursee  
	 */
	public boolean requiresGenealogy() {
		return true;
	}
	
	public abstract void collect(DiscreteGenTree tree);

	/**
	 * Some types of trees fire a TreeCollectionEvent when they are collected, notifying all
	 * interested parties of the new tree. This method is called by default when such an event occurs, and
	 * currently it does nothing besides call .collect() with the new tree.  
	 * 
	 * Note that this is really only used when the SerialTreeSampler is used, and since the SerialTreeSampler isn't
	 * in production this method is unlikely to be called. 
	 */
	public void newTreeCollected(DiscreteGenTree newTree) {
		collect(newTree);
	}

}
