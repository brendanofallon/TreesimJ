package statistics.dna;

import java.util.List;

import population.Locus;

import statistics.Collectible;
import statistics.Options;
import statistics.Statistic;

public class TajimasD extends DNAStatistic {

	NucDiversity nucD = null;	
	SegregatingSites segSites = null;		
	int sampleSize = 20;
	double a1;	//Values used in computing the statistic, notation follows that of the paper (Tajima, 1993)
	double e1;
	double e2;
	
	public static final String identifier = "Tajimas D";
	
	double lastPi = 0;
	double lastTW = 0;
	
	public TajimasD() {
		nucD = new NucDiversity();	
		segSites = new SegregatingSites();	
		canHandleRecombination = true;
	}
	
	
//	public String getScreenLogStr() {
//		Double val = getLastValue();
//		if (val == Double.NaN)
//			return "NaN";
//		else
//			return "(" + formatter.format(lastPi) + " - " + formatter.format(lastTW) + ") : " + formatter.format(val);
//	}
	
	/**
	 * Overrides DNAStatistic.setSampleSize, we need to know the sampleSize to compute some intermediate statistics
	 * that are in turn used to compute the value. We do it here. 
	 */
	public void setSampleSize(int size) {
		this.sampleSize = size;
		
		double n = sampleSize;
		
		System.out.println("Setting sample size of Taj D to : " + n);
		
		a1 = 0.0;
		double a2 = 0.0;
		for(double i=1; i<sampleSize; i++) {
			a1 += 1.0/i;
			a2 += 1.0/(i*i);
		}
		
		
		//double b1 = (n+1.0)/(3.0*(n-1.0));
		double b2 = 2.0*(n*n + n + 3.0)/(9.0*n*(n-1.0));
		//double c1 = b1 - 1.0/a1;
		double c2 = b2 - (n+2.0)/(a1*n) + a2/(a1*a1);
	
		e1 = ( (n+1.0)/3.0/(n-1.0)-1.0/a1 )/a1;
		e2 = c2/(a1*a1 + a2);
	}
	
	public boolean showOnScreenLog() {
		return true;
	}

	public void collect(Collectible pop) {
		double pi;
		double S;
		
		List<Locus> sample = pop.getSample(sampleSize);
		
		pi = nucD.collectPiFromSample(sample);
		lastPi = pi;
		
		S = segSites.computeSegSitesFromSample(sample);
		lastTW = S/a1;
		
		double dif = pi-S/a1;
		double root = e1*S + e2*S*(S-1.0);
		double difNorm = dif / Math.sqrt( root );
		
		if (root<=0) { //This can happen if there's only one segregating site
			values.add(0.0);
			return;
		}
		
		values.add(difNorm);
	}


	public String getDescription() {
		return "Tajima's D neutrality indicator";
	}


	public String getIdentifier() {
		return identifier;
	}


	public Statistic getNew(Options ops) {
		return new TajimasD();
	}

}
