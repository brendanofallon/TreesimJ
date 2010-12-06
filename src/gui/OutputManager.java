package gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import population.Individual;
import population.Population;

import statistics.Collectible;
import statistics.Statistic;
import statistics.TreeCollectionListener;
import statistics.TreeSampler;
import tree.DiscreteGenTree;
import treesimj.SerializablePrintStream;
import treesimj.TreesimJApp;
import treesimj.TreesimJView;
import demographicModel.DemographicModel;
import dnaModels.DNASequence;
import fitnessProviders.FitnessProvider;


/**
 * Handles writing of statistics, trees, and sequence files to various printstreams (files, System.out, etc). Most statistics are
 * handled identically; when collecting data we ask the stat to 'getScreenLogStr', which is a formatted string that
 * summarizes the most recent data collection value. This is then printed to the various streams that have been
 * added via addStream
 * 
 * A couple of special cases are also handled here. First, we allow writing of fasta sequence data to a stream. This is
 * done via setWriteSingleFasta or setWriteMultipleFasta. The single fasta version writes all data to a single output file.
 * The multiple-write version writes to a new file each time. The writeSingleFasta option is actually not used at this 
 * point.
 * 
 * Finally, we allow for emitting trees to the output. This requires having a treeSampler supplied (via addTreeSampler) and
 * some tree-writing streams supplied (via addTreesStream)
 * 
 * @author brendan
 *
 */
public class OutputManager implements Serializable {

	ArrayList<SerializablePrintStream> streams; //Print log files to these streams
	SerializablePrintStream summaryStream;		//Print summary to this stream
	
	SerializablePrintStream treesStream = null;		//Trees are written, in newick form, to this stream
	
	boolean writeFasta = false;		//True if any writing of DNA is to happen
	boolean writeSeparateFastas = false;
	boolean writeTreesWithFasta = false;


	SerializablePrintStream fastaStream = null;
	String fastaFileStem = "";		//File stem to use for fasta file writing
	int fastasWritten = 0;			//Total number of fasta files written, if writing separate fasta files
	
	ArrayList<Statistic> statList; //Print logs for these Statistics
	
	StringBuilder summaryHeader; //Stores information regarding the header for the summary
	
	Date startTime;
	Date endTime;
	
	DemographicModel demoModel = null; //This is just used to get the current generation number
	
	int repeatNumber = 0;
	
	//Maintain a static reference so we can access this easily from a variety of places
	public static OutputManager globalOutputHandler;
	
	public OutputManager(DemographicModel demoModel) {
		this.demoModel = demoModel;
		streams = new ArrayList<SerializablePrintStream>();
		statList = new ArrayList<Statistic>();
		globalOutputHandler = this;
	}
	
	/**
	 * Add a new stream to the list of streams to write output to
	 * @param newStream
	 */
	public void addStream(SerializablePrintStream newStream) {
		if (! streams.contains(newStream)) {
			streams.add(newStream);
		}
	}
	
	
	/**
	 * If true this causes the 'true tree' file to be written with each fasta file emitted.
	 * @param writeTreesWithFasta
	 */
	public void setWriteTreesWithFasta(boolean writeTreesWithFasta) {
		this.writeTreesWithFasta = writeTreesWithFasta;
	}
	
	/**
	 * Add a new statistic to the list of statistics to emit output for
	 * @param stat
	 */
	public void addStatistic(Statistic stat) {
		statList.add(stat);
	}
	
	/**
	 * A a new stream for summary writing
	 * @param filePS
	 */
	public void setSummaryStream(SerializablePrintStream filePS) {
		summaryStream = filePS;
	}
	
	/**
	 * A reference to the list of statistics used by this output manager
	 * @return
	 */
	public ArrayList<Statistic> getStatistics() {
		return statList;
	}
	
	/**
	 * Turn on writing of sample alignments to one big file. Here we just set fastaStream to be a new
	 * stream based on the supplied file stem, and leave it at that.
	 * 
	 * @param fastaFilestem
	 */
	public void setWriteSingleFasta(String fastaFilestem) {
		writeFasta = true;
		fastaFilestem = fastaFilestem + ".fas";
		File fastaFile = new File(fastaFilestem);
		try {
			fastaStream = new SerializablePrintStream(fastaFile);
		}
		catch (IOException ioe) {
			fastaStream = null;
			JFrame frame = TreesimJApp.getApplication().getMainFrame();
			if (frame==null || !frame.isVisible()) {
				System.err.println("Could not open fasta file " + fastaFile.getAbsolutePath() + " for writing, reason: " + ioe);
			}
			else {
				JOptionPane.showMessageDialog(frame,
						"Could not open fasta file " + fastaFile.getAbsolutePath() + " for writing, reason: " + ioe,
						"Output option error",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	public Date getStartTime() {
		return startTime;
	}


	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}


	public Date getEndTime() {
		return endTime;
	}


	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * Constructs a header for the summary file. This should be called prior to the initiation of the simulation (so the
	 * start time will be accurate).
	 * @param startTime
	 * @param fitnessModel
	 * @param demoModel
	 * @param runSettings
	 */
	public void constructSummaryHeader(FitnessProvider fitnessModel, DemographicModel demoModel, RunSettingsPanel runSettings) {
		summaryHeader = new StringBuilder();
		
		summaryHeader.append("\nTreesimJ v." + TreesimJApp.version + " summary file \n");
		summaryHeader.append("Fitness model : " + fitnessModel.getDescription() + "\n");
		summaryHeader.append("Demographic model : " + demoModel.getDescription()  + "\n");
		summaryHeader.append("Total run length : " + runSettings.getRunLength() + " generations"  + "\n");
		summaryHeader.append("Burn in period : " + runSettings.getBurninGens() + " generations"  + "\n");
		summaryHeader.append("Random number seed: " + runSettings.getRandomSeed()  + "\n");
		summaryHeader.append("Output directory: " + runSettings.getBaseDirName()  + "\n");
		summaryHeader.append("Log file name: " + runSettings.getLogFileName()  + "\n");
		
	}
	
	/**
	 *  Turn on writing of fasta alignments, each to a separate file. Actual creation of the print stream is
	 *  handled in writeAnotherLine.  
	 */
	public void setWriteSeparateFastas(String fastaFilestem) {
		writeFasta = true;
		writeSeparateFastas = true;
		fastaFileStem = fastaFilestem; 
	}
	
	/**
	 * Signals if we require a treeSampler to be set, so we can read & write trees
	 * @return
	 */
	public boolean needsTreeSampler() {
		return treesStream != null;
	}
	
	public void writeHeaders() {
		StringBuilder header = new StringBuilder();
		header.append("Generation\t");
		for(Statistic stat : statList) {
			if (stat.showOnScreenLog())
				header.append(stat.getIdentifier() + "\t");
		}
		
		String dateLine = "# TreeSimJ run initiated at : " + DateFormat.getInstance().format(new Date());
		for(SerializablePrintStream stream : streams) {
			stream.println(dateLine);
			stream.println(header);
		}
		
		if (treesStream != null) {
			treesStream.println("# TreesimJ trees file from run initiated on " + DateFormat.getInstance().format(new Date()));
		}
	}
	
	/**
	 * Set the repeat number, which tells the fasta / tree file writer to append this value to the file names. That's it for now. 
	 * This is set in TreesimJView inside the repeats loop, which is in beginSimulationFromGUI
	 * @param num
	 */
	public void setRepeatNumber(int num) {
		repeatNumber = num;
	}
	
	/**
	 * Write the trees and fasta files, if necessary. Note that this does NOT FORCE NEW TREE COLLECTION, we just take whatever
	 * the last tree collected was. 
	 * @param gens
	 */
	public void writeTreesAndFasta(DiscreteGenTree tree) {
		
		if (writeFasta) {
			fastasWritten++;
			if (writeSeparateFastas) {
				String rNum = "";
				if (repeatNumber>0)
					rNum = "_#" + String.valueOf(repeatNumber);
				
				System.out.println("Writing fasta file on generation " + String.valueOf(demoModel.getCurrentGenNumber()));
				
				String fastaFileName = fastaFileStem + /*"_g" + String.valueOf(demoModel.getCurrentGenNumber()) + rNum + */ ".fas";
				String treeFileName = fastaFileStem +  /* "_tree_g" + String.valueOf(demoModel.getCurrentGenNumber()) + rNum + */ ".tre";
				File fastaFile = new File(fastaFileName);
				File treeFile = new File(treeFileName);
				int count = 0;
				while (fastaFile.exists()) {
					count++;
					fastaFileName = fastaFileStem + "_" + count + ".fas";
					fastaFile = new File(fastaFileName);
					treeFileName =  fastaFileStem + "_" + count + ".tre";
					treeFile = new File(treeFileName);
				}
				
				try {
					fastaStream = new SerializablePrintStream(fastaFile);
					
					if (writeTreesWithFasta) {
						
						FileWriter treeWriter = new FileWriter(treeFile);
						String newick = tree.getNewick();
						treeWriter.write(newick + "\n");
						treeWriter.close();
					}
				}
				catch (IOException ioe) {
					System.err.println("Could not write fasta/tree to file : " + ioe);
				}
			}

			String fasta = getFastaFromTreeTips(tree);
			fastaStream.println();
			fastaStream.print(fasta);
			if (writeSeparateFastas) {
				fastaStream.close();
			}
		}
	}
	
	/**
	 * Write an additional line from all statistics to all streams
	 * @param gens
	 */
	public void writeAnotherLine(int gens) {
		StringBuilder line = new StringBuilder();
		line.append(gens + "\t");
		for(Statistic stat : statList) {
			if (stat.showOnScreenLog()) {
				String str = stat.getScreenLogStr();
				if (str != null)
					line.append(str + "\t");
				else
					line.append("NA" + "\t");
			}
		}

		for(SerializablePrintStream stream : streams) {
			stream.println(line);
		}
		
	}
	
	/**
	 * Write a new tree to the tree log file
	 * @param tree Tree to be written (in newick form)
	 */
	public void writeTreeLogLine(DiscreteGenTree tree) {
		//Write to the tree log. 
		if (treesStream != null) {
			treesStream.println(demoModel.getCurrentGenNumber() + "\t" + tree.getNewick());
		}
	}
	
	
	/**
	 * Generates a fasta-esque string from the tips of the current tree from the treeSampler
	 * @return
	 */
	protected String getFastaFromTreeTips(DiscreteGenTree tree) {
			StringBuilder str = new StringBuilder();
			List<Individual> tips = tree.getTips();
			Collections.sort(tips, new PopNumSorter());
			
			for(Individual ind : tips) {
				//output directly to .ima
//				String label = constructLabel(ind);
//				if (label.length()>9)
//					label = label.substring(0, 9);
//				while (label.length()<10)
//					label = label + " ";
//				str.append(label);
//				str.append(ind.getPrimaryDNA().toString() + "\n");
				
				//output to fasta below
				str.append(">");
				str.append(constructLabel(ind));
//				double w = ind.getFitness();
//				str.append("_w" + w);
				str.append("\n");
				DNASequence dna = ind.getPrimaryDNA();
				
				if (dna != null) {
					str.append(dna.toString());
					str.append("\n");
				}
			}
			
			String fas = str.toString();
			if (fas.trim().length()==0)
				System.err.println("Fasta string has zero characters!");
			return fas;
	}
	
	public boolean isWritingFasta() {
		return writeFasta;
	}
	
	/**
	 * Write the final summary of each statistic to the summary stream
	 */
	public void writeSummaries() {
		PrintStream sysOut = null;
		for(SerializablePrintStream stream : streams) {
			if (stream.isSystemOut())
				sysOut = System.out;
		}
		
		summaryHeader.append("\n");

		if (startTime != null) {
			summaryHeader.append("Simulation start time : " + DateFormat.getInstance().format(startTime) + "\n");
			summaryHeader.append("Simulation end time: " + DateFormat.getInstance().format(endTime) + "\n");
			long elapsedMS = endTime.getTime() - startTime.getTime();
			long elapsedSeconds = Math.round(elapsedMS/1000.0);
			long elapsedMinutes = (long)Math.floor(elapsedSeconds/60.0);
			long elapsedHours = (long)Math.floor(elapsedMinutes/60.0);
			long elapsedDays = (long)Math.floor(elapsedHours/24.0);
			
			summaryHeader.append("Total elapsed time : ");
			if (elapsedDays>0)
				summaryHeader.append(elapsedDays + " day" + (elapsedDays==1 ? ", " : "s, ") );
			if (elapsedDays>0 || elapsedHours>0)
				summaryHeader.append(elapsedHours%24 + " hour" + (elapsedHours==1 ? ", " : "s, ") );
			if (elapsedHours>0 || elapsedMinutes>0) {
				summaryHeader.append(elapsedMinutes%60 + " minute" + (elapsedMinutes==1 ? " and " : "s and "));
			}
			summaryHeader.append(elapsedSeconds%60 + " second" + (elapsedSeconds==1 ? " " : "s ") + "\n");
			summaryHeader.append("Final generation reached : " + demoModel.getCurrentGenNumber() + "\n\n");
		}
		
		
		if (summaryHeader!=null) {
			summaryStream.println(summaryHeader.toString());
			
			if (sysOut != null)
				sysOut.println(summaryHeader.toString());
		}
		
		for(Statistic stat : statList) {
			summaryStream.println("\n\n Final summary for statistic : " + stat.getIdentifier());
			stat.summarize(summaryStream.getPrintStream());
			
			if (sysOut != null) {
				sysOut.println("\n\n Final summary for statistic : " + stat.getIdentifier());
				stat.summarize(sysOut);
			}
		}
	}

	/**
	 * Constructs the label for a given individual used when we write this guy to a sequence or tree file. 
	 * @param ind
	 * @return
	 */
	private String constructLabel(Individual ind) {
		if (ind.getDepth()<0)
			return ind.getReadableID();
		else {
			return ind.getReadableID() +"#DATE=" + ind.getDepth();
		}
	}
	
	
	/**
	 * Stream to write tree info to
	 * @param filePS
	 */
	public void setTreesStream(SerializablePrintStream filePS) {
		treesStream = filePS;
	}

	/**
	 * Use the supplied tree sampler to write fasta and true tree files when new trees are collected
	 * @param ts
	 */
	public void addTreeFastaSampler(TreeSampler ts) {
		ts.addTreeCollectionListener(new TreeListener() {
			public void newTreeCollected(DiscreteGenTree tree) {
				writeTreesAndFasta(tree);
			}
		});
	}
	
	/**
	 * Use the supplied tree sampler to write treelog files when new trees are collected
	 * @param ts
	 */
	public void addTreeLogSampler(TreeSampler ts) {
		ts.addTreeCollectionListener(new TreeListener() {
			public void newTreeCollected(DiscreteGenTree tree) {
				writeTreeLogLine(tree);
			}
		});
	}


	//So we can write inner classes that implement TreeCollectionListener
	class TreeListener implements TreeCollectionListener {
		public void newTreeCollected(DiscreteGenTree tree) {
			
		}
	}
	
	/**
	 * A comparator to sort sequences by population # 
	 * @author brendan
	 *
	 */
	public class PopNumSorter implements Comparator<Individual> {

		public int compare(Individual ind1, Individual ind2) {
			return (ind1.getOriginPop() - ind2.getOriginPop());	
		}
		
	}
}
