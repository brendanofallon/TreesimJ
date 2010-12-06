package statistics.fitness;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import cern.jet.random.Uniform;

import population.Locus;
import statistics.Collectible;
import statistics.NoHistoConfigTool;
import statistics.Options;
import statistics.Statistic;

/**
 * A statistic that computes the mean fitness of a (randomly selected) lineage as it is traced back in
 * time
 * @author brendan
 *
 */
public class MeanLineageFitness extends Statistic {

	public final static String identifier = "Lineage Fitness (Mean)";
	
	List<Double> fitnessSums; 
	List<Integer> counts;
	Uniform uniGen = null;
	
	int maxDepth = Integer.MAX_VALUE; //Don't collect at depths beyond this value
	
	public MeanLineageFitness() {
		fitnessSums = new ArrayList<Double>();
		counts = new ArrayList<Integer>();
	}
	
	/**
	 * We need ancestral info
	 */
	public boolean requiresGenealogy() {
		return true;
	}
	
	@Override
	public Statistic getNew(Options ops) {
		return new MeanLineageFitness();
	}
	
	@Override
	public String getIdentifier() {
		return identifier;
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
		return new NoHistoConfigTool(this);
	}
	
	/**
	 * Don't collect me during the burnin period
	 */
	public boolean collectDuringBurnin() {
		return false;
	}
	
	@Override
	public void collect(Collectible pop) {
		if (uniGen == null) {
			uniGen = new Uniform(rng);
		}
		int which =  uniGen.nextIntFromTo(0, pop.size()-1);
		Locus ind = pop.getInd(which);
		int depth = 0;
		while(ind != null && depth < maxDepth) {
			//Expand the lists as necessary
			if (fitnessSums.size() == depth) {
				fitnessSums.add(0.0);
				counts.add(0);
			}
			
			fitnessSums.set(depth,  fitnessSums.get(depth)+ind.getFitness());
			counts.set(depth, counts.get(depth)+1);
			depth++;
			ind = ind.getParent();
		}

	}
	
	/**
	 * We override the custom summary since we don't really have a list of values to report
	 */
	public void summarize(PrintStream out) {
		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
		out.println("Depth \t Fitness \t Samples");
		for(int i=0; i<fitnessSums.size(); i++) {
			out.println( i + "\t" + fitnessSums.get(i)/(double)counts.get(i) + "\t" + counts.get(i));
		}
	}
	
	@Override
	public String getDescription() {
		return "Mean fitness of lineages traced backwards in time";
	}
}
