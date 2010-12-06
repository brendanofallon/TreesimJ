package statistics;

import population.Locus;
import fitnessProviders.TwoAlleleFitness;

public class TwoAlleleFrequency extends Statistic {
	
	public static final String identifier = "Frequency of allele in two-allele fitness model";
	
	public String getIdentifier() {
		return identifier;
	}
	
	public TwoAlleleFrequency() { };
	
	public TwoAlleleFrequency getNew(Options ops) {
		this.options = ops;
		return new TwoAlleleFrequency();
	}
	
	public void collect(Collectible pop) {
		double sum = 0;
		if (pop.getInd(0).getFitnessData() instanceof TwoAlleleFitness) {
			for(Locus ind : pop.getList()) {
				if (((TwoAlleleFitness)ind.getFitnessData()).isMoreFitAllele())
					sum++;
			}

			values.add(sum/(double)pop.size());
		}
	}


	public String getDescription() {
		return "Frequency of the more fit allele in the two allele model";
	}
	
	public boolean showOnScreenLog() {
		return true;
	}

}
