package siteConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import siteModels.GeneralDiscreteFitness;
import siteModels.SiteFitnesses;
import siteModels.GeneralDiscreteFitness.SingleRange;
import statistics.Statistic;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

public class GenDiscreteConfigurator implements SiteModelConfigurator {

	JPanel mainPanel;
	JPanel classesPanel;
	ArrayList<SiteClassPanel> classPanels;
	
	public GenDiscreteConfigurator() {
		classPanels = new ArrayList<SiteClassPanel>();
		
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(20));

		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton newClassButton = new JButton("Add Class");
		newClassButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addClass();
			}
		});
		JButton removeClassButton = new JButton("Remove Class");
		removeClassButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeClass();
			}
		});
		topPanel.add(newClassButton);
		topPanel.add(removeClassButton);
		mainPanel.add(topPanel);
		
		classesPanel = new JPanel();
		classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
		classesPanel.setOpaque(false);
		addClass(1, 0.0);
		classPanels.get(0).getStartField().setEnabled(false);
		classPanels.get(0).getStartField().setEditable(false);
		addClass(200, 0.001);
		addClass(500, 0.01);
		mainPanel.add(classesPanel);
		
		mainPanel.add(Box.createGlue());
	}

	protected void addClass(int start, double sel) {
		SiteClassPanel newPanel = new SiteClassPanel(start, sel);
		classPanels.add(newPanel);
		classesPanel.add(newPanel);
		classesPanel.repaint();
	}
	
	protected void removeClass() {
		if (classPanels.size()==0)
			return;
		
		SiteClassPanel toRemove = classPanels.remove(classPanels.size()-1);
		classesPanel.remove(toRemove);
		classesPanel.revalidate();
		classesPanel.repaint();
	}
	
	protected void addClass() {
		SiteClassPanel newPanel = new SiteClassPanel();
		classPanels.add(newPanel);
		classesPanel.add(newPanel);
		classesPanel.revalidate();
		classesPanel.repaint();
	}
	
	

//	private JPanel makePanel(String labelText) {
//		JPanel p = new JPanel();
//		p.setLayout(new FlowLayout(FlowLayout.LEFT));
//		p.setOpaque(false);
//		p.add(new JLabel(labelText));
//		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
//		return p;
//	}
	
	public JComponent getComponent() {
		return mainPanel;
	}


	public String getIdentifier() {
		return "General discrete site fitnesses";
	}


	public SiteFitnesses getSiteModel(RandomEngine rng) {
		Collections.sort(classPanels, new StartSiteComparator() );
		int[] startSites = new int[classPanels.size()];
		double[] effects = new double[classPanels.size()];
		
		for(int i=0; i<classPanels.size(); i++) {
			startSites[i] = classPanels.get(i).getStart();
			effects[i] = classPanels.get(i).getSelectionCoeff();
		}
		
		//Confusingly, the GeneralDiscreteFitness constructor expects the boundaries
		//to mark the *ends*, not the beginnings, of each site class..
		int[] endBounds = new int[classPanels.size()];
		for(int i=0; i<classPanels.size()-1; i++) {
			endBounds[i] = startSites[i+1]-1;
		}
		endBounds[endBounds.length-1] = Integer.MAX_VALUE;
		return new GeneralDiscreteFitness(endBounds, effects);
	}


	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		
		classPanels.clear();
		classesPanel.removeAll();
		
		while(reader.hasNext() && !(reader.isEndElement() && reader.getLocalName()==TJXMLConstants.SITE_MODEL)) {
			
			if (reader.isStartElement() && reader.getLocalName().equals(SingleRange.XML_ATTR)) {

//				String statType = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE); //Will be an identifier, not an XML_ATTR
//				if ( statType.equals(GeneralDiscreteFitness.SingleRange.XML_ATTR)) {
					String startStr = null;
					String selectionStr = null;
					
					startStr = XMLParseable.Utils.getAttributeForKey(reader, SingleRange.XML_START); 
					selectionStr = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.SELECTION); 
					
					if (startStr!=null && selectionStr != null) {
						try {
							int start = Integer.parseInt(startStr);
							double sel = Double.parseDouble(selectionStr);
							
							addClass(start, sel);
						}
						catch (NumberFormatException nfe) {
							//Don't worry bout it, we just don't add this class
						}
						
					}
				


			}//if the element was of type singlerange
			
			reader.next();
		}//while searching through xml elements


	}

	
	public String getXMLTypeAttr() {
		return GeneralDiscreteFitness.XML_ATTR;
	}

	
	public class StartSiteComparator implements Comparator<SiteClassPanel> {
		public int compare(SiteClassPanel arg0, SiteClassPanel arg1) {
			return arg0.getStart()-arg1.getStart();
		}
	}
}
