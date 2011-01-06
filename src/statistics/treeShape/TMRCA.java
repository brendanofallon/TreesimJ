package statistics.treeShape;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

/**
 * A statistic that calculates the height of the population genealogy, that is, the tree that connects all individuals. 
 * It's not a TreeStatistic since it does not rely on a sampled tree. 
 * @author brendan
 *
 */
public class TMRCA extends Statistic {
	
	public static final String identifier = "Population TMRCA";
	
	public String getIdentifier() {
		return identifier;
	}
	
	NumberFormat formatter = new DecimalFormat("###0.0");
	
	public TMRCA() {
		values = new ArrayList<Double>();
	}
	
	public TMRCA getNew(Options ops) {
		this.options = ops;
		return new TMRCA();
	}
	
	/**
	 * We need ancestral info
	 */
	public boolean requiresGenealogy() {
		return true;
	}
	
	public String getDescription() {
		return "The TMRCA of the whole population";
	}
	
	public void collect(Collectible pop) {
		values.add((double)pop.getInd(0).distToRoot());
	}

	public boolean showOnScreenLog() {
		return true;
	}

	
}
