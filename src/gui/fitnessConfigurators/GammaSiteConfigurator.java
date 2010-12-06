package gui.fitnessConfigurators;

import fitnessProviders.DNAFitness;
import fitnessProviders.FitnessProvider;
import gui.DNAConstructorPane;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamReader;

import mutationModels.MutationModel;
import siteModels.GammaFitnesses;
import cern.jet.random.engine.RandomEngine;
import dnaModels.BitSetDNASequence;
import dnaModels.DNASequence;

public class GammaSiteConfigurator implements FitnessModelConfigurator {

	public static final String GAMMA_FITNESS = "Gamma site";
	
	JSpinner lengthSpinner;
	JTextField meanField;
	JTextField stdField;
	JPanel panel;
	DNAConstructorPane dnaMaker;
	
	public GammaSiteConfigurator() {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		dnaMaker = new DNAConstructorPane();
		
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		lowerPanel.add(new JLabel("Mean :"));
		meanField = new JTextField("0.001");
		lowerPanel.add(meanField);
		lowerPanel.add(new JLabel(" Std: "));
		stdField = new JTextField("0.001");
		lowerPanel.add(stdField);
		panel.add(lowerPanel);
		
		panel.add(dnaMaker);
	}
	
	public JComponent getComponent() {
		return panel;
	}

	public String getXMLTypeAttr() {
		return GammaFitnesses.XML_ATTR;
	}
	
	public FitnessProvider getFitnessModel(RandomEngine rng) {
		Double mean;
		Double std;
		System.out.println("Mean field text : " + meanField.getText() + " std field text : " + stdField.getText());
		try {
			mean = Double.parseDouble(meanField.getText());
			std = Double.parseDouble(stdField.getText());
			MutationModel mutModel = dnaMaker.getMutationModel(rng);
			DNASequence master = new BitSetDNASequence(rng, dnaMaker.getDNALength(), mutModel);
			System.out.println("Making gamma site model with mean : " + mean + " and stdev : " + std);
			FitnessProvider fitness = new DNAFitness(rng, master, new GammaFitnesses(rng, mean, std), mutModel);
			return fitness;
		} 
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse selection coefficient, returning null model");
			return null;
		}
	}
	
	
	public String getXMLBlockString() {
		return GammaFitnesses.XML_ATTR;
	}
	
	public String getIdentifier() {
		return GAMMA_FITNESS;
	}

	public void configureSettings(XMLStreamReader reader) {
		// TODO Auto-generated method stub
		
	}

	public String getDescription() {
		return "";
	}

}
