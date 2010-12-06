package gui.demographicConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import population.Population;

import xml.TJXMLException;
import xml.XMLParseable;
import demographicModel.DemographicModel;
import demographicModel.LinearGrowth;

/**
 * Configurator for the linear growth demographic model, in which population
 * size changes linearly with time
 * @author brendan
 *
 */
public class LinearGrowthConfigurator implements DemographicConfigurator {

	JPanel mainPanel;
	private JTextField periodField;
	
	public LinearGrowthConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(20));
		
		baseSizeField = new JTextField();
		baseSizeField.setText(String.valueOf(500));
		baseSizeField.setPreferredSize(new Dimension(100, 30));
		baseSizeField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p1 = makePanel("Base size:");
		p1.add(baseSizeField);
		mainPanel.add(p1);
		
		growthRateField = new JTextField();
		growthRateField.setText(String.valueOf(1.1));
		growthRateField.setPreferredSize(new Dimension(100, 30));
		growthRateField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p2 = makePanel("Growth slope:");
		p2.add(growthRateField);
		mainPanel.add(p2);
		
		periodField = new JTextField();
		periodField.setText(String.valueOf(2000));
		periodField.setPreferredSize(new Dimension(100, 30));
		periodField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p3 = makePanel("Reduction frequency:");
		p3.add(periodField);
		mainPanel.add(p3);	
		
		mainPanel.add(Box.createGlue());
		
	}
	
	private JPanel makePanel(String labelText) {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.setOpaque(false);
		p.add(new JLabel(labelText));
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		return p;
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}


	public DemographicModel getDemographicModel() {
		try {
			Integer initSize = Integer.parseInt(baseSizeField.getText());
			Integer period = Integer.parseInt(periodField.getText());
			Double slope = Double.parseDouble(growthRateField.getText());
			return new LinearGrowth( initSize, slope, period);
		}
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse values for linear growth model");
			return null;
		}
		
	}


	public String getDescription() {
		return "Population size changes linearly with time";
	}


	public String getIdentifier() {
		return "Linear growth";
	}


	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		Hashtable<String, String> attrMap = XMLParseable.Utils.makeAttributeMap(reader);

		String basesizeStr = attrMap.get(LinearGrowth.XML_INITSIZE);
		String growthSlopeStr = attrMap.get(LinearGrowth.XML_SLOPE);
		String periodStr = attrMap.get(LinearGrowth.XML_PERIOD);

		baseSizeField.setText(basesizeStr);
		growthRateField.setText(growthSlopeStr);
		periodField.setText(periodStr);
	}


	public String getXMLTypeAttr() {
		return LinearGrowth.XML_ATTR;
	}

	
	JTextField baseSizeField;
	JTextField growthRateField;
}
