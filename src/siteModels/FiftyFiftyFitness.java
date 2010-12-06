package siteModels;

import dnaModels.DNASequence;
import xml.TJXMLConstants;


public class FiftyFiftyFitness extends SiteFitnesses {

	public static final String XML_ATTR = "fifty.fifty.sitemodel";
	public static final String XML_LENGTH = "total.length";
	public static final String XML_SELECTION = "selection";
	
	int length;
	double s;
	int selectedSites;
	
	public FiftyFiftyFitness(int length, double s) {
		super(TJXMLConstants.SITE_MODEL);
		this.s = s;
		this.length = length;
		selectedSites = length/2;
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_LENGTH, String.valueOf(length));
		addXMLAttr(XML_SELECTION, String.valueOf(s));
	}
	
	public String getDescription() {
		return "Half (" + (length-selectedSites) + ") neutral, half (" + (selectedSites) + ")selected sites with s = " + s;
	}

	public double getSiteFitness(int site, DNASequence seq) {
		if (site<selectedSites)
			return s;
		else 
			return 0;
	}

	public int numSites() {
		return length;
	}


}
