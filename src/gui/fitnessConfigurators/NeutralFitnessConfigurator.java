package gui.fitnessConfigurators;

import fitnessProviders.FitnessProvider;
import fitnessProviders.NeutralFitness;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamReader;

import cern.jet.random.engine.RandomEngine;

public class NeutralFitnessConfigurator implements FitnessModelConfigurator {

	public static final String NEUTRAL_FITNESS = "Neutral";

	JPanel mainPanel;

	
	public NeutralFitnessConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setOpaque(false);
		JLabel lab = new JLabel("No options to configure");
		lab.setAlignmentX(Component.CENTER_ALIGNMENT);
		lab.setAlignmentY(Component.CENTER_ALIGNMENT);
		lab.setOpaque(false);
		
		mainPanel.add(lab);
	}

	public JComponent getComponent() {
		return mainPanel;
	}

	public String getXMLTypeAttr() {
		return NeutralFitness.XML_ATTR;
	}

	public FitnessProvider getFitnessModel(RandomEngine rng) {
		return new NeutralFitness();
	}
	
	public String getIdentifier() {
		return NEUTRAL_FITNESS;
	}

	public void configureSettings(XMLStreamReader reader) {
		// TODO Auto-generated method stub
		
	}

	public String getDescription() {
		return "All Individuals have the same fitness in this model, and no DNA or other substrates are provided.";
	}

}
