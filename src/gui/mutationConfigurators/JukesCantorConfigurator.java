package gui.mutationConfigurators;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import mutationModels.JukesCantorMutation;
import mutationModels.MutationModel;
import mutationModels.MutationModelConfigurator;
import xml.TJXMLException;
import cern.jet.random.engine.RandomEngine;

public class JukesCantorConfigurator implements MutationModelConfigurator {

	public JukesCantorConfigurator() {
		
	}
	
	public JComponent getComponent() {
		JLabel lab = new JLabel("(no options to configure)");
		lab.setAlignmentX(Component.CENTER_ALIGNMENT);
		lab.setAlignmentY(Component.CENTER_ALIGNMENT);
		return lab;
	}

	public String getDescription() {
		return "Jukes-Cantor mutation";
	}

	public String getIdentifier() {
		return "Jukes-Cantor";
	}


	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		//Nothing to do!
	}

	public String getXMLTypeAttr() {
		return JukesCantorMutation.XML_ATTR;
	}

	public MutationModel getMutationModel(RandomEngine rng,
			double mu) {
		return new JukesCantorMutation(rng, mu);
	}


}
