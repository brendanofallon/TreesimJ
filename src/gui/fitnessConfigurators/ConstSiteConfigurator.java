package gui.fitnessConfigurators;

import fitnessProviders.DNAFitness;
import fitnessProviders.FitnessProvider;
import gui.DNAConstructorPane;

import java.awt.FlowLayout;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import mutationModels.MutationModel;
import siteModels.ConstSiteFitness;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;
import dnaModels.BitSetDNASequence;
import dnaModels.DNASequence;

public class ConstSiteConfigurator implements FitnessModelConfigurator {

	public static final String CONST_S_FITNESS = "Const. s";
	
	JTextField sField;
	JPanel panel;
	DNAConstructorPane dnaMaker;
	RandomEngine rng;
	
	public ConstSiteConfigurator() {
		panel = new JPanel();
		this.rng = rng;
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		lowerPanel.add(new JLabel("Selection coefficient: "));
		sField = new JTextField("0.001");
		lowerPanel.add(sField);
		panel.add(lowerPanel);
		
		dnaMaker = new DNAConstructorPane();
		panel.add(dnaMaker);
	}
	
	public JComponent getComponent() {
		return panel;
	}

	public String getXMLTypeAttr() {
		return DNAFitness.XML_ATTR;
	}
	
	public FitnessProvider getFitnessModel(RandomEngine rng) {
		Double s;
		try {
			s = Double.parseDouble(sField.getText());
			MutationModel mutModel = dnaMaker.getMutationModel(rng);
			DNASequence master = new BitSetDNASequence(rng, dnaMaker.getDNALength(), mutModel);
			FitnessProvider fitness = new DNAFitness(rng, master, new ConstSiteFitness(dnaMaker.getDNALength(), s), mutModel);
			return fitness;
		} 
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse selection coefficient, returning null model");
			return null;
		}
	}
	
	public String getXMLBlockString() {
		return ConstSiteFitness.XML_ATTR;
	}
	
	public String getIdentifier() {
		return CONST_S_FITNESS;
	}

	public void configureSettings(XMLStreamReader reader) throws TJXMLException {
		Hashtable<String, String> attrs = XMLParseable.Utils.makeAttributeMap(reader);
		try {
			dnaMaker.configureSettings(reader);
		} catch (XMLStreamException e) {
			System.err.println("Error parsing XML for the DNA construction panel : " + e);
		}
		
		String s = attrs.get(TJXMLConstants.SELECTION);
		if (s==null) {
			System.err.println("Could not parse S from settings for Const. Site model");
		}
		try {
			Double sval = Double.parseDouble(s);
			sField.setText(String.valueOf(sval));
		} 
		catch (Exception ex) {
			System.err.println("Couldn't parse selection coefficient from : " + s);
		}
		
	}

	public String getDescription() {
		return "Fitnesses are described by the state of a DNA sequence where all sites have the same selection coefficient";
	}

}
