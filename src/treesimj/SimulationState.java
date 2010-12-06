package treesimj;

import gui.OutputManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import population.Population;

import statistics.Statistic;

import demographicModel.DemographicModel;

/**
 * Represents the current state of the simulation - not just what the population looks like, but all of the
 * model settings and Statistics as well.  This is the object that is stored and re-read when the user chooses
 * 'Save simulation state' or 'Load simulation state'
 * 
 * @author brendan
 *
 */
public class SimulationState implements Serializable {
	
	DemographicModel demoModel; //The demographic model
	String settings; 			//A string representing the current settings, which are loaded with the population
	OutputManager outputHandler;//The output handler, which contains the list of Statistics used
	ArrayList<Statistic> stats; //All statistics currently used
	
	public SimulationState(String settingsStr, OutputManager outputManager, DemographicModel demoModel, ArrayList<Statistic> stats) {
		this.settings = settingsStr;
		this.outputHandler = outputManager;
		this.demoModel = demoModel;
		this.stats = stats;
	}
	
//	public Population getPopulation() {
//		return outputHandler.getPopulation();
//	}
	
	public String getSettingsString() {
		return settings;
	}
	
	public OutputManager getOutputManager() {
		return outputHandler;
	}
	
	public ArrayList<Statistic> getStatistics() {
		return stats;
	}

	public DemographicModel getDemoModel() {
		return demoModel;
	}

}
