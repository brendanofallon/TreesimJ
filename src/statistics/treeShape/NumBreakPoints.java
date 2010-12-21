package statistics.treeShape;

import java.io.PrintStream;

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

	
	/**
	 * Here we override the summary basically just to ensure the resulting histogram has integer-width bins
	 * @param out
	 */
	public void summarize(PrintStream out) {
		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
		out.println("Number of samples : \t" + values.size());
		out.println("Mean :\t" + getMean());
		out.println("Variance :\t" + getVariance());

		double max;
		if (userHistoMax!=null)
			max = this.userHistoMax;
		else {
			max = histoBins;
		}

		double min;
		if (userHistoMin!=null)
			min = userHistoMin;
		else {
			min = 0;
		}

		emitHistogram(histoBins, min, max, out);
	}
}
