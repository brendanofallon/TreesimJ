package siteModels;

import dnaModels.DNASequence;
import xml.TJXMLConstants;

/**
 * A model of site fitnesses in which all sites are assigned the same selection coefficient s.
 * @author brendan
 *
 */
public class ConstSiteFitness extends SiteFitnesses {

	public static final String XML_ATTR = "const.sitemodel";
	public static final String XML_SELECTION = "selection";
	
	double s = 0;
	int length = 0;
	
	public ConstSiteFitness(double s) {
		super(TJXMLConstants.SITE_MODEL);
		this.s = s;
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SELECTION, String.valueOf(s));
	}
	
	public ConstSiteFitness(int length, double s) {
		super(TJXMLConstants.SITE_MODEL);
		this.length = length;
		this.s = s;
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SELECTION, String.valueOf(s));
	}
	
	public double getSiteFitness(int site, DNASequence seq) {
		return s;
	}

	public int numSites() {
		return length;
	}
	
	public double getS() {
		return s;
	}
	
	public String getDescription() {
		return "All sites have s = " + s;
	}


}
