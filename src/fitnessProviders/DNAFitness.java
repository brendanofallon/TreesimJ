package fitnessProviders;

import population.Locus;
import population.Recombineable;
import mutationModels.MutationModel;
import siteModels.CodonUtils;
import siteModels.SiteFitnesses;
import siteModels.CodonUtils.AminoAcid;
import xml.TJXMLConstants;
import cern.jet.random.engine.RandomEngine;
import dnaModels.DNASequence;

/**
 * This class calculates fitness based on the state of a DNA sequence. It also provides functions for mutating the 
 * DNA sequence and for plugging in different site models which describe how mutations at different sites impact fitness.
 * 
 * @author brendan
 *
 */
public class DNAFitness extends FitnessProvider implements Recombineable {

	//The XML attribute that identifies this model, the associated xml block will read <Fitness.model type="DNA.fitness">....
	public static final String XML_ATTR = "DNA.fitness";
	

	protected DNASequence master; 		//The sequence with the highest possible fitness. We need a reference to this so we know which mutations are deleterious, etc.
	protected DNASequence seq;				//The actual DNA sequence of this individual
	protected Double currentFitness = 1.0;	//The fitness of this individual. This is updated in the function mutateUpdateFitness
	protected RandomEngine rng;				//The random number engine used to generate random numbers 
	protected MutationModel mutMod; 			//The mutation model of this individual
	protected SiteFitnesses siteModel;		//The model that describes the fitness impact of mutations at different sites
	
	/**
	 * Construct a new DNA fitness model using the supplied arguments. The initial state of the DNA sequence will
	 * be equal to the master sequence
	 * 
	 * @param rng
	 * @param masterSeq
	 * @param sMod
	 * @param mm
	 */
	public DNAFitness(RandomEngine rng, DNASequence masterSeq, SiteFitnesses sMod, MutationModel mm) {
		super(TJXMLConstants.FITNESS_MODEL); //Tell the base class that this model is a fitness model
		seq = (DNASequence)masterSeq.getCopy();	//Initially, our dna is a copy of the master sequence dna

		master = masterSeq;
		this.rng = rng;
		mutMod = mm;
		siteModel = sMod;
		
		addXMLAttributes();
	}

	public void addXMLAttributes() {
		//Add the associated XML attributes and xml children for this model
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(TJXMLConstants.LENGTH, String.valueOf(master.length()));
		addXMLAttr(TJXMLConstants.MUTATIONRATE, String.valueOf(mutMod.getMu()));
		addXMLAttr(TJXMLConstants.RECOMBINATIONRATE, String.valueOf(mutMod.getRecombinationRate()));
		addXMLChild(mutMod);
		addXMLChild(siteModel);
	}
	
	/**
	 * This constructor is only accessible through getCopy, which is only used to create new Individuals as part of Population.newGen
	 * Thus, we can (and must!) ignore XML-parsing related duties here. Otherwise we'll copy all the numerous XML bits with each copy
	 * of each individual, and the simulation will be very, very slow. 
	 * 
	 * @param rng
	 * @param newSeq
	 * @param masterSeq
	 * @param sMod
	 * @param fitness
	 * @param mm
	 */
	private DNAFitness(RandomEngine rng, DNASequence newSeq, DNASequence masterSeq, SiteFitnesses sMod, double fitness, MutationModel mm) {
		super(TJXMLConstants.FITNESS_MODEL);
		seq = (DNASequence)newSeq.getCopy();

		master = masterSeq;
		this.rng = rng;
		this.siteModel = sMod;
		mutMod = mm;
		this.currentFitness = fitness;
	}
	
	public double getMu() {
		return mutMod.getMu();
	}
	
	public MutationModel getMutationModel() {
		return mutMod;
	}
	
	public Object getSubstrate() {
		return seq;
	}
	
	public DNASequence getMaster() {
		return master;
	}
	
	public int getLength() {
		return seq.length();
	}
	
	public SiteFitnesses getSiteModel() {
		return siteModel;
	}
	
	/**
	 * Set the master DNA sequence for this fitness model, causes a complete recalculation of fitness (as it must)
	 * @param newMaster
	 */
	public void setMasterSequence(DNASequence newMaster) {
		master = newMaster;
		//siteModel.setMaster(master);
		currentFitness = siteModel.recomputeFitness(seq, master);
	}
	
	/**
	 * Obtain a copy of this DNAFitness object. This is not really a "deep" copy, since nearly everything is just passed 
	 * by reference, meaning that we don't actually clone any of the objects (site fitness models, mutation models, etc) on which
	 * this this thing depends
	 */
	public FitnessProvider getCopy() {
		return new DNAFitness(rng, seq, master, siteModel, currentFitness, mutMod);
	}
	
	/**
	 * This is called by the OutputManager to construct the summary for the simulation run. 
	 */
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append("Fitness described by DNA of length L = " + seq.length() + "\n");
		desc.append("Mutation model: " + mutMod.getDescription() +"\n");
		desc.append("Site model : " + siteModel.getDescription() +"\n");
		
		return desc.toString();
	}

	
	public Double getDoubleValue() {
		return currentFitness;
	}

	
	public double getFitness() {
		return currentFitness;
	}

	public String getStringValue() {
		return seq.getStringValue();
	}
	
	/**
	 * A debugging method that recomputes the fitness by examining every position in the sequence, this can be checked against
	 * the value of currentFitness to make sure that the two are equal
	 * @param master
	 * @return
	 */
	public void verifyFitness(DNASequence master ) {
		double calcFitness = siteModel.recomputeFitness(seq, master);
		if ( Math.abs(calcFitness-currentFitness)>1e-8) {
			System.out.println("Recalculated fitness is not the same as the current fitness! \n Recalced: " + calcFitness + "\n currentFitness: " + currentFitness);

		}
	}

	
	
	/**
	 * Mutate this DNA sequence. There's potential for a lot of confusion here... 
	 * DNAsequences are also generic inheritables, which means they
	 * must implement .mutate(). However, that version of mutate doesn't allow for updating fitness values, which is 
	 * mandatory for efficiency ( we can't rescan every sequence after mutation to determine fitness)
	 *   So this FitnessProvider ignores the DNASequnce mutation model, and instead uses its own, which calls
	 *  mutateUpdateFitness. So it could be confusing if sequences are instantiated with JC mutation, and DNAFitness
	 *  uses GTR mutation. 
	 */
	public void mutate() {
		//delta is the log *change* in fitness which is calculated as we mutate individual sites. 
		double delta = mutMod.mutateUpdateFitness(seq, master, siteModel);
		
		//In some cases we simply cannot calculate the fitness change for the information given (for instance, if there are multiple 
		//mutations in the same codon, we do not know which one came first, and therefore cannot separate nonsynonymous from 
		//synonymous mutations. In this case, the siteModel returns NaN for delta, which signals a full recomputation of the 
		//fitness of the sequence. Currently, only the codon model uses this flag. 
		if (Double.isNaN(delta)) { 
			currentFitness = siteModel.recomputeFitness(seq, master);
			//System.out.println("Recomputing!");
		}
		else {
			currentFitness *= Math.exp(delta);
		}
		
		//The following lines of code turns on a (very) expensive check for the accuracy of the fitness of individuals by forcing
		//a complete recalculation of their fitness 
//		if (Math.random() < 0.05) {
//			verifyFitness(master);
//		}
	}

	//Can't set fitness for a DNA sequence
	public void setFitness(double w) {	}

	
	@Override
	public void setRandomEngine(RandomEngine rng) {
		this.rng = rng;
		mutMod.setRandomEngine(rng);
	}

	@Override
	public int length() {
		return seq.length();
	}

	@Override
	public Object getRegion(int min, int max) {
		return seq.getRegion(min, max);
	}

	@Override
	public void setRegion(int min, int max, Object region) {
		seq.setRegion(min, max, region);
		currentFitness = siteModel.recomputeFitness(seq, master);
	}

}
