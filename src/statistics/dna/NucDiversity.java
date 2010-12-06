package statistics.dna;

import java.util.ArrayList;
import java.util.List;

import population.Individual;

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

		List<Individual> sample = pop.getSample(sampleSize);
		
		double pi = collectPiFromSample(sample);
		values.add(pi);
		
	}
	
	/**
	 * Calculate and return the nucleotide diversity for the given sample of individuals - this is public since a few other
	 * calculators use it. 
	 * @param sample
	 * @return
	 */
	public double collectPiFromSample(List<Individual> sample) {
		double pi =0;
		for(int j=0; j<sample.get(0).getPrimaryDNA().length(); j++) {
			double siteDiversity;
			siteDiversity = getSiteDiversityFromPrimaryDNA(sample, j);

			pi += siteDiversity;
		}
		return pi;
	}

	private double getSiteDiversityFromPrimaryDNA(List<Individual> sample, int site) {
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
		
		freqs[0] /= (double)sample.size();
		freqs[1] /= (double)sample.size();
		freqs[2] /= (double)sample.size();
		freqs[3] /= (double)sample.size();
		
		double sum = (1.0 - freqs[0]*freqs[0] - freqs[1]*freqs[1] - freqs[2]*freqs[2] - freqs[3]*freqs[3]);
		
		return sum;
	}
	
	
//	private double[] getSiteDiversityFromInheritables(Population pop, int index, int site) {
//		double[] freqs = new double[4];
//
//		
//		for(int i=0; i<sampleSize; i++) {
//			switch( ((DNASequence)pop.getInd(i).getInheritable(index)).getBaseChar(site) ) {
//			case 'A' : freqs[0]++; break;
//			case 'G' : freqs[1]++; break;
//			case 'C' : freqs[2]++; break;
//			case 'T' : freqs[3]++; break;
//			}
//
//		}
//
//		freqs[0] /= (double)maxInd;
//		freqs[1] /= (double)maxInd;
//		freqs[2] /= (double)maxInd;
//		freqs[3] /= (double)maxInd;
//		
//		return freqs;
//	}
	
	public String getDescription() {
		return "The mean number of sites at which two randomly selected sequences differ";
	}

}
