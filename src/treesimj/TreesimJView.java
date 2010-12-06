/*
 * TreesimJView.java
 */

package treesimj;


import errorHandling.ErrorWindow;
import fitnessProviders.DNAFitness;
import fitnessProviders.FitnessProvider;
import gui.DataCollectorsPanel;
import gui.DemographicModelPanel;
import gui.FitnessModelPanel;
import gui.OutputManager;
import gui.RunSettingsPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import population.Locus;
import population.Population;
import population.PopulationRunner;

import statistics.Statistic;
import statistics.StatisticRegistry;
import statistics.dna.DNAStatistic;
import xml.TJXMLConstants;
import xml.TJXMLException;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import demographicModel.DemographicModel;
import dnaModels.DNASequence;

/**
 * The application's main frame. Creates a few buttons, the main tabbedpane, and 
 * a small status label at the bottom. 
 *  
 */
public class TreesimJView extends JFrame implements DoneListener, ProgressListener {
	
	public static final boolean DEBUG = false; //Turn on a debug mode, currently all this does is allow the simulation to
											   //be run in the foreground, in which case all exceptions are caught in
											   //a reasonable manner (normally the sim executes in a background thread in
											   //exceptions are lost in the ether, and just cause the thread to die)
	
	PopulationRunner runner; //The object resposible for actually running population	

	RunSettingsPanel runSettingsPanel;
	private DemographicModelPanel demoModelPanel;
	
	Properties props; //Some basic properties, may be null if we can't find the right file
		
	//Where we store the settings from the last run
	private static final String settingsFromLastRun = ".tj_lastrunsettings.xml";

	//A flag that indicates the appropriate action when the Run Simulation button is pressed
	private boolean startFromLoadedPopulation = false;

	
    public TreesimJView(Properties props) {
        super("TreesimJ");
        this.props = props;
        
        //Set the look and feel to the system version and attempt to avoid metal at all costs
        try {
        	String plaf = UIManager.getSystemLookAndFeelClassName();
        	String gtkLookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        	//Attempt to avoid metal look and feel if possible
        	if (plaf.contains("metal")) {
        		UIManager.setLookAndFeel(gtkLookAndFeel);
        	}

        	UIManager.setLookAndFeel( plaf );
        }
        catch (Exception e) {
            System.err.println("Error initializing system look and feel : " + e.toString());
        }
        
        
        //A couple of mac-specific changes
        String lcOSName = System.getProperty("os.name").toLowerCase();
        boolean isMac = lcOSName.startsWith("mac os x");
        if (isMac) {
        	System.setProperty("apple.laf.useScreenMenuBar", "true");
        	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "treesimJ");
        }
        
        initComponents();
        fileChooser = new JFileChooser();
        
        initPanels(); //Construct the various elements of the main JTabbedPane
        pack();
        setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //We handling closing ourselves so we can be sure to write the properties and do other shutdown stuff
        this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				TreesimJApp.getApplication().shutdown();
			}
		}); 
    }

    
    /**
     * Asks the user if we should reload settings from the last run. Normally, when a new simulation is initiated, settings
     * are saved in a (hidden) file, and a property is set with a value corresponding to the file name. This checks
     * to see if the property is set and then attempts to load the settings from the specified file.
     */
    public void askAboutLastRunSettings() {
        String lastRunSettingsPath = props.getProperty("last.run.settings");
        if (lastRunSettingsPath!=null) {
        	String[] ops = {"Reload settings", "Don't load" };
        	int choice = JOptionPane.showOptionDialog(this, "Reload settings from last run?", "Reload settings", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, ops, ops[0]);
        	
        	if (choice == 0) {
        		try {
        			File settingsFile = new File(lastRunSettingsPath);
        			importSettingsFromFile(settingsFile, false);
        		}
        		catch (Exception ex) {
        			System.err.println("Error loading settings from last run: " + ex);
        		}
        	}
        }
    }
    
    /**
     * Access to the Properties list
     * @param key
     * @return The value associated with the given key in the properties list, or null if no key is found
     */
    public String getProperty(String key) {
    	return props.getProperty(key);
    }
   
    
    /**
     * Store a key-value pair in the list of Properties, these will be written to the Properties file when 
     * the application is shut down
     * @param key
     * @param value
     */
    public void storeProperty(String key, String value) {
    	props.setProperty(key, value);
    }
    

	private void initPanels() {
    	initFitnessPanel();
		initDemographicPanel();
		initDataCollectorPanel();
		initRunSettingsPanel();
	}

	private void initRunSettingsPanel() {
		runSettingsPanel = new RunSettingsPanel(this);
		runSettingsPanel.setOpaque(false);
		mainTabbedPane.addTab("Run Settings", runSettingsPanel);
	}

	private void initDataCollectorPanel() {
		dataCollectorsMainPanel = new DataCollectorsPanel(this);
		dataCollectorsMainPanel.setOpaque(false);
		mainTabbedPane.addTab("Data collection", dataCollectorsMainPanel);
	}

	private void initDemographicPanel() {
		demoModelPanel = new DemographicModelPanel(this);
		demoModelPanel.setOpaque(false);
		mainTabbedPane.addTab("Demographic Model", demoModelPanel);
	}

	private void initFitnessPanel() {
		fitnessModelPanel = new FitnessModelPanel(this);
		fitnessModelPanel.setOpaque(false);
		mainTabbedPane.addTab("Fitness Model", fitnessModelPanel);
	}


	/**
	 * Create an actual Population using the specified fitness provider, demographic model, and random engine.
	 * @param rng
	 * @param demoModel
	 * @param fitnessModel
	 * @return
	 */
	private List<Population> constructPopulation(RandomEngine rng, FitnessProvider fitnessModel, boolean preserveAncestralData) {
		Population pop = new Population(rng, 1, preserveAncestralData, fitnessModel);
		List<Population> pops = new ArrayList<Population>();
		pops.add(pop);
		return pops;
	}

	/**
	 * Construct an outputManager using the current settings in runSettingsPanel and the provided
	 * list of statistics.
	 * @param stats
	 * @return A new OutputManager object that has reflects the currents settings
	 */
	private OutputManager constructOutputManager(List<Statistic> stats, DemographicModel demoModel) {
		OutputManager outputHandler = new OutputManager(demoModel);
		for(Statistic stat : stats) 
			outputHandler.addStatistic(stat);

		if (runSettingsPanel.writeToStdout()) {
			outputHandler.addStream(new SerializablePrintStream(System.out));
		}

		if (runSettingsPanel.writeToFile()) {
			String filename = runSettingsPanel.getLogFileName();
			File outputFile = null;
			if (filename==null || filename.trim().length()==0) {
				//Popup a file chooser
				int retVal = fileChooser.showSaveDialog(this);

				if (retVal == JFileChooser.APPROVE_OPTION) {
					outputFile = fileChooser.getSelectedFile();
				}
				else {
					System.out.println("Not adding any file to output list.");
					outputFile = null;
				}

			}
			else {
				outputFile = new File(filename);
			}
			
			if (outputFile != null) {
				try {
					SerializablePrintStream filePS = new SerializablePrintStream(outputFile);
					outputHandler.addStream(filePS);
				} catch (FileNotFoundException e) {

					JOptionPane.showMessageDialog(this,
							"Could not open file " + outputFile.getAbsolutePath() + " for writing.",
							"Output option error",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		}// if write log to file

		if (runSettingsPanel.writeSummary()) {
			String filename = runSettingsPanel.getSummaryFileName();
			File outputFile = null;
			if (filename==null || filename.trim().length()==0) {
				//Popup a file chooser
				int retVal = fileChooser.showSaveDialog(this);

				if (retVal == JFileChooser.APPROVE_OPTION) {
					outputFile = fileChooser.getSelectedFile();


				}
				else {
					System.out.println("Not adding any file to output list.");
					outputFile = null;
				}
			}
			else {
				outputFile = new File(filename);
				
				try {
					SerializablePrintStream filePS = new SerializablePrintStream(outputFile);
					outputHandler.setSummaryStream(filePS);
					
				} catch (FileNotFoundException e) {

					JOptionPane.showMessageDialog(this,
							"Could not open file " + outputFile.getAbsolutePath() + " for writing.",
							"Output option error",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		
		
		if (runSettingsPanel.getSaveTrees()) {
			String filename = runSettingsPanel.getTreesFileName();
			File outputFile = null;
			
			outputFile = new File(filename);
			
			try {
				SerializablePrintStream filePS = new SerializablePrintStream(outputFile);
				outputHandler.setTreesStream(filePS);
			} catch (FileNotFoundException e) {

				JOptionPane.showMessageDialog(this,
						"Could not open trees file " + outputFile.getAbsolutePath() + " for writing.",
						"Output option error",
						JOptionPane.WARNING_MESSAGE);
			}
			
		}
		
		if (runSettingsPanel.getWriteFasta()) {
			if (runSettingsPanel.getWriteSeparateFastas()) {
				outputHandler.setWriteSeparateFastas(runSettingsPanel.getFastaFileStem());
				outputHandler.setWriteTreesWithFasta(runSettingsPanel.getWriteTreesWithFasta());
			}
			else {
				outputHandler.setWriteSingleFasta(runSettingsPanel.getFastaFileStem());
			}
		}

		return outputHandler;
	}

	/**
	 * Set the simulation to 'pause' mode, which means that it can be restarted when the user clicks on the Run button
	 */
	protected void pauseSimulation() {
		pauseButton.setEnabled(false);
		runButton.setEnabled(true);
		statusLabel.setText("Paused");
		runner.pause();
	}
	
	/**
	 * Stop the simulation, remove the progress bar, and change the enabled state of the toolbar buttons as appropriate
	 */
	protected void stopSimulation() {
		pauseButton.setEnabled(false);
		runButton.setEnabled(true);
		stopButton.setEnabled(false);
		statusLabel.setText("Stopped");
		progressBar.setValue(0);
		progressBar.setVisible(false);
		runner.stop();
	}

	/**
	 * Starts the PopulationRunner using the current values of the run settings, but the passed in values for the
	 * other components. 
	 * 
	 * @param runInForeground
	 * @param pop
	 * @param demoModel
	 * @param outputHandler
	 * @param stats
	 */
	private void startRunner(boolean runInForeground) {
		pauseButton.setEnabled(true);
		runButton.setEnabled(false);
		stopButton.setEnabled(true);
		statusLabel.setText("Running");
		progressBar.setVisible(true);
		
		int burninGens = runSettingsPanel.getBurninGens();
		int totalLength = runSettingsPanel.getRunLength();
		int treeSampleSize = dataCollectorsMainPanel.getTreeSampleSize();
		int dataCollectionFreq = runSettingsPanel.getDataSampleFreq();
		int dnaSampleFreq = runSettingsPanel.getFastaSampleFrequency();
		
		//If the user has not elected to write fasta / tree files, we just set dnaSampleFreq to a huge value
		if (runSettingsPanel.getWriteFasta()==false) 
			dnaSampleFreq = Integer.MAX_VALUE;
		
		runner.setTreeSampleSize(treeSampleSize);
		
		if (runInForeground)
			runner.run(burninGens, totalLength, dataCollectionFreq, dnaSampleFreq, runSettingsPanel.getSaveTrees());
		else
			runner.begin(burninGens, totalLength, dataCollectionFreq, dnaSampleFreq, runSettingsPanel.getSaveTrees());
		
		//Save these run settings to a file so they can be loaded again next time we
		//restart the application
		try {
			File lastRunSettingsFile = new File(settingsFromLastRun);
			FitnessProvider fitnessModel = runner.getDemographicModel().getPop(0).getFitnessModel();
			fitnessModel.addXMLAttributes();
			writeSettingsToFile(lastRunSettingsFile, fitnessModel, runner.getDemographicModel(), runner.getStatisticsList(), false);
			props.setProperty("last.run.settings", settingsFromLastRun);
		}
		catch (Exception ex) {
			//Hmm.. we probably don't care about this not working 
		}
	}
	
	private ArrayList<Statistic> constructStatisticList(RandomEngine rng, FitnessProvider fitnessModel) {
		boolean hasDNAStats = false;
		DNASequence master = null;
		if ( fitnessModel instanceof DNAFitness) {
			master = ((DNAFitness)fitnessModel).getMaster();
		}
		int dnaSampleSize = dataCollectorsMainPanel.getDNASampleSize();
		ArrayList<Statistic> stats = new ArrayList<Statistic>();

		for(Statistic stat : dataCollectorsMainPanel.getActiveCollectors()) {
			try {
				if (stat instanceof DNAStatistic) {
					hasDNAStats = true;
					if (master==null) {
						throw new ClassCastException("No DNA to make a DNA statistic");
					}
					else {
						((DNAStatistic)stat).setMaster(master);
					}
					
					((DNAStatistic)stat).setSampleSize(dnaSampleSize);
				}
				
				stat.setRandomEngine(rng);
				stats.add(stat);
			}
			catch (NullPointerException npe) {
				System.err.println("Could not add statistic " + stat.getIdentifier() + " : Null pointer exception");
			}
			catch (ClassCastException cce) {
				System.err.println("Could not add statistic " + stat.getIdentifier() + " : class cast exception");
			}
			catch (Exception ex) {
				System.err.println("Could not add statistic " + stat.getIdentifier() + " : Other exception : " + ex);
			}
		}
		
		//Warn the user if some DNA statistics were selected, but there's no DNA in the model
		//This will break if at some point we want to be able to attach DNA to non-dna fitness model
		if ( (! (fitnessModel instanceof DNAFitness)) && hasDNAStats) {
			
			JOptionPane.showMessageDialog(this,
					"Some statistics requiring DNA were selected, but a non-DNA fitness model was chosen. The statistics will be ignored.",
					"Non-DNA model chosen",
					JOptionPane.WARNING_MESSAGE);
		}
		
		return stats;
	}
	
	/**
	 * This begins a new simulation using the settings currently stored in the GUI. It does so by constructing the demographic and 
	 * fitness models and the actual Statistics (aka Data Collectors) used. There's a bit of special case code in here right now
	 * to handle the fact that not all statistics can be used all of the time (some require a DNA fitness model, for instance)
	 * At some point it would be nice to clean this up so that each Statistic knows, in some general sense, the conditions 
	 * which must be satisfied for its use.
	 * 
	 * @param run the simulation in the foreground, useful for debugging purposes. 
	 * 
	 */
	public void beginSimulationFromGUI(boolean runInForeground) {

		if (startFromLoadedPopulation) { //This flag gets set if we're starting from a stored simulation state
			//When starting from a stored state, we can elect to have different data collectors and run options, or use the
			//same as from the stored state. We ask the user what they'd like to do here, 
//			Object[] options = {"Saved options",
//			"New options"};
//			int n = JOptionPane.showOptionDialog(ths,
//					"Would you like to use the same run settings and data collectors as the saved population, or use the currently selected options?",
//					"Load new options",
//					JOptionPane.YES_NO_OPTION,
//					JOptionPane.QUESTION_MESSAGE,
//					null,
//					options,
//					options[0]);

			//If the user wants to use the new options, then we constuct a new stat list and new output handler and set the runner to
			//use these. Otherwise, the runner is already initialized with the previous values, so we do nothing. 
//			if (true) {
//				int rngSeed = runSettingsPanel.getRandomSeed();
//				RandomEngine rng = new MersenneTwister( rngSeed );
//
//				ArrayList<Statistic> stats = constructStatisticList(rng, runner.getPopulation().getFitnessModel());
//				runner.getPopulation().setCurrentGenNumber(0);
//				runner.getPopulation().setRandomEngine(rng);
//				for(Individual ind : runner.getPopulation().getList()) {
//					ind.getFitnessData().setRandomEngine(rng);
//				}
//				OutputManager outputHandler = constructOutputManager(stats, runner.getPopulation());
//				outputHandler.constructSummaryHeader(runner.getPopulation().getFitnessModel(), runner.getDemographicModel(), runSettingsPanel);
//				
//				
//				runner.setOutputHandler(outputHandler);
//			}
//			
//			startRunner(runInForeground);
//			startFromLoadedPopulation = false;
			return;
		}
		
		if (runner!=null && runner.isPaused()) {
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			runButton.setEnabled(false);
			statusLabel.setText("Running");
			runner.resume();
		}
		else {
			
			int rngSeed = runSettingsPanel.getRandomSeed();
			RandomEngine rng = new MersenneTwister( rngSeed );
			
			
			Population.resetTotalPopCount();
			boolean preserveAncestralDNAData = false; 
			
			
			FitnessProvider fitnessModel = fitnessModelPanel.constructFitnessModel(rng);
			
			ArrayList<Statistic> stats = constructStatisticList(rng, fitnessModel);

			for(Statistic stat : stats) 
				preserveAncestralDNAData = preserveAncestralDNAData || stat.requiresPreserveAncestralData();
		
			//If we wanted to run multiple repeats, we could do it here
			//but we'd have to work out some way of ensuring that there's only X number of population runners going at once...
			//int repeats = runSettingsPanel.getRepeats();
			
			DemographicModel demoModel = demoModelPanel.constructDemographicModel();
			demoModel.setRng(rng);
			demoModel.initializePopulations(rng, fitnessModel);

			for(Population pop : demoModel.getPopList()) {
				pop.setPreserveData(preserveAncestralDNAData);
			}

			OutputManager outputHandler = constructOutputManager(stats, demoModel);
			//outputHandler.setRepeatNumber(i);

			runner = new PopulationRunner(demoModel, outputHandler);
			runner.addProgressListener(this);	//We'd need know the progress of the simulation to update the progress bar

			outputHandler.constructSummaryHeader(fitnessModel, runner.getDemographicModel(), runSettingsPanel);			
			startRunner(runInForeground);
			
		}
	}

	
	/**
	 * Aborts the current running simulation (if there is one) and attempts to load the a new simulation state file
	 */
//	protected void loadPopulationState() {
//		int val = fileChooser.showOpenDialog(ths);
//
//		if (val == JFileChooser.CANCEL_OPTION || val==JFileChooser.ERROR_OPTION) {
//			return;
//		}
//		
//		File stateFile = fileChooser.getSelectedFile();
//		try {
//			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(stateFile));
//			Object obj = inputStream.readObject();
//			SimulationState state = (SimulationState)obj;
//			Population pop = state.getPopulation();
//			String settingsStr = state.getSettingsString();
//
//			if (runner!=null && runner.isRunning()) {
//				String[] ops = {"Load new run", "Cancel"};
//				int choice = JOptionPane.showOptionDialog(ths, "Abort current simulation run?", "Simulation running", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, ops, ops[0]);
//				if (choice == 1) {
//					return;
//				}
//				stopSimulation();
//			}
//			
//			runButton.setEnabled(true);
//			pauseButton.setEnabled(false);
//			stopButton.setEnabled(false);
//
//			loadSettingsFromString(settingsStr);
//			OutputManager outputHandler = state.getOutputManager();
//			runner = new PopulationRunner(pop, state.getDemoModel(), outputHandler);
//			runner.addProgressListener(this);	//We'd need know the progress of the simulation to update the progress bar
//
//			startFromLoadedPopulation = true; //Indicates that when the Run button is pressed we need to call startRunner again
//			statusLabel.setText("Loaded state from " + stateFile.getName());
//		}
//		catch (IOException ioex) {
//			System.out.println("Error reading file: " + ioex);
//			ErrorWindow.showErrorWindow(ioex);
//		}
//		catch (ClassCastException ccex) {
//			System.out.println("Could not cast object to SimulationState ");
//		} catch (ClassNotFoundException e) {
//			System.out.println("Class was not found..");
//			e.printStackTrace();
//		}
//		
//	}

	/**
	 * Loads settings into the GUI from the string provided. We just write this to a file and then call
	 * importSettingsFromFile
	 * @param settingsStr
	 */
	private void loadSettingsFromString(String settingsStr) {
		try {
			File tmpFile = new File(".tjtmpXMLsettingsfile");
			FileWriter writer = new FileWriter(tmpFile);
			writer.write(settingsStr);
			writer.close();
			importSettingsFromFile( tmpFile, false );
		}
		catch (IOException ioex) {
			System.err.println("Error reading settings");
		}
	}
	
	

	/**
	 * Save the entire population state to a potentially huge file. Currently not used in release code, but may be someday. Seems to work.
	 */
//	protected void savePopulationState() {
//		if (runner != null) {
//			
//			if (!runner.isPaused())
//				pauseSimulation();
//
//			int val = fileChooser.showSaveDialog(ths);
//
//			if (val == JFileChooser.CANCEL_OPTION || val==JFileChooser.ERROR_OPTION) {
//				return;
//			}
//
//			File file = fileChooser.getSelectedFile();
//			try    {
//				FileOutputStream fos = new FileOutputStream(file);
//				ObjectOutputStream out = new ObjectOutputStream(fos);
//				
//				String settings = getXMLSettingsString(runner);
//				
//				SimulationState state = new SimulationState(settings, runner.getOutputHandler(), runner.getDemographicModel(), runner.getStatisticsList());
//				
//				out.writeObject( state );
//				out.close();
//				statusLabel.setText("Saved state to " + file.getName());
//			}
//			catch(IOException ex)
//			{
//				System.err.println("Error saving file: " + ex);
//				ex.printStackTrace();
//			}
//			
//
//			beginSimulationFromGUI(false);
//		}
//
//	}
	
	
	/**
	 * Write the settings to the file and warn the user if something doesn't work.
	 * @param settingsFile
	 * @param fitnessModel
	 * @param demoModel
	 * @param stats
	 */
	private void writeSettingsToFile(File settingsFile, FitnessProvider fitnessModel, DemographicModel demoModel, ArrayList<Statistic> stats) {
		writeSettingsToFile(settingsFile, fitnessModel, demoModel, stats, true);
	}

	/**
	 * Writes all the settings provided in the argument list to a file. This could really just be a list of XMLParseable items....
	 * @param settingsFile
	 * @param fitnessModel
	 * @param demoModel
	 * @param stats
	 */
	private void writeSettingsToFile(File settingsFile, FitnessProvider fitnessModel, DemographicModel demoModel, ArrayList<Statistic> stats, boolean warnUser) {
		OutputStream out;
		try {

			out = new FileOutputStream(settingsFile);

			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);

			writer.writeStartDocument("1.0"); //TODO this may need to change for other platforms
			writer.writeCharacters("\n");
			writer.writeStartElement("treesimJ");
			writer.writeCharacters("\n");
			fitnessModel.writeXMLBlock(writer);
			writer.writeCharacters("\n");
			demoModel.writeXMLBlock(writer);
			writer.writeCharacters("\n");
			writer.writeStartElement(TJXMLConstants.STATISTICS);
			writer.writeAttribute(DataCollectorsPanel.XML_TREESAMPLESIZE, String.valueOf(dataCollectorsMainPanel.getTreeSampleSize()));
			writer.writeAttribute(DataCollectorsPanel.XML_DNASAMPLESIZE, String.valueOf(dataCollectorsMainPanel.getDNASampleSize()));
			writer.writeCharacters("\n");
			for(Statistic stat : stats) {
				stat.writeXMLBlock(writer, "\t");
				writer.writeCharacters("\n");
			}
			writer.writeEndElement();
			writer.writeCharacters("\n");
			runSettingsPanel.writeToXML(writer);
			writer.writeCharacters("\n");
			writer.writeEndDocument();

			writer.flush();
			writer.close();
			out.close();
			if (warnUser)
				statusLabel.setText("Wrote settings to : " + settingsFile.getName());
		} catch (Exception ex) {
			if (warnUser) {
				JOptionPane.showMessageDialog(this, "There was an error saving the settings file (" + ex.getMessage() + ")", "Error writing file", JOptionPane.WARNING_MESSAGE, null);
				statusLabel.setText("Error writing settings to : " + settingsFile.getName());
			}
			ErrorWindow.showErrorWindow(ex, "An error occurred while writing the settings file, and it may not be possible to save settings.");
		}
	}
    
	/**
	 * Import settings from a file that the user selects
	 */
	public void importSettings() {
		int op = fileChooser.showOpenDialog(this);
		
		if (op == JFileChooser.APPROVE_OPTION) {
			File inputFile = fileChooser.getSelectedFile();
			importSettingsFromFile(inputFile, true);
		}
	}
	
	
    /**
     * Called upon application shutdown to write additional resources 
     */
    public void writeProperties() {
    	if (props == null) {
    		System.err.println("Could not write properties file, properties were never initialized.");
    		return;
    	}
    	
    	//First see if we the location of the user properties file is in
    	//the props file itself
    	String propsPath = props.getProperty("props.path");
//    	String fileSep = System.getProperty("file.separator");
//    	String propsFilename = props.getProperty("props.filename");
    	//props.setProperty("iconPath", iconPath);
    	
    	//We also store the location of the base output directory
    	props.setProperty("output.dir", runSettingsPanel.getBaseDirName());
    	
    	int frameWidth = this.getWidth();
    	int frameHeight = this.getHeight();
    	props.setProperty("frame.width", String.valueOf(frameWidth));
    	props.setProperty("frame.height", String.valueOf(frameHeight));
    	
    	if (propsPath==null) {
    		propsPath = System.getProperty("user.dir");
    		props.setProperty("props.path", propsPath);
    	}
    	
    	String propsFullPath = propsPath; // + fileSep + propsFilename;
    	
    	try {
    		FileOutputStream propsStream = new FileOutputStream(propsFullPath);
    		props.store(propsStream, "--- TreesimJ Properties " + (new Date()) + " ----" );
    		propsStream.close();
    		System.out.println("Writing to properties file : " + propsFullPath);
    	}
    	catch (IOException ioe) {
    		System.out.println("Warning : error writing to properties file : " + ioe);
    		System.out.println("Properties may not be properly restored on next run.");
    	}
    }
	
	
	/**
	 * Read various settings from an XML file and use the info to set the options in the various
	 * configurators
	 * 
	 * @param inputFile A TreesimJ settings file
	 */
    protected void importSettingsFromFile(File inputFile, boolean updateLabel)  {

			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			try {
				InputStream inputStream = new FileInputStream(inputFile);
				XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream);
				
				int event = reader.next();
				
				while(reader.hasNext()) {

					if (event == XMLStreamConstants.START_DOCUMENT) {
						if (reader.getLocalName() == TJXMLConstants.TREESIMJ) {
						
						}
						else {
							JOptionPane.showMessageDialog(this,
									"File " + inputFile.getAbsolutePath() + " does not seem to be a treesimj input file",
									"File error",
									JOptionPane.WARNING_MESSAGE);
							return;
						}
					}
					
					if (event == XMLStreamConstants.START_ELEMENT) {
						if (reader.getLocalName().equals(TJXMLConstants.FITNESS_MODEL))
							fitnessModelPanel.readSettingsFromXML(reader);
						
						if (reader.getLocalName().equals(TJXMLConstants.DEMOGRAPHIC_MODEL))
							demoModelPanel.readSettingsFromXML(reader);
						
						if (reader.getLocalName().equals(TJXMLConstants.STATISTICS))
							dataCollectorsMainPanel.configureSettings(reader);
						
						if (reader.getLocalName().equals(TJXMLConstants.SETTINGS))
							runSettingsPanel.configureSettings(reader);
					}
					
					
					event = reader.next();
				}
				if (updateLabel)
					statusLabel.setText("Settings imported from " + inputFile.getName());
				
			}
			catch (FileNotFoundException fnf) {
				JOptionPane.showMessageDialog(this,
						"Could not open file " + inputFile.getAbsolutePath() + " for reading",
						"File error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			catch (XMLStreamException xmlx) {
				JOptionPane.showMessageDialog(this,
						"Error opening file " + inputFile.getAbsolutePath() + " for reading.",
						"Output option error",
						JOptionPane.WARNING_MESSAGE);
				return;
			} catch (TJXMLException e) {
				System.err.println("Error reading XML : " + e);
				e.printStackTrace();
			}
	}
    
    /**
     * Called when the 'Export settings' button is pressed. We have to construct all the models, then have them
     * write their settings to a file that the user chooses.
     */
	protected void writeSettings() {
		DemographicModel demoModel = demoModelPanel.constructDemographicModel();
		
		//Warning! Here we pass in a dummy RandomEngine so that the fitness models can be constructed..
		//but this isn't the RandomEngine that any fitness model ever uses. Will this break anything?
		FitnessProvider fitnessModel = fitnessModelPanel.constructFitnessModel(new MersenneTwister());

		//We have to build the list of statistics, but they're never used so we don't worry about
		//setting their various properties
		ArrayList<Statistic> stats = new ArrayList<Statistic>();
		for(Statistic stat : dataCollectorsMainPanel.getActiveCollectors()) {
				stats.add(stat);
		}
		
		
		int val = fileChooser.showSaveDialog(this);
		
		if (val == JFileChooser.CANCEL_OPTION || val==JFileChooser.ERROR_OPTION) {
			return;
		}
		
		File outputFile = fileChooser.getSelectedFile();
		if (!outputFile.getName().endsWith(".xml")) {
			String newName = outputFile.getName() + ".xml";
			System.out.println("Name was " + outputFile.getName() + " which doesn't end in .xml, so appending .xml to current name..");
			System.out.println("Path is : " + outputFile.getPath() + " new name is : " + newName);
			outputFile = new File(outputFile.getPath() + ".xml");
			System.out.println("New absolute path is : " + outputFile.getAbsolutePath() );
		}
		
		writeSettingsToFile(fileChooser.getSelectedFile(), fitnessModel, demoModel, stats);
	}


	
	
	public void setProgress(int prog, String str) {
		progressBar.setValue(prog);
		if (str != null) {
			progressBar.setStringPainted(true);
			progressBar.setString(str);
		}
		progressBar.setVisible(true);
	}
	

	/**
	 * Called when the simulatin is done running. Right now we just use this to set the button to their
	 * correct enabled() states
	 */
	public void done(Object source) {
		runButton.setEnabled(true);
		pauseButton.setEnabled(false);
		stopButton.setEnabled(false);
	}


	/**
	 * Returns an icon from the given URL, with a bit of exception handling. 
	 * @param url
	 * @return
	 */
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = TreesimJView.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			System.out.println("Error loadind icon from resouce : " + ex);
		}
		return icon;
	}
	
    /** 
     * Initialize all the various components of the main Frame. Building of the various 
     * fitness models, demo models, etc, is handled in gui.FitnessModelPanel, gui.DemographicModelPanel, etc. 
     */
    private void initComponents() {
        mainPanel = new javax.swing.JPanel();	//Everything is in this panel
        toolbar = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        JButton exportButton = new javax.swing.JButton();
        mainTabbedPane = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        mainPanel.setName("mainPanel"); // NOI18N

        toolbar.setName("jToolBar1"); // NOI18N
        
        runButton.setText("Run simulation"); // NOI18N
        runButton.setHorizontalTextPosition(0);
        runButton.setName("jButton1"); // NOI18N
        runButton.setToolTipText("Begin running the simulation with the current settings");
        ImageIcon runIcon = getIcon("icons/forward_24x24.png"); 
        runButton.setIcon(runIcon);
        runButton.setVerticalTextPosition(3);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	beginSimulationFromGUI(false);
            }
        });
        toolbar.add(runButton);
        
        if (DEBUG) {
        	JButton runForegroundButton = new JButton();
        	runForegroundButton.setText("Run Debug");

            runForegroundButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                	beginSimulationFromGUI(true);
                }
            });
            toolbar.add( runForegroundButton );
        }
        

        pauseButton.setText("Pause"); // NOI18N
        pauseButton.setToolTipText("Pause the simulation");
        pauseButton.setHorizontalTextPosition(0);
        pauseButton.setName("jButton2"); // NOI18N
        pauseButton.setVerticalTextPosition(3);
        ImageIcon pauseIcon = getIcon("icons/pause_24x24.png"); 
        pauseButton.setIcon(pauseIcon);
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	pauseSimulation();
            }
        });
        toolbar.add(pauseButton);
        
        stopButton = new JButton();
        stopButton.setText("Stop");
        stopButton.setToolTipText("Stop the simulation (cannot be restarted)");
        stopButton.setHorizontalTextPosition(0);
        stopButton.setVerticalTextPosition(3);
        stopButton.setEnabled(false);
        ImageIcon stopIcon = getIcon("icons/stop_24x24.png");  
        stopButton.setIcon(stopIcon);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	stopSimulation();
            }
        });
        toolbar.add(stopButton);

        importButton.setText("Load settings"); // NOI18N
        importButton.setHorizontalTextPosition(0);
        importButton.setName("jButton3"); // NOI18N
        ImageIcon importIcon = getIcon("icons/reload_24x24.png"); 
        importButton.setToolTipText("Import settings from a file");
        importButton.setIcon(importIcon);
        importButton.setVerticalTextPosition(3);
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	importSettings();
            }
        });
        toolbar.add(importButton);
        
        exportButton.setText("Save settings");
        ImageIcon exportIcon =  getIcon("icons/save_24x24.png");  
        exportButton.setToolTipText("Save the current settings to a file");
        exportButton.setIcon(exportIcon);
        exportButton.setHorizontalTextPosition(0);
        exportButton.setVerticalTextPosition(3);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	writeSettings();
            }
        });
        toolbar.add(exportButton);
        
//        JButton saveStateButton = new JButton("Save state");
//        saveStateButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				savePopulationState();
//			}
//        });
       // toolbar.add(saveStateButton); //Experimental function that loads the population state
        
//        JButton loadStateButton = new JButton("Load state");
//        loadStateButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				loadPopulationState();
//			}
//        });
       // toolbar.add(loadStateButton); //Experimental function that saves the population state

        
        mainTabbedPane.setName("jTabbedPane2"); // NOI18N

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
        
        
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText("File"); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

       
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        
        menuBar.add(fileMenu);

        helpMenu.setText("Help"); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

      
        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
        
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusPanel.setMinimumSize(new Dimension(10, 20));
        statusLabel = new JLabel("Ready");
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(6));
        statusPanel.add(progressBar);
        statusPanel.add(Box.createHorizontalStrut(20));
        progressBar.setVisible(false);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        this.getContentPane().add(mainPanel);
        this.setJMenuBar(menuBar);
        
        //Creates a mac applicationAdapter that listens for application exit events
        boolean isMacOS = System.getProperty("mrj.version") != null;
        if (isMacOS)  {
        	//This should only be created on mac systems
        	try {
        	  //MacQuitHandler macHandler = new MacQuitHandler(this);
        	}
        	catch (NoClassDefFoundError noClassError) {
        		System.out.println("There was an error creating the Mac OS application exit listener.\n This may affect properties file writing on macintosh systems. ");
        	}
        	catch (Exception ex) {
        		System.err.println("There was an error creating the Mac OS application exit listener.\n This may affect properties file writing on macintosh systems. ");
        	}
        }
        else {	//On non-macs we add a quit 
        	quitButton = new JMenuItem("Quit");
        	quitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TreesimJApp.getApplication().shutdown();
				}
        	});
        	
        	fileMenu.add(quitButton);
        }

    }


	FitnessModelPanel fitnessModelPanel;
    DataCollectorsPanel dataCollectorsMainPanel;
    JFileChooser fileChooser;
    JLabel statusLabel;
    
    private javax.swing.JMenuItem quitButton;
    private javax.swing.JButton runButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton importButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JPanel toolbar;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;

    private JDialog aboutBox;

}
