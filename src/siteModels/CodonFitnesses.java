package siteModels;

import java.util.ArrayList;
import java.util.List;

import population.Individual;

import mutationModels.MutationModel;
import cern.jet.random.engine.RandomEngine;
import dnaModels.BitSetDNASequence;
import dnaModels.DNASequence;
import fitnessProviders.DNAFitness;

import siteModels.CodonUtils.AminoAcid;
import xml.TJXMLConstants;

/**
 * A site model in which selection acts to maintain a single sequence of amino acids. Synonymous changes and nonsynonymous changes
 * have different fitness impacts, but changes among non-master states are always neutral, regardless of whether or not the AA
 * coded for has changed. 
 * 
 * @author brendan
 *
 */
public class CodonFitnesses extends SiteFitnesses {

	public static final String XML_ATTR = "codon.sitemodel";
	public static final String XML_SYNSELECTION = "syn.selection";
	public static final String XML_NONSYNSELECTION = "nonsyn.selection";
	
	double syn; //Selection against synonymous changes
	double nonsyn; //Selection against nonsynonymous changes
	
	List<AminoAcid> masterCodons = new ArrayList<AminoAcid>();
	CodonUtils translator = new CodonUtils();
	
	StringBuilder origCodon = new StringBuilder();
	char[] triplet = new char[3];
	
	public CodonFitnesses(double syn, double nonsyn) {
		super(TJXMLConstants.SITE_MODEL);
		this.syn = syn;
		this.nonsyn = nonsyn;
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SYNSELECTION, String.valueOf(syn));
		addXMLAttr(XML_NONSYNSELECTION, String.valueOf(nonsyn));
	}

	/**
	 * Generates a new master sequence with no stop codons, and also sets the list of master codons (for quick lookup
	 * of what the master sequence codon is). 
	 */
	public DNASequence generateMasterSequence(RandomEngine rng, int length, MutationModel mutModel) {
		DNASequence master =  new BitSetDNASequence(rng, length, mutModel, new CodonUtils());
		
		for(int i=0; i<(master.length()-2); i+=3) {
			triplet[0] = master.getBaseChar(i);
			triplet[1] = master.getBaseChar(i+1);
			triplet[2] = master.getBaseChar(i+2);
			String codon = String.valueOf(triplet);
			AminoAcid aa = CodonUtils.translate(codon);
			if (aa==AminoAcid.Stop) {
				System.out.println("Warning : Codon fitness master sequence contains stop codons. This shouldn't happen.");
			}
			masterCodons.add( aa );
		}
		
//		CodonUtils codUtils = new CodonUtils();
//		System.out.println("Master sequence: ");
//		for(int i=0; i<master.length(); i+=3)
//			System.out.print(master.getBaseChar(i) + "" + master.getBaseChar(i+1) + "" + master.getBaseChar(i+2) + " ");
//		System.out.println();
//		for(int i=0; i<masterCodons.size(); i++)
//			System.out.print(" " + codUtils.toChar(masterCodons.get(i)) + "  ");
//		System.out.println();
		return master;
	}


	/**
	 * Change the master sequence *and master codons* for this site model. Note that this doesn't pay attention to
	 * base frequencies or stop codons. 
	 */
	public void setMaster(DNASequence master) {
		
		masterCodons.clear();
		for(int i=0; i<(master.length()-2); i+=3) {
			triplet[0] = master.getBaseChar(i);
			triplet[1] = master.getBaseChar(i+1);
			triplet[2] = master.getBaseChar(i+2);
			String codon = String.valueOf(triplet);
			AminoAcid aa = CodonUtils.translate(codon);
			masterCodons.add( aa );
			
		}
	}
	
	@Override
	public String getDescription() {
		return "Codon fitness model with selection against synonymous changes : " + syn + "\n  Selection against nonsynonymous changes : " + nonsyn + "\n";
	}

	private static String getCodon(int startSite, DNASequence seq) {
		return String.valueOf(new char[]{seq.getBaseChar(startSite), seq.getBaseChar(startSite+1), seq.getBaseChar(startSite+2)});
	}
	
	/**
	 * Compute the fitness of the given dna sequence from scratch. Time-consuming but necessary in some circumstances. 
	 */
	public double recomputeFitness(DNASequence seq, DNASequence master) {
		double sum = 0;
		for(int i=0; i<(seq.length()-2); i+=3) {
			int difs = 0;
			if (seq.getBaseChar(i)!=master.getBaseChar(i)) difs++;
			if (seq.getBaseChar(i+1)!=master.getBaseChar(i+1)) difs++;
			if (seq.getBaseChar(i+2)!=master.getBaseChar(i+2)) difs++;
			if (difs > 0) {
				AminoAcid seqAA = CodonUtils.translate(seq, i);
				AminoAcid masterAA = masterCodons.get(i/3);
				if (seqAA != masterAA)
					sum += nonsyn;
				else {
					sum += difs*syn;
				}
			}
		}
		return Math.exp(-sum);
	}
	
	/**
	 * A debugging function useful for comparing a given sequence to the master and counting differences
	 */
	private static void compareCodonsToMaster(Individual ind) {
		DNASequence seq = ind.getPrimaryDNA();
		CodonUtils codUtils = new CodonUtils();
		DNASequence master = ((DNAFitness)ind.getFitnessData()).getMaster();
		System.out.println("-----");
		
		
		for(int i=0; i<master.length(); i++) {
			if (i>0 && i%3==0)
				System.out.print(" ");
			
			if (master.getBaseChar(i)!=seq.getBaseChar(i)) 
				System.out.print("*");
			else
				System.out.print(" ");
			
		}
		System.out.println();
		for(int i=0; i<master.length(); i+=3)
			System.out.print(master.getBaseChar(i) + "" + master.getBaseChar(i+1) + "" + master.getBaseChar(i+2) + " ");
		System.out.println();
		for(int i=0; i<seq.length(); i+=3)
			System.out.print(seq.getBaseChar(i) + "" + seq.getBaseChar(i+1) + "" + seq.getBaseChar(i+2) + " ");
		System.out.println("\t" + ind.getFitness());
		for(int i=0; i<seq.length(); i+=3) {
			char[] trip = new char[3];
			trip[0] = seq.getBaseChar(i);
			trip[1] = seq.getBaseChar(i+1);
			trip[2] = seq.getBaseChar(i+2);
			AminoAcid aa = CodonUtils.translate(String.valueOf(trip));
			System.out.print(" " + codUtils.toChar(aa) + "  ");
		}
		System.out.println();
		
		if (ind.getFitness() > 1.00) {
			System.out.println("Fitness > 1.0! : " + ind.getFitness());
		}
	}
	
	@Override
	/**
	 * We override the usual function here since we only base things on whether or not the AA has changed..
	 */
	public double getFitnessDelta(DNASequence seq, DNASequence master, List<Integer> mutatedSites, List<Character> originalState) {
		double delta = 0;
		
		for(int i=0; i<mutatedSites.size(); i++) {
			
			int site = mutatedSites.get(i);
			//System.out.println("Mutating site #" + site);
			int codonStart = site - site % 3;

			if (codonStart+3 <= seq.length()) {
				triplet[0] = seq.getBaseChar(codonStart);
				triplet[1] = seq.getBaseChar(codonStart+1);
				triplet[2] = seq.getBaseChar(codonStart+2);
				
				//If there have been multiple mutations at this codon we don't know which 
				//came first, and therefore don't know how much fitness to add/subtract, so
				//we must force a recompute if there are every multiple mutations at a single
				//codon position
				int totalDifs = 0;
				if (triplet[0] != master.getBaseChar(codonStart)) totalDifs++;
				if (triplet[1] != master.getBaseChar(codonStart+1)) totalDifs++;
				if (triplet[2] != master.getBaseChar(codonStart+2)) totalDifs++;
				if (originalState.get(i) != master.getBaseChar(site)) totalDifs++; //if the original state differs by two, we also need to force a recalc
				
				if (totalDifs > 1) {
					//System.out.println("Multiple difs, forcing recalc");
					return Double.NaN;
				}
				String currentCodon = String.valueOf(triplet);
				
				int cIndex = site % 3;
				origCodon.replace(0, 3, currentCodon);
				origCodon.replace(cIndex, cIndex+1, String.valueOf(originalState.get(i)));

				
				AminoAcid origAA = CodonUtils.translate(origCodon.toString());
				AminoAcid thisAA = CodonUtils.translate(triplet[0], triplet[1], triplet[2]);
				AminoAcid masterAA = masterCodons.get(site / 3);

				if (origAA == masterAA && thisAA != masterAA) { //Nonsyn. forward mutation
					delta -= nonsyn;
					continue;
				}
				
				if (origAA != masterAA && thisAA == masterAA) {
					delta += nonsyn;
					continue;
				}
				
				
				if (origAA == thisAA) { //No change in AA state, but there may still be a synonymous fitness impact 
					if (origAA == masterAA) { //We were and we are still in the master AA state
						char masterBase =  master.getBaseChar(site);
						if (origCodon.charAt(cIndex) == masterBase && triplet[cIndex] != masterBase ) {
							delta -= syn;	//synonymous forward change while in the master AA state
//							System.out.println("Forward: orig: " + origCodon.toString() + "\t" + origAA + "\n" +
//										       "      current: " + triplet[0] + "" + triplet[1] + "" + triplet[2] + "\t" + thisAA + "\n" +
//										       "       master: " + master.getBaseChar(codonStart) + "" + master.getBaseChar(codonStart+1) + "" + master.getBaseChar(codonStart+2) + "\t" + masterAA );
//							System.out.println();
							continue;
						}
						
						if (origCodon.charAt(cIndex) != masterBase && triplet[cIndex] == masterBase ) {
							delta += syn;	//synonymous back mutation while in master AA state
//							System.out.println("Backward: orig: " + origCodon.toString() + "\t" + origAA + "\n" +
//								       "      current: " + triplet[0] + "" + triplet[1] + "" + triplet[2] + "\t" + thisAA + "\n" +
//								       "       master: " + master.getBaseChar(codonStart) + "" + master.getBaseChar(codonStart+1) + "" + master.getBaseChar(codonStart+2) + "\t" + masterAA );
//							System.out.println();
						}
						
					}
//					else { 
//						//this is a 'synonymous' mutation among non-master AA's, it has zero fitness effect
//					}
					continue;
				}
				
				//We get here when there's a non-synonymous mutation among non-master bases, this has no fitness effect
				
			}
		}
		
		
		return delta;
	}
	
	@Override
	/**
	 * This never gets called, we do everything in getFitnessDelta
	 * 
	 */
	public double getSiteFitness(int site, DNASequence seq) {
		return 0;
	}

}
