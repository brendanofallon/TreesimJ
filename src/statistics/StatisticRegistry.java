package statistics;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import statistics.dna.DStar;
import statistics.dna.Divergence;
import statistics.dna.FStar;
import statistics.dna.FrequencySpectrum;
import statistics.dna.HaplotypeDiversity;
import statistics.dna.MutNumDistro;
import statistics.dna.MutationRate;
import statistics.dna.NucDiversity;
import statistics.dna.SegregatingSites;
import statistics.dna.TajimasD;
import statistics.dna.WattersonsTheta;
import statistics.fitness.AbsoluteFitnessDistro;
import statistics.fitness.MeanFitness;
import statistics.fitness.MeanLineageFitness;
import statistics.fitness.StdevFitness;
import statistics.fitness.Tau;
import statistics.treeShape.CladeSizeDistro;
import statistics.treeShape.CoalIntervalStat;
import statistics.treeShape.CollessIndex;
import statistics.treeShape.NumBreakPoints;
import statistics.treeShape.PairwiseCTime;
import statistics.treeShape.SackinsIndex;
import statistics.treeShape.SackinsVariance;
import statistics.treeShape.SampleTMRCA;
import statistics.treeShape.TMRCA;

/**
 * Stores a list of all known Statistics (Data collectors, in GUI terms) and handles
 * their creation. Not entirely clear how useful this is, the general idea is that it may be 
 * useful to have some object who knows what all the various statistics are. 
 * 
 * @author brendan
 *
 */
public class StatisticRegistry {

	Hashtable<String, Statistic> allStats;
	
	public StatisticRegistry() {
		allStats = new Hashtable<String, Statistic>();
	
		add(new BranchRateStatistic());
		add(new CladeSizeDistro());
		add(new CollessIndex());
		add(new Divergence());
		add(new MeanFitness());
		add(new MeanSpeed());
		add(new MutNumDistro());
		add(new NucDiversity());
		add(new PairwiseCTime());
		//add(new RelativeFitnessDistro());
		add(new SackinsIndex());
		add(new SackinsVariance());
		add(new SegregatingSites());
		add(new StdevFitness());
		add(new Tau());
		add(new TMRCA());
		add(new SimpleTreeSampler());
		add(new TwoAlleleFrequency());
		add(new PopulationSizeStatistic());
		add(new TajimasD());
		add(new HaplotypeDiversity());
		add(new SampleTMRCA());
		add(new MutationRate());
		add(new CoalIntervalStat());
		add(new FrequencySpectrum());
		add(new WattersonsTheta());
		add(new DStar());
		add(new FStar());
		add(new MeanLineageFitness());
		add(new AbsoluteFitnessDistro());
		add(new NumBreakPoints());
		add(new NodeCount());
		add(new BreakpointDensity());
		add(new TMRCADensity());
	}
	
	public Statistic getInstance(String id) {
		Statistic stat = allStats.get(id);
		if (stat == null) {
			System.err.println("Could not find statistic for identifier : " + id);
			return null;
		}
		else {
			stat = stat.getNew(null);
			return stat;
		}
	}
	
	public Statistic getInstance(String id, statistics.Options ops) {
		Statistic stat = allStats.get(id);
		stat = stat.getNew(ops);
		return stat;
	}
	
	public List<Statistic> getAsList() {
		ArrayList<Statistic> statList = new ArrayList<Statistic>();
	    java.util.Enumeration keys = allStats.keys();

	    while( keys.hasMoreElements() )  {
	        Object key = keys.nextElement();
	        statList.add( allStats.get(key));
	    }
	    return statList;
	}
	
	private void add(Statistic stat) {
		allStats.put(stat.getIdentifier(), stat);
	}
}
