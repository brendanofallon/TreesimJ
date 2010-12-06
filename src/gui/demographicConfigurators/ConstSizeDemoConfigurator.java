package gui.demographicConfigurators;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import population.Population;

import xml.TJXMLException;
import xml.XMLParseable;
import demographicModel.ConstantPopulationSize;
import demographicModel.DemographicModel;

/**
 * Constructs the panel which allows the user to pick the options for the Constant Population Size demographic model,
 * which has only one option: you guessed it.
 *  
 * @author brendan
 *
 */
public class ConstSizeDemoConfigurator implements DemographicConfigurator {

	public static final String identifier = "Constant Population Size";
	
	JPanel panel;
	JSpinner sizeSpinner;
	
	public ConstSizeDemoConfigurator() {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalStrut(20));
		
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		p1.setOpaque(false);
		p1.add(new JLabel("Population size:"));
		SpinnerNumberModel model = new SpinnerNumberModel(Integer.valueOf(1000000), Integer.valueOf(0), null, Integer.valueOf(1));
		sizeSpinner = new JSpinner();
		sizeSpinner.setModel(model);
		sizeSpinner.setValue(Integer.valueOf(1000));
		sizeSpinner.setPreferredSize(new Dimension(100, 30));
		p1.add(sizeSpinner);
		panel.add(p1);
		panel.add(Box.createGlue());
	}
	
	
	public JComponent getComponent() {
		return panel;
	}


	public DemographicModel getDemographicModel() {
		return new ConstantPopulationSize((Integer)sizeSpinner.getValue());
	}

	
	public String getIdentifier() {
		return identifier;
	}


	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		String sizeStr = XMLParseable.Utils.getAttributeForKey(reader, ConstantPopulationSize.XML_SIZE);
		
		try {
			Integer size = Integer.parseInt(sizeStr);
			sizeSpinner.setValue(size);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Constant size demo. model", "Could not parse population size from " + sizeStr);
		}
		
	}


	public String getXMLTypeAttr() {
		return ConstantPopulationSize.XML_ATTR;
	}


	public String getDescription() {
		return "The population remains at a constant size on every generation";
	}

}
