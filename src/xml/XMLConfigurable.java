package xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * An interface that identifies various configurators that can create a model (Site model, DNA model, etc.)
 * from GUI information
 * 
 * @author brendan
 *
 */
public interface XMLConfigurable {

	/**
	 * Read settings from an XML source and set a few local items to match 'em
	 * @param reader
	 * @throws XMLStreamException , TJXMLException
	 */
	public void configureSettings(XMLStreamReader reader) throws TJXMLException, XMLStreamException;
	
	
	/**
	 * This should always return the type attribute of the XML element that the model this creates uses.
	 * For instance, if we're in a SiteModelConfigurator that creates a ConstSiteModel, this should return ConstSiteFitnesses.XML_ATTR
	 * 
	 * @return
	 */
	public String getXMLTypeAttr();
}
