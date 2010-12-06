package statistics.treeShape;

import java.util.ArrayList;

import population.Individual;

import statistics.Options;
import statistics.Statistic;
import tree.DiscreteGenTree;

/**
 * Measures the TMRCA in a sampled tree. Uses the maximum distance found among all
 * tips to the root.
 * 
 * @author brendan
 *
 */
public class SampleTMRCA extends statistics.TreeStatistic {

	public static final String identifier = "Sample TMRCA";

	public SampleTMRCA() {
		
	}
	
	public void collect(DiscreteGenTree tree) {
		if (tree==null)
			return;
		ArrayList<Individual> tips = tree.getTips();
		Double max = 0.0;
		for(Individual tip : tips) {
			int dist = tip.distToRoot();
			if (dist>max)
				max = Double.valueOf(dist);
		}
		
		values.add(max);	
	}


	public String getDescription() {
		return "The time to most recent common ancestor among a sample of individuals";
	}

	
	public boolean showOnScreenLog() {
		return true;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public Statistic getNew(Options ops) {
		return new SampleTMRCA();
	}

}
