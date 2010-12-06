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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import population.Population;

import xml.TJXMLException;
import xml.XMLParseable;
import demographicModel.BottleneckGrowth;
import demographicModel.DemographicModel;

public class BottleneckConfigurator implements DemographicConfigurator {

	JPanel mainPanel;
	JSpinner baseSizeSpinner;
	JSpinner bottleneckSizeSpinner;
	JSpinner frequencySpinner;
	JSpinner durationSpinner;
	
	public BottleneckConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(10));
		
		SpinnerNumberModel model = new SpinnerNumberModel(Integer.valueOf(1000), Integer.valueOf(1), Integer.valueOf(1000000), Integer.valueOf(50));
		baseSizeSpinner = new JSpinner();
		baseSizeSpinner.setModel(model);
		baseSizeSpinner.setValue(Integer.valueOf(1000));
		baseSizeSpinner.setPreferredSize(new Dimension(100, 30));
		JPanel p1 = makePanel("Base size:");
		p1.add(baseSizeSpinner);
		mainPanel.add(p1);

		
		model = new SpinnerNumberModel(Integer.valueOf(100), Integer.valueOf(1), Integer.valueOf(1000000), Integer.valueOf(50));
		bottleneckSizeSpinner = new JSpinner();
		bottleneckSizeSpinner.setModel(model);
		bottleneckSizeSpinner.setValue(Integer.valueOf(100));
		bottleneckSizeSpinner.setPreferredSize(new Dimension(100, 30));
		p1 = makePanel("Bottleneck size:");
		p1.add(bottleneckSizeSpinner);
		mainPanel.add(p1);
		
		model = new SpinnerNumberModel(Integer.valueOf(1000000), Integer.valueOf(1), null, Integer.valueOf(50));
		frequencySpinner = new JSpinner();
		frequencySpinner.setModel(model);
		frequencySpinner.setValue(Integer.valueOf(1000));
		frequencySpinner.setPreferredSize(new Dimension(100, 30));
		p1 = makePanel("Bottleneck frequency:");
		p1.add(frequencySpinner);
		mainPanel.add(p1);
		
		model = new SpinnerNumberModel(Integer.valueOf(100), Integer.valueOf(1), null, Integer.valueOf(10));
		durationSpinner = new JSpinner();
		durationSpinner.setModel(model);
		durationSpinner.setValue(Integer.valueOf(100));
		durationSpinner.setPreferredSize(new Dimension(100, 30));
		p1 = makePanel("Bottleneck duration:");
		p1.add(durationSpinner);
		mainPanel.add(p1);
		
		mainPanel.add(Box.createVerticalGlue());
	}
	
	private JPanel makePanel(String labelText) {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.setOpaque(false);
		p.add(new JLabel(labelText));
		
		return p;
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	public DemographicModel getDemographicModel() {
		return new BottleneckGrowth((Integer)baseSizeSpinner.getValue(), (Integer)bottleneckSizeSpinner.getValue(), (Integer)frequencySpinner.getValue(), (Integer)durationSpinner.getValue());
	}

	public String getIdentifier() {
		return "Periodic Bottlenecks";
	}

	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		Hashtable<String, String> attrMap = XMLParseable.Utils.makeAttributeMap(reader);
		
		try {
			String basesizeStr = attrMap.get(BottleneckGrowth.XML_SIZE);
			String bottlesizeStr = attrMap.get(BottleneckGrowth.XML_BSIZE);
			String freqStr = attrMap.get(BottleneckGrowth.XML_FREQUENCY);
			String durationStr = attrMap.get(BottleneckGrowth.XML_DURATION);
			
			Integer basesize = Integer.parseInt(basesizeStr);
			Integer bottlenecksize = Integer.parseInt(bottlesizeStr);
			Integer frequency = Integer.parseInt(freqStr);
			Integer duration = Integer.parseInt(durationStr);
			
			baseSizeSpinner.setValue(basesize);
			bottleneckSizeSpinner.setValue(bottlenecksize);
			frequencySpinner.setValue(frequency);
			durationSpinner.setValue(duration);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Bottleneck demo. model", "Could not parse required values from XML");
		}
		
	}

	public String getXMLTypeAttr() {
		return BottleneckGrowth.XML_ATTR;
	}

	public String getDescription() {
		return "The population size is normally at 'Base Size', but is reduced to 'Bottleneck Size' periodically. The Frequency value determines how often this happens, and the Duration value sets the length of the bottleneck. All values are in generations";
	}

}
