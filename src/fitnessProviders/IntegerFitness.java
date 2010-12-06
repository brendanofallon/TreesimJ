package fitnessProviders;

import xml.TJXMLConstants;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

public class IntegerFitness extends FitnessProvider {

	public static final String XML_ATTR = "Integer.fitness";
	public static final String XML_MURATE = "mutation.rate";
	public static final String XML_L = "length";
	
	Integer mutnum;
	double mutationRate;
	static Uniform uniGen;
	RandomEngine rng;
	double fitness;
	Integer L;
	double expS;
	
	public IntegerFitness(RandomEngine rng, double mutationRate, double s, int L) {
		super(TJXMLConstants.FITNESS_MODEL);
		if (uniGen == null)
			uniGen = new Uniform(rng);
		this.rng = rng;
		this.mutationRate = mutationRate;
		this.L = L;
		mutnum = 0;
		fitness = 1.0;
		expS = Math.exp(-s);
		addXMLAttributes();
	}
	
	
	@Override
	public void addXMLAttributes() {
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_MURATE, String.valueOf(mutationRate));
		addXMLAttr(XML_L, String.valueOf(L));
	}
	
	@Override
	public void setRandomEngine(RandomEngine rng) {
		this.rng = rng;
		uniGen = new Uniform(rng);
	}
	
	
	private IntegerFitness(RandomEngine rng, int muts, double mutationRate, double expS, double fitness, int L) {
		super(TJXMLConstants.FITNESS_MODEL);
		if (uniGen == null)
			uniGen = new Uniform(rng);
		this.rng = rng;
		this.mutationRate = mutationRate;
		this.L = L;
		this.expS = expS;
		mutnum = muts;
		this.fitness = fitness;
	}
	
	public FitnessProvider getCopy() {
		return new IntegerFitness(rng, mutnum, mutationRate, expS, fitness, L);
	}


	
	public String getDescription() {
		return "Integer fitness with mu=" + mutationRate + " s= " + Math.log(expS) + " and L=" + L;
	}

	public Integer getMuts() {
		return mutnum;
	}

	public Double getDoubleValue() {
		return new Double(mutnum);
	}

	public double getFitness() {
		return fitness;
	}

	public String getStringValue() {
		return mutnum.toString();
	}


	public Object getSubstrate() {
		return mutnum;
	}


	public void mutate() {
		double r = uniGen.nextDouble();
		if (r<mutationRate*(double)L) {
			r = uniGen.nextDouble();
			double prob = ((double)mutnum/(double)L);
			if (r< prob ) { //back mutation
				mutnum--;
				fitness /= expS;
			}
			else {	//forward mutation
				mutnum++;
				fitness *= expS;
			}
		}
	}


	public void setFitness(double w) {
		System.err.println("Error: Can't set fitness of an IntegerFitness model");
	}

	

}
