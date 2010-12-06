package fitnessProviders;

import cern.jet.random.Gamma;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class QGenGammaFitness extends QGenFitness {

	Gamma gammaGen;
	
	public QGenGammaFitness(RandomEngine rng, double tau) {
		super(rng, tau);
		w = 1.0;
		this.tau = tau;
		gammaGen = new Gamma(1.0, 1.0, rng); //The values are just placeholders, new vals are supplied in mutate
	}
	
	public void mutate() {
		double alpha = w*w/tau;
		double lambda = w/tau;
		w = gammaGen.nextDouble(alpha, lambda);
	}
	
	@Override
	public void setRandomEngine(RandomEngine rng) {
		gammaGen = new Gamma(1.0, 1.0, rng); //The values are just placeholders, new vals are supplied in mutate
	}
	
	public String getDescription() {
		return "QGen GAMMA fitness with tau: " + tau;
	}
}
