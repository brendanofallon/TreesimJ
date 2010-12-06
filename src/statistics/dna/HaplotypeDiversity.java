package statistics.dna;

import java.util.ArrayList;


import dnaModels.DNASequence;

import statistics.Collectible;
import statistics.Options;

/**
 * The haplotype diversity is computed as 1 - the sum of the squared haplotype frequencies in a sample.
 * If all individuals have the same haplotype then this statistic will be zero, if all individuals in the
 * sample have different haplotype, then the value will be close to one.
 * @author brendan
 *
 */
public class HaplotypeDiversity extends DNAStatistic {

	
	public static final String identifier = "Haplotype Diversity";
	
	ArrayList<DNASequence> haps;
	ArrayList<Integer> counts;
	
	public HaplotypeDiversity() {
		values = new ArrayList<Double>();
		haps = new ArrayList<DNASequence>();
		counts = new ArrayList<Integer>();
	}
	
	public HaplotypeDiversity getNew(Options ops) {
		this.options = ops;
		return new HaplotypeDiversity();
	}

	
	public void collect(Collectible pop) {
		haps.clear();
		counts.clear();
		for(int i=0; i<Math.min(sampleSize, pop.size()); i++) {
			int index = hapsContainsSequence(pop.getInd(i).getPrimaryDNA());
			if (index>-1) {
				counts.set(index, counts.get(index)+1);
			}
			else {
				haps.add(pop.getInd(i).getPrimaryDNA());
				counts.add(1);
			}
		}
		
		double count = Math.min(sampleSize, pop.size());
		double sum = 1;
		for(int i=0; i<counts.size(); i++) {
			sum -= counts.get(i)*counts.get(i)/(count*count);
		}
		
		values.add(sum);
	}

	public boolean showOnScreenLog() {
		return true;
	}
	
	private int hapsContainsSequence(DNASequence seq) {
		for(int i=0; i<haps.size(); i++) {
			if (haps.get(i).equals(seq))
				return i;
		}
		return -1;
	}
	
	@Override
	public String getDescription() {
		return "Haplotype diversity";
	}

	public String getIdentifier() {
		return identifier;
	}


}
