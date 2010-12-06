package statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import population.Locus;

import tree.DiscreteGenTree;
import dnaModels.DNASequence;

/**
 * Emit fasta files periodically. Also emits true trees with similar file prefixes as the fasta files for easy cross referencing
 * @author brendan
 *
 */
public class FastaEmitter extends TreeStatistic {

	public static final String identifier = "Writes DNA to fasta files";
	
	int filesToWrite = 40;
	int filesWritten = 0;
	int calls = 0;
	int callSkip = 5;
	String fileStem = "simdata_";
	String comment;
	
	int finalChainSamples = 25000;
	int interval = 30;
	int finalChainNumber = 1;
	
	public FastaEmitter() { };
	
	public FastaEmitter(String fileStem) {
		this.fileStem = fileStem;
	}
	

	public FastaEmitter getNew(Options ops) {
		this.options = ops;
		String stem = (String)ops.optData;
		return new FastaEmitter(stem);
	}
	
	
	public String getIdentifier() {
		return identifier;
	}
	
	public boolean collectDuringBurnin() {
		return false;
	}
	
	public void collect(DiscreteGenTree tree) {
		if (tree==null)
			return;
		
		if (calls % callSkip ==0 && filesWritten < filesToWrite) {
			ArrayList<Locus> tips = tree.getTips();
			
			if (tips.get(0).getPrimaryDNA() ==null)
				return;
			
			String filename = fileStem + "_" + filesWritten + ".fas";
			String treeFilename = fileStem + "_truetree_" + filesWritten + ".tre";
			try {
				output.println("Writing lamarc data to file : " + filename);
				BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
				writeFasta(writer, tips, filename);
				filesWritten++;
				writer.close();
				
				String newickString = tree.getNewick();
				writer = new BufferedWriter(new FileWriter(treeFilename));
				writer.write(newickString + "\n");
				writer.close();
				
			}
			catch (IOException ioe) {
				System.out.println("Could not write to file : " + filename + " exception : " + ioe);
			}

		}
		
		calls++;
	}

	private void writeFasta(BufferedWriter writer, ArrayList<Locus> tips,
			String filename) {
		try {
			for(Locus ind : tips) {
				writer.write(">" + ind.getReadableID() + "\n");
				String seq = ind.getPrimaryDNA().getStringValue();
				writer.write(seq + "\n\n");
			}
		}
		catch (IOException ioe) {
			System.err.println("Warning : Caught exception while writing to fasta file " + filename + "\n " + ioe);
		}
	}

	public String getDescription() {
		return "Saves lamarc files of genetic data, using fileStem : " + fileStem + " files saved : " + filesWritten;
	}
}
