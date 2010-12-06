package demographicModel;

import population.Population;
import xml.TJXMLConstants;

/**
 * A demographic model that describes a population whose size never changes. 
 * @author brendan
 *
 */
public class ConstantPopulationSize extends SimpleDemographicModel {

	public static final String XML_ATTR = "constant.size";
	public static final String XML_SIZE = "size";
	
	int size;
	
	public ConstantPopulationSize(int size) {
		super(TJXMLConstants.DEMOGRAPHIC_MODEL);
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_SIZE, String.valueOf(size));
		this.size = size;
	}
	
	public String getDescription() {
		return "Constant population size, N = " + size;
	}

	public int getN(int t) {
		return size;
	}

	
}
