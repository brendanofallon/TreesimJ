package statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import population.Individual;
import dnaModels.DNASequence;
import tree.DiscreteGenTree;

/**
 *  Emits lamarc input files periodically. Also emits true trees as newick files with similar prefixes for easy
 *  cross referencing.
 * 
 * @author brendan
 *
 */
public class LamarcEmitter extends TreeStatistic {

	public static final String identifier = "Write DNA to LAMARC input files";
	
	public String getIdentifier() {
		return identifier;
	}
	
	int filesToWrite = 20;
	int filesWritten = 0;
	int calls = 0;
	int callSkip = 5;
	String fileStem = "simdata_";
	String comment;
	boolean fixTheta = false;
	double fixedThetaVal = 0.04;
	boolean includeNu = true;
	boolean includeBeta = false;
	boolean includeP = true;
	//NeutralSeqGenerator seqGen = null;
	
	int finalChainSamples = 25000;
	int interval = 30;
	int finalChainNumber = 1;

	public LamarcEmitter() { };
	
	private LamarcEmitter(String fileStem, String comment) {
		this.fileStem = fileStem;
		this.comment = comment;
	}
	
	public LamarcEmitter getNew(Options ops) {
		this.options = ops;
		LamarcOptions lops = (LamarcOptions)ops.optData;
		String stem = lops.stem;
		String comment = lops.comment;
		return new LamarcEmitter(stem, comment);
	}


	
	public boolean collectDuringBurnin() {
		return false;
	}
	
	public void collect(DiscreteGenTree tree) {
		if (tree==null)
			return;
		
		if (calls % callSkip ==0 && filesWritten < filesToWrite) {
			ArrayList<Individual> tips = tree.getTips();
			
			if (tips.get(0).getPrimaryDNA() ==null)
				return;
			
			String filename = fileStem + filesWritten + ".xml";
			String treeFilename = fileStem + "truetree_" + filesWritten + ".tre";
			try {
				output.println("Writing lamarc data to file : " + filename);
				BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
				writeLamarcInputFile(writer, tips, filename);
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

	public String getDescription() {
		return "Saves lamarc files of genetic data, using fileStem : " + fileStem + " files saved : " + filesWritten;
	}
	
	private void writeLamarcInputFile(BufferedWriter writer, ArrayList<Individual> inds, String filename) throws IOException {
		String head = "<lamarc version=\"2.1.5\">  \n <!-- " + comment + " --> \n <chains> \n  <replicates>1</replicates> \n  <bayesian-analysis>Yes</bayesian-analysis> \n <heating> \n <adaptive>false</adaptive> \n  <temperatures> 1</temperatures> \n <swap-interval>10</swap-interval> \n </heating> \n  <strategy> \n  <resimulating>0.8</resimulating> \n <tree-size>0.15</tree-size> \n <bayesian>0.05</bayesian>\n<haplotyping>0</haplotyping> \n <trait-arranger>0</trait-arranger> \n </strategy> \n <initial> \n <number>1</number> \n <samples>1000</samples> \n  <discard>1000</discard> \n <interval>20</interval> \n </initial> \n";
		head += "<final> \n <number>2 " + finalChainNumber + " </number> \n <samples> " + finalChainSamples + " </samples> \n <discard>1000</discard> \n <interval> " + interval + " </interval> \n </final>\n";
		head += "</chains> \n <format> \n <convert-output-to-eliminate-zero> Yes </convert-output-to-eliminate-zero> \n  <verbosity>normal</verbosity> \n <progress-reports>normal</progress-reports> \n <results-file>outfile.txt</results-file> \n <use-in-summary>false</use-in-summary> \n <in-summary-file>insumfile.xml</in-summary-file> \n <use-out-summary>false</use-out-summary> \n <out-summary-file>outsumfile.xml</out-summary-file> \n  <use-curvefiles>false</use-curvefiles> <curvefile-prefix>curvefile</curvefile-prefix> \n <use-tracefile>true</use-tracefile> \n  <tracefile-prefix>trace</tracefile-prefix> \n <use-newicktreefile>true</use-newicktreefile> \n";
		head += "<newicktreefile-prefix>newick_" + fileStem + "</newicktreefile-prefix> \n <out-xml-file>menusettings_infile.xml</out-xml-file> \n  </format> \n";
		if (fixTheta) {
			head += "<forces> \n  <coalescence>\n   <start-values> " + fixedThetaVal + " </start-values> \n <method> USER</method> \n <profiles> percentile </profiles> \n <constraints> constant </constraints> \n  <prior type=\"uniform\"> \n <paramindex> all </paramindex> \n <lower> " + fixedThetaVal + " </lower> \n <upper> " + fixedThetaVal + "001 </upper> \n <mean> 0.01 </mean> \n </prior> \n  </coalescence> \n";
		}
		else {
			head += "<forces> \n  <coalescence>\n   <start-values> 0.01 </start-values> \n <method> USER </method> \n <profiles> percentile </profiles> \n <constraints> unconstrained </constraints> \n  <prior type=\"uniform\"> \n <paramindex> all </paramindex> \n <lower> 1e-6 </lower> \n <upper> 5 </upper> \n <mean> 0.01 </mean> \n </prior> \n  </coalescence> \n";
		}
		if (includeNu) head += "<purifyingnu> \n <start-values> 0 </start-values> \n  <profiles> percentile </profiles> \n <constraints> unconstrained </constraints> \n  <prior type=\"uniform\"> \n  <paramindex> 1 </paramindex> \n <lower> -1  </lower> \n <upper> 1 </upper> \n  </prior> \n </purifyingnu> \n ";
		if (includeBeta) head += "<purifyingbeta> \n <start-values> 1000 </start-values> \n  <profiles> percentile </profiles> \n <constraints> unconstrained </constraints> \n  <prior type=\"uniform\"> \n  <paramindex> 1 </paramindex> \n <lower> 0  </lower> \n <upper> 5000 </upper> \n  </prior> \n </purifyingbeta> \n";
		if (includeP) head += "<purifyingp> \n <start-values> 0 </start-values> \n  <profiles> percentile </profiles> \n <constraints> unconstrained </constraints> \n  <prior type=\"uniform\"> \n  <paramindex> 1 </paramindex> \n <lower> 0  </lower> \n <upper> 1 </upper> \n  </prior> \n </purifyingp> \n";
		head += " </forces> \n";
		
		String data = "<data> \n <region name=\"simdata_" + filesWritten + "\"> \n <model name=\"PurifyingRate\"> \n <normalize>false</normalize>\n <categories> \n <num-categories>2 </num-categories> \n <rates> 1 1 </rates> \n <probabilities> 1 1</probabilities> \n </categories> \n <relative-murate>1</relative-murate> \n <base-freqs> 0.25 0.25 0.25 0.25 </base-freqs> \n   <ttratio>1</ttratio> \n <nu> 0.5 </nu> \n <beta> 200 </beta> \n \n <proportion> 0.5 </proportion>  </model> \n";
	     data += "<effective-popsize>1</effective-popsize> \n <spacing> \n";
		writer.write(head);
		writer.write(data);
		writeLamarcDataFile(writer, inds, filename);
	}
	
	private void writeLamarcDataFile(BufferedWriter writer, ArrayList<Individual> inds, String filename) throws IOException {
		int length = inds.get(0).getPrimaryDNA().length();
			writer.write("\t<block name=\"seg1_simdata\">\n");
        writer.write("\t\t<length> " + length + " </length>\n");
        writer.write("\t</block>\n\t</spacing>\n");
        writer.write("\t<population name=\"pop 1 of simdata\">\n");
        
        for(Individual ind : inds) {
        	writer.write("\t\t<individual name=\"" + ind.getReadableID() + "\">\n");
        	writer.write("\t\t\t<sample name=\"" + ind.getReadableID() + "_0\">\n");
            writer.write("\t\t\t<datablock type=\"DNA\"> " + ind.getPrimaryDNA().getStringValue() + " </datablock>\n");
            writer.write("\t\t\t</sample>\n");
            writer.write("\t\t</individual>\n"); 	
        }
        
        writer.write("\t\t</population>\n \t\t</region>\n \t </data>\n </lamarc>\n");    
	}
	
	class LamarcOptions {
		public String stem;
		public String comment;
	}
	
}
