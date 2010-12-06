package fitnessProviders;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

public class QGenUniformFitness extends QGenFitness {

	Uniform uniGen;
	
	public QGenUniformFitness(RandomEngine rng, double tau) {
		super(rng, tau);
		w = 1.0;
		this.tau = tau;
		uniGen = new Uniform(-1.0*tau/(3.0*1.4142135), tau/(3.0*1.4142135), rng);
	}
	
	public void mutate() {
		w += uniGen.nextDouble();
	}

	@Override
	public void setRandomEngine(RandomEngine rng) {
		uniGen = new Uniform(-1.0*tau/(3.0*1.4142135), tau/(3.0*1.4142135), rng);
	}
	
	public String getDescription() {
		return "QGen UNIFORM fitness, with vals from -" + tau/(3.0*1.4142135) + " .. " + tau/(3.0*1.4142135) + " and tau: " + tau;
	}

}
