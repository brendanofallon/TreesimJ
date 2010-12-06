package statistics;

import java.io.PrintStream;

import xml.TJXMLConstants;

/**
 * Histogram statistics store their values in a histogram, not a list of numbers
 * @author brendan
 *
 */
public abstract class HistogramStatistic extends Statistic {

	//These are just defaults - they may be overridden if the user specifies so via
	//a Statistic configuration tool
	protected double dist_min;
	protected double dist_max;
	protected int over = 0;
	protected int under = 0;
	protected int bins;
	protected Histogram histo;
	protected int count = 0;
	
	public HistogramStatistic() { }
	
	public void summarize(PrintStream out) {
		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
		out.println("Number of samples : \t" + histo.getCount());
		out.println("Histogram :");
		
		emitHistogram(out);
	}
	
	public void emitHistogram(PrintStream out) {
		String histoStr = histo.toString();
		out.println(histoStr);
	}
	
	public void clear() {
		histo.clear();
	}
	
	public int getHistoBins() {
		return histoBins;
	}
	
	public Double getUserHistoMin() {
		return userHistoMin;
	}
	
	public void setUserHistoBins(int bins) {
		this.histoBins = bins;
		double newMin = dist_min;
		double newMax = dist_max;
		if (userHistoMin!=null) 
			newMin = userHistoMin;
		if (userHistoMax!=null)
			newMax = userHistoMax;
		
		double binSize = (newMax-newMin)/bins;
		
		histo = new Histogram(bins, newMin, binSize);		
		addXMLAttr(TJXMLConstants.HISTOBINS, String.valueOf(bins));
	}
	
	public void setUserHistoMin(Double min) {
		this.userHistoMin = min;
		
		double newMin = userHistoMin;
		double newMax = dist_max;
		int newBins = histoBins;
		if (userHistoMax!=null)
			newMax = userHistoMax;
		
		double binSize = (newMax-newMin)/newBins;
		
		histo = new Histogram(newBins, newMin, binSize);
		addXMLAttr(TJXMLConstants.HISTOMIN, String.valueOf(min));
	}
	
	public Double getUserHistoMax() {
		return userHistoMax;
	}
	
	public void setUserHistoMax(Double max) {
		this.userHistoMax = max;
		
		
		double newMin = dist_min;
		double newMax = max;
		int newBins = histoBins;
		if (userHistoMin!=null)
			newMin = userHistoMin;
		
		double binSize = (newMax-newMin)/newBins;
		
		histo = new Histogram(newBins, newMin, binSize);	
		addXMLAttr(TJXMLConstants.HISTOMAX, String.valueOf(max));	
	}

}
