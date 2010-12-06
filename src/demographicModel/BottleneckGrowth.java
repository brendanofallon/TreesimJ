package demographicModel;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import population.Population;

import xml.TJXMLConstants;


/**
 * A model of population size in which the population generally remains at 'baseSize', but periodically is 
 * changed to another size (bottleneck size) for a certain number of generations.
 * 
 * @author brendan
 *
 */
public class BottleneckGrowth extends SimpleDemographicModel {

	public static final String XML_ATTR = "bottleneck";
	public static final String XML_SIZE = "base.size";
	public static final String XML_BSIZE = "bottleneck.size";
	public static final String XML_FREQUENCY = "frequency";
	public static final String XML_DURATION = "duration";
	
	int baseSize;
	int bottleneckSize;
	int frequency;
	int duration;
	
	public BottleneckGrowth(Integer baseSize, Integer bottleneckSize, Integer bottleneckFrequency, Integer bottleneckDuration) {
		super(TJXMLConstants.DEMOGRAPHIC_MODEL);
		this.baseSize = baseSize;
		this.bottleneckSize = bottleneckSize;
		this.frequency = bottleneckFrequency;
		this.duration = bottleneckDuration;
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SIZE, String.valueOf(baseSize));
		addXMLAttr(XML_BSIZE, String.valueOf(bottleneckSize));
		addXMLAttr(XML_FREQUENCY, String.valueOf(bottleneckFrequency));
		addXMLAttr(XML_DURATION, String.valueOf(bottleneckDuration));
	}

	public String getDescription() {
		return "Bottleneck demographic model. Base size : " + baseSize + " bottleneck size: " + bottleneckSize + " B.neck frequency: " + frequency + " B.neck duration: " + duration;
	}


	public int getN(int t) {
		if (t%frequency < duration) {
			return bottleneckSize;
		}
		else 
			return baseSize;
	}


	public Object readXMLBlock(XMLStreamReader reader)
			throws XMLStreamException {
		//No longer in use
		return null;
	}

}
