package statistics.dna;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import population.Locus;

import statistics.Collectible;
import statistics.Options;

/**
 * Returns Watterson's estimator of Theta, which is based on the number of sites segregating in a sample. Specifically, it's just
 * S divided by a1, where a1 is a simple function of the sample size.  
 * @author brendan
 *
 */
public class WattersonsTheta extends DNAStatistic {

	public static final String identifier = "Watterson's Theta";

	SegregatingSites segSites;
	
	public String getIdentifier() {
		return identifier;
	}
	
	NumberFormat formatter = new DecimalFormat("###0.0#");
	
	public WattersonsTheta() {
		segSites = new SegregatingSites();
		values = new ArrayList<Double>();
	}
	
	public WattersonsTheta getNew(Options ops) {
		this.options = ops;
		return new WattersonsTheta();
	}

	public boolean showOnScreenLog() {
		return true;
	}
	
	public double computeFromSample(List<Locus> sample) {
		int S = segSites.computeSegSitesFromSample(sample);
		double a1 = 0;
		for(double i=1; i<sample.size(); i++) 
			a1 += 1.0/i;
		
		double tw = S / a1;
		return tw;
	}
	
	public void collect(Collectible pop) {
		List<Locus> sample = pop.getSample(sampleSize);
		
		double tw = computeFromSample(sample);
		values.add(tw);
	}

	@Override
	public String getDescription() {
		return "Watterson's estimator of Theta";
	}
	
}
