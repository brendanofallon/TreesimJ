package fitnessProviders;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import cern.jet.random.engine.RandomEngine;

import xml.TJXMLConstants;


public class NeutralFitness extends FitnessProvider {

	public static final String XML_ATTR = "Neutral.fitness";
	
	public NeutralFitness() { 
		super(TJXMLConstants.FITNESS_MODEL);
		addXMLAttributes();
	}
	
	public void addXMLAttributes() {
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
	}
	
	/**
	 * Don't bother with XML writing stuff for this constructor
	 * @param dummy
	 */
	private NeutralFitness(boolean dummy) { 
		super(TJXMLConstants.FITNESS_MODEL);
	}
	
	@Override
	public void setRandomEngine(RandomEngine rng) {
		//This is a no-op, we don't ever generate random numbers
	}
	
	public String getDescription() {
		return "Neutral fitness";
	}
	
	public Object getSubstrate() {
		return new Double(1.0);
	}

	public Double getDoubleValue() {
		return 1.0;
	}

	public double getFitness() {
		return 1.0;
	}

	public String getStringValue() {
		return null;
	}

	public void mutate() {
	}
	
	public void setFitness(double w) {

	}
	
	public NeutralFitness getCopy() {
		return new NeutralFitness(false);
	}

	
	public Object readXMLBlock(XMLStreamReader reader)
			throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

}
