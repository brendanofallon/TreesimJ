package statistics.fitness;

import population.Locus;
import statistics.Collectible;
import statistics.Histogram;
import statistics.HistogramStatistic;
import statistics.Options;

public class RelativeFitnessDistro extends HistogramStatistic {

	public static final String identifier = "Distribution of relative fitnesses";
	
	double lastVal = 0;
	
	public RelativeFitnessDistro() {
		bins = 50;
		dist_min = 0.95;
		dist_max = 1.025;
		histo = new Histogram(bins, dist_min, 0.0025);
		canHandleRecombination = true;
	}
	
	public RelativeFitnessDistro getNew(Options ops) {
		this.options = ops;
		return new RelativeFitnessDistro();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void collect(Collectible pop) {
		double mean = 0;
		for(Locus ind : pop.getList() ) {
			histo.addValue(ind.getRelFitness());
			count++;
			mean += ind.getRelFitness();
		}
		
		lastVal = mean / (double)count;
	}
	

	public double getLastValue() {
		return lastVal;
	}

	public String getDescription() {
		return "Distribution of relative fitness values (p(w))";
	}

}
