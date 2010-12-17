package mutationModels;

import java.util.ArrayList;
import java.util.List;

import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import dnaModels.DNASequence;


/**
 * This is the superclass of all mutation models that utilize a matrix to lookup mutation probabilities
 * All but the simplest of mutation models will likely want to extend this class, and not MutationModel.
 * This class also houses some handy functions for calculating fitness changes as individual sites are mutated
 * @author brendan
 *
 */
public abstract class MutationMatrixModel extends MutationModel {
	
	static double[][] matrix;	//A matrix of probabilities of mutation from a given base to another, conditional 
								//on a mutation occurring... this does NOT include the mutation rate

	
	
	protected static int matrixSize = 4;
	protected static final int A = 0;
	protected static final int C = 1;
	protected static final int T = 2;
	protected static final int G = 3;
	
	static double[] rowTotals;	//The sum of each row of the matrix. Represents the probability that a base mutates to any other base
	static double rowTotalMax = 1.0;  	//The maximum row total, helpful for picking a base to mutate
	static boolean rowTotalsCalculated = false;
	
	protected static Uniform uniRNG;
	protected static Poisson poissonRNG;
	protected static double mu = -1;	//The probability that any individual base mutates in a given generation
	
	protected double recRate = 0.0;
	
	//A couple of buffers that are used to store information about which sites have been mutated, this info
	//is then passed to the sitemodel to calculate fitness quickly
	List<Integer> mutatedSites = new ArrayList<Integer>(3); 
	List<Character> originalStates = new ArrayList<Character>(3);
	
	public MutationMatrixModel(RandomEngine rng, double mu) {
		super(TJXMLConstants.MUTATION_MODEL);
		MutationMatrixModel.mu = mu;
		matrix = new double[matrixSize][matrixSize];
		uniRNG = new Uniform(rng);
		poissonRNG = new Poisson(1.0, rng);
		rowTotals = new double[matrixSize];
		rowTotalsCalculated = false;

	}
	
	/**
	 * Set the recombination rate for this sequence evolution model
	 * @param recRate
	 */
	public void setRecombinationRate(double recRate) {
		this.recRate = recRate;
	}
	
	/**
	 * Set the random engine for this mutation model (this sets both the uniform and poisson generators to use the new engine)
	 * @param rng
	 */
	public void setRandomEngine(RandomEngine rng) {
		uniRNG = new Uniform(rng);
		poissonRNG = new Poisson(1.0, rng);
	}
	
	protected int indexForBase(char base) {
		if (base=='A')	return A;
		if (base=='C')	return C;
		if (base=='T')	return T;
		if (base=='G')	return G;	
		return -1;
	}
	
	protected char baseForIndex(int index) {
		if (index==A) return 'A';
		if (index==C) return 'C';
		if (index==T) return 'T';
		if (index==G) return 'G';
		return 'X';
	}
	
	protected void calculateRowTotals() {
		double max = 0;
		for(int row=0; row<matrixSize; row++) {
			double sum = 0;
			for(int col=0; col<matrixSize; col++) {
				sum += matrix[row][col];
			}
			rowTotals[row] = sum;
			if (max < sum)
				max = sum;
		}
		
		if (max < 0.001) {
			System.out.println("Max row total is too low, something is wrong...");
		}
		
		rowTotalMax = max;
		rowTotalsCalculated = true;
	}
	
	protected double getProb(char from, char to) {
		return matrix[indexForBase(from)][indexForBase(to)];
	}

	protected double[] getProbRow(char from) {
		return matrix[indexForBase(from)];
	}
	
	public double getMu() {
		return mu;
	}
	
	public double getRecombinationRate() {
		return recRate;
	}
	
	public void mutate(DNASequence seq) {
		if (!rowTotalsCalculated) {
			calculateRowTotals();
		}
		
		int L = seq.length();
		
		double mean = mu*(double)L;
		
		poissonRNG.setMean(mean);
		int howmany = poissonRNG.nextInt();
		
		for(int i=0; i<howmany; i++) {
			int site = pickSiteToMutate(seq);
			char newBase = mutateBase(seq.getBaseChar(site));
			seq.setBaseChar(site, newBase);
		}//for howmany 

	}
	
	/**
	 * Picks a new base to mutate from the old one, using the probabilities stored in the appropriate
	 * row of the matrix
	 * @param initialBase (in char form)
	 * @return new base (in char form)
	 */
	private char mutateBase(char initialBase) {
		double r = uniRNG.nextDouble();
		
		double rowTotal = rowTotals[indexForBase(initialBase)];
		
		double[] prob = getProbRow(initialBase); //A list of mutation probabilities from 'site' to index
		double sum = 0;
		for(int j=0; j<matrixSize; j++) {
			sum += prob[j] / rowTotal; 	//Since we are mutating this base for sure, the row total must sum to 1.0, so we divide by the (precalculated) row total
			if (r<sum) 
				return baseForIndex(j);
		}
		System.err.println("Somehow a new base was not selected in MutationMatrixModel... sum is : " + sum + " row is "  + prob);
//		for(int i=0; i<prob.length; i++)
//			System.out.print(prob[i] + "\t");
//		System.out.println();
		return initialBase;
	}
	
	/**
	 * Pick a base to mutate. Not as easy as it sounds since each base may have a different
	 * mutation rate. We use a two step process to handle this. First, all sites are sampled with
	 * equal probability.  Second, a uniform 0..1 random variable is generated and compared to the
	 * rowTotal for the sampled base. If the random var is less than the rowTotal, we accept the site. 
	 * If not, we pick a new site. This is the same scheme used to sample Individuals in the 
	 * Population according to their fitness, and should result in bases being sampled in proportion
	 * to their mutation probability (the sum of the probabilities of mutating to each other base). 
	 *  
	 * @param seq
	 * @return
	 */
	//
	protected int pickSiteToMutate(DNASequence seq) {
		int site = uniRNG.nextIntFromTo(0, seq.length()-1);
		double r = uniRNG.nextDouble();
		
		int count = 0; //Sanity check
		
		//Try another base if the uniform r.v. is greater than the rowTotal
		while (r > rowTotals[indexForBase(seq.getBaseChar(site))]/rowTotalMax ) {
			site = uniRNG.nextIntFromTo(0, seq.length()-1);
			r = uniRNG.nextDouble();
			count++;
			if (count > 100) {
				System.out.println("Yikes ! Count is > 100, somethings wrong...");

				for(int i=0; i<rowTotals.length; i++) {
					System.out.println(baseForIndex(i) + " : " + rowTotals[i]);
				}
				if (rowTotalsCalculated) {
					System.out.println("Supposedly row totals have been calculated");
				}
				else {
					System.out.println("Row totals were not calculated, how did we get here?");
				}
				
			}
		}
			
		return site; 
	}
	
	/**
	 * This function both mutates a given DNA sequence according to the probabilities given in the mutationMatrix, and also
	 * tabulates the log of the total amount of change to fitness for this sequence (these calculations actually take place in the
	 * given site model). This is necessary during the mutational process because it's too slow to scan each sequence independently 
	 * after mutation to calculate it's new fitness, instead we just look at the difference for each site. 
	 * 
	 * @return The log of the change in fitness to this sequence, the new fitness will be calculated as (oldfitness)*Math.exp(-delta), where delta is the value returned
	 */
	public double mutateUpdateFitness(DNASequence seq, DNASequence master,
			SiteFitnesses siteModel) {
		
		if (!rowTotalsCalculated) {
			calculateRowTotals();
		}
		
		int L = seq.length();
		double mean = mu*(double)L;
		poissonRNG.setMean(mean);
		double delta = 0;
		int howmany = poissonRNG.nextInt();
		
		if (howmany > 0) {
			mutatedSites.clear();
			originalStates.clear();
		}
		
		for(int i=0; i<howmany; i++) {
			int site = pickSiteToMutate(seq);
			char originalState = seq.getBaseChar(site);
			
			//Some site models break if multiple mutations occur at the same site on the same generation
			//This line of code prevents this from happening
			if (! mutatedSites.contains(site)) {
				mutatedSites.add(site);
				originalStates.add(originalState);
			}
			
			char newBase = mutateBase(originalState);
			
			seq.setBaseChar(site, newBase);
		}
		
		if (howmany>0) {
			delta = siteModel.getFitnessDelta(seq, master, mutatedSites, originalStates);
		}
		
		return delta;	
	}
	
}
