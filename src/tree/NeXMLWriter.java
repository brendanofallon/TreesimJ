package tree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import population.Locus;
import population.Population;
import tree.GraphDecomposer.Branch;
import tree.GraphDecomposer.GraphNode;

/**
 * Currently this emits a tree as a collection of nodes and edges, in a somewhat graphml like XML format. It does not emit
 * NeXML.
 * @author brendan
 *
 */
public class NeXMLWriter implements TreeWriter {

	public static final String XML_NODE = "node";
	public static final String XML_EDGE = "edge";
	public static final String XML_SECTION = "graph";
	public static final String XML_HEADER = "graphml";
	
	@Override
	public void writeTree(DiscreteGenTree tree, BufferedWriter writer)
			throws IOException {
		

		GraphDecomposer graphDecomp = new GraphDecomposer();
		graphDecomp.parseGraph(tree);
		
		List<GraphNode> nodes = graphDecomp.getNodes();
		List<Branch> edges = graphDecomp.getEdges();
			
		writer.write("<" + XML_SECTION + ">\n");
	
		for(GraphNode node : nodes) {
			writer.write("\t <" + XML_NODE + " id=\"" + node.id + "\" height=\"" + node.height + "\"/>\n");
		}
		
		int edgeCount = 1;
		for(Branch edge : edges) {
			writer.write("\t <" + XML_EDGE + " id=\"branch" + edgeCount + "\" source=\"" + edge.source.id + "\" target=\"" + edge.target.id + "\"/>\n");
			edgeCount++;
		}
		
		writer.write("</" + XML_SECTION + ">\n");
	}
	


}
