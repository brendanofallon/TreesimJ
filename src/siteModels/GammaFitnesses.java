package siteModels;

import java.util.Arrays;

import mutationModels.MutationModel;

import xml.TJXMLConstants;
import cern.jet.random.Gamma;
import cern.jet.random.engine.RandomEngine;
import dnaModels.BitSetDNASequence;
import dnaModels.DNASequence;

public class GammaFitnesses extends SiteFitnesses {

	public static final String XML_ATTR = "gamma.sitemodel";
	
	double[] ws;
	double mean = 0;
	double var = 0;
	double actualmean = 0; //The actual mean (and variance) may differ from the parameters a bit, it may be nice to know them
	double actualVariance = 0;
	boolean isValid = false;
	RandomEngine rng;
	
	public GammaFitnesses(RandomEngine rng, double mean, double stdev) {
		super(TJXMLConstants.SITE_MODEL);

		this.mean = mean;
		this.var = stdev*stdev;
		this.rng = rng;
		
		addXMLAttr(TJXMLConstants.TYPE, XML_ATTR);
		addXMLAttr(TJXMLConstants.MEAN,  String.valueOf(mean));
		addXMLAttr(TJXMLConstants.STDEV, String.valueOf(stdev));
	}
	
	
	public DNASequence generateMasterSequence(RandomEngine rng, int length, MutationModel mutModel) {
		DNASequence master =  new BitSetDNASequence(rng, length, mutModel);
		ws = new double[length];
		
		double max = 0;
		
		if (var<=0) {
			for(int i=0; i<length; i++)
				ws[i] = mean;
		}
		else {
			double alpha = mean*mean/var;
			double lambda = mean/var;
			Gamma gamGen = new Gamma(alpha, lambda, rng);
			for(int i=0; i<length; i++) {
				ws[i] = gamGen.nextDouble();
				actualmean += ws[i];
				if (max < ws[i]) 
					max = ws[i];
			}
		}
		
		actualmean /= (double)length;
		for(int i=0; i<length; i++)  {
			actualVariance += (ws[i]-actualmean)*(ws[i]-actualmean);
		}
		actualVariance /= length;	
		
		
//		System.out.println("Actual mean : " + actualmean + " actual stdev. : " + Math.sqrt(actualVariance));
//		System.out.println("Distribution of selection coefficients :");
//		double[] hist = getHistogram(50, 0, max);
//		for(int i=0; i<50; i++) {
//			System.out.println((double)i/50.0*max*33000 + " : " + hist[i]);
//		}
		
		//No reason not to keep these sorted, right?
		Arrays.sort(ws);
		addXMLAttr(TJXMLConstants.LENGTH, String.valueOf(length));
		return master;
		
	}
	
	public double[] getHistogram(int bins, double min, double max ) {
		int i;
		  int morethanmax = 0;
		  int lessthanmin = 0;
		  double binsize = (max-min) / bins;
		  double[] hist = new double[bins];
		  
		  for(i=0; i<ws.length; i++) {
		    if (ws[i] >= max) {
		      morethanmax++;
		    }
		    else
		      if (ws[i] < min) {
		    	  lessthanmin++;
		      }
		      else
			hist[ (int)Math.floor( (ws[i]-min)/(max-min)*bins ) ]++;
		  }

		  for(i=0; i<hist.length; i++) {
		      hist[i] = hist[i] / (double)ws.length;
		  }

		  return hist;
	}
	
	public String getDescription() {
		return "Sites have gamma-distributed selection coefficients with mean: " + actualmean + " and stdev : " + Math.sqrt(actualVariance);
	}

	public double getSiteFitness(int site, DNASequence seq) {
		return ws[site];
	}

	public int numSites() {
		return ws.length;
	}


}
