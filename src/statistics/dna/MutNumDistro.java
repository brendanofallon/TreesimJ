package statistics.dna;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import population.Individual;

import dnaModels.DNASequence;

import statistics.Collectible;
import statistics.Histogram;
import statistics.HistogramStatistic;
import statistics.Options;
import fitnessProviders.DNAFitness;
import fitnessProviders.IntegerFitness;
import fitnessProviders.TwoStateFitness;

public class MutNumDistro extends HistogramStatistic {

	public static final String identifier = "Distribution of mutation number";
	

	
	PrintStream output;
	int sampleSize;
	int calls = 0;
	double lastVal  = 0;
	DecimalFormat formatter = new DecimalFormat("###0.0#");
	
	public MutNumDistro() {
		this.sampleSize = 30;
		dist_min = 0;
		dist_max = 500;
		bins = 501;
		histo = new Histogram(bins, dist_min, 1);
	}
	
	public MutNumDistro getNew(Options ops) {
		this.options = ops;
		//Integer sample = (Integer)ops.optData;
		return new MutNumDistro();
	}
	
	//True if this should be shown on the screen log
	public boolean showOnScreenLog() {
		return true;
	}
	
	//Should be overridden in stats that are shown on the screen log to return some information
	public String getScreenLogStr() {
		return formatter.format(lastVal);
	}
	
	public void collect(Collectible pop) {
		double mean = 0;
		double c = 0;
		List<Individual> sample = pop.getSample(sampleSize);
		if (sample.get(0).getFitnessData() instanceof TwoStateFitness) {
			for(Individual ind : sample) {
				int num = ((TwoStateFitness)(ind.getFitnessData())).getMuts();
				mean+=num;
				c++;
				if (num<bins) {
					histo.addValue(num);
					count++;
				}
			}
		
		}
		if (sample.get(0).getFitnessData() instanceof IntegerFitness) {
			for(Individual ind : sample) {
				int num = ((IntegerFitness)(ind.getFitnessData())).getMuts();
				mean+=num;
				c++;
				if (num<bins) {
					histo.addValue(num);
					count++;
				}
			}
		}
		
//		if (sample.get(0).getFitnessData() instanceof DNAFitness) {
//			for(Individual ind : sample) {
//				int num = ((DNAFitness)(ind.getFitnessData())).getMuts();
//				mean+=num;
//				c++;
//				if (num<bins) {
//					histo.addValue(num);
//					count++;
//				}
//			}
//		}
		
		lastVal = mean/c;
		
	}

	public String getIdentifier() {
		return identifier;
	}
	
	protected double[] calcSiteFreq(ArrayList<DNASequence> seqs, int site) {
		double[] freqs = new double[4];
		for(DNASequence seq : seqs) {
			char base = seq.getBaseChar(site);
			switch(base) {
			case 'A' : freqs[0]++; break;
			case 'G' : freqs[1]++; break;
			case 'C' : freqs[2]++; break;
			case 'T' : freqs[3]++; break;
			}
		}
		for(int i=0; i<freqs.length; i++)
			freqs[i] /= (double)seqs.size();
		return freqs;
	}
	
	protected int maxIndex(double[] list) {
		double max = list[0];
		int maxIndex = 0;
		for(int i=1; i<list.length; i++)
			if (max < list[i]) {
				max = list[i];
				maxIndex = i;
			}
		return maxIndex;
	}
	
	protected int getMutNum(DNASequence seq, char[] majorBases) {
		int sum = 0;
		for(int i=0; i<seq.length(); i++)
			if (seq.getBaseChar(i) != majorBases[i])
				sum++;
		return sum;
	}
	
	/**
	 * Calculates an array of minor allele frequencies
	 * @param seqs
	 * @return
	 */
	protected int[] getMutNumbers(ArrayList<DNASequence> seqs) {
		int[] nums = new int[seqs.size()];
		char[] majorBases = new char[seqs.get(0).length()];
		for(int i=0; i<majorBases.length; i++) {
			double[] freqs = calcSiteFreq(seqs, i);
			int maxIndex = maxIndex(freqs);
			switch(maxIndex) {
				case 0: majorBases[i] = 'A'; break;
				case 1: majorBases[i] = 'G'; break;
				case 2: majorBases[i] = 'C'; break;
				case 3: majorBases[i] = 'T'; break;
			}
		}
		
		for(int i=0; i<seqs.size(); i++) {
			DNASequence seq = seqs.get(i);
			nums[i] = getMutNum(seq, majorBases);
		}
		return nums;
	}


	public String getDescription() {
		return "Mutation number distribution, with sample size : " + sampleSize;
	}

}
