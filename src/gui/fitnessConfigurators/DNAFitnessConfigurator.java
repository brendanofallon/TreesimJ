package gui.fitnessConfigurators;

import fitnessProviders.DNAFitness;
import fitnessProviders.FitnessProvider;
import gui.DNAConstructorPane;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import mutationModels.MutationModel;
import siteConfigurators.CodonSiteConfigurator;
import siteConfigurators.ConstFitnessConfigurator;
import siteConfigurators.GammaFitnessConfigurator;
import siteConfigurators.GenDiscreteConfigurator;
import siteConfigurators.NeutralSiteConfigurator;
import siteConfigurators.SiteModelConfigurator;
import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;
import dnaModels.BitSetDNASequence;
import dnaModels.DNASequence;

/**
 * Creates a user-configurable DNAFitness model, which houses a DNAConstructor pane to make a new DNA sequence complete
 * with a mutational model, and a separate panel for the SiteModel.
 * @author brendan
 *
 */
public class DNAFitnessConfigurator implements FitnessModelConfigurator {

	JPanel mainPanel;
	DNAConstructorPane dnaMaker;
	JPanel siteModelCardPanel;
	
	ArrayList<SiteModelConfigurator> siteModels;
	ArrayList<String> siteModelNames;
	String currentSiteModel;
	
	JComboBox siteFitnessesBox;
	
	public DNAFitnessConfigurator() {
		createSiteFitnessModels();

		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		dnaMaker = new DNAConstructorPane();
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		dnaMaker.setBorder(BorderFactory.createTitledBorder("Sequence Properties"));
		mainPanel.add(dnaMaker);
		mainPanel.setMaximumSize(new Dimension(10000, 100));
		
		JPanel sitePanel = new JPanel();
		sitePanel.setOpaque(false);
		sitePanel.setBorder(BorderFactory.createTitledBorder("Site fitness model"));
		sitePanel.setLayout(new BoxLayout(sitePanel, BoxLayout.Y_AXIS));
		siteFitnessesBox = new JComboBox();
		siteFitnessesBox.setModel(new DefaultComboBoxModel(siteModelNames.toArray()));

        siteFitnessesBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent evt) {
        		changeSiteModel((String)evt.getItem());
        	}
        });
		sitePanel.setPreferredSize(new Dimension(250, 200));
		sitePanel.add(siteFitnessesBox);
		mainPanel.add(sitePanel);
		siteModelCardPanel = new JPanel();
		siteModelCardPanel.setOpaque(false);
		siteModelCardPanel.setLayout(new CardLayout());
		sitePanel.add(siteModelCardPanel);
		for(SiteModelConfigurator sConf : siteModels) {
			siteModelCardPanel.add(sConf.getComponent(), sConf.getIdentifier());
		}
		
		dnaMaker.setPreferredSize(new Dimension(250, 200));
		dnaMaker.setOpaque(false);
	}
	
	protected void changeSiteModel(String item) {
		CardLayout cl = (CardLayout)(siteModelCardPanel.getLayout());
		currentSiteModel = item;
		
		cl.show(siteModelCardPanel, item);		
		
		siteModelCardPanel.repaint();
	}

	/**
	 * Adds a new site model configurator to the list
	 */
	private void createSiteFitnessModels() {
		siteModelNames = new ArrayList<String>();
		siteModels = new ArrayList<SiteModelConfigurator>();
		
		
		siteModels.add(new ConstFitnessConfigurator());
		siteModels.add(new NeutralSiteConfigurator());
		siteModels.add(new GammaFitnessConfigurator());
		siteModels.add(new GenDiscreteConfigurator());
		siteModels.add(new CodonSiteConfigurator());
		
		for(SiteModelConfigurator conf : siteModels) {
			siteModelNames.add(conf.getIdentifier());
		}
		
		currentSiteModel = siteModels.get(0).getIdentifier();
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	public FitnessProvider getFitnessModel(RandomEngine rng) {
		SiteFitnesses siteModel = null;
		for(SiteModelConfigurator sModel : siteModels) {
			if (sModel.getIdentifier()==currentSiteModel) {
				siteModel = sModel.getSiteModel(rng);
			}
		}
		
		MutationModel mutModel = dnaMaker.getMutationModel(rng);
		double recRate = dnaMaker.getRecombinationRate();
		mutModel.setRecombinationRate(recRate);
		
		DNASequence master = siteModel.generateMasterSequence(rng, dnaMaker.getDNALength(), mutModel); //new BitSetDNASequence(rng, dnaMaker.getDNALength(), mutModel);

		return new DNAFitness(rng, master, siteModel, mutModel);
	}

	public String getIdentifier() {
		return "DNA Fitness";
	}
	
	public String getXMLTypeAttr() {
		return DNAFitness.XML_ATTR;
	}

	public void configureSettings(XMLStreamReader reader) throws TJXMLException, XMLStreamException {
		if (reader.isStartElement() && reader.getLocalName()==TJXMLConstants.FITNESS_MODEL) {
			String type = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE);
			if (type.equals( DNAFitness.XML_ATTR)) { //We're in the right place
				
				String length = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.LENGTH);
				String muRate = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.MUTATIONRATE);
				String recRate = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.RECOMBINATIONRATE);
				
				try {
					Integer l = Integer.parseInt(length);
					dnaMaker.setDNALength(l);
				}
				catch (Exception ex) {
					System.err.println("Could not set DNA length, couldn't parse an integer from : " + length + "\n" + ex);
				}
				
				try {
					Double mu = Double.parseDouble(muRate); 
					dnaMaker.setMutationRate(mu);
				}
				catch (Exception ex) {
					System.err.println("Could not set mutation rate, couldn't parse a double from " + muRate);
				}
				
				try {
					if (recRate != null) {
						Double r = Double.parseDouble(recRate); 
						dnaMaker.setRecombinationRate(r);
					}
					else {
						dnaMaker.setRecombinationRate(0.0);
					}
				}
				catch (Exception ex) {
					System.err.println("Could not set recombination rate, couldn't parse a double from " + recRate);
				}
				
				advanceToNextStart(reader);

				while(true) {
					if (reader.isStartElement()) {
						if (reader.getLocalName().equals(TJXMLConstants.SITE_MODEL)) {
							String siteModelType = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE);
							SiteModelConfigurator siteModelConfig = findSiteModel(siteModelType);
							siteModelConfig.configureSettings(reader);
							
							siteFitnessesBox.setSelectedItem( siteModelConfig.getIdentifier() );
						}
						
						if (reader.getLocalName().equals(TJXMLConstants.MUTATION_MODEL)) {
							dnaMaker.configureSettings(reader);
						}
					}
					

					reader.next();
					if ((reader.isEndElement() && reader.getLocalName().equals(TJXMLConstants.FITNESS_MODEL)) || (!reader.hasNext())) {
						break;
					}
				}
				
			}
			else {
				throw new TJXMLException("DNA Fitness config", "Wrong fitness model for DNA fitness (got" + type + ")");
			}
		}
		else {
			throw new TJXMLException("DNA Fitness config", "Not a start element of a fitness model");
		}

	}

	private void advanceToNextStart(XMLStreamReader reader) throws XMLStreamException, TJXMLException {
		int event = reader.next();
		while(event != XMLStreamConstants.START_ELEMENT && event != XMLStreamConstants.END_DOCUMENT) {
			event = reader.next();
		}
		if (event == XMLStreamConstants.END_DOCUMENT) {
			throw new TJXMLException("DNA fitness config", "Reached end of document while looking for models");
		}
	}
	
	private SiteModelConfigurator findSiteModel(String siteModelType) {
		for(SiteModelConfigurator model : siteModels) {
			if (model.getXMLTypeAttr().equals(siteModelType)) 
				return model;
		}
		return null;
	}

	public String getDescription() {
		return "Fitnesses are described by the state of a DNA sequence. Several mutation models and selection models are provided.";
	}

}
