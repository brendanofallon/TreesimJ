package demographicModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import demographicModel.MultiPopCollectible.Strategy;
import fitnessProviders.FitnessProvider;

import population.Locus;
import population.Population;
import statistics.Collectible;
import xml.TJXMLConstants;

/**
 * A demographic model of multiple populations with equal and constant size, all linked to each other with equal migration rates
 * @author brendan
 *
 */
public class IslandDemoModel extends MultiPopDemoModel {
	
	public static final String XML_ATTR = "island";
	public static final String XML_SIZE = "pop.size";
	public static final String XML_MIGRATION = "migration.rate";
	public static final String XML_NUMPOPS = "num.pops";
	public static final String XML_SAMPLESTRATEGY = "sampling.strategy";
	public static final String XML_SINGLEPOP = "sample.pop";
	
	double migRate;
	
	int populationSize;
	
	Poisson poiGen;
	
	public IslandDemoModel(int numPops, int size, double m) {
		super(TJXMLConstants.DEMOGRAPHIC_MODEL);
		
		for(int i=0; i<numPops; i++) {
			Population pop = new Population();
			popList.add(pop);
		}
		
		populationSize = size;
		migRate = m;
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SIZE, String.valueOf(size));
		addXMLAttr(XML_MIGRATION, String.valueOf(m));
		addXMLAttr(XML_NUMPOPS, String.valueOf(numPops));
	}

	public void setRng(RandomEngine rng) {
		this.rng = rng;
		poiGen = new Poisson(populationSize*migRate, rng);		
	}
	
	
	/**
	 * A debugging function that counts the total number of nodes in the tree, helpful for detecting memory leaks. 
	 * @return
	 */
	public int countTotalTreeSize() {
		Stack<Locus> inds = new Stack<Locus>();
		inds.push(globalRoot);
		int count = 0;
		int tips = 0;
		while(inds.size()>0) {
			Locus ind = inds.pop();
			count++;
			if (ind.isLeaf()) {
				tips++;
			}
			for(Locus kid : ind.getOffspring())
				inds.push(kid);
		}
		
		return count;
	}
	
	public void setSamplingStrategy(MultiPopCollectible.Strategy strat, int popNum) {
		this.samplingStrategy = strat;
		this.singlePopNum = popNum;
		addXMLAttr(XML_SAMPLESTRATEGY, String.valueOf(strat));
		if (strat == Strategy.SINGLE)
			addXMLAttr(XML_SINGLEPOP, String.valueOf(popNum));
	}
	
	/**
	 * Calls newGen on all populations, and then calls migrate() 
	 */
	@Override
	public void reproduceAll() {
		for(Population pop : popList) {
			pop.newGen( populationSize );
		}
		migrate();
		shortenGlobalRoot();	
	}
	
	private void migrate() {
		for(int i=0; i<popList.size(); i++) {
			for(int j=0; j<popList.size(); j++) {
				if (i!=j) {
					int numMigrants = poiGen.nextInt();
					if (numMigrants>0) {
						List<Locus> migrants = popList.get(i).removeIndividuals( numMigrants );
						popList.get(j).addIndividuals( migrants );
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "Island population model with " + popList.size() + " populations of size : " + popList.get(0).size() + " and migration rate: " + migRate;
	}

	@Override
	public void reproduce(int popNum) {
		throw new IllegalStateException("reproduce(int) should not be called for this demo. model");
	}
	
	
}
