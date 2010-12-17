package gui;

import gui.mutationConfigurators.F84Configurator;
import gui.mutationConfigurators.JukesCantorConfigurator;
import gui.mutationConfigurators.K2PConfigurator;
import gui.mutationConfigurators.TN93Configurator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import mutationModels.MutationModel;
import mutationModels.MutationModelConfigurator;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLConfigurable;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

/**
 * A panel that contains various options for creation of DNA (including the DNA length and the mutation model). This
 * also  houses a list of various mutation models.
 * @author brendan
 *
 */
public class DNAConstructorPane extends javax.swing.JPanel implements XMLConfigurable {

	public static final String XML_ATTR = "dna.constructor";
	
	JSpinner lengthSpinner;
	JTextField mutationRateField;
	JComboBox mutationModelBox;
	JTextField ttField;
	JPanel muModelCardPanel;
	
	JTextField recRateField;
	
	ArrayList<MutationModelConfigurator> mutationModels;
	String currentMuMod;
	
	//String[] mutationModelChoices = {JukesCantorMutation.XML_ATTR, K2PMutation.XML_ATTR, TN93Mutation.XML_ATTR};
	
	public DNAConstructorPane() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		
		JPanel lengthPanel = new JPanel();
		lengthPanel.setOpaque(false);
		lengthPanel.setLayout(new FlowLayout(FlowLayout.LEFT,2, 1));
		lengthSpinner = new JSpinner();
		SpinnerNumberModel model = new SpinnerNumberModel(Integer.valueOf(1000000), Integer.valueOf(0), null, Integer.valueOf(1));
		lengthSpinner.setModel(model);
		lengthSpinner.setValue(Integer.valueOf(1000));
		lengthSpinner.setMinimumSize(new Dimension(80, 1));
		lengthSpinner.setPreferredSize(new Dimension(100, 24));
		lengthPanel.add(new JLabel("Sequence length: "));
		lengthPanel.add(lengthSpinner);
		add(lengthPanel);
		
		add(Box.createVerticalStrut(2));
		
		JPanel muRatePanel = new JPanel();
		muRatePanel.setOpaque(false);
		muRatePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		muRatePanel.add(new JLabel("Mutation rate :"));
		mutationRateField = new JTextField("1.0e-6");
		mutationRateField.setPreferredSize(new Dimension(80, 24));
		mutationRateField.setMinimumSize(new Dimension(80, 1));
		muRatePanel.add(mutationRateField);
		muRatePanel.add(Box.createGlue());
		
		add(muRatePanel);
		
		
		JPanel recRatePanel = new JPanel();
		recRatePanel.setOpaque(false);
		recRatePanel.setLayout(new FlowLayout((FlowLayout.LEFT)));
		recRatePanel.add(new JLabel("Recombination rate:"));
		recRateField = new JTextField("0.0");
		recRateField.setPreferredSize(new Dimension(80, 24));
		recRateField.setMinimumSize(new Dimension(80, 10));
		recRateField.add(Box.createGlue());
		recRatePanel.add(recRateField);
		recRatePanel.setToolTipText("Per sequence, per generation rate of recombination");
		add(recRatePanel);
		
		add(Box.createVerticalStrut(5));
		
	
		JPanel muModelPanel = new JPanel();
		muModelPanel.setOpaque(false);
		muModelPanel.setLayout(new BorderLayout());
		JPanel muTopPanel = new JPanel();
		muTopPanel.setOpaque(false);
		muTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		muTopPanel.add(new JLabel("Mutation model:") );
		mutationModelBox = new JComboBox();
		mutationModelBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent evt) {
        		changeMutModel((String)evt.getItem());
        	}
        });
		muTopPanel.add(mutationModelBox);
		
		
		muModelCardPanel = new JPanel();
		muModelCardPanel.setOpaque(false);
		muModelCardPanel.setLayout(new CardLayout());
		addMutationModelComponents();
		
		muModelPanel.add(muTopPanel, BorderLayout.NORTH);
		muModelPanel.add(muModelCardPanel, BorderLayout.CENTER);
		add(muModelPanel);
		
		add(Box.createGlue());
	}
	
	/**
	 * Adds a bunch of different mutation models to the mutation model list. New models should be added here. 
	 */
	private void addMutationModelComponents() {
		mutationModels = new ArrayList<MutationModelConfigurator>();
		
		mutationModels.add(new JukesCantorConfigurator());
		mutationModels.add(new K2PConfigurator());
		mutationModels.add(new F84Configurator());
		mutationModels.add(new TN93Configurator());
		
		currentMuMod = mutationModels.get(0).getIdentifier();
		
		String[] muModIDs = new String[mutationModels.size()];
		
		int i=0;
		for(MutationModelConfigurator mmConf : mutationModels) {
			muModelCardPanel.add(mmConf.getComponent(), mmConf.getIdentifier());
			muModIDs[i] = mmConf.getIdentifier();
			i++;
		}
		
		mutationModelBox.setModel(new javax.swing.DefaultComboBoxModel(muModIDs));
	}

	/**
	 * Called when the user has selected a new item in the mutation model box and causes a new panel to be shown
	 * in the mutation model area. 
	 * @param item
	 */
	protected void changeMutModel(String item) {
		CardLayout cl = (CardLayout)(muModelCardPanel.getLayout());
		currentMuMod = item;
		mutationModelBox.setSelectedItem(item);
		cl.show(muModelCardPanel, item);	
	}

	public int getDNALength() {
		return (Integer)lengthSpinner.getValue();
	}
	
	public void setDNALength(int length) {
		lengthSpinner.setValue(new Integer(length));
	}
	
	public void disableComponents() {
		lengthSpinner.setEnabled(false);
		mutationRateField.setEnabled(false);
		mutationModelBox.setEnabled(false);
		repaint();
	}
	
	public void enableComponents() {
		lengthSpinner.setEnabled(true);
		mutationRateField.setEnabled(true);
		mutationModelBox.setEnabled(true);
		repaint();
	}
	
	public Double getMutationRate() {
		try {
			Double val = Double.parseDouble(mutationRateField.getText());
			return val;
		}
		catch (NumberFormatException nfe) {
			System.err.println("Couldn't parse a number from the mutation rate field");
			return null;
		}
	}
	
	
	public Double getRecombinationRate() {
		try {
			Double val = Double.parseDouble(recRateField.getText());
			return val;
		}
		catch (NumberFormatException nfe) {
			System.err.println("Couldn't parse a number from the recombination rate field");
			return null;
		}
	}
	
	public MutationModel getMutationModel(RandomEngine rng) {
		MutationModelConfigurator conf = getConfByID(currentMuMod);
		Double mu = 1e-6;
		try {
			mu = Double.parseDouble(mutationRateField.getText());
		}
		catch (Exception ex) {
			System.err.println("Could not parse mutation rate value from : " + mutationRateField.getText());
		}
		
		return conf.getMutationModel(rng, mu);
	}

	private MutationModelConfigurator getConfByID(String id) {
		for(MutationModelConfigurator conf : mutationModels) {
			if (conf.getIdentifier().equals(id))
				return conf;
		}
		return null;
	}
	
	private MutationModelConfigurator getConfByXMLAttr(String attrType) {
		for(MutationModelConfigurator conf : mutationModels) {
			if (conf.getXMLTypeAttr().equals(attrType))
				return conf;
		}
		return null;
	}
	
	public void configureSettings(XMLStreamReader reader) throws TJXMLException, XMLStreamException {
		Hashtable<String, String> attrs = XMLParseable.Utils.makeAttributeMap(reader);
		
		String mumod = attrs.get(TJXMLConstants.TYPE);
		int i;
		if (mumod == null) {
			throw new TJXMLException("DNA constructor", "Could not parse mutation model, got null string");
		}
		else {
			boolean found = false;
			for(i=0; i<mutationModels.size(); i++) {
				if (mutationModels.get(i).getXMLTypeAttr().equals( mumod)) {
					mutationModelBox.setSelectedIndex(i);
					MutationModelConfigurator mmConf = getConfByXMLAttr(mumod);
					mmConf.configureSettings(reader);
					found = true;
					break;
				}
			}

			if (!found)
				throw new TJXMLException("DNA constructor", "Could not parse mutation model, got " + mumod);
		}
	}

	public void setMutationRate(Double mu) {
		mutationRateField.setText(String.valueOf(mu));
	}
	
	public void setRecombinationRate(Double r) {
		recRateField.setText(String.valueOf(r));
	}
	
	public String getXMLTypeAttr() {
		return XML_ATTR;
	}

	
	
}
