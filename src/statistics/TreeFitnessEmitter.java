package statistics;

import population.Individual;
import tree.DiscreteGenTree;

/**
 * Something about fitness and clade size differences?
 * @author brendan
 *
 */
public class TreeFitnessEmitter extends TreeStatistic {

	public static final String identifier = "Clade size as a function of fitness";
	
	public String getIdentifier() {
		return identifier;
	}
	
	public TreeFitnessEmitter() { }
	
	public TreeFitnessEmitter getNew(Options ops) {
		this.options = ops;
		return new TreeFitnessEmitter();
	}
	
	public void collect(DiscreteGenTree tree) {
		if (tree==null)
			return;
		
		addCladeSizeDifs(tree.getRoot());
	}

	protected void addCladeSizeDifs(Individual ind) {
		if (ind.numOffspring()==0)
			return;
		if (ind.numOffspring()==1)
			addCladeSizeDifs(ind.getOffspring(0));
		else {
			if (ind.numOffspring()==2) {
				int tips0 = DiscreteGenTree.getNumTips(ind.getOffspring(0));
				int tips1 = DiscreteGenTree.getNumTips(ind.getOffspring(1));
				double val = (double)Math.abs(tips0-tips1)/(double)Math.max(tips0, tips1);
				int dist1 = DiscreteGenTree.getDistToParent( DiscreteGenTree.getNextNode( ind.getOffspring(0)));
				int dist2 = DiscreteGenTree.getDistToParent( DiscreteGenTree.getNextNode( ind.getOffspring(1)));
				output.println(ind.getFitness() + "\t" + val);
			}
			for(Individual kid : ind.getOffspring())
				addCladeSizeDifs(kid);
		}
	}
	
	public String getDescription() {
		return "Emits node subclade size differences as a function of fitness";
	}

}
