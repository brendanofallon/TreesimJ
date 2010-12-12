package population;

/**
 * Interface for things (typically fitnessProviders) that can recombine.
 * @author brendan
 *
 */
public interface Recombineable {

	public int length(); //The number of sites which can recombine, generally there are length-1 potential breakpoints
	
	public Object getRegion(int min, int max); //Obtain region (typically genetic sequence) that will be exchanged
	
	public void setRegion(int min, int max, Object region); //Set the section between min and max to be the given region
	
}
