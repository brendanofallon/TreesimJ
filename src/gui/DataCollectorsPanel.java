package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import statistics.BranchRateStatistic;
import statistics.MeanSpeed;
import statistics.PopulationSizeStatistic;
import statistics.Statistic;
import statistics.StatisticRegistry;
import statistics.TreeStatistic;
import statistics.TwoAlleleFrequency;
import statistics.dna.DStar;
import statistics.dna.Divergence;
import statistics.dna.FStar;
import statistics.dna.FrequencySpectrum;
import statistics.dna.HaplotypeDiversity;
import statistics.dna.MutNumDistro;
import statistics.dna.MutationRate;
import statistics.dna.NucDiversity;
import statistics.dna.SegregatingSites;
import statistics.dna.TajimasD;
import statistics.dna.WattersonsTheta;
import statistics.fitness.AbsoluteFitnessDistro;
import statistics.fitness.MeanFitness;
import statistics.fitness.MeanLineageFitness;
import statistics.fitness.RelativeFitnessDistro;
import statistics.fitness.StdevFitness;
import statistics.fitness.Tau;
import statistics.treeShape.CoalIntervalStat;
import statistics.treeShape.CollessIndex;
import statistics.treeShape.PairwiseCTime;
import statistics.treeShape.SackinsIndex;
import statistics.treeShape.SackinsVariance;
import statistics.treeShape.SampleTMRCA;
import statistics.treeShape.TMRCA;
import treesimj.TreesimJView;
import xml.TJXMLConstants;
import xml.TJXMLException;
import xml.XMLConfigurable;
import xml.XMLParseable;

/**
 * The panel displaying the list of potential data collectors
 * @author brendan
 *
 */
public class DataCollectorsPanel extends JPanel implements XMLConfigurable {

	public static final String XML_TREESAMPLESIZE = "tree.samplesize";
	public static final String XML_DNASAMPLESIZE = "dna.samplesize";
	
	TreesimJView tj;
	private JPanel dataCollectorsComponent; //Panel which contains the list of data collectors
	ArrayList<Statistic> activeCollectors;		//Currently selected data collectors
	ArrayList<DataCollectorPanelItem> dataCollectorPanels;	//References to each data collector panel in the list
	List<DataCollectorPanelItem> treePanels = new ArrayList<DataCollectorPanelItem>(); //List of panels to be disabled if we're not collecting trees
	
	JSpinner treeSampleSize;	//Used for selecting the size of the sampled tree
	JSpinner dnaSampleSize;		//Used for selecting the size of the sample to use for DNA statistics
	
	JCheckBox collectTreesBox;  //Turns on / off tree sampling
	
	Color lightColor = Color.white;
	Color darkColor = new Color(238, 238, 238);
	
	public DataCollectorsPanel(TreesimJView tj) {
		this.tj = tj;
		activeCollectors = new ArrayList<Statistic>();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JScrollPane dataCollectorsScrollPane = new JScrollPane();
		dataCollectorPanels = new ArrayList<DataCollectorPanelItem>();
		dataCollectorsComponent = new JPanel();

		dataCollectorsComponent.setLayout(new BoxLayout(dataCollectorsComponent, BoxLayout.PAGE_AXIS));
		
		SpinnerNumberModel model = new SpinnerNumberModel(Integer.valueOf(25), Integer.valueOf(2), Integer.valueOf(1000), Integer.valueOf(1));
		treeSampleSize = new JSpinner(model);
		treeSampleSize.setToolTipText("Number of tips of the sampled tree");
		treeSampleSize.setPreferredSize(new Dimension(65, 24));
		
		collectTreesBox = new JCheckBox("Sample trees");
		collectTreesBox.setToolTipText("Turn on / off tree collecting");
		collectTreesBox.setSelected(true);
		collectTreesBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnableTreeStats(collectTreesBox.isSelected());
			}	
		});
		
		model = new SpinnerNumberModel(Integer.valueOf(20), Integer.valueOf(2), Integer.valueOf(10000), Integer.valueOf(1));
		dnaSampleSize = new JSpinner(model);
		dnaSampleSize.setToolTipText("Number of Individuals to use for DNA sampling");
		dnaSampleSize.setPreferredSize(new Dimension(65, 24));
		
		createDataCollectorsItems();
		
		dataCollectorsScrollPane.setViewportView(dataCollectorsComponent);
		this.add(dataCollectorsScrollPane);
	}


	/**
	 * Enables / disables collection on all collectors which can / can't handle collecting in the absence of trees. This is called
	 * when the user switches the state on the collectTreesBox, which turns on / off tree collection. 
	 * @param selected
	 */
	protected void setEnableTreeStats(boolean selected) {
		for(DataCollectorPanelItem panel : treePanels) {
			panel.setBoxEnabled(selected);
			panel.repaint();
		}
	}


	/**
	 * Returns the current value of the treeSampleSize spinner
	 * @return
	 */
	public int getTreeSampleSize() {
		return (Integer)treeSampleSize.getValue();
	}
	
	
	/**
	 * Returns the current value of the dnaSampleSize spinner
	 * @return
	 */
	public int getDNASampleSize() {
		return (Integer)dnaSampleSize.getValue();
	}
	
	
	/**
	 * Whether we should collect trees.
	 * @return
	 */
	public boolean getSampleTrees() {
		return collectTreesBox.isSelected();
	}
	
	/**
	 * The list of all data collectors to display is created here
	 */
	private void createDataCollectorsItems() {
		StatisticRegistry sReg = new StatisticRegistry();
				
		addSeparator("Tree shape statistics     Tree size:", treeSampleSize /*, collectTreesBox */);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(PairwiseCTime.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(TMRCA.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(SampleTMRCA.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(CoalIntervalStat.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(SackinsIndex.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(SackinsVariance.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(CollessIndex.identifier), lightColor);
		
		addSeparator("Fitness statistics");
		addSingleDataCollectorItem((Statistic)sReg.getInstance(MeanFitness.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(StdevFitness.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(RelativeFitnessDistro.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(AbsoluteFitnessDistro.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(Tau.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(MeanLineageFitness.identifier), lightColor);
		
		addSeparator("DNA statistics      Sample size:", dnaSampleSize);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(NucDiversity.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(SegregatingSites.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(HaplotypeDiversity.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(WattersonsTheta.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(Divergence.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(MutationRate.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(TwoAlleleFrequency.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(TajimasD.identifier), lightColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(DStar.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(FStar.identifier), lightColor);
		
		addSingleDataCollectorItem((Statistic)sReg.getInstance(FrequencySpectrum.identifier), darkColor);
		
		
		addSeparator("Utilities");
		addSingleDataCollectorItem((Statistic)sReg.getInstance(MeanSpeed.identifier), darkColor);
		addSingleDataCollectorItem((Statistic)sReg.getInstance(PopulationSizeStatistic.identifier), lightColor);
		
		addSeparator("Experimental");
		addSingleDataCollectorItem((Statistic)sReg.getInstance(BranchRateStatistic.identifier), darkColor);
	}
	
	
	private void addSeparator(String text, Component comp1, Component comp2) {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel lab = new JLabel(text);
		lab.setHorizontalAlignment(SwingConstants.CENTER);

		lab.setAlignmentX(CENTER_ALIGNMENT);
		JSeparator sep = new JSeparator();
		sep.setAlignmentX(CENTER_ALIGNMENT);
		sep.setPreferredSize(new Dimension(100, 14));
		p.add(sep, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		centerPanel.add(lab);
		centerPanel.add(comp1);
		centerPanel.add(comp2);
		p.add(centerPanel, BorderLayout.CENTER);
		dataCollectorsComponent.add( p );
	}
	
	private void addSeparator(String text, Component comp) {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel lab = new JLabel(text);
		lab.setHorizontalAlignment(SwingConstants.CENTER);

		lab.setAlignmentX(CENTER_ALIGNMENT);
		JSeparator sep = new JSeparator();
		sep.setAlignmentX(CENTER_ALIGNMENT);
		sep.setPreferredSize(new Dimension(100, 14));
		p.add(sep, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		centerPanel.add(lab);
		centerPanel.add(comp);
		p.add(centerPanel, BorderLayout.CENTER);
		dataCollectorsComponent.add( p );
	}
	
	private void addSeparator(String text) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JLabel lab = new JLabel(text);
		lab.setHorizontalAlignment(SwingConstants.CENTER);

		lab.setAlignmentX(CENTER_ALIGNMENT);
		JSeparator sep = new JSeparator();
		sep.setAlignmentX(CENTER_ALIGNMENT);
		sep.setPreferredSize(new Dimension(100, 14));
		p.add(sep);
		p.add(lab);
		dataCollectorsComponent.add( p );
	}
	
	private void addSingleDataCollectorItem(Statistic stat, Color bgColor) {
		DataCollectorPanelItem panel = new DataCollectorPanelItem(tj, this, stat);
		dataCollectorPanels.add(panel);
		panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		dataCollectorsComponent.add(panel);
		panel.setBackground(bgColor);
		if (stat.requiresGenealogy()) {
			treePanels.add(panel);
		}
	}

	public ArrayList<Statistic> getActiveCollectors() {
		return activeCollectors;
	}
	
	/**
	 * We maintain a list of all currently selected data collectors
	 * @param selected
	 * @param identifier
	 */
	public void updateActiveDataCollectors(boolean selected,
			Statistic stat) {
		if (selected && ! activeCollectors.contains(stat)) 
			activeCollectors.add(stat);

		if (! selected && activeCollectors.contains(stat))
			activeCollectors.remove(stat);
	}

	public void unselectAllStats() {
		for(DataCollectorPanelItem dcItem : dataCollectorPanels) {
			dcItem.setSelected(false);
		}
	}

	public void selectStat(String id) {
		for(DataCollectorPanelItem dcItem : dataCollectorPanels) {
			if (dcItem.getIdentifier().equals(id))
				dcItem.setSelected(true);
		}
	}
	
	private Statistic getStatForId(String id) {
		for(DataCollectorPanelItem dcItem : dataCollectorPanels) {
			if (dcItem.getIdentifier().equals(id)) {
				return dcItem.getStatistic();
			}
		}
		return null;
	}
	
	/**
	 * Set settings based on XML from specified stream
	 */
	public void configureSettings(XMLStreamReader reader)
			throws TJXMLException, XMLStreamException {
		if (reader.isStartElement() && reader.getLocalName().equals(TJXMLConstants.STATISTICS)) {
			String sampleSizeStr = XMLParseable.Utils.getAttributeForKey(reader, XML_TREESAMPLESIZE);
			if (sampleSizeStr != null) {
				treeSampleSize.setValue(Integer.parseInt(sampleSizeStr));
			}
			sampleSizeStr = XMLParseable.Utils.getAttributeForKey(reader, XML_DNASAMPLESIZE);
			if (sampleSizeStr != null) {
				dnaSampleSize.setValue(Integer.parseInt(sampleSizeStr));
			}
			
			unselectAllStats();
			while(reader.hasNext() && !(reader.isEndElement() && reader.getLocalName().equals(TJXMLConstants.STATISTICS))) {

				if (reader.isStartElement() && reader.getLocalName().equals(TJXMLConstants.STATISTIC)) {
					String statType = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.TYPE); //Will be an identifier, not an XML_ATTR
					selectStat(statType);
					
					Statistic stat = getStatForId(statType);
					String collectionFreq = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.SAMPLEFREQUENCY); 
					if (collectionFreq != null) {
						stat.setSampleFrequency(Integer.parseInt(collectionFreq));
					}
					
					String histoBins = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.HISTOBINS); 
					if (histoBins != null) {
						stat.setUserHistoBins(Integer.parseInt(histoBins));
					}
					
					String histoMin = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.HISTOMIN);
					if (histoMin != null) {
						stat.setUserHistoMin(Double.parseDouble(histoMin));
					}
					
					String histoMax = XMLParseable.Utils.getAttributeForKey(reader, TJXMLConstants.HISTOMAX); 
					if (histoMax != null) {
						stat.setUserHistoMax(Double.parseDouble(histoMax));
					}

				}

				reader.next();
			}

		}
		else {
			throw new TJXMLException("Data Collectors", "Reader at incorrect position (Not start of Statistics block)");
		}
		
	}
	

	/**
	 * No particular XML type attr to report here (we're not associated with any special Statistic)
	 */
	public String getXMLTypeAttr() {
		return null;
	}
}
