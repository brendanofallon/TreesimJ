package statistics.treeShape;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import population.Locus;

import statistics.Collectible;
import statistics.Histogram;
import statistics.HistogramStatistic;
import statistics.Options;
import cern.jet.random.Uniform;

public class PairwiseCTime extends HistogramStatistic {

	public static final String identifier = "Pairwise coalescence time";

	Uniform uniGen = null;
	NumberFormat formatter = new DecimalFormat("#0.0####");
	
	double lastVal = 0;
	

	public PairwiseCTime() {
		values = new ArrayList<Double>();

		dist_min = 0;
		dist_max = 5;
		userHistoMin = dist_min;
		userHistoMax = dist_max;
		bins = 50;
		histo = new Histogram(bins, 0, 0.05);
	}
	
	public PairwiseCTime getNew(Options ops) {
		this.options = ops;
		return new PairwiseCTime();
	}
	
	public String getDescription() {
		return "Mean time for two individuals to first share a common ancestor";
	}

	
	/**
	 * We need ancestral info
	 */
	public boolean requiresGenealogy() {
		return true;
	}
	
	public String getOptionDefault() {
		return String.valueOf(20);
	}
	
	/**
	 * Return the time until one and two share a common ancestor AT A RANDOMLY SELECTED SITE
	 * @param one
	 * @param two
	 * @return
	 */
	private int pcTime(Locus one, Locus two) {
		int t = 0;
		
		int site = 0;
		if (one.getRecombineableData().length()>0)
			site = uniGen.nextIntFromTo(0,  one.getRecombineableData().length() );
		
		while(one != null && two != null && one != two) {
			one = one.getParentForSite(site);
			two = two.getParentForSite(site);
			t++;
		}
		return t;
	}
	
	public void collect(Collectible pop) {
		int pairs = 40; 

		if (uniGen == null) {
			uniGen = new Uniform(rng);
		}
		lastVal = 0;
		for(int i=0; i<pairs; i++) {
			Locus one = pop.getInd( uniGen.nextIntFromTo(0, pop.size()-1));
			Locus two = pop.getInd( uniGen.nextIntFromTo(0, pop.size()-1));
			while (one == two) {
				two = pop.getInd( uniGen.nextIntFromTo(0, pop.size()-1));
			}
			double pct = pcTime(one, two);
			
			pct /= (double)pop.size();
			histo.addValue(pct);
		
			count++;
			lastVal += pct;
		}
		lastVal /= (double)pairs;
	}
	
	public boolean showOnScreenLog() {
		return true;
	}
	
	public String getScreenLogStr() {
		return formatter.format( lastVal );
	}
	
	public String getIdentifier() {
		return identifier;
	}

}
