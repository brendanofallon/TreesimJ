package fitnessProviders;

import xml.TJXMLConstants;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class QGenFitness extends FitnessProvider  {
	
	public static final String XML_ATTR = "Qgen.fitness";
	public static final String XML_DISTRO = "normal";
	public static final String XML_TAU = "tau";
	
	double w;
	double tau;
	Normal gaussGen;
	
	public QGenFitness(RandomEngine rng, double tau) {
		super(TJXMLConstants.FITNESS_MODEL);
		w = 1.0;
		gaussGen = new Normal(0, tau, rng);
		this.tau = tau;

		addXMLAttributes();
	}
	
	public void addXMLAttributes() {
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(TJXMLConstants.DISTRIBUTION, XML_DISTRO);
		addXMLAttr(TJXMLConstants.STDEV, String.valueOf(tau));
	}
	
	protected QGenFitness(double tau, double currentFitness, Normal gaussianGenerator) {
		super(TJXMLConstants.FITNESS_MODEL);
		w = currentFitness;
		gaussGen = gaussianGenerator;
		this.tau = tau;
	}
	
	@Override
	public void setRandomEngine(RandomEngine rng) {
		gaussGen = new Normal(0, tau, rng);
	}
	
	public FitnessProvider getCopy() {
		QGenFitness newQG = new QGenFitness(tau, w, gaussGen);
		return newQG;
	}

	
	public double getTau() {
		return tau;
	}
	
	public Object getSubstrate() {
		return new Double(w);
	}
	
	public void setFitness(double newFitness) {
		w = newFitness;
	}

	public String getDescription() {
		return "QGen NORMAL fitness with tau (stdev) : " + tau;
	}

	public Double getDoubleValue() {
		return w;
	}

	public double getFitness() {
		return w;
	}

	
	public String getStringValue() {
		return null;
	}

	public void mutate() {
		w += gaussGen.nextDouble();
	}

}
