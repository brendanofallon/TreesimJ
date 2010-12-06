package gui.fitnessConfigurators;

import javax.swing.JComponent;

import xml.XMLConfigurable;
import cern.jet.random.engine.RandomEngine;
import fitnessProviders.FitnessProvider;

/**
 * These classes can display a JComponent, then build a FitnessProvider based on the settings in the component
 * @author brendan
 *
 */
public interface FitnessModelConfigurator extends XMLConfigurable {

	public JComponent getComponent();	//The GUI component used to create the model
	
	public String getIdentifier();	//A short, user-readable name of the model
	
	public String getDescription();	//A user-readable description of the fitenss model

	public FitnessProvider getFitnessModel(RandomEngine rng);//Retrieve the fitness provider created by this configurator
	
}
