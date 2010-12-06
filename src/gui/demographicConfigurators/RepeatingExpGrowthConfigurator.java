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
import demographicModel.RepeatingExpGrowth;

public class RepeatingExpGrowthConfigurator implements DemographicConfigurator {

	
	public RepeatingExpGrowthConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(10));

		delayField = new JTextField();
		delayField.setText(String.valueOf(5000));
		delayField.setPreferredSize(new Dimension(100, 30));
		delayField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p0 = makePanel("Growth begin:");
		delayField.setToolTipText("No growth will occur prior to this generation");
		p0.add(delayField);
		mainPanel.add(p0);

		
		maxGenField = new JTextField();
		maxGenField.setText(String.valueOf(10000));
		maxGenField.setPreferredSize(new Dimension(100, 30));
		maxGenField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p05 = makePanel("Growth end:");
		maxGenField.setToolTipText("No growth will occur after this generation");
		p05.add(maxGenField);
		mainPanel.add(p05);
		
		baseSizeField = new JTextField();
		baseSizeField.setText(String.valueOf(500));
		baseSizeField.setPreferredSize(new Dimension(100, 30));
		baseSizeField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p1 = makePanel("Base size:");
		baseSizeField.setToolTipText("The initial size of the population");
		p1.add(baseSizeField);
		mainPanel.add(p1);
		
		growthRateField = new JTextField();
		growthRateField.setText(String.valueOf(0.001));
		growthRateField.setPreferredSize(new Dimension(100, 30));
		growthRateField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p2 = makePanel("Growth rate:");
		p2.add(growthRateField);
		growthRateField.setToolTipText("Exponential growth rate of the population");
		mainPanel.add(p2);
		
		periodField = new JTextField();
		periodField.setText(String.valueOf(2000));
		periodField.setPreferredSize(new Dimension(100, 30));
		periodField.setHorizontalAlignment(JTextField.RIGHT);
		JPanel p3 = makePanel("Reduction frequency:");
		periodField.setToolTipText("Population is reduced to base size at this frequency");
		p3.add(periodField);
		mainPanel.add(p3);		
		
		mainPanel.add(Box.createVerticalGlue());
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	private JPanel makePanel(String labelText) {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.setOpaque(false);
		p.add(new JLabel(labelText));
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		return p;
	}
	
	public DemographicModel getDemographicModel() {	
		Integer baseSize =  (int)Math.round(parseNumber(baseSizeField));
		Integer period = (int)Math.round(parseNumber(periodField));
		Integer delay = (int)Math.round(parseNumber(delayField));
		Integer max = (int)Math.round(parseNumber(maxGenField));
		Double growthRate = parseNumber(growthRateField);
		return new RepeatingExpGrowth(period, baseSize, growthRate, delay, max);
	}

	private Double parseNumber(JTextField field) {
		try {
			Double val = Double.parseDouble(field.getText());
			return val;
		}
		catch (NumberFormatException nfe) {
			System.err.println("Could not parse a value from field : " + field.getText());
			return null;
		}
	}
	
	public String getDescription() {
		return "The population grows exponentially at the given rate between generations begin .. end. Optionally the population may be periodically reduced to the base size during the growth phase.";
	}

	
	public String getIdentifier() {
		return "Repeating Exp. Growth";
	}


	public void configureSettings(XMLStreamReader reader)
	throws TJXMLException, XMLStreamException {

		Hashtable<String, String> attrMap = XMLParseable.Utils.makeAttributeMap(reader);


		String basesizeStr = attrMap.get(RepeatingExpGrowth.XML_BASESIZE);
		String periodStr = attrMap.get(RepeatingExpGrowth.XML_PERIOD);
		String growthRateStr = attrMap.get(RepeatingExpGrowth.XML_RATE);
		String delayStr = attrMap.get(RepeatingExpGrowth.XML_DELAY);
		String maxStr = attrMap.get(RepeatingExpGrowth.XML_MAXGEN);
		
		//Older TJ versions may not have this field... so watch out for null
		if (delayStr != null)
			delayField.setText(delayStr);
		else 
			delayField.setText("0");
		
		if (maxStr != null)
			maxGenField.setText(maxStr);
		else 
			maxGenField.setText("10000");
		
		
		baseSizeField.setText(basesizeStr);
		periodField.setText(periodStr);
		growthRateField.setText(growthRateStr);

	}

	
	public String getXMLTypeAttr() {
		return RepeatingExpGrowth.XML_ATTR;
	}

	
	JPanel mainPanel;
	JTextField delayField;
	JTextField maxGenField;
	JTextField baseSizeField;
	JTextField periodField;
	JTextField growthRateField;
}
