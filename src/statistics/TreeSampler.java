package statistics;

import java.util.ArrayList;

import population.Population;

import tree.DiscreteGenTree;

/**
 * Base class of things that collect trees. We support a simple listener architecture to notify interested parties of
 * when trees are collected... however, this architecture has a downside, which is that the user won't be able to 
 * (easily) set different collection frequencies for TreeStatistics (since TreeStatistics would just passively listen
 * to TreeSamplers for tree collection events)
 *  
 * @author brendan
 *
 */
public abstract class TreeSampler extends Statistic {

	
	ArrayList<TreeCollectionListener> treeListeners;
	
	/**
	 * Add the treeCollectionListener to the list of listeners to be notified when a new tree
	 * is collected. 
	 * @param treeListener
	 */
	public void addTreeCollectionListener(TreeCollectionListener treeListener) {
		if (treeListeners==null)
			treeListeners = new ArrayList<TreeCollectionListener>();
		treeListeners.add(treeListener);
	}
	
	/**
	 * Remove a listener from the list of listeners
	 * @param treeListener
	 */
	public void removeTreeCollectionListener(TreeCollectionListener treeListener) {
		if (treeListeners!=null) {
			treeListeners.remove(treeListener);
		}
	}
	
	/**
	 * Notify registered listeners (typically, a list of treeStatistics) of the new tree collection event
	 * @param newTree
	 */
	public void fireNewTreeEvent(DiscreteGenTree newTree) {
		if (treeListeners!=null) {
			for(TreeCollectionListener tcl : treeListeners) {
				tcl.newTreeCollected(newTree);
			}
		}
	}
	
	/**
	 * Asks this treeSampler to generate a tree and have a given listener collect the data from it.
	 * Hmmm, this will always invoke newTreeCollected on the listener, but a new tree may not have been collected...
	 * @param pop
	 * @param listener
	 */
	public void requestCollection(Collectible pop, TreeCollectionListener listener) {
		collectWithoutFiring(pop);
		listener.newTreeCollected(getLastTree());
	}
	
	/**
	 * Sample a new tree and then notify interested parties of the event
	 * @param pop
	 */
	public void collectAndFire(Collectible pop) {
		collect(pop);
		fireNewTreeEvent(getLastTree());
	}
	
	
	/**
	 * Sample a new tree without firing a collection event 
	 * @param pop
	 */
	public void collectWithoutFiring(Collectible pop) {
		collect(pop);
	}
	
	public abstract DiscreteGenTree getLastTree(); 

}
