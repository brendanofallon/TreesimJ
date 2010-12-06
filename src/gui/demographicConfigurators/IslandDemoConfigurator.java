package gui.demographicConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import xml.TJXMLException;
import xml.XMLParseable;
import demographicModel.BottleneckGrowth;
import demographicModel.DemographicModel;
import demographicModel.IslandDemoModel;
import demographicModel.MultiPopCollectible;
import demographicModel.MultiPopCollectible.Strategy;

public class IslandDemoConfigurator implements DemographicConfigurator {

	JPanel mainPanel;
	JSpinner baseSizeSpinner;
	JSpinner numPopsSpinner;
	JTextField migRateField;
	
	JComboBox strategyBox;
	JSpinner singlePopNum;
	String[] strats = {"Random", "Even", "From single pop"};
	
	public IslandDemoConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(10));

		SpinnerNumberModel model = new SpinnerNumberModel(Integer.valueOf(1000), Integer.valueOf(1), Integer.valueOf(1000000), Integer.valueOf(50));
		baseSizeSpinner = new JSpinner();
		baseSizeSpinner.setModel(model);
		baseSizeSpinner.setValue(Integer.valueOf(1000));
		baseSizeSpinner.setPreferredSize(new Dimension(100, 30));
		JPanel p1 = makePanel("Population size:");
		p1.add(baseSizeSpinner);
		p1.setToolTipText("The size of each population");
		mainPanel.add(p1);
		
		model = new SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(1), Integer.valueOf(100), Integer.valueOf(1));
		numPopsSpinner = new JSpinner();
		numPopsSpinner.setModel(model);
		numPopsSpinner.setValue(Integer.valueOf(5));
		numPopsSpinner.setPreferredSize(new Dimension(100, 30));
		JPanel p2 = makePanel("Number of populations:");
		p2.add(numPopsSpinner);
		p2.setToolTipText("The total number of populations in the model");
		mainPanel.add(p2);
		
		migRateField = new JTextField();
		migRateField.setText("0.001");
		migRateField.setPreferredSize(new Dimension(100, 30));
		JPanel p3 = makePanel("Migration rate:");
		p3.setToolTipText("The per-individual, per generation probability of migration");
		p3.add(migRateField);
		mainPanel.add(p3);
		
		
		singlePopNum = new JSpinner();
		singlePopNum.setModel(new SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(100), Integer.valueOf(1)));
		singlePopNum.setPreferredSize(new Dimension(50, 30));
		singlePopNum.setEnabled(false);
		strategyBox = new JComboBox(strats);
		strategyBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (strategyBox.getSelectedIndex()==2) {
					singlePopNum.setEnabled(true);
				}
				else {
					singlePopNum.setEnabled(false);
				}
			}
		});

		JPanel p4 = makePanel("Sampling strategy:");
		p4.setToolTipText("Choose a stratgy for sampling individuals from populations");
		p4.add(strategyBox);
		p4.add(new JLabel("Pop. num:"));
		p4.add(singlePopNum);
		mainPanel.add(p4);
		
		mainPanel.add(Box.createVerticalGlue());
	}
	
	
	private JPanel makePanel(String labelText) {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.setOpaque(false);
		p.add(new JLabel(labelText));
		
		return p;
	}
	
	
	public void configureSettings(XMLStreamReader reader) throws TJXMLException, XMLStreamException {
		Hashtable<String, String> attrMap = XMLParseable.Utils.makeAttributeMap(reader);

		Integer basesize = 1000;
		Integer numPops = 5;
		Integer sPopNum = 1;
		
		String popSizeStr = attrMap.get(IslandDemoModel.XML_SIZE);
		String numPopsStr = attrMap.get(IslandDemoModel.XML_NUMPOPS);
		String migRateStr = attrMap.get(IslandDemoModel.XML_MIGRATION);
		try {
			basesize = Integer.parseInt(popSizeStr);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Island demo. model", "Could not parse population size from XML");
		}
		try{
			numPops = Integer.parseInt(numPopsStr);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Island demo. model", "Could not parse number of populations from XML");
		}
		
		String sampleStrat = attrMap.get(IslandDemoModel.XML_SAMPLESTRATEGY);
		try {
			int index = indexForStrategy(sampleStrat);
			strategyBox.setSelectedIndex(index);
			
		}
		catch (Exception ex) {
			System.err.println("Could not set sample strategy to : " + sampleStrat);
		}

		try{
			String singlePopStr = attrMap.get(IslandDemoModel.XML_SINGLEPOP);
			if (singlePopStr != null)
				sPopNum = Integer.parseInt( singlePopStr)+1;
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Island demo. model", "Could not parse sample population number from XML");
		}
		
		
		
		baseSizeSpinner.setValue(basesize);
		numPopsSpinner.setValue(numPops);
		singlePopNum.setValue(sPopNum);
		migRateField.setText(migRateStr);
	}

	/**
	 * Return the index of the strategy in the strats array whose name is given as an argument . 
	 * @param singlePopStr
	 * @return
	 */
	private int indexForStrategy(String singlePopStr) {
		if (singlePopStr.equalsIgnoreCase(Strategy.RANDOM.toString()))
			return 0;
		if (singlePopStr.equalsIgnoreCase(Strategy.EVEN.toString()))
			return 1;
		if (singlePopStr.equalsIgnoreCase(Strategy.SINGLE.toString()))
			return 2;
		
		
		return 0;
	}


	
	public String getXMLTypeAttr() {
		return IslandDemoModel.XML_ATTR;
	}

	
	public JComponent getComponent() {
		return mainPanel;
	}

	
	public DemographicModel getDemographicModel() {
		int size = (Integer)baseSizeSpinner.getValue();
		int numPops = (Integer)numPopsSpinner.getValue();
		
		double mig = Double.parseDouble( migRateField.getText() );
		
		IslandDemoModel model = new IslandDemoModel(numPops, size, mig);
		Strategy strat = getCurrentStrategy();
		int popNum = (Integer)singlePopNum.getValue()-1; //The user indexes from 1
		model.setSamplingStrategy(strat, popNum);
		return model;
	}

	private Strategy getCurrentStrategy() {
		if (strategyBox.getSelectedIndex()==0)
			return MultiPopCollectible.Strategy.RANDOM;
		if (strategyBox.getSelectedIndex()==1)
			return MultiPopCollectible.Strategy.EVEN;
		if (strategyBox.getSelectedIndex()==2)
			return MultiPopCollectible.Strategy.SINGLE;
			
		throw new IllegalStateException("Could not obtain the sampling strategy, returning null");
	}


	
	public String getIdentifier() {
		return "Island model";
	}

	
	public String getDescription() {
		return "A model of multiple populations of equal, constant size, all equally linked by migration";
	}

}
