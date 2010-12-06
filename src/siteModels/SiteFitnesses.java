package siteModels;

import java.io.Serializable;
import java.util.List;

import cern.jet.random.engine.RandomEngine;
import dnaModels.BitSetDNASequence;
import dnaModels.DNASequence;
import dnaModels.EnumDNASequence;
import mutationModels.MutationModel;
import xml.XMLParseable;

/**
 * Base class of all things that describe the distribution of fitness effects across a stretch of DNA. Fitnesses are 
 * calculated for the most part through the getFitnessDelta function, which provides the current state of the DNA sequence
 * in question, as well as a list of all sites mutated in the last generation (since the last call to getFitnessDelta), and the
 * states of these sites pre-mutation (such that, if we wanted to, we could fully reconstruct the state of the DNA sequence 
 * prior to mutation). getFitnessDelta then returns the **log change** in fitness to the DNA sequence.
 *  In most fitness models the fitness effects at all sites are independent, and thus getFitnessDelta works the same for all
 * of these models and simply compares the new state to the old state, and if there was a change from the master sequence
 * or to the master sequence the delta is computed accordingly. All such models can use the getFitnessDelta function as given
 * in this base class.
 *  In models where fitness effects at sites are not independent, such as the codon models, getFitnessDelta is overridden and
 * some more complex machinations occur to compute the fitness delta. In some cases, it's not directly possible to compute
 * the change in fitness, and in those cases we return Double.NaN to signal a full recomputation of the fitness.  
 *  
 *  Note that this class is 'stateless' - it does not contain any fields (besides XML parsing stuff). This is because, unlike
 *  FitnessProviders, only one of these is made and all Individuals share references to it. All stuff that may vary among
 *  Individuals, for instance the master sequence, should be contained in the FitnessProvider class and not in here. 
 *  
 * @author brendan
 *
 */
public abstract class SiteFitnesses extends XMLParseable implements Serializable {
	
	public SiteFitnesses(String blockName) {
		super(blockName);
	}
	
	/**
	 * Constructs a new 'master' sequence and sets the current master sequence to it. 
	 * The default here is just to generate a bunch of random bases, but some classes
	 * construct their own 'master' sequences. For instance, the CodonFitness model 
	 * generates a master sequence with no stop codons. 
	 * @param rng
	 * @param length
	 * @param mutModel
	 * @return A new DNA sequence
	 */
	public DNASequence generateMasterSequence(RandomEngine rng, int length, MutationModel mutModel) {
		DNASequence master =  new BitSetDNASequence(rng, length, mutModel);
		return master;
	}

	/**
	 * Set the master sequence used for this site fitness model
	 * @param master
	 */
//	public void setMaster(DNASequence master) {
//		this.master = master;
//	}
	
	/**
	 * A debugging function that calculates the fitness of a particular sequence from scratch, the result
	 * of this should always be equal to the currentFitness of the individual owning the sequence in question.
	 * Only used in some sitefitness models, the default is only appropriate for models in which fitness effects 
	 * across sites are independent 
	 * @param seq
	 * @return The fitness of the sequence
	 */
	public double recomputeFitness(DNASequence seq, DNASequence master) { 
		double sum = 0;
		for(int i=0; i<seq.length(); i++) {
			if (seq.getBaseChar(i) != master.getBaseChar(i)) 
				sum += getSiteFitness(i, seq);
		}
		return Math.exp(-sum);
	}

	/**
	 * Returns the *log change* in fitness for the sequence in question, given the list of sites that have been mutated
	 * and in addition the original states of each of the mutated sites (the state of the sequence prior to mutation). 
	 * This is overridden in some sitefitness models that are more complicated than this generic version (such as codonfitnesses)
	 * 
	 * @param seq
	 * @param mutatedSites
	 * @param originalState
	 * @return
	 */
	public double getFitnessDelta(DNASequence seq, DNASequence master, List<Integer> mutatedSites, List<Character> originalState) {
		double delta = 0;
		for(int i=0; i<mutatedSites.size(); i++) {
			int site = mutatedSites.get(i);

			boolean wasMaster = originalState.get(i).equals( master.getBaseChar(site));
			
			boolean nowMaster = seq.getBaseChar(site) == master.getBaseChar(site);
			//If it was in most fit state, and now it isn't, then subtract s from delta 
			if (wasMaster && ( ! nowMaster)) {
				delta -= getSiteFitness(site, seq);
			}
			//If it wasn't in the most fit state, and now it is, add to delta
			if (! wasMaster && nowMaster) {
				delta += getSiteFitness(site, seq);
			}
		}
		
		return delta;
		
	}
	
	/**
	 * Returns the fitness effect of this site when in the mutated (non-master) state. Some models require knowing the
	 * state of the entire DNA sequence in question to compute this, so we pass this in as well.   
	 * @param site
	 * @return
	 */
	public abstract double getSiteFitness(int site, DNASequence seq);
	
	//public abstract int numSites();
	
	public abstract String getDescription();
}
