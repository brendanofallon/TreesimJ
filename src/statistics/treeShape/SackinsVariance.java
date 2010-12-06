package statistics.treeShape;

import java.util.ArrayList;

import population.Individual;

import statistics.Options;
import statistics.TreeStatistic;
import tree.DiscreteGenTree;

public class SackinsVariance extends TreeStatistic {

	public static final String identifier = "Sackin's index of tree imbalance (variance)";
	
	public String getIdentifier() {
		return identifier;
	}
	
	public SackinsVariance() {	}
	
	public SackinsVariance getNew(Options ops) {
		this.options = ops;
		return new SackinsVariance();
	}
	
	public void collect(DiscreteGenTree tree) {
		if (tree==null)
			return;
		
		ArrayList<Individual> tips = tree.getTips();


		ArrayList<Double> vals = new ArrayList<Double>();
		for(Individual tip : tips) {
			vals.add( (double)DiscreteGenTree.getNodesToRoot(tip));
		}
		
		values.add( variance(vals) );	
	}

	public boolean showOnScreenLog() {
		return true;
	}
	
	private double mean(ArrayList<Double> vals) {
		double sum = 0;
		for(Double num : vals) 
			sum+=num;
		return sum/(double)vals.size();
	}
	
	private double variance(ArrayList<Double> vals) {
		double meanVal = mean(vals);
		double sum = 0;
		for(Double num : vals) 
			sum+= (meanVal - num)*(meanVal-num);
		return sum/(double)(vals.size()-1.0);
	}
	public String getDescription() {
		return "Variance in number of nodes separating tips from root";
	}

}
