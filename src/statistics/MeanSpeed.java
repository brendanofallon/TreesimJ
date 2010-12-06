package statistics;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;



/**
 * Computes (roughly) the speed of the simulation in gens / sec over a 10*generationInterval window
 * @author brendan
 *
 */
public class MeanSpeed extends Statistic {

	public static final String identifier = "Simulation speed (gens / sec)";
	
	int lastGenNum = -1;
	long lastTime = -1;
	NumberFormat formatter = new DecimalFormat("###0.0");
	
	public MeanSpeed() {
		values = new ArrayList<Double>();
	}
	
	public MeanSpeed getNew(Options ops) {
		this.options = ops;
		return new MeanSpeed();
	}
	
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void summarize(PrintStream out) {
		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
		out.println("Number of samples : \t" + values.size());
		out.println("Mean :\t" + getMean());
	}
	
	public String getDescription() {
		return " Processing speed (gens/sec) : ";
	}
	
	public void collect(Collectible pop) {
		if (pop.getCurrentGenNumber() == lastGenNum) 
			return;
		
		if (pop.getCurrentGenNumber() < lastGenNum)
			lastGenNum = 0;
		
		long currentTime = System.currentTimeMillis();
		
		if (lastTime>0 && lastGenNum>=0) {
			long elapsedTime = currentTime - lastTime;
			int elapsedGens = pop.getCurrentGenNumber() - lastGenNum;

			values.add( 1000.0*(double)elapsedGens / (double)elapsedTime);
		}
		
		lastTime = currentTime;
		lastGenNum = pop.getCurrentGenNumber();

	}

	public boolean showOnScreenLog() {
		return true;
	}
	
	public String getScreenLogStr() {
		return formatter.format( getLastValue() );
	}

}
