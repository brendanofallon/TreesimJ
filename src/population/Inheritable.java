package population;

import java.io.Serializable;

/**
 * Some things in the world are inherited but do not directly affect fitness. These items
 * must implement this interface. Inheritables may also mutate, and mutate will be called on them 
 * whenever they are passed on to a new Individual. 
 *   Currently DNA sequences are the only type of generic inheritable, this is so they can be linked
 *   to other types of fitness models. It would be handy to, say, implement a microsatellite Inheritable
 *   or a quantitative-genetic inheritable item at some point. 
 * 
 * @author brendan
 *
 */
public interface Inheritable extends Serializable {

	public void mutate();
	
	public Inheritable getCopy();
	
	public String getStringValue();
	
	public Double getDoubleValue();
}
