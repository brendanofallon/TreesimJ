package statistics.treeShape;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import statistics.Histogram;
import statistics.Options;
import statistics.Statistic;
import statistics.TreeStatistic;
import tree.DiscreteGenTree;

/**
 * This statistic measures the average time at which each coalescent event occurs. Each coalescent interval is stored as a 
 * (separate) histogram of values.  
 * @author brendan
 *
 */
public class CoalIntervalStat extends TreeStatistic {

	ArrayList<Histogram> intervalHistos;
	int samples = 0;
	
	final double maxHistoTime = 5; //Maximum number of theta-units to use for histograms, in absence of user input
	
	public static final String identifier = "Coalescent interval times";
	
	public CoalIntervalStat() {
		intervalHistos = new ArrayList<Histogram>();
	}
	
	public void clear() {
		if (intervalHistos != null)
			intervalHistos.clear();
	}
	
	public void collect(DiscreteGenTree tree) {
		if (tree==null)
			return;
		
		ArrayList<Double> coalTimes = tree.getNodeTimes();
		Collections.sort(coalTimes);
		
		while (intervalHistos.size()<coalTimes.size()) {
			double min = 0;
			if (getUserHistoMin()!=null)
				min = getUserHistoMin();
			double max = maxHistoTime;
			if (getUserHistoMax()!=null)
				max = getUserHistoMax();
			double binWidth = (max-min)/(double)histoBins;
			intervalHistos.add(new Histogram(histoBins, min, binWidth));
		}
		
		for(int i=0; i<coalTimes.size(); i++) {
			intervalHistos.get(i).addValue(coalTimes.get(i)/(double)pop.size());
		}
		samples++;
	}
	
	
	/**
	 * There's nothing really worth reporting on the screen log... unless we want to emit every coalescent interval? This could be 
	 * a customization option, if we wanted to override the default statistic configurator, perhaps. 
	 */
	public boolean showOnScreenLog() {
		return false;
	}
	
	/**
	 * Since there's no information in the values list, we need to provide our own summary.
	 */
	public void summarize(PrintStream out) {
		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
		out.println("Number of trees sampled : \t" + samples);
		
		double binWidth = intervalHistos.get(0).getBinWidth();
		
		System.out.println("Average coalescent time across samples for each interval:");
		for(int i=0; i<intervalHistos.size(); i++) {
			out.println((i+1) + "\t" + formatter.format(intervalHistos.get(i).getMean()));
		}
		
		out.println("Histograms for all intervals:");
		
		out.println("Interval\t");
		
		for(int i=0; i<intervalHistos.size(); i++) {
			out.print("\t #" + (i+1));
		}
		out.println();
		
		for(int row=0; row<histoBins; row++) {
			out.print(row*binWidth + "-" + (row+1)*binWidth + "\t");
			
			for(int col=0; col<intervalHistos.size(); col++) {
				out.print(formatter.format(intervalHistos.get(col).getFreq(row)) + "\t");
			}
			
			out.println();
		}

	}
	

	public String getDescription() {
		return "Logs the times at which coalescent events occur";
	}


	public String getIdentifier() {
		return identifier;
	}


	public Statistic getNew(Options ops) {
		return new CoalIntervalStat();
	}

}
