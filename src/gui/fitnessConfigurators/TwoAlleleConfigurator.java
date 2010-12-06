package gui.fitnessConfigurators;

import fitnessProviders.FitnessProvider;
import fitnessProviders.TwoAlleleFitness;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Hashtable;

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
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

/**
 * The fitness configurator for the TwoAllele fitness model. Fitness is described by a single locus that may be 
 * in one of two states. 
 * @author brendan
 *
 */
public class TwoAlleleConfigurator implements FitnessModelConfigurator {

	JPanel mainPanel;
	JTextField sField;
	JTextField forwardMuField;
	JTextField backMuField;
	
	RandomEngine rng;
	
	
	public TwoAlleleConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		sField = new JTextField();
		forwardMuField = new JTextField();
		backMuField = new JTextField();
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add( addLabelledField("Selection coefficient   :", sField, "0.01"));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add( addLabelledField("Forward mutation rate  :", forwardMuField, "0.001"));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add( addLabelledField("Backward mutation rate :", backMuField, "0.001"));
		mainPanel.add(Box.createGlue());
		mainPanel.setOpaque(false);
	}
	
	private JPanel addLabelledField(String label, JTextField field, String initStr) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel(label));
		field.setText(initStr);
		field.setPreferredSize(new Dimension(100, 30));
		field.setMinimumSize(new Dimension(75, 1));
		field.setHorizontalAlignment(JTextField.RIGHT);
		panel.add(field);
		
		return panel;
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	public FitnessProvider getFitnessModel(RandomEngine rng) {
		try {
			String sStr = sField.getText();
			Double sVal = Double.parseDouble(sStr);
			Double forwardMu = Double.parseDouble(forwardMuField.getText());
			Double backwardMu = Double.parseDouble(backMuField.getText());
			Uniform uniGen = new Uniform(rng);
			return new TwoAlleleFitness(uniGen, true, sVal, forwardMu, backwardMu);
		}
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse appropriate values from TwoAlleleFitness");
			return null;
		}

	}

	public String getIdentifier() {
		return "Two Alleles";
	}

	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		if (reader.isStartElement() && reader.getLocalName()==TJXMLConstants.FITNESS_MODEL) {
			String type = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE);
			if (type.equals(TwoAlleleFitness.XML_ATTR)) {
				Hashtable<String, String> attrMap = XMLParseable.Utils.makeAttributeMap(reader);
				String sStr = attrMap.get(TJXMLConstants.SELECTION);
				String fMu = attrMap.get(TJXMLConstants.FORWARD_MUTATION);
				String backMu = attrMap.get(TJXMLConstants.BACK_MUTATION);
				
				if (sStr != null)
					sField.setText(sStr);
				if( fMu != null) {
					forwardMuField.setText(fMu);
				}
				if (backMu != null) {
					backMuField.setText(backMu);
				}
			}
		}
		
	}

	public String getXMLTypeAttr() {
		return TwoAlleleFitness.XML_ATTR;
	}

	public String getDescription() {
		return "A one-locus, two allele model of fitness. One allele has an absolute fitness of 1.0, the other has fitness 1-s, where s is specified below. The 'Forward mutation rate' describes the mutation rate from the allele with fitness 1 to the other allele";
	}

}
