package gui;

import gui.demographicConfigurators.BottleneckConfigurator;
import gui.demographicConfigurators.ConstSizeDemoConfigurator;
import gui.demographicConfigurators.DemographicConfigurator;
import gui.demographicConfigurators.IslandDemoConfigurator;
import gui.demographicConfigurators.LinearGrowthConfigurator;
import gui.demographicConfigurators.PopSplitConfigurator;
import gui.demographicConfigurators.RepeatingExpGrowthConfigurator;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import population.Population;

import treesimj.TreesimJView;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import demographicModel.DemographicModel;
import errorHandling.ErrorWindow;

/**
 * This panel creates the "Demographic Model" tab in the main JTabbedPane, and allows the user to select a demographic model
 * from a list of potential options. 
 * @author brendan
 *
 */
public class DemographicModelPanel extends JPanel {

	TreesimJView tj;
	JTextArea descriptionArea;
	JComboBox demoModelBox;
	JPanel demoCardPanel;
	
	//Known demographic configurators
	ArrayList<DemographicConfigurator> demographicConfigurators;
	String currentDemoModel;
	
	String[] demoModelIds;
	
	public DemographicModelPanel(TreesimJView tj) {
		this.tj = tj;
		this.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalStrut(4));
		
		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);

		descriptionArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(2, 2, 2, 2)) );
		descriptionArea.setPreferredSize(new Dimension(200, 55));
		descriptionArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
		descriptionArea.setOpaque(false);
		
		demoCardPanel = new JPanel();
		createDemographicConfigurators();
		demoCardPanel.setLayout(new CardLayout());
		demoCardPanel.setOpaque(false);
		demoModelBox = new JComboBox();
		demoModelBox.setPreferredSize(new Dimension(200, 25));
		demoModelBox.setMaximumSize(new Dimension(250, 35));
		demoModelBox.setModel(new DefaultComboBoxModel(demoModelIds));
		demoModelBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent evt) {
        		changeDemogModel((String)evt.getItem());
        	}
        });
		this.add(demoModelBox);
		

		this.add(descriptionArea);
		
		
		addDemoModelComponents(demoCardPanel);
		this.add(demoCardPanel);
	}
	
	/**
	 * Adds all known demographic configurators to the list. Plug in new ones here. 
	 */
    public void createDemographicConfigurators() {
    	demographicConfigurators = new ArrayList<DemographicConfigurator>();
    	
    	DemographicConfigurator demoModel = new ConstSizeDemoConfigurator();
    	
    	demographicConfigurators.add(demoModel);
    	currentDemoModel = demoModel.getIdentifier();
    	descriptionArea.setText(demoModel.getDescription());
    	
    	try {
    		demographicConfigurators.add(new IslandDemoConfigurator());
    	}
    	catch (Exception ex) {
    		ErrorWindow.showErrorWindow(new Exception("There was an error loading the island demographic model; it will not be loaded."));
    	}
    	
    	try {
    		demographicConfigurators.add(new PopSplitConfigurator());
    	}
    	catch (Exception ex) {
    		ErrorWindow.showErrorWindow(new Exception("There was an error loading the population splitting demographic model; it will not be loaded."));
    	}
    	demographicConfigurators.add(new BottleneckConfigurator());
    	demographicConfigurators.add(new RepeatingExpGrowthConfigurator());
    	demographicConfigurators.add(new LinearGrowthConfigurator());
    	
    	//demographicConfigurators.add( /* your new model here */ );
    	
    	
    	demoModelIds = new String[demographicConfigurators.size()];
    	int i = 0;
    	for(DemographicConfigurator conf : demographicConfigurators) {
    		demoModelIds[i] = conf.getIdentifier();
    		i++;
    	}
    }
    

    /**
     * Returns the currently selected configurator, using the value of currentDemoModel
     * @return
     */
    private DemographicConfigurator getCurrentConfigurator() {
		for(DemographicConfigurator conf : demographicConfigurators) {
			if (conf.getIdentifier().equals(currentDemoModel)) {
				return conf;
			}
		}
		return null;
    }
	
	public DemographicModel constructDemographicModel() {
		DemographicModel model = null;
		DemographicConfigurator conf = getCurrentConfigurator();
		model = conf.getDemographicModel();
		return model;
	}
	
	private void addDemoModelComponents(JPanel demoModelPanel) {
		for(DemographicConfigurator conf : demographicConfigurators) {
			conf.getComponent().setOpaque(false);
			demoModelPanel.add(conf.getComponent(), conf.getIdentifier());
		}			
	}
	
	protected void changeDemogModel(String item) {
		CardLayout cl = (CardLayout)(demoCardPanel.getLayout());
		currentDemoModel = item;
		DemographicConfigurator conf = getCurrentConfigurator();
		descriptionArea.setText(conf.getDescription());
		cl.show(demoCardPanel, item);
	}


	public void readSettingsFromXML(XMLStreamReader reader) throws TJXMLException, XMLStreamException {
		if (reader.isStartElement() && reader.getLocalName()==TJXMLConstants.DEMOGRAPHIC_MODEL) {
			String model = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE); 
			
			for(DemographicConfigurator demoConf : demographicConfigurators) {
				String xmlAttr = demoConf.getXMLTypeAttr();
				if (model.equals(xmlAttr)) {
					demoModelBox.setSelectedItem(demoConf.getIdentifier());
					demoConf.configureSettings(reader);
				}
			}

		}
		else {
			throw new TJXMLException("Fitness model", "Reader at incorrect position (Not start of fitness.model)");
		}
		
	}

	
}
