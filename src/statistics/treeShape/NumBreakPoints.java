package statistics.treeShape;

import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;
import statistics.TreeStatistic;
import statistics.dna.DNAStatistic;
import tree.DiscreteGenTree;

/**
 * Just counts the number of recombination breakpoints in the sample
 * @author brendan
 *
 */
public class NumBreakPoints extends TreeStatistic {

	public static final String identifier = "Number of recombination breakpoints";

	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Show on screen log, and we accept the default formatting version
	 */
	public boolean showOnScreenLog() {
		return true;
	}
	
	@Override
	public Statistic getNew(Options ops) {
		return new NumBreakPoints();
	}

	@Override
	public String getDescription() {
		return "Number of recombination breakpoints ancestral to the sample";
	}

	@Override
	public void collect(DiscreteGenTree tree) {
		if (tree == null)
			return;
		
		int bps = tree.collectBreakPoints().size();
		values.add((double) bps);
		
	}

}
