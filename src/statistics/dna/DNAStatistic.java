package statistics.dna;

import dnaModels.DNASequence;
import statistics.Statistic;

/**
 * Superclass of statistics that calculate things with using DNA. Mostly just a marker,
 * but it is supplied with the 'master' (most fit) sequence at the outset, although
 * only some statistics use this (I belive just Divergence, as of 3/1/2010);
 * @author brendan
 *
 */
public abstract class DNAStatistic extends Statistic {

	DNASequence master = null;
	int sampleSize = 20;
	
	public DNAStatistic() {
		canHandleRecombination = true; //Most DNA stats don't require a genealogy and hence can operate with recombination
	}
	
	public void setSampleSize(int size) {
		this.sampleSize = size;
	}
	
	public void setMaster(DNASequence masterSeq) {
		this.master = masterSeq;
	}


}
