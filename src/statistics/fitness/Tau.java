package statistics.fitness;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

public class Tau extends Statistic {

	public static final String identifier = "Diff. in parent-offspring relative fitness (Tau)";
	
	public String getIdentifier() {
		return identifier;
	}
	
	NumberFormat formatter = new DecimalFormat("0.0###");
	
	public Tau() {
		values = new ArrayList<Double>();
	}

	public Tau getNew(Options ops) {
		this.options = ops;
		return new Tau();
	}
	
	/**
	 * We need ancestral info
	 */
	public boolean requiresGenealogy() {
		return true;
	}
	
	public void collect(Collectible pop) {
		ArrayList<Double> difs = new ArrayList<Double>();
		for(int i=0; i<Math.min(250, pop.size()); i++) {
			difs.add( pop.getInd(i).getRelFitness() - pop.getInd(i).getParent().getRelFitness() );
			//System.out.println("ind :" + pop.getInd(i).getRelFitness() + " par : " +  pop.getInd(i).getParent().getRelFitness() + " dif: " + (pop.getInd(i).getRelFitness()- pop.getInd(i).getParent().getRelFitness()));
		}
		
		values.add( Math.sqrt(variance(difs)));
	}

	private double variance(ArrayList<Double> vals) {
		double mean = 0;
		for(Double x : vals)
			mean += x;
		
		mean =  mean/(double)vals.size();

		double var = 0;
		for(Double x : vals) {
			var += (x-mean)*(x-mean);
		}
		
		return var/(double)vals.size();
		
	}

	public String getDescription() {
		return "Std. dev. in fitness differences among parents and offspring";
	}
	
	public boolean showOnScreenLog() {
		return true;
	}

}
