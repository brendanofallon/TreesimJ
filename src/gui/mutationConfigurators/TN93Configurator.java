package gui.mutationConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import mutationModels.MutationModel;
import mutationModels.MutationModelConfigurator;
import mutationModels.TN93Mutation;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

public class TN93Configurator implements MutationModelConfigurator {

	JPanel mainPanel;
	JTextField arField;
	JTextField ayField;
	JTextField bField;
	JTextField piAField;
	JTextField piGField;
	JTextField piCField;
	JTextField piTField;
	
	public TN93Configurator() {
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		piAField = new JTextField();
		piGField = new JTextField();
		piCField = new JTextField();
		piTField = new JTextField();
		arField = new JTextField();
		ayField = new JTextField();
		bField = new JTextField();
		
		mainPanel.add(makePanel("A Freq : ", piAField, 0.25, "G Freq : ", piGField, 0.25));
		mainPanel.add(makePanel("C Freq : ", piCField, 0.25, "T Freq : ", piTField, 0.25));

		
		mainPanel.add(makePanel("Alpha R :", arField, 2.0, "Alpha Y :", ayField, 2.0));
		//mainPanel.add(makePanel("Alpha Y", ayField, 2.0));
		mainPanel.add(makePanel("Beta : ", bField, 1.0));

		piAField.setToolTipText("Stationary frequency of A");
		piGField.setToolTipText("Stationary frequency of G");
		piCField.setToolTipText("Stationary frequency of C");
		piTField.setToolTipText("Stationary frequency of T");
		
		arField.setToolTipText("Transition rate among purines (A+G)");
		ayField.setToolTipText("Transition rate among pyrimidines (C+T)");
		bField.setToolTipText("Transversion rate");
		
		
		mainPanel.add(Box.createGlue());
	}
	
	private JPanel makePanel(String title, JTextField comp, double initVal) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel(title));
		
		comp.setText(String.valueOf(initVal));
		p.add(comp);
		
		comp.setPreferredSize(new Dimension(80, 24));
		comp.setMinimumSize(new Dimension(80, 10));
		
		return p;
	}
	
	private JPanel makePanel(String title, JTextField comp, double initVal, String title2, JTextField comp2, double initval2) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel(title));
		
		comp.setText(String.valueOf(initVal));
		p.add(comp);
		
		comp.setPreferredSize(new Dimension(60, 24));
		comp.setMinimumSize(new Dimension(50, 10));
		
		p.add(new JLabel(title2));
		comp2.setText(String.valueOf(initval2));
		p.add(comp2);
		comp2.setPreferredSize(new Dimension(60, 24));
		comp2.setMinimumSize(new Dimension(50, 24));
		
		
		return p;
	}

	private Double doubleFromField(JTextField field) {
		try {
			Double val = Double.parseDouble(field.getText());
			return val;
		}
		catch (Exception ex) {
			System.err.println("Could not parse value from field with text : " + field.getText());
		}
		return null;
	}
	

	public JComponent getComponent() {
		return mainPanel;
	}


	public String getDescription() {
		return "The Tamura-Nei (1993) mutation model";
	}


	public String getIdentifier() {
		return "TN93";
	}

	public MutationModel getMutationModel(RandomEngine rng, double mu) {
		return new TN93Mutation(rng, mu, 
								doubleFromField(arField), 
								doubleFromField(ayField), 
								doubleFromField(bField), 
								doubleFromField(piAField), 
								doubleFromField(piGField), 
								doubleFromField(piCField), 
								doubleFromField(piTField));
	}

	
	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		
		Map<String, String> attrMap = XMLParseable.Utils.makeAttributeMap(reader);
		
		arField.setText( attrMap.get(TN93Mutation.ALPHAR) );
		ayField.setText( attrMap.get(TN93Mutation.ALPHAY));
		bField.setText( attrMap.get(TN93Mutation.BETA) );
		
		piAField.setText( attrMap.get(TN93Mutation.PIA));
		piGField.setText( attrMap.get(TN93Mutation.PIG));
		piCField.setText( attrMap.get(TN93Mutation.PIC));
		piTField.setText( attrMap.get(TN93Mutation.PIT));

	}

	public String getXMLTypeAttr() {
		return TN93Mutation.XML_ATTR;
	}

}
