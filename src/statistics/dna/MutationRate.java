package statistics.dna;

import dnaModels.DNASequence;
import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

public class MutationRate extends DNAStatistic {

	public static final String identifier = "Mutation rate";
	
	public void collect(Collectible pop) {
		
		double sum = 0;
		double count = 0;
		for(int i=0; i<Math.min(pop.size(), sampleSize); i++) {
			sum += calculateMuPair(pop.getInd(i).getPrimaryDNA(), pop.getInd(i).getParent().getPrimaryDNA());
			count++;
		}

		values.add( sum/count );

	}
	
	
	/**
	 * We need ancestral info
	 */
	public boolean requiresGenealogy() {
		return true;
	}
	
	public boolean showOnScreenLog() {
		return true;
	}
	
	/**
	 * We need to keep the ancestral DNA data around so we can compare kids to offspring. 
	 */
	public boolean requiresPreserveAncestralData() {
		return true;
	}
	
	/**
	 * Counts the total number of differences between two sequences.
	 * @param one
	 * @param two
	 * @return
	 */
	private double calculateMuPair(DNASequence one, DNASequence two) {
		double sum = 0;
		for(int i=0; i<Math.min(one.length(), two.length()); i++) {
			if (one.getBaseChar(i)!=two.getBaseChar(i))
				sum++;
		}
		
		return sum/(double)Math.min(one.length(), two.length());
	}

	public String getDescription() {
		return "Mean number of sites at which parents differ from offspring";
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public Statistic getNew(Options ops) {
		return new MutationRate();
	}

}
