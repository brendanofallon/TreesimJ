package statistics.treeShape;

import population.Individual;
import statistics.Options;
import statistics.TreeStatistic;
import tree.DiscreteGenTree;

public class CladeSizeDistro extends TreeStatistic {

	public static final String identifier = "Distribution of clade sizes";
	
	public String getIdentifier() {
		return identifier;
	}
	
	int treesCounted = 0;
	
	public CladeSizeDistro() { };
	
	public CladeSizeDistro getNew(Options ops) {
		this.options = ops;
		return new CladeSizeDistro();
	}
	
	public void collect(DiscreteGenTree tree) {
		if (tree==null)
			return;
		Individual root = tree.getRoot();
		addCladeSizes(root);
		treesCounted++;
	}

	protected void addCladeSizes(Individual ind) {
		if (ind.numOffspring()==0)
			return;
		else  {
			if (ind.numOffspring()>1) {
				int tips = DiscreteGenTree.getNumTips(ind);
				values.add((double)tips);
				for(Individual kid : ind.getOffspring()) {
					addCladeSizes(kid);
				}
			}
			else {
				addCladeSizes(ind.getOffspring(0));
			}
		}
	}
	
	public String getDescription() {
		return "The distribution of clade sizes, trees counted : " + treesCounted;
	}

}
