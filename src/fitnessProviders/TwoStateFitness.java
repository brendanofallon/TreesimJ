package fitnessProviders;

import java.util.BitSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import siteModels.ConstSiteFitness;
import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;


/**
 * A 'bit string' fitness model where all sites can exist in one of only two states. 
 * @author brendan
 *
 */
public class TwoStateFitness extends FitnessProvider {

	public static final String XML_ATTR = "twostate.fitness";

	
	BitSet bits;
	double mu;
	int length;
	SiteFitnesses siteModel;
	Poisson poisGen;
	Uniform uniGen;
	int muts;
	double sumEffects;
	double s = Double.NEGATIVE_INFINITY;
	
	public TwoStateFitness(RandomEngine rng, int theLength, SiteFitnesses sMod, double mutationRate) {
		super(TJXMLConstants.FITNESS_MODEL);
		this.length = theLength;
		this.siteModel = sMod;
		this.mu = mutationRate;
		this.length = theLength;
		bits = new BitSet(length);
		poisGen = new Poisson(length*mutationRate, rng);
		uniGen = new Uniform(rng);
		muts = 0;
		sumEffects = 0;
		addXMLAttributes();
	}
	
	public void addXMLAttributes() {
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
	}
	
	public TwoStateFitness(RandomEngine rng, int theLength, double s, double mutationRate) {
		super(TJXMLConstants.FITNESS_MODEL);
		this.length = theLength;
		this.siteModel = new ConstSiteFitness(theLength, s);
		this.mu = mutationRate;
		this.length = theLength;
		bits = new BitSet(length);
		poisGen = new Poisson(length*mutationRate, rng);
		uniGen = new Uniform(rng);
		muts = 0;
		sumEffects = 0;
		this.s = s;
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
	}

	private TwoStateFitness(Poisson poiGenerator, Uniform uniGenerator, BitSet newbits, int theLength, SiteFitnesses sMod, double mutationRate, double sumEffects, double s) {
		super(TJXMLConstants.FITNESS_MODEL);
		this.bits = (BitSet)newbits.clone();
		this.siteModel = sMod;
		this.mu = mutationRate;
		this.length = theLength;
		this.poisGen = poiGenerator;
		this.uniGen = uniGenerator;
		muts = bits.cardinality();
		this.sumEffects = sumEffects;
		this.s = s;
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
	}
	
	public void writeXMLBlock(XMLStreamWriter writer) throws XMLStreamException {
		System.err.println("XML writing not implemented yet for TwoStateFitness model");
	}
	
	public FitnessProvider getCopy() {
		TwoStateFitness copy = new TwoStateFitness(poisGen, uniGen, bits, length, siteModel, mu, sumEffects, s);
		return copy;
	}

	
	public String getDescription() {
		return "Two-state fitness model : \n \t Site mode : " + siteModel.getDescription() + "\n \t Mutation rate : " + mu;
	}
	
	public int getMuts() {
		return muts;
	}
	
	public SiteFitnesses getSiteModel() {
		return siteModel;
	}
	
	public double getMu() {
		return mu;
	}
	
	public int getLength() {
		return length;
	}

	public Double getDoubleValue() {
		return (double)(muts);
	}

	public double getFitness() {
		//double val =  Math.exp(-sumEffects);
		double val = Math.pow(1.0-sumEffects, muts);
		//System.out.println("val : " + val);
		if (val < 0 || val > 1.0) {
			System.err.println("Bad val...s: " + s + " muts : " + muts);
			System.exit(0);
		}
		return val;
	}


	public String getStringValue() {
		return null;
	}


	public Object getSubstrate() {
		return bits;
	}


	public void mutate() {
		int howmany = poisGen.nextInt();
		for(int i=0; i<howmany; i++) {
			int which = uniGen.nextIntFromTo(0, length-1);
			if (bits.get(which)) {
				muts--;
				sumEffects -= siteModel.getSiteFitness(which, null);
			}
			else {
				muts++;
				sumEffects += siteModel.getSiteFitness(which, null);
			}
			bits.flip(which);
		}
	}

	public void setFitness(double w) {		
	}

	@Override
	public void setRandomEngine(RandomEngine rng) {
		poisGen = new Poisson(length*mu, rng);
		uniGen = new Uniform(rng);
	}

}
