package statistics.fitness;

import java.util.ArrayList;

import population.Individual;
import statistics.Collectible;
import statistics.Histogram;
import statistics.HistogramStatistic;
import statistics.Options;
import statistics.Statistic;

/**
 * A statistic to record the distribution of absolute fitnesses across all individuals. 
 * @author brendan
 *
 */
public class AbsoluteFitnessDistro extends HistogramStatistic {

	public static final String identifier = "Distribution of absolute fitnesses";
	
	
	public String getIdentifier() {
		return identifier;
	}
	
	public AbsoluteFitnessDistro() {
		values = new ArrayList<Double>();
		dist_min = 0;
		dist_max = 2;
		userHistoMin = dist_min;
		userHistoMax = dist_max;
		bins = 100;
		histo = new Histogram(bins, 0, 0.02);
	}

	public AbsoluteFitnessDistro getNew(Options ops) {
		this.options = ops;
		return new AbsoluteFitnessDistro();
	}
	
	public String getDescription() {
		return "Distribution of absolute fitnesses across all individuals";
	}

	public void collect(Collectible pop) {
		for(Individual ind : pop.getList() ) {
			histo.addValue( ind.getFitness() );
		}
	}
	
	public boolean showOnScreenLog() {
		return false;
	}
	
}
