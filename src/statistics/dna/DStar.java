package statistics.dna;

import java.util.Arrays;
import java.util.List;

import population.Individual;

import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

/**
 * Compute Fu and Li's D* statistic, which is similar to Tajima's D but uses the estimated internal vs. external branch lengths of the tree
 * @author brendan
 *
 */
public class DStar extends DNAStatistic {

	public static final String identifier = "Fu & Li's D*";
	
	double an = 0;
	double vd = 0;
	double ud = 0;
	
	double[] counts = new double[4];
	
	SegregatingSites segSites;
	
	public DStar() {
		segSites = new SegregatingSites();
	}
	
	@Override
	public Statistic getNew(Options ops) {
		this.options = ops;
		return new DStar();
	}
	
	
	/**
	 * Overrides DNAStatistic.setSampleSize, we need to know the sampleSize to compute some intermediate statistics
	 * that are in turn used to compute the value. We do it here. 
	 */
	public void setSampleSize(int size) {
		this.sampleSize = size;
		
		double n = sampleSize;
		
		an = 0.0;
		double bn = 0.0;
		for(double i=1; i<sampleSize; i++) {
			an += 1.0/i;
			bn += 1.0/(i*i);
		}
		
		double anp1 = an+1.0/(double)sampleSize;
		
		double cn = 2*(n*an-2*(n-1.0))/((n-1.0)*(n-2.0));
		
		double dn = cn + (n-2)/(n-1)/(n-1)+2/(n-1)*(1.5- (2*anp1-3)/(n-2)-1.0/n);
		
		vd = (n/(n-1.0)*n/(n-1.0)*bn+an*an*dn-2*n*an*(an+1)/(n-1.0)/(n-1.0))/(an*an+bn);  //1+ an*an / (bn + an*an)*(cn- (n+1)/(n-1));
		ud = n/(n-1.0)*(an-n/(n-1))-vd;
		
	}
	
	public boolean showOnScreenLog() {
		return true;
	}
	
	@Override
	public void collect(Collectible pop) {
		List<Individual> sample = pop.getSample(sampleSize);
		
		double n = sampleSize;
		
		double eta = segSites.computeSegSitesFromSample(sample); //The wonders of the infinite sites assumption
		double etaS = countSingletons(sample);
		
		double num = n/(n-1.0)*eta - an*etaS; 
		double var = ud*eta + vd*eta*eta;
		
		double root = Math.sqrt(var);
		
		double dStar;
		if (eta==0)
			dStar = 0;
		else
			dStar = num / root;
		
		values.add(dStar);
	}
	
	private int countSingletons(List<Individual> sample) {
		int singletons = 0;
		for(int i=0; i<sample.get(0).getPrimaryDNA().length(); i++) {
			if (isSingleton(sample, i))
				singletons++;
		}
		return singletons;
	}
	
	/**
	 * Returns true if the base with the second-greatest frequency at this site appears exactly once
	 * @param sample
	 * @param site
	 * @return
	 */
	private boolean isSingleton(List<Individual> sample, int site) {
		for(int i=0; i<counts.length; i++)
			counts[i] = 0;
		
		for(int i=0; i<sampleSize; i++) {
			switch( sample.get(i).getPrimaryDNA().getBaseChar(site) ) {
			case 'A' : counts[0]++; break;
			case 'G' : counts[1]++; break;
			case 'C' : counts[2]++; break;
			case 'T' : counts[3]++; break;
			}
		}

		Arrays.sort(counts);
		
		if(counts[2]==1)
			return true;
		else
			return false;

	}

	@Override
	public String getDescription() {
		return "Fu and Li's D* neutrality statistic";
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	

}
