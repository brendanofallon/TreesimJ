package mutationModels;

import siteModels.SiteFitnesses;
import xml.TJXMLConstants;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import dnaModels.DNASequence;


/**
 * The simplest possible mutation model, where each base mutates to every other base with
 * equal probability
 * 
 * @author brendan
 *
 */
public class JukesCantorMutation extends MutationMatrixModel {

	public static final String XML_ATTR = "Jukes-Cantor";
	public static final String XML_MUTATIONRATE = "mutation.rate";

	
	public JukesCantorMutation(RandomEngine rng, Double bpMutationRate) {
		super(rng, bpMutationRate);
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				if (i==j)
					matrix[i][j] = 0;
				else {
					matrix[i][j] = 0.33333333334;
							
				}
					
			}
		}
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(XML_MUTATIONRATE, String.valueOf(mu));
	}

	
	public String getDescription() {
		return "Single parameter JC mutation with mu = " + mu;
	}
	
	public double getMu() {
		return mu;
	}


	public double getPiA() {
		return 0.25;
	}
	
	public double getPiG() {
		return 0.25;
	}
	
	public double getPiC() {
		return 0.25;
	}
	
	public double getPiT() {
		return 0.25;
	}

	
}
