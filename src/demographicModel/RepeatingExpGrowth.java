package demographicModel;

import population.Population;
import xml.TJXMLConstants;


/**
 * Implements a repeating exponential growth period followed by immediate reduction to a base-size, followed by more growth model
 * @author brendan
 *
 */
public class RepeatingExpGrowth extends SimpleDemographicModel {

	public static final String XML_ATTR = "repeating.exp";
	public static final String XML_RATE = "growth.rate"; 
	public static final String XML_BASESIZE = "base.size"; //Population starts growing from this size
	public static final String XML_PERIOD = "period"; 	   //Population is reduced to base size this often
	public static final String XML_DELAY = "delay";		   //No growth happens for this many generations
	public static final String XML_MAXGEN = "max.gen";	   //No growth after this generation
	
	int period;
	int maxGen;
	int delay;
	double growthRate;
	int baseSize;
	
	int lastSize;
	
	public RepeatingExpGrowth(int period, int baseSize, double r, int delay, int max) {
		super(TJXMLConstants.DEMOGRAPHIC_MODEL);
		
		this.period = period;
		this.baseSize = baseSize;
		this.delay = delay;
		this.maxGen = max;
		lastSize = baseSize;
		growthRate = r;
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_RATE, String.valueOf(growthRate));
		addXMLAttr(XML_BASESIZE, String.valueOf(baseSize));
		addXMLAttr(XML_PERIOD, String.valueOf(period));
		addXMLAttr(XML_DELAY, String.valueOf(delay));
		addXMLAttr(XML_MAXGEN, String.valueOf(maxGen));
	}
	
	public String getDescription() {
		return "Repeating exponential growth model with delay: " + delay + " base size: " + baseSize + " period: " + period + " and growth rate: " + growthRate;
	}


	
	public int getN(int t) {
		if (t<=delay) 
			return baseSize;
		
		int size;
		if (t < maxGen) {
			int mod;
			if (period >0)
				mod = (t-delay) % period;
			else 
				mod = t-delay;
			
			size = (int)Math.round(baseSize * Math.exp(mod * growthRate));
			lastSize = size;
		}
		else {
			size = lastSize;
		}
		return size;
	}

}
