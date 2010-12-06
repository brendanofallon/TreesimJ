package mutationModels;

import javax.swing.JComponent;

import xml.XMLConfigurable;
import cern.jet.random.engine.RandomEngine;

public interface MutationModelConfigurator extends XMLConfigurable {
	
	public JComponent getComponent();	//The component that allows the user to set various options regarding the demographic model
	
	public MutationModel getMutationModel(RandomEngine rng, double mu); //Create the model from the options in the component
	
	public String getIdentifier();	//A short, user-readable description of the model, i.e. "Constant pop. size"
	
	public String getDescription(); //A longer, user-readable description of the model i.e. "A population whose size remains at a constant value"

}
