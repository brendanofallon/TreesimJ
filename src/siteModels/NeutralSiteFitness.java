package siteModels;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import dnaModels.DNASequence;

import xml.TJXMLConstants;

/**
 * A neutral model of sequence evolution, all sites have no effect on fitness when mutated. 
 * @author brendan
 *
 */
public class NeutralSiteFitness extends SiteFitnesses {

	public static final String XML_ATTR = "Neutral.sites";
	
	public NeutralSiteFitness() {
		super(TJXMLConstants.SITE_MODEL);
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
	}


	public String getDescription() {
		return "Neutral fitness";
	}

	public double getSiteFitness(int site, DNASequence seq) {
		return 0.0;
	}

	public int numSites() {
		return 0;
	}

	public Object readXMLBlock(XMLStreamReader reader)
			throws XMLStreamException {
		//No config necessary
		return null;
	}

}
