package statistics;

import tree.DiscreteGenTree;

/**
 * Entities which would like to be notified of when new trees are collected should implement this interface.
 * @author brendan
 *
 */
public interface TreeCollectionListener {

	public void newTreeCollected(DiscreteGenTree tree);
	
}
