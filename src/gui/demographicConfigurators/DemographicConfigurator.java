package gui.demographicConfigurators;

import java.util.List;

import javax.swing.JComponent;

import population.Population;

import xml.XMLConfigurable;
import demographicModel.DemographicModel;

/**
 * All things which allow the user to construct a demographic model should implement this interface. 
 * @author brendan
 *
 */
public interface DemographicConfigurator extends XMLConfigurable {

	public JComponent getComponent();	//The jcomponent that allows the user to set various options regarding the demographic model
	
	public DemographicModel getDemographicModel(); //Create the model from the options in the component and the list of populations
	
	public String getIdentifier();	//A short, user-readable description of the model, i.e. "Constant pop. size"
	
	public String getDescription(); //A longer, user-readable description of the model i.e. "A population whose size remains at a constant value"
	
}
