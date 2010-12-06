package demographicModel;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import demographicModel.MultiPopCollectible.Strategy;
import dnaModels.DNASequence;
import fitnessProviders.DNAFitness;
import gui.OutputManager;

import population.Individual;
import population.Population;
import statistics.Statistic;
import statistics.dna.DNAStatistic;
import xml.TJXMLConstants;

/**
 * A demographic model that simulation a single population evolving for a few generations (splittingTime generations), followed by
 * a split into two descendant populations, between which there may be migration.  
 * @author brendan
 *
 */
public class PopSplitDemoModel extends MultiPopDemoModel {

	public static final String XML_ATTR = "split";
	public static final String XML_SIZE0 = "pop0.size";
	public static final String XML_SIZE1 = "pop1.size";
	public static final String XML_SIZE2 = "pop2.size";
	public static final String XML_SPLITTIME = "split.time";
	public static final String XML_MIG12 = "mig12";
	public static final String XML_MIG21 = "mig21";
	public static final String XML_SAMPLESTRATEGY = "sampling.strategy";
	public static final String XML_SINGLEPOP = "sample.pop";

	//double reducedMigrationFactor = 0.0; //1 indicates mismatched types don't migrate at all
	
	int splittingTime; 		//Number of generations before population split
	int ancestralPopSize;   //Size of the ancestral population
	int popOneSize;			//Size of descendant population one
	int popTwoSize;			//Size of descendant population two
	double m12;				//Migration rate from one to two
	double m21;				//Migration rate from two to one
	
	Population ancPop;
	Population pop1;
	Population pop2;
	
	//DNASequence pop1Master;
	//DNASequence pop2Master;
	
	Poisson poiGen;
	Uniform uniGen;
	
	public PopSplitDemoModel(int p0, int p1, int p2, int t0, double m12, double m21) {
		super(TJXMLConstants.DEMOGRAPHIC_MODEL);
		
		ancestralPopSize = p0;
		popOneSize = p1;
		popTwoSize = p2;
		splittingTime = t0;
		this.m12 = m12;
		this.m21 = m21;
		
		ancPop = new Population();
		popList.add(ancPop);
		
		//System.out.println("Creating new pop split model with p0: " + p0 + " p1: " + p1 + " p2: " + p2 + " t0: " + t0 + " m12: " + m12 + " m21: " + m21);
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SIZE0, String.valueOf(p0));
		addXMLAttr(XML_SIZE1, String.valueOf(p1));
		addXMLAttr(XML_SIZE2, String.valueOf(p2));
		addXMLAttr(XML_SPLITTIME, String.valueOf(t0));
		addXMLAttr(XML_MIG12, String.valueOf(m12));
		addXMLAttr(XML_MIG21, String.valueOf(m21));
	}

	
	public void setRng(RandomEngine rng) {
		this.rng = rng;
		poiGen = new Poisson(1.0, rng);		
		uniGen = new Uniform(rng);
	}
	
	/**
	 * Experimental, returns true if an individual has any base that matches the pop1Master
	 * @param ind
	 * @param maxSite
	 * @return
	 */
//	private boolean hasPop1Matches(Individual ind, int maxSite) {
//		DNASequence seq = ind.getPrimaryDNA();
//		for(int i=0; i<maxSite; i++) {
//			if (seq.getBaseChar(i)==pop1Master.getBaseChar(i)) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	public void setSamplingStrategy(MultiPopCollectible.Strategy strat, int popNum) {
		this.samplingStrategy = strat;
		this.singlePopNum = popNum;
		addXMLAttr(XML_SAMPLESTRATEGY, String.valueOf(strat));
		if (strat == Strategy.SINGLE)
			addXMLAttr(XML_SINGLEPOP, String.valueOf(popNum));
	}
	
	
	public void reproduceAll() {
		int t = popList.get(0).getCurrentGenNumber();
		
		if (t<splittingTime) {
			ancPop.newGen( ancestralPopSize);
		}
		
		if (t==splittingTime) {
			//System.out.println("Splitting pop!");
			popList.remove(ancPop);
			
			pop1 = new Population();
			pop2 = new Population();
			pop1.setAutoShortenRoot(false);
			pop2.setAutoShortenRoot(false);
			globalRoot = ancPop.getRoot();
			
			pop1.initialize(rng, ancPop, Math.min(popOneSize, ancPop.size()));
			pop2.initialize(rng, ancPop, Math.min(popTwoSize, ancPop.size()));
			
			pop1.newGen(popOneSize);
			pop2.newGen(popTwoSize);
			
			//pop2Master = ((DNAFitness)pop2.getInd(0).getFitnessData()).getMaster();
			//pop1Master = changeMasterSequence(pop1);
			
			//The pops must be added to the popList if we want to be able to sample from them 
			popList.add(pop1);
			popList.add(pop2);
		}
		
		if (t>splittingTime) {
			pop1.newGen(popOneSize);
			pop2.newGen(popTwoSize);
			
			if (m12>0) {
				poiGen.setMean(popOneSize*m12);
				int numMigrants = poiGen.nextInt();
				if (numMigrants>0) {
					List<Individual> migrants = pop1.removeIndividuals( numMigrants );
					pop2.addIndividuals( migrants );
				}
			}
			
			if (m21>0) {
				poiGen.setMean(popTwoSize*m21);
				int numMigrants = poiGen.nextInt();
				if (numMigrants>0) {
					List<Individual> migrants = pop2.removeIndividuals( numMigrants );
					pop1.addIndividuals( migrants );
				}
			}
		}
		
		shortenGlobalRoot();
	}
	
	@Override
	public void reproduce(int popNum) {
		throw new IllegalStateException("Can't call reproduce on individual populations for the splitting model");
		
	}

	@Override
	public String getDescription() {
		return "Population split model with ancestral size: " + ancestralPopSize + "\n pop 1 size: " + popOneSize + "\n pop 2 size: " + popTwoSize + "\n Split time: " + splittingTime + "\n Migration 1->2: " + m12 + "\n Migration 2->1: " + m21 + "\n";
	}

	
	/**
	 * Experimental : Generate a new master sequence by 'tweaking' the current master sequence in some defined way, and then
	 * set all individuals and all statistics to use the new master
	 */
	private DNASequence changeMasterSequence(Population pop) {
		int sitesToChange = 30;
		DNASequence oldMaster = ((DNAFitness) pop.getInd(0).getFitnessData()).getMaster();
		DNASequence newMaster = oldMaster.getCopy();
		//The current changing strategy is to alter the master sequence at the first 'sitesToChange' sites
		for(int i=0; i<sitesToChange; i++) {
			char base = oldMaster.getBaseChar(i);
			switch (base) {
			case 'A' : newMaster.setBaseChar(i, 'G'); break;
			case 'T' : newMaster.setBaseChar(i, 'A'); break;
			case 'G' : newMaster.setBaseChar(i, 'C'); break;
			case 'C' : newMaster.setBaseChar(i, 'G'); break;
			}
		}
		
		pop.changeMasterSequence(newMaster);
		for(Statistic stat : OutputManager.globalOutputHandler.getStatistics()) {
			if (stat instanceof DNAStatistic) {
				((DNAStatistic)stat).setMaster(newMaster);
			}
		}
		
		System.out.print("Old master: ");
		for(int i=0; i<30; i++) {
			System.out.print( oldMaster.getBaseChar(i));
		}
		System.out.print("\nNew master: ");
		for(int i=0; i<30; i++) {
			System.out.print( newMaster.getBaseChar(i));
		}
		System.out.println();
		return newMaster;
	}
}
