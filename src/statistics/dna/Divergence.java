package statistics.dna;

import java.util.ArrayList;


import dnaModels.DNASequence;

import statistics.Collectible;
import statistics.Options;
import fitnessProviders.TwoStateFitness;
/**
 * Measures divergence (number of sites) from a non-changing master sequence
 * @author brendan
 *
 */
public class Divergence extends DNAStatistic {

	public static final String identifier = "Divergence from most fit sequence";
	
	static final int dist_max = 500;
	double[] distro;

	
	public Divergence() {
		values = new ArrayList<Double>();
		master = null;
		canHandleRecombination = true;
	}
	
	public Divergence getNew(Options ops) {
		this.options = ops;
		//DNASequence mas = (DNASequence)ops.optData;
		return new Divergence();
	}
	
	/**
	 * Show on the screen log, and yes we accept the default formatting. 
	 */
	public boolean showOnScreenLog() {
		return true;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void collect(Collectible pop) {
		double divsum = 0;
		if (master == null) {
			throw new NullPointerException("Master sequence has not been set in Divergence statistic");
		}
		
		distro = new double[dist_max];
		if (pop.getInd(0).getFitnessData() instanceof TwoStateFitness) {
			for(int i=0; i<Math.min(1000, pop.size()); i++) {
				int muts = ((TwoStateFitness)(pop.getInd(i).getFitnessData())).getMuts();
				divsum += muts;
				if (muts<dist_max)
					distro[muts]++;
			}
		}
		else {
			boolean useFitnessData = pop.getInd(0).getFitnessData().getSubstrate() instanceof DNASequence;
			for(int i=0; i<Math.min(1000, pop.size()); i++) {
				if (useFitnessData) {
					int muts = countDifs(master, (DNASequence)pop.getInd(i).getFitnessData().getSubstrate());
					divsum += muts;
					if (muts<dist_max)
						distro[muts]++;
				}
			}
		}
		for(int i=0; i<dist_max; i++) {
			distro[i] /= (double)Math.min(1000, pop.size());
		}
		
		values.add( divsum/(double)Math.min(1000, pop.size()));
	}

	private int countDifs(DNASequence one, DNASequence two) {
		int sum = 0;
		for(int i=0; i<Math.min(one.length(), two.length()); i++) {
			if ( one.getBaseChar(i) != two.getBaseChar(i)) 
				sum++;
		}
		return sum;
	}
	
	
	public void emitDistro() {
		if (distro==null)
			return;
		int firstNonZero =0 ;
		while(distro[firstNonZero]==0)
			firstNonZero++;
		
		int lastNonZero = dist_max-1;
		while (distro[lastNonZero]==0 && lastNonZero>0)
			lastNonZero--;
		
		for(int i=firstNonZero; i<lastNonZero; i++) {
			System.out.println(i + "\t" + distro[i]);
		}
	}
	
	public String getDescription() {
		return "Number of sites not in most fit state";
	}

}
