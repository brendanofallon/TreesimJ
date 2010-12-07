package tree;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Writes trees to the output buffer in newick format
 * @author brendan
 *
 */
public class NewickTreeWriter {

	public void writeTree(DiscreteGenTree tree, BufferedWriter writer)
	throws IOException {
		String newick = tree.getNewick();
		writer.write(newick + "\n");

	}

}
