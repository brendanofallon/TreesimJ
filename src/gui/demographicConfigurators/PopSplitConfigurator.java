package gui.demographicConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import demographicModel.DemographicModel;
import demographicModel.IslandDemoModel;
import demographicModel.MultiPopCollectible;
import demographicModel.MultiPopCollectible.Strategy;
import demographicModel.PopSplitDemoModel;
import errorHandling.ErrorWindow;

import xml.TJXMLException;
import xml.XMLParseable;

public class PopSplitConfigurator implements DemographicConfigurator {

	JPanel mainPanel;
	JSpinner ancSizeSpinner;
	JSpinner pop1SizeSpinner;
	JSpinner pop2SizeSpinner;
	JSpinner splitTimeSpinner;
	JTextField m21Field;
	JTextField m12Field;
	
	JComboBox strategyBox;
	JSpinner singlePopNum;
	String[] strats = {"Random", "Even", "From first population"};
	
	public PopSplitConfigurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(10));

		SpinnerNumberModel model = new SpinnerNumberModel(Integer.valueOf(1000), Integer.valueOf(1), Integer.valueOf(1000000), Integer.valueOf(50));
		ancSizeSpinner = new JSpinner();
		ancSizeSpinner.setModel(model);
		ancSizeSpinner.setValue(Integer.valueOf(1000));
		ancSizeSpinner.setPreferredSize(new Dimension(100, 30));
		JPanel p1 = makePanel("Ancestral Pop. size:");
		p1.add(ancSizeSpinner);
		p1.setToolTipText("The size of the ancestral population");
		mainPanel.add(p1);
		
		model = new SpinnerNumberModel(Integer.valueOf(500), Integer.valueOf(1), Integer.valueOf(100000), Integer.valueOf(50));
		pop1SizeSpinner = new JSpinner();
		pop1SizeSpinner.setModel(model);
		pop1SizeSpinner.setValue(Integer.valueOf(1000));
		pop1SizeSpinner.setPreferredSize(new Dimension(100, 30));
		JPanel p2 = makePanel("Pop 1 size:");
		p2.add(pop1SizeSpinner);
		p2.setToolTipText("The size of the first descendant population");
		mainPanel.add(p2);
		
		model = new SpinnerNumberModel(Integer.valueOf(500), Integer.valueOf(1), Integer.valueOf(1000000), Integer.valueOf(50));
		pop2SizeSpinner = new JSpinner();
		pop2SizeSpinner.setModel(model);
		pop2SizeSpinner.setValue(Integer.valueOf(1000));
		pop2SizeSpinner.setPreferredSize(new Dimension(100, 30));
		JPanel p3 = makePanel("Pop 2 size:");
		p3.add(pop2SizeSpinner);
		p3.setToolTipText("The size of the other descendant population");
		mainPanel.add(p3);
		
		model = new SpinnerNumberModel(Integer.valueOf(50000), Integer.valueOf(1), null, Integer.valueOf(1000));
		splitTimeSpinner = new JSpinner();
		splitTimeSpinner.setModel(model);
		splitTimeSpinner.setPreferredSize(new Dimension(100, 30));
		JPanel p7 = makePanel("Population split time:");
		p7.add(splitTimeSpinner);
		p7.setToolTipText("The number of generations before the ancestral population splits");
		mainPanel.add(p7);
		
		
		m12Field = new JTextField();
		m12Field.setText("0.001");
		m12Field.setPreferredSize(new Dimension(100, 30));
		JPanel p4 = makePanel("Migration rate from 1 to 2:");
		p4.setToolTipText("The per-individual, per generation probability of migration from pop 1 to pop 2");
		p4.add(m12Field);
		mainPanel.add(p4);
		
		m21Field = new JTextField();
		m21Field.setText("0.001");
		m21Field.setPreferredSize(new Dimension(100, 30));
		JPanel p5 = makePanel("Migration rate from 2 to 1:");
		p5.setToolTipText("The per-individual, per generation probability of migration from pop 2 to pop 1");
		p5.add(m21Field);
		mainPanel.add(p5);
		
		JPanel p6 = makePanel("Sampling strategy:");
		p6.setToolTipText("Choose a stratgy for sampling individuals from populations");
		strategyBox = new JComboBox(strats);
		p6.add(strategyBox);
		mainPanel.add(p6);
		
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
	
		
		Integer ancSize = 1000;
		Integer p1Size = 1000;
		Integer p2Size = 1000;
		Integer splitTime = 50000;
		
		String ancPopSizeStr = attrMap.get(PopSplitDemoModel.XML_SIZE0);
		String p1SizeStr = attrMap.get(PopSplitDemoModel.XML_SIZE1);
		String p2SizeStr = attrMap.get(PopSplitDemoModel.XML_SIZE2);
		String splitTimeStr = attrMap.get(PopSplitDemoModel.XML_SPLITTIME);
		
		String m12Str = attrMap.get(PopSplitDemoModel.XML_MIG12);
		String m21Str = attrMap.get(PopSplitDemoModel.XML_MIG21);
		
		try {
			ancSize = Integer.parseInt(ancPopSizeStr);
			ancSizeSpinner.setValue(ancSize);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Pop. Split demo. model", "Could not parse population size from XML");
		}
		
		try {
			p1Size = Integer.parseInt(p1SizeStr);
			pop1SizeSpinner.setValue(p1Size);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Pop. Split demo. model", "Could not parse population size from XML");
		}
		
		try {
			p2Size = Integer.parseInt(p2SizeStr);
			pop2SizeSpinner.setValue(p2Size);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Pop. Split demo. model", "Could not parse population size from XML");
		}
		
		try {
			splitTime = Integer.parseInt(splitTimeStr);
			splitTimeSpinner.setValue(splitTime);
		}
		catch (NumberFormatException nfe) {
			throw new TJXMLException("Pop. Split demo. model", "Could not parse splitting time from XML");
		}
		
		m12Field.setText(m12Str);
		m21Field.setText(m21Str);
		
		
		String sampleStrat = attrMap.get(IslandDemoModel.XML_SAMPLESTRATEGY);
		try {
			int index = indexForStrategy(sampleStrat);
			strategyBox.setSelectedIndex(index);
			
		}
		catch (Exception ex) {
			throw new TJXMLException("Pop. Split demo. model", "Could not set sampling strategy from XML");
		}
		
	}
	
	
	public String getXMLTypeAttr() {
		return PopSplitDemoModel.XML_ATTR;
	}

	public JComponent getComponent() {
		return mainPanel;
	}

	
	public DemographicModel getDemographicModel() {
		int ancSize = (Integer)ancSizeSpinner.getValue();
		int p1Size = (Integer)pop1SizeSpinner.getValue();
		int p2Size = (Integer)pop2SizeSpinner.getValue();
		
		int splitTime = (Integer)splitTimeSpinner.getValue();
		Double m12 = 0.001; 
		Double m21 = 0.001; Double.parseDouble( m21Field.getText() );

		try {
			m12 = Double.parseDouble( m12Field.getText() );
		}
		catch (NumberFormatException nfe) {
			ErrorWindow.showErrorWindow(new Exception("Could not read a number from the migration 1->2 field"));
		}
		
		try {
			m21 = Double.parseDouble( m21Field.getText() );
		}
		catch (NumberFormatException nfe) {
			ErrorWindow.showErrorWindow(new Exception("Could not read a number from the migration 2->1 field"));
		}
		
		
		
		PopSplitDemoModel model = new PopSplitDemoModel(ancSize, p1Size, p2Size, splitTime, m12, m21);
		Strategy strat = getCurrentStrategy();
		model.setSamplingStrategy(strat, 0);
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
	
	public String getIdentifier() {
		return "Population splitting";
	}


	
	public String getDescription() {
		return "A model in which a single ancestral population splits into two descendant populations, which may be connected by migration";
	}
}
