package treesimj;

import demographicModel.DemographicModel;
import population.Population;
import statistics.Statistic;

/**
 * The base class of exceptions that are thrown during the simulation run. We use this to
 * wrap 'real' exceptions that are thrown during calls to reproduce(..) or collect(..) so that
 * we can associate the exception with a source operation or object (such as a demographic 
 * model or Statistic) 
 * @author brendan
 *
 */
public class TJRunTimeException extends Exception {

	Object tjSource = null;
	
	Exception originalException;
	
	public TJRunTimeException(Object source, Exception ex) {
		tjSource = source;
		originalException  = ex;
	}
	
	public Exception getSourceException() {
		return originalException;
	}
	
	public Object getTJSource() {
		return tjSource;
	}
	
}
