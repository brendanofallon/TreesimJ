package statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import population.Locus;
import population.Population;

import mutationModels.FakePurifyingMutation;
import mutationModels.MutationModel;
import tree.DiscreteGenTree;
import cern.jet.random.engine.RandomEngine;
import dnaModels.BitSetDNASequence;
import dnaModels.DNASequence;


/**
 * More a utility than a statistic, this takes a tree and generates a neutral sequence over it
 * @author brendan
 *
 */
public class NeutralSeqGenerator {

	public static final String identifier = "Neutral sequence generator (exp)";
	
	public String getIdentifier() {
		return identifier;
	}
	
	PrintStream output = System.out;
	int calls = 0;
	int alignmentsGenerated = 0;
	MutationModel mutMod;
	RandomEngine rng;
	int length;
	String fileStem;
	int filesWritten = 0;
	int currentTreeHeight = 0;
	int sampleSize;
	
	public NeutralSeqGenerator(PrintStream out, RandomEngine rng, int length, MutationModel mm, String fileStem, int sampleSize) {
		output = out;
		//values = new ArrayList<Double>();
		mutMod = mm;
		this.rng = rng;
		this.length = length;
		this.fileStem = fileStem;
		this.sampleSize = sampleSize;
	}
	
	public void collect(Population pop) {
		calls++;
		if (calls%5 == 0) {
			Locus root = pop.getSampleTree(sampleSize);
			DiscreteGenTree tree = new DiscreteGenTree(root);
			alignmentsGenerated++;
			DNASequence rootSeq = new BitSetDNASequence(rng, length, mutMod);
			String filename = fileStem + "_" + filesWritten + ".fas";
			String treeFilename = fileStem +"_truetree_" + filesWritten + ".tre";
			try {
				output.println("Writing lamarc data to file : " + filename);
				BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
				currentTreeHeight = tree.getMaxHeight();
				emitSequencesFromTree(tree.getRoot(), rootSeq, writer, 0);
				filesWritten++;
				
				writer.close();

				String newickString = tree.getNewick();
				writer = new BufferedWriter(new FileWriter(treeFilename));
				writer.write(newickString + "\n");
				writer.close();				
			}
			catch (IOException ioe) {
				System.out.println("Could not write to file : " + filename + " exception : " + ioe);
				currentTreeHeight = 0;
			}
			
			currentTreeHeight = 0;
			output.println("True tree : \n" + tree.getNewick());
		}
	}

	public boolean collectDuringBurnin() {
		return false;
	}
	
	public double getMean() {
		return alignmentsGenerated;
	}
	
	public String getDescription() {
		return "Neutral sequence auto-generator, alignments generated : ";
	}

	
	private void emitSequencesFromTree(Locus root, DNASequence seq, BufferedWriter writer, int distFromRoot) {
		if (root.numOffspring()==0) {
			((FakePurifyingMutation)seq.getMutationModel()).setDepth(currentTreeHeight-distFromRoot);
			seq.mutate();
			try {
				writer.write(">" +root.getReadableID() + "\n");
				writer.write(seq.getStringValue() +"\n");
			}
			catch (IOException ioe) {
				System.err.println("Exception caught during neutral seq. file writing : " + ioe);
			}
		}
		else if (root.numOffspring()==1) {
			((FakePurifyingMutation)seq.getMutationModel()).setDepth(currentTreeHeight-distFromRoot);
			seq.mutate();
			emitSequencesFromTree(root.getOffspring(0), seq, writer, distFromRoot+1);
		}
		else {
			for(Locus kid : root.getOffspring()) {
				DNASequence newSeq = (DNASequence)seq.getCopy();
				((FakePurifyingMutation)newSeq.getMutationModel()).setDepth(currentTreeHeight-distFromRoot);
				newSeq.mutate();
				emitSequencesFromTree(kid, newSeq, writer, distFromRoot+1);
			}
		}
	}
}
