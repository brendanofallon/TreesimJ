package siteConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamReader;

import siteModels.GammaFitnesses;
import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

public class GammaFitnessConfigurator implements SiteModelConfigurator {

	JSpinner lengthSpinner;
	JTextField meanField;
	JTextField stdField;
	JPanel panel;
	
	public GammaFitnessConfigurator() {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setOpaque(false);
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		p1.add(new JLabel("Mean of coefficients:"));
		p1.setOpaque(false);
		meanField = new JTextField("0.001");
		meanField.setPreferredSize(new Dimension(100, 25));
		meanField.setMaximumSize(new Dimension(100, 25));
		p1.add(meanField);
		panel.add(p1);
		
		JPanel p2 = new JPanel();
		p2.setOpaque(false);
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("Std. dev. of coefficients: "));
		stdField = new JTextField("0.001");
		stdField.setPreferredSize(new Dimension(100, 25));
		stdField.setMaximumSize(new Dimension(100, 25));
		p2.add(stdField);
		panel.add(p2);
		panel.add(Box.createGlue());
	}
	
	public JComponent getComponent() {
		return panel;
	}

	public String getXMLTypeAttr() {
		return GammaFitnesses.XML_ATTR;
	}
	
	public SiteFitnesses getSiteModel(RandomEngine rng) {
		Double mean;
		Double std;

		try {
			mean = Double.parseDouble(meanField.getText());
			std = Double.parseDouble(stdField.getText());
			return new GammaFitnesses(rng, mean, std);
		} 
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse selection coefficient, returning null model");
			return null;
		}

	}

	
	public String getIdentifier() {
		return "Gamma distributed coeffs.";
	}

	public void configureSettings(XMLStreamReader reader) throws TJXMLException {

		Hashtable<String, String> attrs = XMLParseable.Utils.makeAttributeMap(reader);

		String mean = attrs.get(TJXMLConstants.MEAN);
		String stdev = attrs.get(TJXMLConstants.STDEV);
		if (mean!=null)
			meanField.setText(mean);
		if (stdev!=null)
			stdField.setText(stdev);
	}



}
