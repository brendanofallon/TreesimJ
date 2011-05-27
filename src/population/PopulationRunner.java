package population;

import errorHandling.ErrorWindow;
import fitnessProviders.DNAFitness;
import gui.OutputManager;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdesktop.swingworker.SwingWorker;


import siteModels.CodonFitnesses;
import siteModels.SiteFitnesses;
import statistics.Options;
import statistics.SerialTreeSampler;
import statistics.SimpleTreeSampler;
import statistics.Statistic;
import statistics.StatisticRegistry;
import statistics.TreeSampler;
import statistics.TreeStatistic;
import statistics.dna.DNAStatistic;
import statistics.treeShape.SerialPairwiseCTime;
import treesimj.ProgressListener;
import treesimj.TJRunTimeException;
import treesimj.TreesimJView;
import demographicModel.DemographicModel;
import dnaModels.DNASequence;



/**
 * Handles the running of the population and manages the SwingWorker that runs the population in the background. 
 * 
 * @author brendan
 *
 */
public class PopulationRunner {

	private DemographicModel demoModel;		//The demographic model to use
	private OutputManager outputHandler;	//The object which handles all output to files, screen, etc
	private boolean paused = false;			//Whether or not the runWorker is 'paused'
	private boolean running = false;	    //True if simulation is currently in progress, i.e. has not been 'stopped'
	private PopRunnerWorker runWorker = null;	//The SwingWorker that runs the simulation in a separate thread
	private ArrayList<ProgressListener> progListeners;	//A list of things to be notified of when the simulation is done running 
	private int treeSampleSize = 25; //Number of tips to use in tree sampling
	
	//This gets set to true if the simulation has reached the final generation
	private boolean completed = false;
	
	
	//private int masterChangeOffset = 0;
	//private int masterChangeFrequency = 500000; //Experimental : change the master sequence every so often
	
	
	public PopulationRunner(DemographicModel demoModel, OutputManager output) {
		this.demoModel = demoModel;
		outputHandler = output;
		progListeners = new ArrayList<ProgressListener>();
	}
	
	public boolean hasCompleted() {
		return completed;
	}
	
	/**
	 * Things that should be notified of the progress of the simulation run
	 * @param pl
	 */
	public void addProgressListener(ProgressListener pl) {
		progListeners.add(pl);
	}

	/**
	 * Get the current OutputManager object
	 * @return
	 */
	public OutputManager getOutputHandler() {
		return outputHandler;
	}
	
	/**
	 * Set the current output handler. 
	 * @param output
	 */
	public void setOutputHandler(OutputManager output) {
		this.outputHandler = output;
	}
	
	/**
	 * Returns true if we're currently paused
	 * @return
	 */
	public boolean isPaused() {
		return paused;
	}
	
	/**
	 * Pauses the simulation
	 */
	public void pause() {
		paused = true;
	}
	
	/**
	 * Returns true is a simulation is running, even if the simulation is currently 'paused'. Simulations
	 * are 'running' if they have not been 'stopped'
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Stops the simulation entirely, cannot be restarted.
	 */
	public void stop() {
		if (runWorker != null) {
			paused = false;
			runWorker.cancel(true);
			simulationIsDone();
			running = false;
		}
	}
	
	/**
	 * Resume running simulation from paused state
	 */
	public void resume() {
		paused = false;
	}
	
	/**
	 * Begin running the simulation, we do this by telling the runWorker to start executing. 
	 * @param burninGens The number of 'Burn-in' generations, before real data collection begins
	 * @param length	Total length of the simulation
	 */
	public void begin(int burninGens, int length, int dataCollectionFreq, int dnaSampleFreq, boolean writeTreeLog) {
		runWorker = new PopRunnerWorker(this, burninGens, length, dataCollectionFreq, dnaSampleFreq, writeTreeLog);
		runWorker.execute();
	}
	

	/**
	 * Called when the runWorker is done running the simulation, we must notify all interested parties that we're done. 
	 */
	public void simulationIsDone() {
		for(ProgressListener pl: progListeners) {
			pl.done(this);
		}
	}
	
	/**
	 * Called periodically by the runWorker to set the progress level. This is used in the TreesimJView to 
	 * set the progress bar
	 * @param prog The PERCENTAGE of total progress represented as an int from 0..100 
	 * @param str A string to write on the progress bar, via jprogressbar.setString
	 */
	public void updateProgress(int prog, String str) {
		for(ProgressListener pl: progListeners) {
			pl.setProgress(prog, str);
		}
	}
	
	/**
	 * Set the number of tips to use in tree collection
	 * @param size
	 */
	public void setTreeSampleSize(int size) {
		treeSampleSize = size;
	}
	
	/**
	 * Return the current demographic model
	 * @return
	 */
	public DemographicModel getDemographicModel() {
		return demoModel;
	}
	
	/**
	 * Return the list of currently used statistics (but not any tree samplers)
	 * @return
	 */
	public ArrayList<Statistic> getStatisticsList() {
		return outputHandler.getStatistics();
	}
	
	/**
	 * Actual running of the simulation happens in here. Generations take place in a big for loop, and every generation we ask
	 * all the various statistics if they need to collect, and we perform some data collection if they do. 
	 * 
	 * Potentially confusing is the use of two different collection mechanisms for serially sampled vs. 'simple' tree collection. 
	 * The usual way of collecting trees is via a 'SimpleTreeSampler', which can collect trees at any given time and hence can comply
	 * with whatever the user sets for statistic or dna or treelogging collection frequencies. In this case collection is relatively 
	 * simple; we just call .collect( treeSampler.getLastTree() ) whenever the statistic wants to collect. 
	 * 
	 * Serially sampled trees are more complicated for two reasons. First, we can only be collecting one at a time. This means that DNA, treelogging, 
	 * and statistics must all rely on the same underlying treesampler. Second, tree collection only happens at certain, discrete time intervals, thus
	 * the user cannot set how long s/he wants between collection events (we can't collect a new CollessIndex value every 5 generations and a new tree
	 * every 10000 generations). The current scheme involves just letting the user set whatever they want for collection frequencies, and
	 * just referring them to the most recently collected tree when they want to collect the new values. The most recently collected tree
	 * is periodically updated when the serial sampler gets around to collecting a new tree. 
	 * 
	 * 
	 * @param burninGens Length of burn-in
	 * @param totalLength Total length of simulation
	 * @param dataCollectionFreq default data collection frequency
	 * @param dnaSampleFreq fasta file writing / tree file writing (but not treelog writing) frequency
	 */
	public void run(int burninGens, int totalLength, int dataCollectionFreq, int dnaSampleFreq, boolean writeTreeLog) {
		int t = 0;
		int REPORT_INTERVAL = dataCollectionFreq;
		completed = false;
		List<Statistic> stats = outputHandler.getStatistics();
		
		running = true;
		
		int COLLECT_OFFSET = 0;
		int lastProgressVal = 0;
		
		boolean useSerialSampler = false;	//Whether or not to use the experimental serial tree sampling scheme
		
		TreeSampler treeStatSampler = null; //The sampler that collects trees for statistics use
		TreeSampler treeDNASampler = null; //The sampler that collects trees for fasta / tree file writing
		TreeSampler treeLogSampler = null; //The sampler that collects tree for tree log writing
		
		if (! useSerialSampler) {
			StatisticRegistry sReg = new StatisticRegistry();
			
			boolean hasTreeStats = false;
			for(Statistic stat : stats) {
				if (stat instanceof TreeStatistic)
					hasTreeStats = true;
			}
			
			//We maintain three different types of  tree samplers, one that is used to generate statistics (treeStatSampler)...
			if (hasTreeStats) {
				treeStatSampler = (SimpleTreeSampler)sReg.getInstance(SimpleTreeSampler.identifier, new Options(treeSampleSize) );
				treeStatSampler.setSampleFrequency(dataCollectionFreq);
			}
			
			//...and another sampler that is used to write fasta / tree files. Unfortunately this won't really work with the serial
			//tree sampler, since we can only have one of those at a time..
			treeDNASampler = (SimpleTreeSampler)sReg.getInstance(SimpleTreeSampler.identifier, new Options(treeSampleSize) ); 		
			treeDNASampler.setSampleFrequency(dnaSampleFreq);

			//...and another to write the tree log
			treeLogSampler = null;
			if (writeTreeLog) {
				treeLogSampler = (SimpleTreeSampler)sReg.getInstance(SimpleTreeSampler.identifier, new Options(treeSampleSize) );
				treeLogSampler.setSampleFrequency(dataCollectionFreq);
			}

		}
		else {
			
			//Experimental serial tree sampling stuff below. This should not be in release code. Yet. 
			int[] samplingTimes = new int[]{0, 100, 200};
			int[] sampleSizes = new int[]{20, 20, 50};
			TreeSampler serialSampler = new SerialTreeSampler(samplingTimes, sampleSizes);
			//TreeSampler serialSampler = new SerialTreeSampler(2, 20, 200);
			int sampleFreq = Math.max(dataCollectionFreq, 500);
			serialSampler.setSampleFrequency(sampleFreq);
			
			treeDNASampler = serialSampler;
			treeStatSampler = serialSampler;
			treeLogSampler = null;
			if (writeTreeLog) 
				treeLogSampler = serialSampler;
			
			SerialPairwiseCTime serialPCTime = new SerialPairwiseCTime((SerialTreeSampler)serialSampler);
			serialPCTime.setPopSize(demoModel.getPop(0).size());
			outputHandler.addStatistic(serialPCTime);
						
			//Tell all TreeStatistics who's generating the trees so they can listen for
			//tree sampling events 
			if (treeStatSampler != null && treeStatSampler instanceof SerialTreeSampler) {
				for(Statistic stat : stats) {
					if (stat instanceof TreeStatistic) {
						treeStatSampler.addTreeCollectionListener( ((TreeStatistic)stat) );
					}
				}
			}

		}// else we're using the SerialTreeSampler


		//OutputHandler needs a reference to the treesampler for writing both trees and fasta files		
		if (treeDNASampler!=null)
			outputHandler.addTreeFastaSampler(treeDNASampler);
		
		//We use a separate tree collection tool to write the treelog
		if (treeLogSampler!=null)
			outputHandler.addTreeLogSampler(treeLogSampler);
		
		outputHandler.setStartTime(new Date());
		for(Statistic stat : stats) {
			stat.setCollectData(false); //Don't collect data during burnin
		}
		outputHandler.writeHeaders();
		
		int finalGen = demoModel.getPop(0).getCurrentGenNumber() + totalLength;
		
		try {
			for(t=demoModel.getPop(0).getCurrentGenNumber(); t<=finalGen; t++) {

				//Produce another generation with size given by the demographic model
				try {
					demoModel.reproduceAll();
				}
				catch (Exception ex) {
					TJRunTimeException rtex = new TJRunTimeException(demoModel, ex); 
					throw rtex;
				}
				
				
				//If we're cancelled then abort
				if (runWorker!=null && runWorker.isCancelled()) {
					throw new InterruptedException();
				}
				
				//If we're paused go to sleep, but wake up periodically
				while(paused) {
					Thread.sleep(500); //Wake up every 0.5 seconds to see if we're unpaused
					if (runWorker!=null && runWorker.isCancelled()) { //If we're cancelled during a pause, throw an exception to break the run loop
						throw new InterruptedException();
					}
				}
				
				//At burnin gens, we clear all stats so they won't appear in any summary
				if (t==burninGens) {
					for(Statistic stat : stats) {
						stat.setCollectData(true);
					}
				}
				
				
				//Experimental changing-of master seq stuff
//				if (t>0 && (t-masterChangeOffset)%masterChangeFrequency==0) {
//					System.out.println("Changing master sequence at gen: " + t);
//					changeMasterSequence();
//				}
				
				try {

					if (treeStatSampler != null && t>=burninGens && treeStatSampler.getSampleFrequency()>0 && (t%treeStatSampler.getSampleFrequency()==0)) {
						treeStatSampler.collectStatistic(demoModel.getCollectible());
					}

				}
				catch (Exception ex) {
					TJRunTimeException rtex = new TJRunTimeException(treeStatSampler, ex); 
					throw rtex;
				}
				
				
				try {
					if (treeDNASampler != null && t>=burninGens && treeDNASampler.getSampleFrequency()>0 && (t%treeDNASampler.getSampleFrequency()==0)) {
						//System.out.println("TreeDNA sampler is collecting");
						if (treeDNASampler instanceof SerialTreeSampler)
							treeDNASampler.collectStatistic(demoModel.getCollectible());
						else
							treeDNASampler.collectAndFire(demoModel.getCollectible());
					}

				}
				catch (Exception ex) {
					TJRunTimeException rtex = new TJRunTimeException(treeDNASampler, ex); 
					throw rtex;
				}
				
				
				try {
				if (TreesimJView.storeAncestry && treeLogSampler != null && t>=burninGens && treeLogSampler.getSampleFrequency()>0 && (t%treeLogSampler.getSampleFrequency()==0)) {
					treeLogSampler.collectAndFire(demoModel.getCollectible());
				}
				}
				catch (Exception ex) {
					TJRunTimeException rtex = new TJRunTimeException(treeLogSampler, ex); 
					throw rtex;
				}
				
				
				
				//Each generation we ask each statistic if it wants to collect. Stats may either use the default
				//collection frequency (dataCollectionFreq), or their own frequency. If any stat is a 
				//TreeStatistic, then it uses requestCollection, which causes a new tree to be collected but does not fire
				//a general tree collection event, so other listeners registered on the same sampler will not be notified

				for(Statistic stat : stats) {
					if ((stat.useDefaultCollectionFrequency() && (t+COLLECT_OFFSET)%dataCollectionFreq==0) || (!stat.useDefaultCollectionFrequency() && t%stat.getSampleFrequency()==0)) {
						if (t>=burninGens || (t<burninGens && stat.collectDuringBurnin())) {
							try {
								stat.collectStatistic(demoModel.getCollectible()); //Call collect for ALL statistics... (tree stats also need this)

								if (stat instanceof TreeStatistic) {
									((TreeStatistic)stat).collect( treeStatSampler.getLastTree() );
								}
							}
							catch (Exception ex) {
								TJRunTimeException rtex = new TJRunTimeException(stat, ex); 
								throw rtex;
							}

						}
					}
				}

				
				
				//Write another log line of data
				if (t%REPORT_INTERVAL == 0 && t>0) {
					outputHandler.writeAnotherLine(t);
				}
				
				//Update the progress bar in the GUI
				int prog = (int)Math.round(t*100.0/(double)totalLength);
				if (prog>lastProgressVal || t%100==0) {
					lastProgressVal = prog;
					updateProgress(lastProgressVal, String.valueOf(t));
				}

			} //for t, the generations loop
			
			
			completed = true; //Reached end of loop, no exceptions, so we've completed
			
		}
		catch (InterruptedException iex) {
			//Nothing to do here, most likely the run was canceled by the user
			System.out.println("Simulation interrupted at generation " + t);
		}
		catch (TJRunTimeException tjex) {
			System.err.println("Caught runtime exception : " + tjex.getSourceException().getMessage() );
			String messageStr = null;
			if (tjex.getTJSource() instanceof DemographicModel) {
				messageStr = "An error occurred during reproduction in the demographic model " + tjex.getTJSource().getClass();
			}
			if (tjex.getTJSource() instanceof Statistic) {
				messageStr = "An error occurred while using the data collector " + tjex.getTJSource().getClass();
			}
			if (tjex.getTJSource() instanceof TreeSampler) {
				messageStr = "An error occurred while using the tree sampler " + tjex.getTJSource().getClass();
			}
			ErrorWindow.showErrorWindow(tjex.getSourceException(), messageStr);
			
			System.err.println("The stack trace was : " );
			tjex.getSourceException().printStackTrace();
		}
		catch (Exception e) {
			System.err.println("Caught exception : " + e.getMessage() + "\n" + e.toString() );
			e.printStackTrace();
			System.err.println("An error was enountered during the simulation and the run can no longer continue. \n Attempting to write the data summary...");
			outputHandler.writeSummaries();
		}
		
		running = false;
		
		if (t<=burninGens) {
			System.err.println(" Run did not proceed past burnin, no summary data collected. ( Generations : " + t + " burn-in : " + burninGens + " )");
		}
		else {
			outputHandler.setEndTime(new Date());
			outputHandler.writeSummaries();
		}
	}
	
	
	/**
	 * A class that represents the simulation running in another thread. Pretty much it just calls .run()
	 * @author brendan
	 *
	 */
	public class PopRunnerWorker extends SwingWorker<Void, Void> {

		int burnin;
		int totalLength;
		int dataCollectionFreq;
		int dnaSampleFreq;
		PopulationRunner runner;
		boolean writeTreeLog;
		
		public PopRunnerWorker(PopulationRunner runner, int burnin, int length, int dataCollectionFreq, int dnaSampleFreq, boolean writeTreeLog) {
			this.runner = runner;
			this.burnin = burnin;
			this.totalLength = length;
			this.dnaSampleFreq = dnaSampleFreq;
			this.dataCollectionFreq = dataCollectionFreq;
			this.writeTreeLog = writeTreeLog;
		}


		public Void doInBackground() {
			
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				public void uncaughtException(Thread t, Throwable e) {
					System.out.println("An error was encountered while running the simulation :\n" + e.getMessage());
				}
			});
			
			runner.run(burnin, totalLength, dataCollectionFreq, dnaSampleFreq, writeTreeLog);

			return null;
		}

		public void done() {
			simulationIsDone();
		}
	}





	
}
