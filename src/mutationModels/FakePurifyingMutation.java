package mutationModels;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import dnaModels.DNASequence;

/**
 * Experimental mutation model, not currently in use.
 * @author brendan
 *
 */
public class FakePurifyingMutation extends MutationModel {

	double mu;
	double nu;
	double beta;
	double p;
	double depth =0;
	
	Poisson poissonGenerator;
	Uniform uniGenerator;
	
	
	
	public FakePurifyingMutation(RandomEngine rng, double mu, double nu, double beta, double p) {
		super(TJXMLConstants.MUTATION_MODEL);
		this.mu = mu;
		this.nu = nu;
		this.beta = beta;
		this.p = p;
		
		poissonGenerator = new Poisson(mu, rng); //Mean actually gets set in .mutate
		uniGenerator = new Uniform(rng);
	}
	
	public String getDescription() {
		return " Decreasing mutation probability at some sites going back in time (fakepurifyingrate) with mu: " + mu + " nu: " + nu + " beta: " + beta + " p:" + p;
	}

	public double getRateAtDepth(double aDepth) {
		return mu*(1.0-nu*(1.0-Math.exp(-beta*aDepth)));
	}
	
	public void writeXMLBlock(XMLStreamWriter writer) throws XMLStreamException {
		System.err.println("Can't write FakePurifyingMutation to XML yet, sorry");
	}
	
	public void emitRates() {
		System.out.println("Rates at depth : ");
		for(int i=0; i<2000; i+=10) 
			System.out.println(i + "\t" + getRateAtDepth(i));
			
	}
	
	public double getMu() {
		return mu;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}
	
	@Override
	public void setRandomEngine(RandomEngine rng) {
		poissonGenerator = new Poisson(mu, rng); //Mean actually gets set in .mutate
		uniGenerator = new Uniform(rng);
	}
	
	public void mutate(DNASequence seq) {
		int pInt = (int)Math.floor(p*seq.length());
		mutate(seq, 0, pInt, mu);
		
		double satRate = getRateAtDepth(depth);
		mutate(seq, pInt+1, (seq.length()-pInt-1), satRate);
	}
	
	private void mutate(DNASequence seq, int start, int length, double rate) {
		//System.out.println("Mutating from " + start + " to " + (start+length) + " at rate : " + rate);
		double mean = rate*(double)length;
		poissonGenerator.setMean(mean);
		int howmany = poissonGenerator.nextInt();
		for(int i=0; i<howmany; i++) {
			int site = uniGenerator.nextIntFromTo(0, length)+start;
			double r = uniGenerator.nextDouble();
			switch (seq.getBaseChar(site)) {
			case 'A' :  if (r<0.333) seq.setBaseChar(site, 'G');
							else if (r<0.667) seq.setBaseChar(site, 'C');
							else seq.setBaseChar(site, 'T');
						break;
			case 'G' :  if (r<0.333) seq.setBaseChar(site, 'A');
							else if (r<0.667) seq.setBaseChar(site, 'C');
							else seq.setBaseChar(site, 'T');
							break;
			case 'C' :  if (r<0.333) seq.setBaseChar(site, 'G');
							else if (r<0.667) seq.setBaseChar(site, 'A');
							else seq.setBaseChar(site, 'T');
						break;
			case 'T' :  if (r<0.333) seq.setBaseChar(site, 'G');
							else if (r<0.667) seq.setBaseChar(site, 'C');
							else seq.setBaseChar(site, 'A');
						break;
			}
		}//for howmany 
	}

	
	public double mutateUpdateFitness(DNASequence seq, DNASequence master,
			SiteFitnesses siteModel) {
		System.err.println("Can't update fitness with the FakePurifyingRate mutation model. ");
		return 0;
	}
	
	
	public double getPiA() {
		return 0.25;
	}
	
	public double getPiG() {
		return 0.25;
	}
	
	public double getPiC() {
		return 0.25;
	}
	
	public double getPiT() {
		return 0.25;
	}



}
