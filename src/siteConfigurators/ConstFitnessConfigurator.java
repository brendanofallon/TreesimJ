package siteConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamReader;

import siteModels.ConstSiteFitness;
import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

public class ConstFitnessConfigurator implements SiteModelConfigurator {

	JPanel panel;
	JTextField sField;
	
	public ConstFitnessConfigurator() {
		panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Selection coefficient:"));
		sField = new JTextField();
		sField.setText(String.valueOf(0.001));
		sField.setMinimumSize(new Dimension(100, 1));
		sField.setPreferredSize(new Dimension(100, 24));
		panel.add(sField);
	}
	
	public JComponent getComponent() {
		return panel;
	}

	public String getXMLTypeAttr() {
		return ConstSiteFitness.XML_ATTR;
	}
	
	public String getIdentifier() {
		return "Constant site fitnesses";
	}

	public SiteFitnesses getSiteModel(RandomEngine rng) {
		try {
			double sVal = Double.parseDouble(sField.getText());
			return new ConstSiteFitness(sVal);
		}
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse a double value from " + sField.getText());
			return new ConstSiteFitness(0);
		}
	}

	public void configureSettings(XMLStreamReader reader) throws TJXMLException {
		Hashtable<String, String> attrs = XMLParseable.Utils.makeAttributeMap(reader);

		String sVal = attrs.get(TJXMLConstants.SELECTION);
		if (sVal == null) {
			throw new TJXMLException("ConstFitness configurator", "Selection coefficient not found");
		}
		sField.setText(sVal);
	}

}
