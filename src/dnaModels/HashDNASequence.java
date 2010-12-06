package dnaModels;

import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xml.internal.security.signature.Reference;

import mutationModels.MutationModel;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import dnaModels.EnumDNASequence.Bases;

public class HashDNASequence extends DNASequence {

	public enum Base {A, C, T, G};
	
	HashMap<Integer, Base> mutatedSites;
	
	DNASequence reference;
	
	RandomEngine rng;
	
	long[] bits;
	
	public HashDNASequence(RandomEngine rng, BitSetDNASequence reference, MutationModel mm) {
		super(rng, reference.length(), mm);
		this.rng = rng;
		this.reference = reference;
		bits = new long[32];
		//mutatedSites = new HashMap<Integer, Base>();
		
//		Uniform uniGen = new Uniform(rng);
//		for(int i=0; i<length; i++) {
//			double r = uniGen.nextDouble();
//			if (r<mm.getPiA())
//				setBaseChar(i, 'A');
//			else if (r<(mm.getPiA()+mm.getPiG()))
//				setBaseChar(i, 'G');
//			else if (r<(mm.getPiA()+mm.getPiG()+mm.getPiC()))
//				setBaseChar(i, 'C');
//			else
//				setBaseChar(i, 'T');
//		}
	}
	
	private HashDNASequence(RandomEngine rng, int length, long[] otherbits, MutationModel mm, DNASequence reference) {
		super(rng, length, mm);
		this.reference = reference;
		//mutatedSites = (HashMap<Integer, Base>)muts.clone();
		bits = (long[])otherbits.clone();
	}

	
	@Override
	public char getBaseChar(int site) {
		return reference.getBaseChar(site);
//		Base b = mutatedSites.get(site);
//		if (b != null) {
//			switch (b) {
//			case A: return 'A';
//			case G: return 'G';
//			case C: return 'C';
//			case T: return 'T';
//			}
//		}
//		else 
//			return reference.getBaseChar(site);
//		
//		return 0;
	}

	@Override
	public DNASequence getCopy() {
		return new HashDNASequence(rng, length, bits, mutationModel, reference);
	}

	private Base baseForChar(char b) {
		switch (b) {
		case 'A': return Base.A;
		case 'G': return Base.G;
		case 'C': return Base.C;
		case 'T': return Base.T;
		}
		return null;
	}
	
	@Override
	public void setBaseChar(int site, char base) {
		
//		char master = reference.getBaseChar(site);
//		
//		if (master != base) {
//			Base newBase = baseForChar(base);
//			mutatedSites.put(site, newBase);
//		}
//		else {
//			mutatedSites.remove(site);
//		}
		
	}

}
