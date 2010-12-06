package statistics.fitness;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;


public class StdevFitness extends Statistic {

	public static final String identifier = "Std. dev. of relative fitnesses";
	
	public String getIdentifier() {
		return identifier;
	}
	
	NumberFormat formatter = new DecimalFormat("0.0###");
	
	public StdevFitness() {
		values = new ArrayList<Double>();
	}
	
	public StdevFitness getNew(Options ops) {
		this.options = ops;
		return new StdevFitness();
	}
	
	public void collect(Collectible pop) {
		double var = 0;
		for(int i=0; i<pop.size(); i++) {
			var += (pop.getInd(i).getRelFitness()-1.0)*(pop.getInd(i).getRelFitness()-1.0);
		}
		
		var /= (double)pop.size();
		
		values.add( Math.sqrt( var ) );
	}
	

	public String getDescription() {
		return "Standard deviation in relative fitness";
	}
	
	
	public boolean showOnScreenLog() {
		return true;
	}
	

}
