package statistics;

import java.io.PrintStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JFrame;


import treesimj.SerializablePrintStream;
import xml.TJXMLConstants;
import xml.XMLParseable;
import cern.jet.random.engine.RandomEngine;

/**
 * Abstract base class of things that collect data during simulation runs. PopulationRunners periodically call 
 * the .collect(Population) method, and classes are free to collect whatever information they want. This class
 * also provides a list of values and some basic operations on that list (mean, variance, histogram, etc), which
 * is useful for types of statistics that just collect a single value upon each call to .collect() (things like mean p
 * population fitness, number of segregating sites, the frequency of an allele, etc). Such subclasses probably will need
 * just a few lines of code in a .collect() method to work 
 * 
 * Noteable subclasses are HistogramStatistic (those stats that collect many numbers on each call, and store them in a histogram),
 * and TreeStatistic, which calculate things based on a tree, not on a population
 * 
 * @author brendan
 *
 */
public abstract class Statistic extends XMLParseable implements Serializable {

	protected ArrayList<Double> values;	//List of values collected during operation
	protected SerializablePrintStream output;		//Stream to write output to
	protected Options options;			//Additional info (such as sequence ranges or file stems) the statistic may require
	protected RandomEngine rng;			//Some Statistics need to generate random numbers
	
	
	protected Double userHistoMin = null;
	protected Double userHistoMax = null;
	protected Integer histoBins = 50;
	
	boolean hasUserSuppliedSampleFrequency = false;
	int userSampleFrequency=Integer.MAX_VALUE;
	
	protected NumberFormat formatter = new DecimalFormat("######0.0####");
	
	//Recombination breaks some types of statistics, especially tree-shape stats. Stats that
	//can operate correctly in the presence of recombination should set this to true. 
	protected boolean canHandleRecombination = false;
	
	/**
	 * Creates a new statistic and initializes the XMLParseable thingy
	 */
	public Statistic() {
		super(TJXMLConstants.STATISTIC); //All stats have XML block name 'Statistic'
		output = new SerializablePrintStream(System.out);
		values = new ArrayList<Double>();
		addXMLAttr(TJXMLConstants.TYPE, getIdentifier());
	}


	/**
	 * Indicated whether or not this statistic requires genealogical information - if it ever references parents, then it does. 
	 * This is used to test for compatibility when we turn on / off tree sampling in the data collectors panel. 
	 */
	public boolean requiresGenealogy() {
		return false;
	}
	
	/**
	 * Assigns an outputstream to this statistic for writing information. Not currently used in the GUI version.
	 * @param out
	 */
	public void setOutput(SerializablePrintStream out) {
		output = out;
	}
	
	public void setRandomEngine(RandomEngine rng) {
		this.rng = rng;
	}
	
	/**
	 * Returns true if this statistic can operate correctly in the presence of recombination
	 * @return
	 */
	public boolean canHandleRecombination() {
		return canHandleRecombination;
	}
	
	/**
	 * True if a configuration tool is available for this statistic
	 * @return
	 */
	public boolean hasConfigurationTool() {
		return true;
	}
	
	/**
	 * Returns a GUI component that allows the user to configure some options of this statistic
	 * @return
	 */
	public JFrame getConfigurationTool() {
		return new DefaultStatisticConfigurator(this);
	}
	
	/**
	 * Whether or not we allow the user (or other parts of the program) to set our sampling frequency (the number
	 * of generations between calls to .collect() )
	 * @return
	 */
	public boolean useDefaultCollectionFrequency() {
		return ! hasUserSuppliedSampleFrequency;
	}
	
	/**
	 * Retrieve the user supplied (non-default) collection frequency
	 * @return
	 */
	public int getSampleFrequency() {
		return userSampleFrequency;
	}
	
	public void unsetUserSampleFrequency() {
		hasUserSuppliedSampleFrequency = false;
	}
	
	/**
	 * Sets the sample frequency;
	 * @param freq
	 */
	public void setSampleFrequency(int freq) {
		hasUserSuppliedSampleFrequency = true;
		userSampleFrequency = freq;
		addXMLAttr(TJXMLConstants.SAMPLEFREQUENCY, String.valueOf(freq));
	}
	
	/**
	 * Returns a new instance of this statistic, initialzed with the options Ops
	 */
	public abstract Statistic getNew(Options ops);
	
	/**
	 * The default summary of a statistic is to use the values list as the list of values, and to emit 
	 * the sample number, mean, variance, and a histogram. Many stats will probably want to override this. 
	 * @param out
	 */
	public void summarize(PrintStream out) {
		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
		out.println("Number of samples : \t" + values.size());
		out.println("Mean :\t" + getMean());
		out.println("Variance :\t" + getVariance());

		double max;
		if (userHistoMax!=null)
			max = this.userHistoMax;
		else {
			max = getMax(values);
			if (max>1000)
				max = 100*Math.round(max*1.1/100.0);
		}

		double min;
		if (userHistoMin!=null)
			min = userHistoMin;
		else {
			min = getMin(values);
			if (min>0 && min< (max-min)/10.0) 
				min = 0;
		}

		emitHistogram(histoBins, min, max, out);
	}
	
	public int getHistoBins() {
		return histoBins;
	}
	
	public Double getUserHistoMin() {
		return userHistoMin;
	}
	
	public void setUserHistoBins(int bins) {
		this.histoBins = bins;
		addXMLAttr(TJXMLConstants.HISTOBINS, String.valueOf(bins));
	}
	
	public void setUserHistoMin(Double min) {
		this.userHistoMin = min;
		addXMLAttr(TJXMLConstants.HISTOMIN, String.valueOf(min));
	}
	
	public Double getUserHistoMax() {
		return userHistoMax;
	}
	
	public void setUserHistoMax(Double max) {
		this.userHistoMax = max;
		addXMLAttr(TJXMLConstants.HISTOMAX, String.valueOf(max));
	}
	
	/**
	 * True if we need some additional data (such as sample size or a file stem) to make this work
	 * @return
	 */
	public boolean requiresOption() {
		return false;
	}
	
	/**
	 * A user-readable string that describes the option required
	 * @return
	 */
	public String getOptionDescription() {
		return null;
	}
	
	public String getOptionDefault() {
		return "";
	}
	
	
	/**
	 * A unique, human readable identifier. Doesn't really make sense since identifiers are public. 
	 * 
	 */
	public abstract String getIdentifier();
	
	/**
	 * This method is called periodically during the simulation run. Derived classes should collect their
	 * data in here
	 * @param pop The current simulated population 
	 */
	public abstract void collect(Collectible pop);
	
	/**
	 * A brief, user-readable description of the statistic
	 */
	public abstract String getDescription();
	
	/**
	 * If we should write information to the log
	 * @return
	 */
	public boolean showOnScreenLog() {
		return false;
	}
	
	/**
	 * Some statistics require the use of ancestral genetic data. By default this data is not stored
	 * since it takes a lot of space. However, if any statistics require storing it they should
	 * override this to return true. 
	 * @return
	 */
	public boolean requiresPreserveAncestralData() {
		return false;
	}
	/**
	 * String that is written to the log. Default is to write whatever getLastValue() returns, but
	 * many Statistics will want to override this to provide something more sensible. 
	 * @return
	 */
	public String getScreenLogStr() {
		Double val = getLastValue();
		if (val == Double.NaN)
			return "NaN";
		else
			return formatter.format(val);
	}
	
	//If we should sample this statistic during the burnin period
	public boolean collectDuringBurnin() {
		return true;
	}
	
	public double getMean() {
		double mean = 0;
		for(Double x : values)
			mean += x;
		
		return mean/(double)getCount();
	}
	
	public double getMin(ArrayList<Double> vals) {
		if (vals.size()>0) {
			double min = vals.get(0);
			for(int i=1; i<vals.size(); i++) 
				if (min > vals.get(i))
					min = vals.get(i);
			return min;
		}
		else {
			return Double.NaN;
		}
	}
	
	public double getMax(ArrayList<Double> vals) {
		if (vals.size()>0) {
			double max = vals.get(0);
			for(int i=1; i<vals.size(); i++) 
				if (max < vals.get(i))
					max = vals.get(i);
			return max;
		}
		else {
			return Double.NaN;
		}
	}
	
	public double getVariance() {
		double mean = getMean();
		double var = 0;
		for(Double x : values) {
			var += (x-mean)*(x-mean);
		}
		
		return var/(double)getCount();
	}
	
	public int getCount() {
		return values.size();
	}

	
	public double getLastValue() {
		if (values==null || values.size()==0)
			return Double.NaN;
		else
			return values.get(values.size()-1);
	}
	
	public void clear() {
		values.clear();
	}
	
	
	public void emitHistogram(int binSpacing, int max, PrintStream out) {
		int i;
		int min = 0;
		int bins = max/binSpacing;
		int morethanmax = 0;
		int lessthanmin = 0;
		double binsize = binSpacing;
		double[] hist = new double[bins];

		for(i=0; i<values.size(); i++) {
			if (values.get(i) >= max) {
				morethanmax++;
			}
			else
				if (values.get(i) < min) {
					lessthanmin++;
				}
				else
					hist[ (int)Math.floor( (values.get(i)-min)/(max-min)*bins ) ]++;
		}

		for(i=0; i<hist.length; i++) {
			hist[i] = hist[i] / (double)values.size();
		}

		out.println(" < " + min + " \t:" + lessthanmin/(double)values.size() ) ;
		for(i=0; i<hist.length; i++) {
			out.println( i*binsize+min + " .. " + ((i+1)*binsize+min-0.0001) + "\t: " + hist[i] ); 
		}
		out.println( " > " + max + " \t:" + morethanmax/(double)values.size() );

		out.println( " Total elements in list : " + getCount() );
		out.println(" Mean : " + getMean() ); 
		out.println(" Stdev : " + Math.sqrt(getVariance()) );
	}
	
	public void emitHistogram(int bins, double min, double max, PrintStream out) {
		int i;
		  int morethanmax = 0;
		  int lessthanmin = 0;
		  NumberFormat formatter = new DecimalFormat("####0.0#####");
		  double binsize = (max-min) / bins;
		  double[] hist = new double[bins];
		  if (values.size()==0) {
			  output.println("No values found for this statistic, cannot emit histogram.");
			  return;
		  }
		  for(i=0; i<values.size(); i++) {
		    if (values.get(i) >= max) {
		      morethanmax++;
		    }
		    else
		      if (values.get(i) < min) {
		    	  lessthanmin++;
		      }
		      else
			hist[ (int)Math.floor( (values.get(i)-min)/(max-min)*bins ) ]++;
		  }

		  for(i=0; i<hist.length; i++) {
		      hist[i] = hist[i] / (double)values.size();
		  }

		  out.println(" < " + min + " :" + lessthanmin/(double)values.size() ) ;
		  for(i=0; i<hist.length; i++)
			  out.println( formatter.format((double)i*binsize+min) + "\t " + formatter.format(hist[i]) ); 
		  
		  out.println( " > " + max + " :" + morethanmax/(double)values.size() );

		  out.println( " Total elements in list : " + getCount() );
		  out.println(" Mean : " + getMean() ); 
		  out.println(" Stdev : " + Math.sqrt(getVariance()) );
		 }
	
	public double[] getHistogram(int bins, int min, int max ) {
		int i;
		  int morethanmax = 0;
		  int lessthanmin = 0;
		  double binsize = (max-min) / bins;
		  double[] hist = new double[bins];
		  
		  for(i=0; i<values.size(); i++) {
		    if (values.get(i) >= max) {
		      morethanmax++;
		    }
		    else
		      if (values.get(i) < min) {
		    	  lessthanmin++;
		      }
		      else
			hist[ (int)Math.floor( (values.get(i)-min)/(max-min)*bins ) ]++;
		  }

		  for(i=0; i<hist.length; i++) {
		      hist[i] = hist[i] / (double)values.size();
		  }

		  return hist;
	}


}
