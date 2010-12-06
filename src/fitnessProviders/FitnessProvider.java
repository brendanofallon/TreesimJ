package fitnessProviders;

import java.io.Serializable;

import cern.jet.random.engine.RandomEngine;

import xml.XMLParseable;


/**
 * Fitness providers endow Individuals with fitness. Each individual has their own, and they are inherited from parent to offspring 
 * each generation.  
 */
public abstract class FitnessProvider extends XMLParseable implements Serializable {

	public FitnessProvider(String blockName) {
		super(blockName);
	}

	/**
	 * Forces XML attributes to be added to the base class, this must be implemented if we want settings to be saved
	 * from objects (like fitness providers) that don't maintain the settings when being copied
	 * @return 
	 */
	public abstract void addXMLAttributes();
	
	/**
	 * Set the random number generator for this fitness provider - we need to do this when a saved simulation state is restored. 
	 * @param rng
	 */
	public abstract void setRandomEngine(RandomEngine rng);
	
	/**
	 * The key to all fitness models is that they return a fitness value for each individual. This can
	 * be any non-negative real number. The individual's expected number of offspring is this value divided 
	 * by the mean of all these values across Individuals in the generation. 
	 * @return The absolute fitness of this individual
	 */
	public abstract double getFitness();
	
	/**
	 * Most models can ignore this. 
	 * @param w
	 */
	public void setFitness(double w) { };
	
	/**
	 * Called each generation after inheritance, the substrate should be mutated at this point
	 */
	public abstract void mutate();
	
	
	/**
	 * Provides a user-readable description of the model
	 * @return
	 */
	public abstract String getDescription();
	
	
	/**
	 * Obtain a copy of this fitness provider - this is called every time we copy an individual and/or create the offspring of a parent, hence
	 * this is where DNA or other fitness substrates are cloned
	 */
	public abstract FitnessProvider getCopy();
	
	/**
	 * Returns the object whose state is evaluated to provide the fitness value (for instance, a DNASequence, a Double for the QGen models
	 * or a Boolean for the TwoAllele model). This isn't really used too often and may be removed, or given a no-op default soon. 
	 * @return
	 */
	public abstract Object getSubstrate();
	
	//Not currently in use and will probably be removed soon
	public abstract String getStringValue();
	
	//Not currently in use and will probably be removed soon
	public abstract Double getDoubleValue();

}
