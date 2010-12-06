package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import treesimj.TreesimJView;
import xml.TJXMLConstants;
import xml.XMLParseable;

/**
 * Creates the panel 'Run Settings' used in the main JTabbedPane, with various options regarding run length,
 * burn-in generations, and output.
 * 
 * @author brendan
 *
 */
public class RunSettingsPanel extends JPanel {

	TreesimJView tj;
	
	public RunSettingsPanel(TreesimJView tj) {
		this.tj = tj;
		
		initComponents();
	}


	/**
	 * Called when the state of the write dna check box has changed
	 */
	protected void fastaBoxStateChanged() {
		if (fastaBox.isSelected()) {
			fastaFileStemField.setEnabled(true);
			fastaFrequencySpinner.setEnabled(true);
			writeTreesTooBox.setEnabled(true);
			//separateFastasBox.setEnabled(true);
		}
		else {
			fastaFileStemField.setEnabled(false);
			fastaFrequencySpinner.setEnabled(false);
			//separateFastasBox.setEnabled(false);
			writeTreesTooBox.setEnabled(false);
		}
	}

	public boolean getWriteFasta() {
		return fastaBox.isSelected();
	}
	
	public int getFastaSampleSize() {
		return (Integer)fastaFrequencySpinner.getValue();
	}
	
	public String getFastaFileStem() {
		return baseDirField.getText() + System.getProperty("file.separator") + fastaFileStemField.getText();
	}
	
	public String getShortFastaFileStem() {
		return fastaFileStemField.getText();
	}
	
	/**
	 * Deprectaed - we always write separate fasta files
	 * @return
	 */
	public boolean getWriteSeparateFastas() {
		return true;
		//return separateFastasBox.isSelected();
	}
	
	public boolean writeToStdout() {
		return writeToStdoutBox.isSelected();
	}
	
	public boolean writeToFile() {
		return writeToFileBox.isSelected();
	}
	
	public boolean getSaveTrees() {
		return saveTreesBox.isSelected();
	}
	
	public String getTreesFileName() {
		return baseDirField.getText() + System.getProperty("file.separator") + treesField.getText();
	}
	
	public String getLogFileName() {
		return baseDirField.getText() + System.getProperty("file.separator") + outputFileField.getText();
	}
	
	public boolean writeSummary() {
		return writeSummaryBox.isSelected();
	}
	
	public String getSummaryFileName() {
		return baseDirField.getText() + System.getProperty("file.separator") + writeSummaryField.getText();
	}
	
	public String getTreesShortName() {
		return treesField.getText();
	}
	
	public String getLogShortName() {
		return outputFileField.getText();
	}
	
	public Integer getRepeats() {
		return (Integer)repeatSpinner.getValue();
	}
	
	public String getSummaryShortName() {
		return writeSummaryField.getText();
	}
	
//	public String getFastaShortName() {
//		return treesField.getText();
//	}
	
	public Integer getBurninGens() {
		return (Integer)burninSpinner.getValue();
	}
	
	public Integer getRunLength() {
		return (Integer)runlengthSpinner.getValue();
	}
	
	
	public Integer getFastaSampleFrequency() {
		if (getWriteFasta())
			return (Integer)fastaFrequencySpinner.getValue();
		else
			return 0;
	}
	
	public String getBaseDirName() {
		return baseDirField.getText();
	}
	
	public int getDataSampleFreq() {
		return (Integer)collectionFreqSpinner.getValue();
	}

	public boolean getWriteTreesWithFasta() {
		return writeTreesTooBox.isSelected();
	}
	
	public int getRandomSeed() {
		return Integer.parseInt(randomSeedField.getText());
	}

	
	/**
	 * Write the current settings to the given XML writer
	 * @param xmlWriter
	 * @throws XMLStreamException
	 */
	public void writeToXML(XMLStreamWriter xmlWriter) throws XMLStreamException {
		RunSettingsOptions ops = new RunSettingsOptions(getRunLength(),
													getBurninGens(),
													writeToStdout(),
													writeToFile(),
													writeSummary(),
													getWriteFasta(),
													//getWriteSeparateFastas(),
													getWriteTreesWithFasta(),
													getFastaSampleFrequency(),
													getSaveTrees(),
													getLogShortName(),
													getSummaryShortName(),
													getShortFastaFileStem(),
													getTreesShortName(),
													getBaseDirName(),
													getDataSampleFreq(),
													getRandomSeed(),
													getRepeats());
		XMLSettingsWriter writer = new XMLSettingsWriter(ops);
		writer.writeXMLBlock(xmlWriter);
	}


	/**
	 * Read settings from the XML stream and use 'em to set the values of the fields in here
	 * uughh... This needs something more elegant
	 * @param reader
	 */
	public void configureSettings(XMLStreamReader reader) {
		if (reader.isStartElement() && reader.getLocalName()==TJXMLConstants.SETTINGS) {
			Hashtable<String, String> attrMap = XMLParseable.Utils.makeAttributeMap(reader);
			try {
				runlengthSpinner.setValue(Integer.parseInt(attrMap.get(XMLSettingsWriter.XML_RUNLENGTH)));
			}
			catch (NumberFormatException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				burninSpinner.setValue(Integer.parseInt(attrMap.get(XMLSettingsWriter.XML_BURNIN)));
			}
			catch (NumberFormatException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				writeToStdoutBox.setSelected(Boolean.parseBoolean(attrMap.get(XMLSettingsWriter.XML_WRITESTDOUT)) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			
			try {
				writeToFileBox.setSelected(Boolean.parseBoolean(attrMap.get(XMLSettingsWriter.XML_WRITELOG)) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			
			try {
				saveTreesBox.setSelected(Boolean.parseBoolean(attrMap.get(XMLSettingsWriter.XML_WRITETREELOG)) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			
			try {
				writeSummaryBox.setSelected(Boolean.parseBoolean(attrMap.get(XMLSettingsWriter.XML_WRITESUMMARY)) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}

			try {
				fastaBox.setSelected(Boolean.parseBoolean(attrMap.get(XMLSettingsWriter.XML_WRITEFASTA)) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				writeTreesTooBox.setSelected(Boolean.parseBoolean(attrMap.get(XMLSettingsWriter.XML_WRITETREESTOO)));
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			//separateFastasBox.setSelected(Boolean.parseBoolean(attrMap.get(XMLSettingsWriter.XML_FASTASEP)) );
			try {
				if (!fastaFrequencySpinner.isEnabled())
					fastaFrequencySpinner.setEnabled(true);
				fastaFrequencySpinner.setValue(Integer.parseInt(attrMap.get(XMLSettingsWriter.XML_FASTAFREQ)));
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				outputFileField.setText(attrMap.get(XMLSettingsWriter.XML_LOGFILENAME) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				writeSummaryField.setText(attrMap.get(XMLSettingsWriter.XML_SUMMARYFILENAME) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				fastaFileStemField.setText(attrMap.get(XMLSettingsWriter.XML_FASTASTEM) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}

			try {
				treesField.setText(attrMap.get(XMLSettingsWriter.XML_TREESFILENAME) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				baseDirField.setText( attrMap.get(XMLSettingsWriter.XML_BASEDIR) );
			}
			catch (NullPointerException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			try {
				collectionFreqSpinner.setValue( Integer.parseInt(attrMap.get(XMLSettingsWriter.XML_DATAFREQ)) );
			}
			catch (NumberFormatException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			
			try {
				repeatSpinner.setValue( Integer.parseInt(attrMap.get(XMLSettingsWriter.XML_REPEATS)) );
			}
			catch (NumberFormatException nfe) {
				System.err.println("Error reading settings from XML : " + nfe);
			}
			
			try {
				String rStr = attrMap.get(XMLSettingsWriter.XML_RANDOMSEED);
				if (rStr != null) {
					Integer seed = Integer.parseInt(rStr);
					randomSeedField.setText(String.valueOf(seed));
				}
				else {
					throw new NullPointerException(); //Immediately caught below
				}
			}
			catch (Exception nfe) {
				int date = (int)(new Date()).getTime();
				int val = (int)(Math.random()*date);
				int newVal = (int)val;
				if (newVal<0)
					newVal *= -1;
				randomSeedField.setText(String.valueOf(newVal));
				System.out.println("No random seed found, using " + newVal + " for seed.");
				
			}

		}
			
	}
	
	
	private void initComponents() {
		this.setBorder(BorderFactory.createEmptyBorder(2, 2, 6, 6));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalStrut(10));
		
		fileChooser = new JFileChooser();
		
		JPanel runLengthPanel = new JPanel();
		runLengthPanel.setLayout(new BoxLayout(runLengthPanel, BoxLayout.X_AXIS));
		runLengthPanel.add(Box.createHorizontalStrut(6));
		runLengthPanel.add(new JLabel("Total length of simulation : "));
		runlengthSpinner = new JSpinner();
        runlengthSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1000000), Integer.valueOf(0), null, Integer.valueOf(100)));
        runlengthSpinner.setPreferredSize(new Dimension(100, 24));
        runlengthSpinner.setMaximumSize(new Dimension(130, 34));
        runLengthPanel.add(runlengthSpinner);
        runLengthPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        runLengthPanel.add(Box.createGlue());
        runLengthPanel.setMaximumSize(new Dimension(10000, 28));
        
        collectionFreqSpinner = new JSpinner();
        collectionFreqSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1000), Integer.valueOf(1), null, Integer.valueOf(10)));
        collectionFreqSpinner.setPreferredSize(new Dimension(100, 24));
        collectionFreqSpinner.setMaximumSize(new Dimension(130, 34));
        collectionFreqSpinner.setToolTipText("How often new data is collected");
        runLengthPanel.add(new JLabel("Data Sample Frequency :"));
        runLengthPanel.add(collectionFreqSpinner);
        

        
        runLengthPanel.setOpaque(false);
        this.add(runLengthPanel);
		
        JPanel burninPanel = new JPanel();
        burninPanel.setLayout(new BoxLayout(burninPanel, BoxLayout.X_AXIS));
        burninPanel.add(Box.createHorizontalStrut(81));
        burninPanel.add(new JLabel("Burn-in period :"));
        burninSpinner = new JSpinner();
        burninSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10000), Integer.valueOf(0), null, Integer.valueOf(100)));
        burninSpinner.setPreferredSize(new Dimension(100, 24));
        burninSpinner.setMaximumSize(new Dimension(130, 34));
        burninPanel.add(burninSpinner);
        burninPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        burninPanel.add(Box.createGlue());
        burninPanel.setMaximumSize(new Dimension(10000, 28));
        burninPanel.setOpaque(false);
        
        burninPanel.add(new JLabel("Random seed :"));
        randomSeedField = new JTextField();
        Integer seed = (int)((new Date()).getTime());
        randomSeedField.setText(String.valueOf(seed));
        burninPanel.add(randomSeedField);
        randomSeedField.setPreferredSize(new Dimension(120, 34));
        randomSeedField.setMaximumSize(new Dimension(150, 34));
        randomSeedField.setHorizontalAlignment(JTextField.RIGHT);
        randomSeedField.setToolTipText("Set the seed value for the random number generator");
        newSeedButton = new JButton("New");
        newSeedButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int date = (int)(new Date()).getTime();
				int val = (int)(Math.random()*date);
				int newVal = (int)val;
				if (newVal<0)
					newVal *= -1;
				randomSeedField.setText(String.valueOf(newVal));	
			}
        });
        burninPanel.add(newSeedButton);
        this.add(Box.createVerticalStrut(4));
        this.add(burninPanel);
        
        JPanel repeatPanel = new JPanel();
        repeatPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        repeatPanel.setOpaque(false);
        repeatSpinner = new JSpinner();
        repeatSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        repeatSpinner.setPreferredSize(new Dimension(100, 24));
        repeatSpinner.setMaximumSize(new Dimension(130, 34));
        repeatSpinner.setToolTipText("Repeat the entire run this number of times");
        repeatPanel.add(new JLabel("Number of repeats :"));
        repeatPanel.add(repeatSpinner);
        this.add(repeatPanel);
        this.add(Box.createVerticalStrut(25));

      
        JPanel baseDirPanel = new JPanel();
        baseDirPanel.setOpaque(false);
        baseDirPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        baseDirPanel.add(new JLabel("Output directory :"));
        baseDirField = new JTextField();
        baseDirField.setPreferredSize(new Dimension(250, 30));
        baseDirField.setHorizontalAlignment(JTextField.RIGHT);
        baseDirField.setMinimumSize(new Dimension(200, 1));
        String outputDir = tj.getProperty("output.dir");
        if (outputDir==null) {
        	String thisDir = System.getProperty("user.dir");
        	if (thisDir != null) {
        		baseDirField.setText(thisDir);
        	}
        }
        else {
        	baseDirField.setText(outputDir);
        }
        baseDirField.setToolTipText("Set the directory for file output");
        baseDirPanel.add(baseDirField);
        chooseBaseDirButton = new JButton("Choose");
        
        chooseBaseDirButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fileChooser.showSaveDialog(getParent());
				if (retval==JFileChooser.APPROVE_OPTION) {
					baseDirField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
        });
        baseDirPanel.add(chooseBaseDirButton);
        this.add(baseDirPanel);
        	
        
        
        RoundedBorderPanel writeFilePanel = new RoundedBorderPanel();
        writeFilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        writeToFileBox = new JCheckBox();
        writeToFileBox.setText("Write data log");
        writeToFileBox.setToolTipText("Periodically write values of the data collectors to a file");
        writeFilePanel.setBorderComponent(writeToFileBox);
        writeToFileBox.setSelected(true);
        outputFileField = new JTextField();
        outputFileField.setText("treesimj_output.log");
        outputFileField.setMinimumSize(new Dimension(150, 1));
        outputFileField.setPreferredSize(new Dimension(150, 24));
        outputFileField.setHorizontalAlignment(JTextField.RIGHT);
        writeFilePanel.add(new JLabel("Data log file name :"));
        writeFilePanel.add(outputFileField);  

        writeFilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
		writeToStdoutBox = new JCheckBox();
        writeToStdoutBox.setText("Write log to std. out");
        writeToStdoutBox.setSelected(true);
        writeToStdoutBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        writeFilePanel.add(writeToStdoutBox);
        writeFilePanel.setOpaque(false);
        this.add(writeFilePanel);
        
        
        RoundedBorderPanel writeSummaryPanel = new RoundedBorderPanel();
        writeSummaryPanel.setOpaque(false);
        writeSummaryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        writeSummaryBox = new JCheckBox();
        writeSummaryBox.setText("Write summary");
        writeSummaryBox.setToolTipText("Write a final summary of all data collectors to a file.");
        writeSummaryBox.setSelected(true);
        writeSummaryPanel.setBorderComponent(writeSummaryBox);
        writeSummaryField = new JTextField();
        writeSummaryPanel.add(new JLabel("Summary file name :"));
        writeSummaryField.setText("treesimj_summary.txt");
        writeSummaryField.setMinimumSize(new Dimension(150, 1));
        writeSummaryField.setPreferredSize(new Dimension(150, 24));
        writeSummaryField.setHorizontalAlignment(JTextField.RIGHT);
        writeSummaryPanel.add(writeSummaryField);    
        writeSummaryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(writeSummaryPanel);
        
        
        
        RoundedBorderPanel fastaPanel = new RoundedBorderPanel();
        fastaBox = new JCheckBox();
        fastaBox.setText("Save DNA");
        fastaBox.setToolTipText("Periodically write sampled DNA sequences to a file or files. Sample size is set by the Tree Size selector in the Data Collectors panel");
        fastaBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				fastaBoxStateChanged();
			}
        });
        fastaPanel.setBorderComponent(fastaBox);
        fastaPanel.setOpaque(false);
        fastaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        fastaPanel.add(new JLabel("File stem :"));
        fastaFileStemField = new JTextField("tj_dna");
        fastaFileStemField.setPreferredSize(new Dimension(75, 20));
        fastaFileStemField.setToolTipText("File name prefix for fasta file writing");
        fastaFileStemField.setEnabled(fastaBox.isSelected());
        fastaFileStemField.setMinimumSize(new Dimension(100, 1));
        fastaFileStemField.setPreferredSize(new Dimension(100, 24));
        fastaFileStemField.setHorizontalAlignment(JTextField.RIGHT);
        fastaPanel.add(fastaFileStemField);
        fastaPanel.add(new JLabel("Sample Frequency :"));
        fastaFrequencySpinner = new JSpinner();
        fastaFrequencySpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5000), Integer.valueOf(1), null, Integer.valueOf(100)));
        fastaFrequencySpinner.setPreferredSize(new Dimension(80, 24));
        fastaFrequencySpinner.setMaximumSize(new Dimension(130, 34));
        fastaFrequencySpinner.setEnabled(fastaBox.isSelected());
        fastaPanel.add(fastaFrequencySpinner);
        writeTreesTooBox = new JCheckBox("Write trees too");
        writeTreesTooBox.setEnabled(fastaBox.isSelected());
        writeTreesTooBox.setToolTipText("Write the tree for each DNA sequence to an additional file");
        fastaPanel.add(writeTreesTooBox);
//        separateFastasBox = new JCheckBox();
//        separateFastasBox.setText("Separate files");
//        separateFastasBox.setToolTipText("Write data for each sampling period to a separate file");
//        separateFastasBox.setEnabled(fastaBox.isSelected());
//        fastaPanel.add(separateFastasBox);
        this.add(fastaPanel);
  
        RoundedBorderPanel treesPanel = new RoundedBorderPanel();
        treesPanel.setOpaque(false);
        treesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        saveTreesBox = new JCheckBox();
        saveTreesBox.setText("Write tree log");
        saveTreesBox.setToolTipText("Save sampled trees written as newick strings to a file");
        saveTreesBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				treesBoxStateChanged();
			}
        });
        treesPanel.setBorderComponent(saveTreesBox);
        treesField = new JTextField("treesimj_trees.trees");
        treesField.setEnabled(false);
        treesField.setMinimumSize(new Dimension(130, 1));
        treesField.setPreferredSize(new Dimension(130, 24));
        treesField.setHorizontalAlignment(JTextField.RIGHT);
        treesPanel.add(new JLabel("Tree log file name :"));
        treesPanel.add(treesField);   
        this.add(treesPanel);

        this.add(Box.createVerticalGlue());
		
	}
	
	protected void treesBoxStateChanged() {
		if (saveTreesBox.isSelected()) {
			treesField.setEnabled(true);
		}
		else {
			treesField.setEnabled(false);
		}
	}

	/**
	 * Handles writing of all various run settings options to the xml file
	 * @author brendan
	 *
	 */
	class XMLSettingsWriter extends XMLParseable {

		public static final String XML_RUNLENGTH = "run.length";
		public static final String XML_BURNIN = "burnin";
		public static final String XML_WRITESTDOUT = "write.stdout";
		public static final String XML_WRITELOG = "write.log";
		public static final String XML_WRITESUMMARY = "write.summary";
		public static final String XML_FASTAFREQ = "fasta.frequency";
		public static final String XML_WRITEFASTA = "write.fasta";
		//public static final String XML_FASTASEP = "fasta.separate";
		public static final String XML_WRITETREESTOO = "write.trees.too";
		public static final String XML_WRITETREELOG = "write.treelog";
		public static final String XML_LOGFILENAME = "log.filename";
		public static final String XML_SUMMARYFILENAME = "summary.filename";
		public static final String XML_FASTASTEM = "fasta.stem";
		public static final String XML_TREESFILENAME = "trees.filename";
		public static final String XML_BASEDIR = "base.dir";
		public static final String XML_DATAFREQ = "data.sample.freq";
		public static final String XML_RANDOMSEED = "random.seed";
		public static final String XML_REPEATS = "repeats";
		
		public XMLSettingsWriter(RunSettingsOptions ops) {
			super(TJXMLConstants.SETTINGS);
			
			addXMLAttr(XML_RUNLENGTH, String.valueOf(ops.runLength));
			addXMLAttr(XML_BURNIN, String.valueOf(ops.burnin));
			addXMLAttr(XML_WRITESTDOUT, String.valueOf(ops.writeToStdout));
			addXMLAttr(XML_WRITELOG, String.valueOf(ops.writeLog ));
			addXMLAttr(XML_WRITESUMMARY, String.valueOf(ops.writeSummary ));
			addXMLAttr(XML_WRITEFASTA, String.valueOf(ops.writeFasta));
			//addXMLAttr(XML_FASTASEP, String.valueOf(ops.writeSeparateFasta));
			addXMLAttr(XML_WRITETREELOG, String.valueOf(ops.writeTreeLog));
			addXMLAttr(XML_WRITETREESTOO, String.valueOf(ops.writeTreesToo));
			addXMLAttr(XML_LOGFILENAME, String.valueOf(ops.logFilename));
			addXMLAttr(XML_SUMMARYFILENAME, String.valueOf(ops.summaryFilename));
			addXMLAttr(XML_FASTASTEM, String.valueOf(ops.fastaStem));
			addXMLAttr(XML_TREESFILENAME, String.valueOf(ops.treeFilename));
			addXMLAttr(XML_FASTAFREQ, String.valueOf(ops.fastaFreq));
			addXMLAttr(XML_BASEDIR, String.valueOf(ops.baseDir));
			addXMLAttr(XML_DATAFREQ, String.valueOf(ops.dataSampleFreq));
			addXMLAttr(XML_RANDOMSEED, String.valueOf(ops.randomSeed));
			addXMLAttr(XML_REPEATS, String.valueOf(ops.repeats));
		}


		
		
		//Doesn't do anything
		public Object readXMLBlock(XMLStreamReader reader)
				throws XMLStreamException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
	
	/**
	 * Encapsulates all various run settings options
	 * @author brendan
	 *
	 */
	class RunSettingsOptions {
		public int runLength;
		public int burnin;
		public boolean writeToStdout;
		public boolean writeLog;
		public boolean writeSummary;
		public boolean writeFasta;
		//public boolean writeSeparateFasta;
		public boolean writeTreeLog;
		public boolean writeTreesToo;
		public int fastaFreq;
		public String logFilename;
		public String summaryFilename;
		public String fastaStem;
		public String treeFilename;
		public String baseDir;
		public int dataSampleFreq;
		public int randomSeed;
		public int repeats;
		
		public RunSettingsOptions(int runLength,
								  int burnin,
								  boolean writeStdout,
								  boolean writeLog,
								  boolean writeSummary,
								  boolean writeFasta,
								 // boolean writeSeparateFasta,
								  boolean writeTreesToo,
								  int fastaSampleFreq,
								  boolean writeTrees,
								  String logFilename,
								  String summaryFilename,
								  String fastaStem,
								  String treeFilename,
								  String baseDirName,
								  int dataSampleFreq,
								  int randomSeed,
								  int repeats) {
			this.runLength = runLength;
			this.burnin = burnin;
			this.writeToStdout = writeStdout;
			this.writeLog = writeLog;
			this.writeSummary = writeSummary;
			this.writeFasta = writeFasta;
			this.writeTreesToo = writeTreesToo;
			this.writeTreeLog = writeTrees;
			this.logFilename = logFilename;
			this.summaryFilename = summaryFilename;
			this.fastaStem = fastaStem;
			this.treeFilename = treeFilename;
			this.fastaFreq = fastaSampleFreq;
			this.baseDir = baseDirName;
			this.dataSampleFreq = dataSampleFreq;
			this.randomSeed = randomSeed;
			this.repeats = repeats;
		}
		
	}

	
	JCheckBox fastaBox;
	//JCheckBox separateFastasBox;
	JCheckBox writeTreesTooBox;
	JSpinner fastaFrequencySpinner;
	JTextField fastaFileStemField;
	JSpinner collectionFreqSpinner;
	JTextField baseDirField;
	JButton chooseLogfileButton;
	JButton chooseSummaryFileButton;
	JButton chooseTreelogButton;
	JButton chooseFastaDirectoryButton;
	JButton chooseBaseDirButton;
	JTextField randomSeedField;
	JButton newSeedButton;
	
	JSpinner repeatSpinner;
	
	JCheckBox saveTreesBox;
	JTextField treesField;
    JSpinner runlengthSpinner;
    JSpinner burninSpinner;
    JTextField writeSummaryField;
    JTextField outputFileField;
    JCheckBox writeToStdoutBox;
    JCheckBox writeToFileBox;
    JCheckBox writeSummaryBox;
    JFileChooser fileChooser;
}
