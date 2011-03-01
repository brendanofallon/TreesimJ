package statistics.dna;

import java.util.ArrayList;
import java.util.List;

import population.Locus;

import dnaModels.DNASequence;

import statistics.Collectible;
import statistics.Options;

/**
 * Computes the nucleotide diversity, aka pi, which is the mean number of sites at which two randomly selected sequences differ
 * @author brendan
 *
 */
public class NucDiversity extends DNAStatistic {

	public static final String identifier = "Nucleotide diversity (pi)";
	
	double[] freqs = new double[4];
	
	public String getIdentifier() {
		return identifier;
	}
	
	public NucDiversity() {
		values = new ArrayList<Double>();
	}
	
	public NucDiversity getNew(Options ops) {
		this.options = ops;
		return new NucDiversity();
	}
	
	/**
	 * Show on screen log, and we accept the default formatting version
	 */
	public boolean showOnScreenLog() {
		return true;
	}
	
	public void collect(Collectible pop) {

		List<Locus> sample = pop.getSample(sampleSize);
		
		double pi = collectPiFromSample(sample);
		values.add(pi);
		
	}
	
	/**
	 * Calculate and return the nucleotide diversity for the given sample of individuals - this is public since a few other
	 * calculators use it. 
	 * @param sample
	 * @return
	 */
	public double collectPiFromSample(List<Locus> sample) {
		double pi =0;
		for(int j=0; j<sample.get(0).getPrimaryDNA().length(); j++) {
			double siteDiversity;
			siteDiversity = getSiteDiversityFromPrimaryDNA(sample, j);

			pi += siteDiversity;
		}
		return pi;
	}

	private double getSiteDiversityFromPrimaryDNA(List<Locus> sample, int site) {
		for(int i=0; i<freqs.length; i++)
			freqs[i] = 0;
		
		//double n2 = (sample.size()*(sample.size()));
		
		for(int i=0; i<sample.size(); i++) {
			switch( sample.get(i).getPrimaryDNA().getBaseChar(site) ) {
			case 'A' : freqs[0]++; break;
			case 'G' : freqs[1]++; break;
			case 'C' : freqs[2]++; break;
			case 'T' : freqs[3]++; break;
			default: System.out.println("Yikes! Got a non valid base at site " + site + " in nuc diversity!");
			}
		}
		
		
		double s0 = (double)sample.size();
		double s1 = (double)sample.size()-1;

		freqs[0] = freqs[0]/s0 * (freqs[0]-1)/s1;
		freqs[1] = freqs[1]/s0 * (freqs[1]-1)/s1;
		freqs[2] = freqs[2]/s0 * (freqs[2]-1)/s1;
		freqs[3] = freqs[3]/s0 * (freqs[3]-1)/s1;		
		
		double sum = (1.0 - freqs[0] - freqs[1] - freqs[2] - freqs[3]);
		if (sum < 0 || sum > 1.0) {
			throw new IllegalStateException("Error calculating site diversity for site: " + site + ", frequency is: " + sum);
		}
		return sum;
	}

	
	public String getDescription() {
		return "The mean number of sites at which two randomly selected sequences differ";
	}

}
