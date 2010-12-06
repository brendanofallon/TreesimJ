package demographicModel;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import population.Population;

import xml.TJXMLConstants;

/**
 * The population growth linearly with slope 'slope' from an initial size, and size
 * is reset to initialSize every 'period' generations. 
 * @author brendan
 *
 */
public class LinearGrowth extends SimpleDemographicModel {

	public static final String XML_ATTR = "linear.growth";
	public static final String XML_SLOPE = "slope";
	public static final String XML_INITSIZE = "initial.size";
	public static final String XML_PERIOD = "period";
	
	int initN;
	int period;
	double slope;
	
	public LinearGrowth(int initialSize, double slope, int period) {
		super(TJXMLConstants.DEMOGRAPHIC_MODEL);
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SLOPE, String.valueOf(slope));
		addXMLAttr(XML_INITSIZE, String.valueOf(initialSize));
		addXMLAttr(XML_PERIOD, String.valueOf(period));
		initN = initialSize;
		this.slope = slope;
		this.period = period;
	}
	
	public String getDescription() {
		return "Linear growth rate starting at " + initN + " with slope " + slope + " and period " + period;
	}

	public int getN(int t) {
		int mod = t % period;
		int size = initN + (int)Math.round((double)mod*slope); 
		if (size < 1)
			return 1;
		else 
			return size;
	}


}
