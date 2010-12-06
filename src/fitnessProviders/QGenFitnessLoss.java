package fitnessProviders;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

public class QGenFitnessLoss extends QGenFitness {

	double loss = 0;
	
	public QGenFitnessLoss(RandomEngine rng, double tau, double loss) {
		super(rng, tau);
		w = 1.0;
		this.loss = loss;
	}
	
	protected QGenFitnessLoss(double tau, double currentFitness, double loss, Normal gaussianGen) {
		super(tau, currentFitness, gaussianGen);
		this.loss = loss;
	}
	
	public FitnessProvider getCopy() {
		QGenFitness newQG = new QGenFitnessLoss(tau, w, loss, gaussGen);
		return newQG;
	}

	public String getDescription() {
		return "QGen Fitness + Loss with tau (stdev) : " + tau;
	}


	public void mutate() {
		w += gaussGen.nextDouble()-loss;
	}
}
