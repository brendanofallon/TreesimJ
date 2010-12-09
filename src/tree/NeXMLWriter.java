package tree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import population.Locus;
import population.Population;

public class NeXMLWriter implements TreeWriter {

	@Override
	public void writeTree(DiscreteGenTree tree, BufferedWriter writer)
			throws IOException {
		
		List<Locus> tips = tree.getTips();
		List<Integer> breakpoints = tree.collectBreakPoints();
		if (breakpoints.size()>0)
			System.out.println("NeXML found breakpoints : " + breakpoints);
		
	}

}
