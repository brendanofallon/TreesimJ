package fitnessProviders;

import cern.jet.random.Exponential;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class QGenExponentialFitness extends QGenFitness {

	Exponential expGen;
	
	public QGenExponentialFitness(RandomEngine rng, double tau) {
		super(rng, tau);
		w = 1.0;
		this.tau = tau;
		expGen = new Exponential(1.0/tau, rng); 
	}
	
	@Override
	public void setRandomEngine(RandomEngine rng) {
		expGen = new Exponential(1.0/tau, rng); 
	}
	
	public void mutate() {
		w += expGen.nextDouble();
	}
	
	public String getDescription() {
		return "QGen EXPONENTIAL fitness with lambda = " + 1.0/tau + " & tau: " + tau;
	}
	
}
