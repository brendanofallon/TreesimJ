package siteConfigurators;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamReader;

import siteModels.NeutralSiteFitness;
import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import xml.TJXMLException;
import cern.jet.random.engine.RandomEngine;

/**
 * Site model configurator for the NeutralSiteFitness site model, in which all sites have zero effect on fitness. 
 * @author brendan
 *
 */
public class NeutralSiteConfigurator implements SiteModelConfigurator {

	JPanel mainPanel;
	
	public NeutralSiteConfigurator() {
		mainPanel = new JPanel();
		JLabel lab = new JLabel("(No options to configure)");
		lab.setOpaque(false);
		mainPanel.add(lab);
		mainPanel.setOpaque(false);
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	public String getIdentifier() {
		return "Neutral fitness";
	}

	public SiteFitnesses getSiteModel(RandomEngine rng) {
		return new NeutralSiteFitness();
	}
	
	public String getXMLTypeAttr() {
		return NeutralSiteFitness.XML_ATTR;
	}

	public void configureSettings(XMLStreamReader reader) throws TJXMLException {
		if (reader.isStartElement() && reader.getLocalName()==TJXMLConstants.SITE_MODEL) {
			//Nothing to do
		}
		else {
			throw new TJXMLException("Neutral site config", "Invalid start block");
		}
	}

}
