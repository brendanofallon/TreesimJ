package mutationModels;

import xml.TJXMLConstants;
import cern.jet.random.engine.RandomEngine;

/**
 * The Timura-Nei 1993 mutation model. There's a constant transversion frequency, but the frequencies of transitions 
 * may vary between purines and pyrimidines. 
 * 
 * @author brendan
 *
 */
public class TN93Mutation extends MutationMatrixModel {

	public static final String XML_ATTR = "Tamura-Nei93";
	public static final String ALPHAR = "alphaR";
	public static final String ALPHAY = "alphaY";
	public static final String BETA = "beta";
	public static final String PIA = "piA";
	public static final String PIG = "piG";
	public static final String PIC = "piC";
	public static final String PIT = "piT";
	
	String descriptionStr;
	
	double piA;
	double piC;
	double piG;
	double piT;
	
	/**
	 * Construct a new TN93 mutation model
	 * @param rng The random number generator
	 * @param mu The mutation rate (the probability of a site mutating per generation)
	 * @param alphaR  The transition rate among purines
	 * @param alphaY The transition rate among pyrimidines
	 * @param beta The transversion rate
	 * @param piA The frequency of A
	 * @param piG The frequency of G
	 * @param piC Frequency of C
	 * @param piT Frequency of T
	 */
	public TN93Mutation(RandomEngine rng, double mu, 
										double alphaR, 
										double alphaY, 
										double beta,
										double piA, 
										double piG, 
										double piC, 
										double piT) 
	{
		super(rng, mu);
		
		double sum = piA+piG+piC+piT;
		piA = piA/sum;
		piG = piG/sum;
		piT = piT/sum;
		piC = piC/sum;
		
		double piR = piA+piG;
		double piY = piC+piT;
		
		this.piA = piA;
		this.piG = piG;
		this.piC = piC;
		this.piT = piT;
		
		//We must ensure that the total departure rate is equal to mu. One way to do this is to normalize
		//each row, but this technique warps the notion that different bases may have different departure
		//rates (that is, in the TN93 model A's may mutate more rapidly than G's if piG is really big. 
		//Similarly, purines may mutate more rapidly than pyrimidines if alphaR is really big.) 
		
		matrix[A][G] = alphaR*piG/piR + beta*piG ;
		matrix[A][C] = beta*piC;
		matrix[A][T] = beta*piT;
		
		matrix[G][A] = alphaR*piA/piR + beta*piA;  
		matrix[G][C] = beta*piC;
		matrix[G][T] = beta*piT;
		
		matrix[C][A] = beta*piA;
		matrix[C][T] = alphaY*piT/piY + beta*piT;
		matrix[C][G] = beta*piG;
	
		matrix[T][A] = beta*piA;
		matrix[T][C] = beta*piG;
		matrix[T][G] = alphaY*piC/piY + beta*piC;
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(TJXMLConstants.MUTATIONRATE, String.valueOf(mu));
		addXMLAttr(ALPHAR, String.valueOf(alphaR));
		addXMLAttr(ALPHAY, String.valueOf(alphaY));
		addXMLAttr(BETA, String.valueOf(beta));
		addXMLAttr(PIA, String.valueOf(piA));
		addXMLAttr(PIC, String.valueOf(piC));
		addXMLAttr(PIG, String.valueOf(piG));
		addXMLAttr(PIT, String.valueOf(piT));
		
		descriptionStr = "Tamura-Nei 1993 Mutation with mu=" + mu + " alpha R: " + alphaR + " alpha Y : " + alphaY + " beta: " + beta + " pi-A: " + piA + " pi-G: " + piG + " pi-T: " + piT + " pi-C: " + piC;
	}
	
	public String getDescription() {
		return descriptionStr;
	}
	
	
	public double getPiA() {
		return piA;
	}
	
	public double getPiG() {
		return piG;
	}
	
	public double getPiC() {
		return piC;
	}
	
	public double getPiT() {
		return piT;
	}

	@Override
	public double getRecombinationRate() {
		// TODO Auto-generated method stub
		return 0;
	}
}
