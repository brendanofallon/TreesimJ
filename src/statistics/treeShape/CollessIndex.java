package statistics.treeShape;

import java.util.ArrayList;

import population.Locus;

import statistics.Options;
import statistics.TreeStatistic;
import tree.DiscreteGenTree;

public class CollessIndex extends TreeStatistic {

	public static final String identifier = "Colless' index of tree imbalance";
	
	
	
	private ArrayList<Integer> tmp;
	
	public CollessIndex() {
		tmp = new ArrayList<Integer>();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public CollessIndex getNew(Options ops) {
		this.options = ops;
		return new CollessIndex();
	}
	
	public void collect(DiscreteGenTree tree) {
		//System.out.println("Colless' index is collecting");
		if (tree==null)
			return;
		Locus root = tree.getRoot();
		tmp.clear();
		addCladeSizeDifs(root);
		double sum = 0;
		for(Integer val : tmp) {
			sum += val;
		}
		values.add(sum);
	}
	
	protected void addCladeSizeDifs(Locus ind) {
		if (ind.numOffspring()==0)
			return;
		if (ind.numOffspring()==1)
			addCladeSizeDifs(ind.getOffspring(0));
		else {
			if (ind.numOffspring()==2) {
				int tips0 = DiscreteGenTree.getNumTips(ind.getOffspring(0));
				int tips1 = DiscreteGenTree.getNumTips(ind.getOffspring(1));
				tmp.add( Math.abs(tips0-tips1) );
			}
			for(Locus kid : ind.getOffspring())
				addCladeSizeDifs(kid);
		}
	}

	public boolean showOnScreenLog() {
		return true;
	}
	
	
	public String getDescription() {
		return "The Colless Index of tree skewiness";
	}

}
