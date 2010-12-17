package mutationModels;

import java.io.Serializable;

import cern.jet.random.engine.RandomEngine;

import dnaModels.DNASequence;
import siteModels.SiteFitnesses;
import xml.XMLParseable;

public abstract class MutationModel extends XMLParseable implements Serializable {

	public MutationModel(String blockName) {
		super(blockName);
	}
	
	public abstract void setRandomEngine(RandomEngine rng);
	
	public abstract void setRecombinationRate(double recRate);
	
	public abstract void mutate(DNASequence seq);
	
	public abstract double mutateUpdateFitness(DNASequence seq, DNASequence master, SiteFitnesses siteModel);
	
	public abstract double getMu();
	
	public abstract double getRecombinationRate();
	
	public abstract String getDescription();
	
	public abstract double getPiA();
	
	public abstract double getPiG();
	
	public abstract double getPiC();
	
	public abstract double getPiT();
}
