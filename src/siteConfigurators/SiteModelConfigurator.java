package siteConfigurators;

import javax.swing.JComponent;

import siteModels.SiteFitnesses;
import xml.XMLConfigurable;
import cern.jet.random.engine.RandomEngine;


/**
 * Interface for all things which can generate a new site model from a GUI component
 * @author brendan
 *
 */
public interface SiteModelConfigurator extends XMLConfigurable {

	public SiteFitnesses getSiteModel(RandomEngine rng);
	
	public String getIdentifier();
	
	public JComponent getComponent();
}
