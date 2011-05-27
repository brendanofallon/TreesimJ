package statistics;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import population.Locus;

import tree.DiscreteGenTree;
import treesimj.TreesimJView;


/**
 * Collects a tree from a sample of individuals from the population. The tree is a brand new tree, not a list of 
 * references to individuals in the actual population. The main purpose of this class is not to calculate a single
 * statistic, but to provide a central way to get trees from the population, since all TreeStatistic classes will
 * need a sample tree to do their work. Also, we just generate one tree from the population with this, instead of all
 * TreeStatistic classes doing it repeatedly on their own. 
 *  
 * @author brendan
 *
 */
public class SimpleTreeSampler extends TreeSampler {
	
	public static final String identifier = "Samples trees from the population";
	
	int treesCounted = 0;
	int calls = 0;
	int sampleSize;
	ArrayList<Double> times;
	int ctimesCounted = 0;
	int popSize;
	DiscreteGenTree lastTree = null;
	
	
	public SimpleTreeSampler() { }
	
	private SimpleTreeSampler(int sampleSize) {
		values = new ArrayList<Double>();
		this.sampleSize = sampleSize;
		times = new ArrayList<Double>();
		for(int i=0; i<sampleSize-1; i++)
			times.add(0.0);
	}

	public Statistic getNew(Options ops) {
		this.options = ops;
		Integer sample = (Integer)ops.optData;
		return new SimpleTreeSampler(sample);
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public DiscreteGenTree getLastTree() {
		return lastTree;
	}
	
	public void summarize(PrintStream out) {
		out.println("Summary for " + getDescription() + " ");
		out.println("Total Number of trees counted : \t" + treesCounted);
	}
	
	public void collect(Collectible pop) {
		calls++;
		lastTree = null;
		
		if (! TreesimJView.storeAncestry) {
			List<Locus> inds = pop.getSample(sampleSize);
			Locus fakeRoot = new Locus(rng);
			for(Locus ind : inds) {
				fakeRoot.addOffspring(ind);
				ind.setParent(fakeRoot);
			}
			DiscreteGenTree tree = new DiscreteGenTree(fakeRoot, inds);
			lastTree = tree;
			treesCounted++;
		}
		else {
			DiscreteGenTree tree = pop.getSampleTree(sampleSize);
			//DiscreteGenTree tree = new DiscreteGenTree(root);
			lastTree = tree;
			ArrayList<Double> nodeTimes = tree.getNodeTimes();

			values.add(nodeTimes.get(nodeTimes.size()-1));
			if (nodeTimes.size() != sampleSize-1) {
				//System.err.println("Uh-oh, didn't get exactly samplesize-1 coalescent interval times (got " + nodeTimes.size() + ")\n");
			} 
			else  {
				//System.out.println("times size : " + times.size() + " node times size: " + nodeTimes.size() + " samplesize-1  : " + (sampleSize-1));
				for(int i=0; i<sampleSize-1; i++) {
					times.set(i, times.get(i)+nodeTimes.get(i));
				}
				ctimesCounted++;
			}

			treesCounted++;

			popSize = pop.size();
		}
	}
	
	public void emitMeanCoalTimes() {
		int lineages = sampleSize;
		double totTime = 0;
		for(int i=0; i<times.size(); i++)
			times.set(i, times.get(i)/ctimesCounted);
		
		for(int i=0; i<times.size(); i++) {
			lineages = sampleSize-i;
			int nC2 = lineages*(lineages-1)/2;
			double Etime = (double)popSize/(double)nC2;
			totTime += Etime;
			if (i>0) 
				output.println(i + "\t" + times.get(i) + "\t" + totTime + "\t" + (times.get(i)-times.get(i-1)) + "\t" + Etime);
			else
				output.println(i + "\t" + times.get(i) + "\t" + totTime + "\t" + (times.get(i)) + "\t" + Etime);
			
		}
	}
	
	public void clear() {
		values.clear();
		times.clear();
		for(int i=0; i<sampleSize-1; i++)
			times.add(0.0);
		ctimesCounted =0 ;
	}
	
	public String getDescription() {
		return " Tree sampler. Sample size : " + sampleSize + " avg. TMRCA : ";
	}


}
