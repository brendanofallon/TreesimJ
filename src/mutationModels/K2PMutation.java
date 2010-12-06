package mutationModels;

import xml.TJXMLConstants;
import cern.jet.random.engine.RandomEngine;


/**
 * Implements 'Kimura 2 parameter mutation, which allows for a transition / transversion ratio, but not
 * any sort of base-frequencies model. 
 * @author brendan
 *
 */
public class K2PMutation extends MutationMatrixModel {

	public static final String XML_ATTR = "Kimura2Parameter";
	public static final String XML_TTRATIO = "TTRatio";
	
	double ttRatio;
	
	public K2PMutation(RandomEngine rng, double mutationProb, double ttRatio) {
		super(rng, mutationProb);
	
		double transitionProb = ttRatio / (2.0+ttRatio);
		double transversionProb = 1.0 / (2.0+ttRatio); 
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				if (i==j)
					matrix[i][j] = 0;
				else {
					if ((baseForIndex(i)=='A' && baseForIndex(j)=='G') ||
						(baseForIndex(i)=='G' && baseForIndex(j)=='A') ||
						(baseForIndex(i)=='C' && baseForIndex(j)=='T') ||
						(baseForIndex(i)=='T' && baseForIndex(j)=='C')) {
						matrix[i][j] = transitionProb;
					}
					else {
						matrix[i][j] = transversionProb;
					}
							
				}
					
			}
		}
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(TJXMLConstants.MUTATIONRATE, String.valueOf(mu));
		addXMLAttr(XML_TTRATIO, String.valueOf(ttRatio));
	}

	public String getDescription() {
		return "Kimura two parameter mutation with mu=" + mu + " and ts/tv ratio of " + ttRatio;
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
