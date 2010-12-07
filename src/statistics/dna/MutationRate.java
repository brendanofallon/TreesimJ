package statistics.dna;

import population.Locus;
import dnaModels.DNASequence;
import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

/**
 * This shoudl use a histogram...
 * @author brendan
 *
 */
public class MutationRate extends DNAStatistic {

	public static final String identifier = "Mutation rate";
	
	public void collect(Collectible pop) {
		
		double count = 0;
		for(int i=0; i<Math.min(pop.size(), sampleSize); i++) {

			double mu = calculateMu(pop.getInd(i));

			values.add( mu ); 
			count++;
		}

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
	private double calculateMu(Locus loc) {
		double sum = 0;
		DNASequence one = loc.getPrimaryDNA();
		if (loc.hasRecombination()) {
			for(int i=0; i<one.length(); i++) {
				DNASequence two = loc.getParentForSite(i).getPrimaryDNA();
				if (one.getBaseChar(i)!=two.getBaseChar(i))
					sum++;
			}

			return sum/(double)one.length();
		}
		else {
			DNASequence two = loc.getParent().getPrimaryDNA();
			for(int i=0; i<one.length(); i++) {
				if (one.getBaseChar(i)!=two.getBaseChar(i))
					sum++;
			}

			return sum/(double)one.length();
		}
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
