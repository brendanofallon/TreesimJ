package statistics.fitness;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import population.Individual;

import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

/**
 * A statistic that calculates the mean fitness of the entire population. 
 * @author brendan
 *
 */
public class MeanFitness extends Statistic {
	
	public static final String identifier = "Mean population fitness";
	
	public String getIdentifier() {
		return identifier;
	}
	
	public MeanFitness() {
		values = new ArrayList<Double>();
	}

	public MeanFitness getNew(Options ops) {
		this.options = ops;
		return new MeanFitness();
	}
	
	public String getDescription() {
		return "Mean absolute fitness of the population";
	}

	public void collect(Collectible pop) {
		double mean = 0;

		for(Individual ind : pop.getList() ) {
			mean += ind.getFitness();
		}
		values.add( mean /(double)pop.size() );
	}
	
	public boolean showOnScreenLog() {
		return true;
	}


}
