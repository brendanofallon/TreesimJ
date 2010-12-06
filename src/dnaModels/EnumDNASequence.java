package dnaModels;

import java.util.BitSet;

import siteModels.CodonUtils;
import mutationModels.MutationModel;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;


/**
 * A different implementation of DNA sequence that was thought to be faster at one point, but is actually about
 * half as fast as a BitSetDNASequence. Gets the job done fine, but it's much slower than its bitset-based counterpart. 
 * @author brendan
 *
 */
public class EnumDNASequence extends DNASequence {

	public enum Bases {A, C, T, G};
	
	Bases[] bases;
	RandomEngine rng;
	
	public EnumDNASequence(RandomEngine rng, int length, MutationModel mm) {
		super(rng, length, mm);
		this.rng = rng;
		bases = new Bases[length];
		
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
	 * A constructor for cloning 
	 * @param rng
	 * @param seqToClone
	 * @param mm
	 */
	private EnumDNASequence(RandomEngine rng, Bases[] seqToClone, MutationModel mm) {
		super(rng, seqToClone.length, mm);
		
		bases = new Bases[seqToClone.length];
		for(int i=0; i<bases.length; i++)
			bases[i] = seqToClone[i];

	}

	
	/**
	 * This constructor makes a DNA sequence that looks sort of like an ORF, it starts with a start and contains no stop codons
	 * @param rng
	 * @param theLength
	 * @param mm
	 * @param useCodonsFlag
	 */
	public EnumDNASequence(RandomEngine rng, int theLength, MutationModel mm, CodonUtils useCodonsFlag) {
		super(rng, theLength, mm);

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
	
	@Override
	public char getBaseChar(int site) {
		if (site>=bases.length) {
			throw new IllegalArgumentException("Can't retrieve site #" + site + " because length is: " + bases.length);
		}
		switch (bases[site]) {
		case A: return 'A';
		case G: return 'G';
		case C: return 'C';
		case T: return 'T';
		}
		//We'll never get here
		return 'T';
	}

	@Override
	public DNASequence getCopy() {
		EnumDNASequence seq = new EnumDNASequence(rng, bases, mutationModel);
		return seq;
	}

	@Override
	public void setBaseChar(int site, char base) {
		switch (base) {
		case 'A': bases[site]=Bases.A;
		case 'G': bases[site]=Bases.G;
		case 'C': bases[site]=Bases.C;
		case 'T': bases[site]=Bases.T;
		}
	}

}
