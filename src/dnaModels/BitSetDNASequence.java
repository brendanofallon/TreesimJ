package dnaModels;

import java.util.BitSet;

import siteModels.CodonUtils;
import siteModels.CodonUtils.AminoAcid;

import mutationModels.MutationModel;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;


/**
 * An implementation of DNA sequence that uses two bitsets to store the data. Currently the fastest model. 
 * @author brendan
 *
 */
public class BitSetDNASequence extends DNASequence {

	BitSet bits1;
	BitSet bits2;

	BitSetPool pool = null;
	
	RandomEngine rng;
	
	/**
	 * Constucts a new DNA sequence using the stationary frequencies from the given mutation model
	 * @param rng A random number generator 
	 * @param theLength The length of this DNA sequence
	 * @param mm The mutation model associated with this DNA sequence. 
	 */
	public BitSetDNASequence(RandomEngine rng, int theLength, MutationModel mm) {
		super(rng, theLength, mm);
		this.rng = rng;
		bits1 = new BitSet(length);
		bits2 = new BitSet(length);

		Uniform uniGen = new Uniform(rng);
		for(int i=0; i<length; i++) {
			double r = uniGen.nextDouble();
			if (r<mm.getPiA())
				setBaseChar(i, 'A');
			else if (r<(mm.getPiA()+mm.getPiG()))
				setBaseChar(i, 'G');
			else if (r<(mm.getPiA()+mm.getPiG()+mm.getPiC()))
				setBaseChar(i, 'C');
			else
				setBaseChar(i, 'T');
		}
		
	}
	
	/**
	 * This constructor makes a DNA sequence that looks sort of like an ORF, it starts with a start and contains no stop codons
	 * @param rng
	 * @param theLength
	 * @param mm
	 * @param useCodonsFlag
	 */
	public BitSetDNASequence(RandomEngine rng, int theLength, MutationModel mm, CodonUtils useCodonsFlag) {
		super(rng, theLength, mm);
		this.rng = rng;
		bits1 = new BitSet(length);
		bits2 = new BitSet(length);

		Uniform uniGen = new Uniform(rng);
		
		//First make a string with a bunch of non-stop codons
		StringBuilder seqStr = new StringBuilder();
		seqStr.append("ATG");
		while(seqStr.length() < theLength) {
			String codon = makeNewCodon(mm, uniGen);
			seqStr.append(codon);
		}
		
		//Then actually set the bits based on the characters in the string
		for(int i=0; i<length; i++) {
			setBaseChar(i, seqStr.charAt(i));	
		}

	}
	
	


	private BitSetDNASequence(BitSet b1, BitSet b2, int theLength, MutationModel mm /* BitSetPool pool */) {
		super(null, theLength, mm);
		bits1 = b1;
		bits2 = b2;
		mutationModel = mm;
		this.length = theLength;
	}
	
	/**
	 * Returns a char representing the base at a given position
	 * @param which The site to get the char for
	 * @return A char representing the base
	 */
	public char getBaseChar(int which) {
		if (bits1.get(which)) {
			if (bits2.get(which)) return 'A';
			else return 'G';
		}
		else {
			if (bits2.get(which)) return 'C';
			else return 'T';
		}
	}
	
	/**
	 * Sets a DNA sequence based at a particular site based on a char representing the base
	 * @param which The site to set the base at
	 * @param base The base (A,C,G,or T), must be in uppercase
	 */
	public void setBaseChar(int which, char base) {
		switch(base) {
		case 'A' :  bits1.set(which, true);
					bits2.set(which, true);
					break;
		case 'G' :  bits1.set(which, true);
					bits2.set(which, false);
					break;
		case 'C' :  bits1.set(which, false);
					bits2.set(which, true);
					break;
		case 'T' :  bits1.set(which, false);
					bits2.set(which, false);
					break;
		}
	}
	
	
	/**
	 * Clones this DNA sequence, but does not deep-copy anything else, such as the mutation model.
	 * Something like 50% of the simulation run time is spent here. 
	 */
	public Object clone() {
		BitSet newB1;
		BitSet newB2;
		if (pool!=null) {
			 newB1 = pool.getNew();
			 newB1.clear();
			 newB1.or(bits1);
			 
			 newB2 = pool.getNew();
			 newB2.clear();
			 newB2.or(bits2);
			 
		}
		else {
		 newB1 = (BitSet)bits1.clone();
		 newB2 = (BitSet)bits2.clone();
		}
		
		return new BitSetDNASequence(newB1, newB2, length, mutationModel);
	}
	
	/**
	 * Used in the experimental BitSetPool implementation, which is currently not in use
	 */
	public void retireBits() {
		if (pool!=null) {
			pool.retire(bits1);
			pool.retire(bits2);
		}
	}

	public BitSet getBits1() {
		return bits1;
	}
	
	public BitSet getBits2() {
		return bits2;
	}
	
	public DNASequence getCopy() {
		return (DNASequence)this.clone();
	}

	public Double getDoubleValue() {
		return null;
	}

	/**
	 * A string representation of this sequence
	 */
	public String getStringValue() {
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<length; i++) {
			buf.append( getBaseChar(i));
		}
		return buf.toString();
	}

	/**
	 * OK, I guess we can just use toString as well
	 */
	public String toString() {
		return getStringValue();
	}


	
	
}
