package gui.fitnessConfigurators;

import fitnessProviders.FitnessProvider;
import fitnessProviders.QGenFitness;

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

public class QGenConfigurator implements FitnessModelConfigurator {

	JPanel mainPanel;
	JTextField tauField;
	
	public QGenConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
	
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(10));
		JPanel p1 = new JPanel();
		p1.setOpaque(false);
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		p1.add(new JLabel("Standard dev. of fitness change"));
		tauField = new JTextField();
		tauField.setText("0.01");
		tauField.setToolTipText("The standard deviation of the Gaussian random variable added to each fitness each generation");
		p1.add(tauField);
		mainPanel.add(p1);
		mainPanel.add(Box.createGlue());
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	public FitnessProvider getFitnessModel(RandomEngine rng) {
		try {
			Double tau = Double.parseDouble(tauField.getText());
			return new QGenFitness(rng, tau);
		}
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse a double from Stdev. fitness field");
		}
		return null;
	}

	public String getIdentifier() {
		return "Quantitative genetic fitness";
	}

	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		if (reader.isStartElement() && reader.getLocalName()==TJXMLConstants.FITNESS_MODEL) {
			String type = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE);
			if (type.equals( getXMLTypeAttr())) { //We're in the right place
				String tauVal = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.STDEV);
				if (tauVal == null) {
					throw new TJXMLException("QGen fitness model", "Could not find variable tau in input file");
				}
				tauField.setText(tauVal);
			}
		}
	}

	public String getXMLTypeAttr() {
		return QGenFitness.XML_ATTR;
	}

	public String getDescription() {
		return "Fitnesses are described by a single, non-negative real number that describes the expected number of offspring. Each generation a Gaussian random variable with mean 0 is added to each fitness, and then all values are normalized to have mean 1.";
	}

}
