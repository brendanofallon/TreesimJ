package fitnessProviders;

import xml.TJXMLConstants;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

public class TwoAlleleFitness extends FitnessProvider {

	public static final String XML_ATTR = "two.allele.fitness";
	
	Uniform uniGen;
	boolean state = true;
	double s = 0;
	double oneMinusS;
	double forwardMu = 0;
	double backMu = 0;
	
	public TwoAlleleFitness(Uniform uniformRng, boolean initialState, double s, double forwardMu, double backMu) {
		super(TJXMLConstants.FITNESS_MODEL);
		this.state = initialState;
		this.s = s;
		oneMinusS = 1.0-s;
		this.forwardMu = forwardMu;
		this.backMu = backMu;
		this.uniGen = uniformRng;

		addXMLAttributes();
	}
	
	public void addXMLAttributes() {
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(TJXMLConstants.FORWARD_MUTATION, String.valueOf(forwardMu));
		addXMLAttr(TJXMLConstants.BACK_MUTATION, String.valueOf(backMu));
		addXMLAttr(TJXMLConstants.SELECTION, String.valueOf(s));
	}
	
	
	public void setRandomEngine(RandomEngine rng) {
		uniGen = new Uniform(rng);
	}
	
	
	/**
	 * The dummy variable is just to discriminate this constructor from the public one. We dont' want to use the constructor
	 * that adds XML attributes for copying, since we'll end up copying all the XML info every time a new individuals inherits this
	 * 
	 * @param uniformRng
	 * @param initialState
	 * @param s
	 * @param forwardMu
	 * @param backMu
	 * @param dummy
	 */
	private TwoAlleleFitness(Uniform uniformRng, boolean initialState, double s, double forwardMu, double backMu, int dummy) {
		super(TJXMLConstants.FITNESS_MODEL);
		this.state = initialState;
		this.s = s;
		oneMinusS = 1.0-s;
		this.forwardMu = forwardMu;
		this.backMu = backMu;
		this.uniGen = uniformRng;
	}
	
	/**
	 * Called to copy information from parent to offspring Individual
	 */
	public FitnessProvider getCopy() {
		return new TwoAlleleFitness(uniGen,state, s, forwardMu, backMu, 0); //Must use the private constructor since it doesn't add XML info
	}


	public String getDescription() {
		return "Two-Allele fitness model with s=" + s + " forward mutation rate = " + forwardMu + " back mutation rate = " + backMu;
	}

	public boolean isMoreFitAllele() {
		return state;
	}

	public Double getDoubleValue() {
		return s;
	}

	public double getFitness() {
		return state ? 1.0 : oneMinusS;
	}

	public String getStringValue() {
		return "";
	}

	
	public Object getSubstrate() {
		return state;
	}


	public void mutate() {
		double r = uniGen.nextDouble();
		if (state) {
			if (r<forwardMu) { //forward mutation, to less fit state
				state = false;
			}
		} else {
			if (r<backMu) {	//back mutation, to more fit state
				state = true;
			}
		}
	}

	public void setFitness(double w) {
		System.err.println("Can't set fitness of a two-allele fitness model");
	}

}
