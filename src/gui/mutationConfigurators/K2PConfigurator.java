package gui.mutationConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import mutationModels.K2PMutation;
import mutationModels.MutationModel;
import mutationModels.MutationModelConfigurator;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

public class K2PConfigurator implements MutationModelConfigurator {
	
	JPanel mainPanel;
	JTextField ttField;
	
	public K2PConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(new JLabel("Ts/Tv ratio:"));
		
		ttField = new JTextField("2.0");
		ttField.setPreferredSize(new Dimension(50, 24));
		ttField.setMinimumSize(new Dimension(50, 1));
		mainPanel.add(ttField);
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}


	public String getDescription() {
		return "Kimura 2-parameter mutation";
	}

	public String getIdentifier() {
		return "Kimura 2 Parameter";
	}


	public MutationModel getMutationModel(RandomEngine rng, double mu) {
		Double ttRatio = 2.0;
		try {
			ttRatio = Double.parseDouble(ttField.getText());
		}
		catch (NumberFormatException nfe) {
			System.err.println("Error : Could not parse Ts/Tv ratio from field with : " + ttField.getText());
		}
		return new K2PMutation(rng, mu, ttRatio);
	}


	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		String ttRatio = XMLParseable.Utils.getAttributeForKey(reader, K2PMutation.XML_TTRATIO);
		
		if (ttRatio != null) {
			ttField.setText(ttRatio);
		}
	}

	public String getXMLTypeAttr() {
		return K2PMutation.XML_ATTR;
	}

}
