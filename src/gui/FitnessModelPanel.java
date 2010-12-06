package gui;

import fitnessProviders.FitnessProvider;
import gui.fitnessConfigurators.DNAFitnessConfigurator;
import gui.fitnessConfigurators.FitnessModelConfigurator;
import gui.fitnessConfigurators.NeutralFitnessConfigurator;
import gui.fitnessConfigurators.QGenConfigurator;
import gui.fitnessConfigurators.TwoAlleleConfigurator;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import treesimj.TreesimJView;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;


/**
 * Creates the panel that displays the various fitness models in the main JTabbedPane. 
 * To add a new fitness model, simply add a line in 'createFitnessConfigurators' that adds the
 * new configurator (the GUI component that can create the new fitness model) to the list of available
 * configurators. 
 *  
 * @author brendan
 *
 */
public class FitnessModelPanel extends javax.swing.JPanel {

	JPanel fitnessCardPanel;
	
	JTextArea descriptionArea;
	
	//A list of known fitness configurators
	ArrayList<FitnessModelConfigurator> fitnessConfigurators;
	String currentFitnessModel;
	String[] fitnessModelIds;
	JComboBox fitnessModelBox;
	TreesimJView tj;
	
	public FitnessModelPanel(TreesimJView treesimj) {
		this.tj = treesimj;
		this.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		
		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);

		descriptionArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(2, 2, 2, 2)) );
		descriptionArea.setPreferredSize(new Dimension(200, 55));
		descriptionArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
		descriptionArea.setOpaque(false);
		
		createFitnessConfigurators();
		
		fitnessModelBox = new JComboBox();
		fitnessModelBox.setModel(new DefaultComboBoxModel(fitnessModelIds));
		fitnessModelBox.setMaximumSize(new Dimension(200, 30));
        fitnessModelBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent evt) {
        		changeFitnessModel((String)evt.getItem());
        	}
        });
		this.add(fitnessModelBox);
		this.add(Box.createVerticalStrut(6));
		this.add(descriptionArea);
		
		fitnessCardPanel = new JPanel();
		fitnessCardPanel.setOpaque(false);
		
		fitnessCardPanel.setLayout(new CardLayout());
		addFitnessModelComponents(fitnessCardPanel);
		this.add(fitnessCardPanel);
		this.setPreferredSize(new Dimension(300, 200));
		this.add(Box.createGlue());
	}
	
	/**
	 * Read the various settings frmo an XML file
	 * @param reader
	 * @throws XMLStreamException
	 * @throws TJXMLException
	 */
	public void readSettingsFromXML(XMLStreamReader reader) throws XMLStreamException, TJXMLException {
		if (reader.isStartElement() && reader.getLocalName()==TJXMLConstants.FITNESS_MODEL) {
			String model = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE); 
			
			for(FitnessModelConfigurator fitnessConf : fitnessConfigurators) {
				String xmlAttr = fitnessConf.getXMLTypeAttr();
				if (model.equals(xmlAttr)) {
					changeFitnessModel(fitnessConf.getIdentifier());
					fitnessConf.configureSettings(reader);
				}
			}

		}
		else {
			throw new TJXMLException("Fitness model", "Reader at incorrect position (Not start of fitness.model)");
		}
	}
	
	
	/**
	 * Creates a list of FitnessModelConfigurators and generates the array of fitnessModelIds. All
	 * new fitness models should be added here. 
	 */
    public void createFitnessConfigurators() {
    	fitnessConfigurators = new ArrayList<FitnessModelConfigurator>();
    	
    	FitnessModelConfigurator neutModel = new NeutralFitnessConfigurator();
    	fitnessConfigurators.add(neutModel);
    	descriptionArea.setText(neutModel.getDescription());
    	currentFitnessModel = neutModel.getIdentifier(); 
    		
    	fitnessConfigurators.add(new DNAFitnessConfigurator());
    	fitnessConfigurators.add(new TwoAlleleConfigurator());
    	fitnessConfigurators.add(new QGenConfigurator());
    	
    	fitnessModelIds = new String[fitnessConfigurators.size()];
    	int i = 0;
    	for(FitnessModelConfigurator conf : fitnessConfigurators) {
    		fitnessModelIds[i] = conf.getIdentifier();
    		i++;
    	}
    }
	
	protected void changeFitnessModel(String item) {
		CardLayout cl = (CardLayout)(fitnessCardPanel.getLayout());
		currentFitnessModel = item;
		fitnessModelBox.setSelectedItem(item);
		FitnessModelConfigurator conf = getFitnessConfigById(currentFitnessModel);
		descriptionArea.setText(conf.getDescription());
		cl.show(fitnessCardPanel, item);		
	}
    
    public FitnessModelConfigurator getFitnessConfigById(String id) {
    	for(FitnessModelConfigurator conf : fitnessConfigurators) {
    		if (conf.getIdentifier().equals(id))
    			return conf;
    	}
    	return null;
    }

    /**
     * Create and return a new FitnessProvider based on the current panel settings. 
     * @param rng
     * @return A new FitnessProvider
     */
	public FitnessProvider constructFitnessModel(RandomEngine rng) {
		FitnessProvider model = null;
		for(FitnessModelConfigurator conf : fitnessConfigurators) {
			if (conf.getIdentifier().equals(currentFitnessModel)) {
				model = conf.getFitnessModel(rng);
			}
		}
		return model;
	}
    
	private void addFitnessModelComponents(JPanel fitnessPanel) {
		for(FitnessModelConfigurator conf : fitnessConfigurators) {
			fitnessPanel.add(conf.getComponent(), conf.getIdentifier());
		}	
	}
}
