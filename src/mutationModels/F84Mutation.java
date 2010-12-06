package mutationModels;

import xml.TJXMLConstants;
import cern.jet.random.engine.RandomEngine;

/**
 * F84 mutation provides for separate base frequencies as well as separate transition and transversion rates,
 * it is a special case of TN94 with alphaR=alphaY
 * @author brendan
 *
 */
public class F84Mutation extends MutationMatrixModel {

	public static final String XML_ATTR = "F84";
	public static final String ALPHA = "alpha";
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
	 * Construct a new F84 mutation model, which is the same thing as TN93 with alphar = alphay
	 * @param rng The random number generator
	 * @param mu The mutation rate (the probability of a site mutating per generation)
	 * @param alpha  The transition rate
	 * @param beta The transversion rate
	 * @param piA The frequency of A
	 * @param piG The frequency of G
	 * @param piC Frequency of C
	 * @param piT Frequency of T
	 */
	public F84Mutation(RandomEngine rng, double mu, 
										double alpha, 
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
		
		matrix[A][G] = alpha*piG/piR + beta*piG ;
		matrix[A][C] = beta*piC;
		matrix[A][T] = beta*piT;
		
		matrix[G][A] = alpha*piA/piR + beta*piA;  
		matrix[G][C] = beta*piC;
		matrix[G][T] = beta*piT;
		
		matrix[C][A] = beta*piA;
		matrix[C][T] = alpha*piT/piY + beta*piT;
		matrix[C][G] = beta*piG;
	
		matrix[T][A] = beta*piA;
		matrix[T][C] = beta*piG;
		matrix[T][G] = alpha*piC/piY + beta*piC;
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(TJXMLConstants.MUTATIONRATE, String.valueOf(mu));
		addXMLAttr(ALPHA, String.valueOf(alpha));
		addXMLAttr(BETA, String.valueOf(beta));
		addXMLAttr(PIA, String.valueOf(piA));
		addXMLAttr(PIC, String.valueOf(piC));
		addXMLAttr(PIG, String.valueOf(piG));
		addXMLAttr(PIT, String.valueOf(piT));
		
		descriptionStr = "F84 (Felsenstein '84) Mutation with mu=" + mu + " alpha : " + alpha + " beta: " + beta + " pi-A: " + piA + " pi-G: " + piG + " pi-T: " + piT + " pi-C: " + piC;
	}

	
	public String getDescription() {
		return descriptionStr;
	}

	
	public double getPiA() {
		return piA;
	}


	public double getPiC() {
		return piC;
	}

	
	public double getPiG() {
		return piG;
	}

	public double getPiT() {
		return piT;
	}

}
