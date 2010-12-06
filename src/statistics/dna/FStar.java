package statistics.dna;

import java.util.Arrays;
import java.util.List;

import population.Locus;

import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

/**
 * Another 'neutrality statistic', similar to Tajima's D and Fu & Li's D*, but this time uses the difference between the
 * nucleotide diversity and the number of singletons. 
 * @author brendan
 *
 */
public class FStar extends DNAStatistic {

public static final String identifier = "Fu & Li's F*";
	
	double an = 0;
	double vf = 0;
	double uf = 0;
	
	double[] counts = new double[4];
	
	NucDiversity nucD = null;
	SegregatingSites segSites = null;
	
	public FStar() {
		nucD = new NucDiversity();
		segSites = new SegregatingSites();
	}
	
	@Override
	public Statistic getNew(Options ops) {
		this.options = ops;
		return new FStar();
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
		
		vf = (dn+2*(n*n+n+3)/(9.0*n*(n-1)) - 2.0/(n-1)*(4.0*bn-6.0+8.0/n))/(an*an+bn);
		uf = (n/(n-1.0)+(n+1)/3/(n-1)-4/n/(n-1)+2*(n+1)/(n-1)/(n-1)*(anp1-2*n/(n+1)))/an-vf;
		
	}
	
	public boolean showOnScreenLog() {
		return true;
	}
	
	@Override
	public void collect(Collectible pop) {
		List<Locus> sample = pop.getSample(sampleSize);
		
		double n = sampleSize;
		
		double eta = segSites.computeSegSitesFromSample(sample);
		double pi = nucD.collectPiFromSample(sample);
		double etaS = countSingletons(sample);
		
		double num = pi - (n-1.0)/n*etaS; 
		double var = uf*eta + vf*eta*eta;
		
		double root = Math.sqrt(var);
		
		double dStar;
		if (eta==0)
			dStar = 0;
		else
			dStar = num / root;
		
		values.add(dStar);
	}
	
	private int countSingletons(List<Locus> sample) {
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
	private boolean isSingleton(List<Locus> sample, int site) {
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
		return "Fu and Li's F* neutrality statistic";
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}
	
}
