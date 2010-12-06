package statistics.dna;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import population.Individual;

import dnaModels.DNASequence;

import statistics.Collectible;
import statistics.Options;

/**
 * Computes the number of 'segregating', aka polymorphic, aka non-constant, sites in a sample of individuals
 * @author brendan
 *
 */
public class SegregatingSites extends DNAStatistic {

	public static final String identifier = "Number of segregating sites (S)";

	public String getIdentifier() {
		return identifier;
	}
	
	NumberFormat formatter = new DecimalFormat("###0.0#");
	
	public SegregatingSites() {
		values = new ArrayList<Double>();
	}
	
	public SegregatingSites getNew(Options ops) {
		this.options = ops;
		return new SegregatingSites();
	}

	
	public void collect(Collectible pop) {
		//Find inheritable index of DNA. This will produce an error if the index
		//is not the same across all individuals in the population
		List<Individual> sample = pop.getSample(sampleSize);
		int S = computeSegSitesFromSample(sample);
		values.add((double)S);
//		boolean useFitnessData = false;
//		int num;
//		int index = 0;
//		DNASequence ref;
//		if (pop.getInd(0).getFitnessData().getSubstrate() instanceof DNASequence ) {
//			ref = (DNASequence)pop.getInd(0).getFitnessData().getSubstrate();
//			useFitnessData = true;
//		}
//		else {		
//			num = pop.getInd(0).getNumInheritables();
//			index = 0;
//			while(index<num && (! (pop.getInd(0).getInheritable(index) instanceof DNASequence)) ) {
//				index++;
//			}
//			if (index==num)
//				return;
//
//			ref = (DNASequence)pop.getInd(0).getInheritable(index) ;
//			useFitnessData = false;
//		}
//		
//		
//		int maxInd = sampleSize;
//		
//		DNASequence comp;
//		int segSites = 0;
//		for(int j=0; j<ref.length(); j++) {
//			
//			char refBase = ref.getBaseChar(j);
//			
//			for(int i=1; i<maxInd; i++) {
//				if (useFitnessData)
//					comp = (DNASequence)pop.getInd(i).getFitnessData().getSubstrate();
//				else 
//					comp = (DNASequence)pop.getInd(i).getInheritable(index);
//				
//				if ( comp.getBaseChar(j) != refBase ) {
//					segSites++;
//					i=pop.size();
//				}
//			}
//		}
			
//		values.add((double)segSites);

	}
	
	/**
	 * Compute and return the number of segregating sites in the given sample. A couple of other calculators use this
	 * function
	 * @param sample
	 * @return
	 */
	public int computeSegSitesFromSample(List<Individual> sample) {
		int segSites = 0;
		DNASequence ref = sample.get(0).getPrimaryDNA();
		for(int j=0; j<ref.length(); j++) {
			
			char refBase = ref.getBaseChar(j);
			
			for(int i=1; i<sample.size(); i++) {
				DNASequence comp = sample.get(i).getPrimaryDNA();
				
				if ( comp.getBaseChar(j) != refBase ) {
					segSites++;
					i=sample.size();
				}
			}
		}
		return segSites;
	}

	public String getDescription() {
		return "Number of polymorphic sites";
	}
	
	public boolean showOnScreenLog() {
		return true;
	}


}
