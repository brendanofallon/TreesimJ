package dnaModels;

import java.io.Serializable;
import java.util.BitSet;

import population.Inheritable;

import siteModels.CodonUtils;
import siteModels.CodonUtils.AminoAcid;

import mutationModels.MutationModel;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;


/**
 * A abstract sequence of DNA. This can be used as a generic inheritable (something that is inherited, but that does not affect fitness)
 * and also as the substrate of a FitnessProvider. Various subclasses provide alternative ways to describe the DNA. 
 * WARNING : When used with a FitnessProvider, the MutaionModel provided to this constructor is ignored, and the MutationModel
 * supplied to the FitnessProvider takes precedence. This allows for more efficient calculation of fitnesses.
 * 
 * If we'd like to be able to set the global DNASequence 'type, we should implement a DNASequenceFactory class that creates
 * DNASequences of certain kinds, for use by all the various models that create / manipulate them.
 * 
 * @author brendan
 *
 */
public abstract class DNASequence implements Inheritable, Serializable {

	protected MutationModel mutationModel;
	protected static Uniform uniGen = null; //Keep this static so it's not cloned when we copy this sequence. 
	protected static boolean rngInitialized = false;
	
	int length;
	
	public DNASequence(RandomEngine rng, int length, MutationModel mutationModel) {
		if (! rngInitialized) {
			initializeRNG(rng);
		}
		
		this.length = length;
		this.mutationModel = mutationModel;
	}
	
	protected void initializeRNG(RandomEngine rng) {
		uniGen = new Uniform(rng);
	}
	
	public MutationModel getMutationModel() {
		return mutationModel;
	}
	
	
	public boolean equals(DNASequence seq1, DNASequence seq2) {
		if (seq1.length != seq2.length) {
			return false;
		}
		else {
			for(int i=0; i<seq1.length; i++) {
				if (seq1.getBaseChar(i)!=seq2.getBaseChar(i))
					return false;
			}
			return true;
		}
	}
	
	/**
	 * Mutate this sequence
	 */
	public void mutate() {
		mutationModel.mutate(this);
	}
	
	
	/**
	 * Obtain a new copy of the sequence
	 */
	public abstract DNASequence getCopy();
	
	
	/**
	 * A char representing the base at a particular site
	 * @param site
	 * @return
	 */
	public abstract char getBaseChar(int site);
	
	
	/**
	 * Set the site to be a particular base.
	 * 
	 * @param site
	 * @param base
	 */
	public abstract void setBaseChar(int site, char base);
	
	
	/**
	 * The length of the sequence
	 * @return
	 */
	public int length() {
		return length;
	}
	
	/**
	 * Generates a new non-stop codon using the base frequencies specified in the given mutational model
	 * @param mm
	 * @param uniGen
	 * @return
	 */
	protected String makeNewCodon(MutationModel mm, Uniform uniGen) {
		StringBuilder codon = new StringBuilder();
		
		do {
			codon.setLength(0);
			for(int i=0; i<3; i++) {
				double r = uniGen.nextDouble();
				if (r<mm.getPiA())
					codon.append('A');
				else if (r<(mm.getPiA()+mm.getPiG()))
					codon.append('G');
				else if (r<(mm.getPiA()+mm.getPiG()+mm.getPiC()))
					codon.append('C');
				else
					codon.append('T');
			}
		} while (CodonUtils.translate(codon.toString())==AminoAcid.Stop);
		
		return codon.toString();
	}
	
	
	/***** Deprecated methods ************/
	
	public String getStringValue() { return ""; };
	
	public Double getDoubleValue() { return Double.NaN; };
	
	
}
