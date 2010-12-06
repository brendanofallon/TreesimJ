package statistics;

import java.text.DecimalFormat;

import javax.swing.JFrame;



public class PopulationSizeStatistic extends Statistic {

	public static final String identifier = "Population size";

	public PopulationSizeStatistic() {
		formatter = new DecimalFormat("######00");
	}
	
	public void collect(Collectible pop) {
		values.add((double)pop.size());
	}


	public String getDescription() {
		return "Population size";
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public boolean showOnScreenLog() {
		return true;
	}
	
	public boolean hasConfigurationTool() {
		return false;
	}
	
	public JFrame getConfigurationTool() {
		return null;
	}
	
	@Override
	public Statistic getNew(Options ops) {
		return new PopulationSizeStatistic();
	}

}
