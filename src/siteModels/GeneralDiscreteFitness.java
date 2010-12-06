package siteModels;

import dnaModels.DNASequence;
import xml.TJXMLConstants;
import xml.XMLParseable;

/**
 * A site model in which the sequence is broken into a number of discrete classes, each with
 * a (potentially) unique selection coefficient.
 * 
 * The constructor assumes that bounds[] is a list of the *ends* of the ranges, the ranges are
 * assumed to start immediately after the previous end (there's no such thing as between-the-ranges).
 * The fitness effect for each range is given by the fitnesses[] array. It's an error to pass in
 * arrays that are of different lengths to the constructor.
 *  
 * @author brendan
 *
 */
public class GeneralDiscreteFitness extends SiteFitnesses {

	public static final String XML_ATTR = "general.discrete.sitemodel";
	
	double[] effects;
	int[] bounds;
	double[] fitnesses;
	
	
	public GeneralDiscreteFitness(int[] bounds, double[] fitnesses) {
		super(TJXMLConstants.SITE_MODEL);
		this.bounds = bounds;
		this.fitnesses = fitnesses;
		effects = new double[bounds[bounds.length-2]]; //Yup, we ignore the last fitness class - it will fill in for every request beyond the end of the second-to-last class
		int startj = 0;
		int j;
		
		//We actually only keep track of all the classes up to bounds.length-1, and assume that every 
		//site after that has fitness fitnesses[fitness.length-1]
		for(int i=0; i<bounds.length-1; i++) {
			//System.out.println("Setting fitness effects from " + startj + " to " + bounds[i] + " to be : " + fitnesses[i]);
			for(j = startj; j<bounds[i]; j++) {
				effects[j] = fitnesses[i];
			}
			startj = j;
		}
		//System.out.println("Fitnesses from " + bounds[bounds.length-2] + "..inf are fitnesses " + fitnesses[fitnesses.length-1]);
		
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLChild(new SingleRange(SingleRange.XML_ATTR, 0, bounds[0]-1, fitnesses[0]));
		for(int i=1; i<bounds.length; i++) {
			addXMLChild(new SingleRange(SingleRange.XML_ATTR, bounds[i-1], bounds[i]-1, fitnesses[i]));
		}
		
	}
	
	public String getDescription() {
		StringBuilder str = new StringBuilder();
		str.append("General fitness category model with " + bounds.length + " categories\n");
		int start = 0;
		for(int i=0; i<bounds.length; i++) {
			str.append(start + ".." + bounds[i] + " : " + fitnesses[i] + "\n");
			start = bounds[i];
		}
		
		return str.toString();
		
	}

	public double getSiteFitness(int site, DNASequence seq) {
		double val = site<effects.length ? effects[site] : fitnesses[fitnesses.length-1];
		return val;
	}

	public int numSites() {
		return effects.length;
	}
	
	
	/**
	 * A little class to embody a single range of equal selection coefficients, used for XML parsing
	 * @author brendan
	 *
	 */
	public class SingleRange extends XMLParseable {

		public static final String XML_ATTR = "single.range";
		public static final String XML_START = "start.pos";
		public static final String XML_END = "end.pos";
		
		public SingleRange(String blockName, int begin, int end, double s) {
			super(blockName);
			addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
			addXMLAttr(XML_START, String.valueOf(begin));
			addXMLAttr(XML_END, String.valueOf(end));
			addXMLAttr(TJXMLConstants.SELECTION, String.valueOf(s));
		}

		
	}

}
