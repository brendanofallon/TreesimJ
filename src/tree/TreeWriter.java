package tree;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Interface for things that can write a tree to a file
 * @author brendan
 *
 */
public interface TreeWriter {

	public void writeTree(DiscreteGenTree tree, BufferedWriter writer) throws IOException;
	
	public String getSuffix();
	
}
