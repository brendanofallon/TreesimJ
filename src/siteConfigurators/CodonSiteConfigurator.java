package siteConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamReader;

import siteModels.CodonFitnesses;
import siteModels.ConstSiteFitness;
import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

/**
 * Configuration tool for the Codon Site Fitnesses model, which bases selection coefficients on whether
 * or not a mutation changes the amino acid coded for. 
 * @author brendan
 *
 */
public class CodonSiteConfigurator implements SiteModelConfigurator {

	JPanel panel;
	JTextField synField;
	JTextField nonsynField;
	
	public CodonSiteConfigurator() {
		panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel synPanel = new JPanel();
		synPanel.setOpaque(false);
		synPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		synPanel.add(new JLabel("Selection against syn. changes:"));
		synField = new JTextField();
		synField.setText(String.valueOf(0.0));
		synField.setMinimumSize(new Dimension(100, 1));
		synField.setPreferredSize(new Dimension(100, 24));
		synPanel.add(synField);
		panel.add(synPanel);
		
		JPanel nonsynPanel = new JPanel();
		nonsynPanel.setOpaque(false);
		nonsynPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		nonsynPanel.add(new JLabel("Selection against nonsyn. changes:"));
		nonsynField = new JTextField();
		nonsynField.setText(String.valueOf(0.001));
		nonsynField.setMinimumSize(new Dimension(100, 1));
		nonsynField.setPreferredSize(new Dimension(100, 24));
		nonsynPanel.add(nonsynField);
		panel.add(nonsynPanel);
		
//		JPanel stopPanel = new JPanel();
//		stopPanel.setOpaque(false);
//		stopPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		stopPanel.add(new JLabel("Selection against stop codons:"));
//		stopField = new JTextField();
//		stopField.setText(String.valueOf(0.1));
//		stopField.setMinimumSize(new Dimension(100, 1));
//		stopField.setPreferredSize(new Dimension(100, 24));
//		stopPanel.add(stopField);
//		panel.add(stopPanel);
		
		panel.add(Box.createGlue());
	}
	
	public JComponent getComponent() {
		return panel;
	}

	public String getXMLTypeAttr() {
		return CodonFitnesses.XML_ATTR;
	}
	
	public String getIdentifier() {
		return "Codon-based site fitnesses";
	}

	public SiteFitnesses getSiteModel(RandomEngine rng) {
		try {
			double synVal = Double.parseDouble(synField.getText());
			double nonsynVal = Double.parseDouble(nonsynField.getText());
			//double stopVal = Double.parseDouble(stopField.getText());
			return new CodonFitnesses(synVal, nonsynVal);
		}
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse a double value from field" + nfe.getMessage());
			return new CodonFitnesses(0, 0);
		}
	}

	public void configureSettings(XMLStreamReader reader) throws TJXMLException {
		Hashtable<String, String> attrs = XMLParseable.Utils.makeAttributeMap(reader);

		String sVal = attrs.get(CodonFitnesses.XML_SYNSELECTION);
		if (sVal == null) {
			throw new TJXMLException("Codon fitness configurator", "Synonymous selection coefficient not found");
		}
		synField.setText(sVal);
		
		sVal = attrs.get(CodonFitnesses.XML_NONSYNSELECTION);
		if (sVal == null) {
			throw new TJXMLException("Codon fitness configurator", "Nonsynonymous selection coefficient not found");
		}
		nonsynField.setText(sVal);
		
//		sVal = attrs.get(CodonFitnesses.XML_STOPSELECTION);
//		if (sVal == null) {
//			throw new TJXMLException("Codon fitness configurator", "Stop selection coefficient not found");
//		}
//		stopField.setText(sVal);
	}

	
}
