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
public class GraphMLWriter implements TreeWriter {

	public static final String XML_NODE = "node";
	public static final String XML_EDGE = "edge";
	public static final String XML_SECTION = "graph";
	public static final String XML_HEADER = "graphml";
	public static final String XML_RANGE = "range";
	public static final String XML_START = "start";
	public static final String XML_END = "end";
	public static final String XML_RANGEMIN = "rangemin";
	public static final String XML_RANGEMAX = "rangemax";
	
	static final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n <graphml> \n";
	static final String closer = "</graphml>\n";
	
	@Override
	public void writeTree(DiscreteGenTree tree, BufferedWriter writer)
			throws IOException {
		
		GraphDecomposer graphDecomp = new GraphDecomposer();
		graphDecomp.parseGraph(tree);
		Locus tip = tree.getTips().get(0);
		int seqLength = tip.getRecombineableData().length();
		
		String str = getString(graphDecomp, seqLength);
		writer.write(header);
		writer.write(str);
		writer.write(closer);
		
	}
	
	/**
	 * Converts the given number to the same format that the OutputManager uses to identify sequences, so we 
	 * can easily associate tips in an ARG with sequence 
	 * @param id
	 * @return
	 */
	private static String convertID(GraphNode node) {
		if (node.height > 0)
			return "" + node.id;
		else
			return String.valueOf("i" + Math.abs(node.id)).substring(0, 6);
	}
	
	private String getString(GraphDecomposer decomp, int length) {
		List<GraphNode> nodes = decomp.getNodes();
		List<Branch> edges = decomp.getEdges();
		StringBuffer strBuf = new StringBuffer();
				
		strBuf.append("<" + XML_SECTION + "  " + XML_RANGEMIN + "=\"" + 0 + "\"  " + XML_RANGEMAX + "=\"" + length + "\">\n");
		//System.out.println("<" + XML_SECTION + ">");
	
		for(GraphNode node : nodes) {
			strBuf.append("\t <" + XML_NODE + " id=\"" + convertID(node) + "\" height=\"" + node.height + "\"/>\n");
			//System.out.println("\t <" + XML_NODE + " id=\"" + node.id + "\" height=\"" + node.height + "\"/>");
		}
		
		int edgeCount = 1;
		for(Branch edge : edges) {
			if (edge.hasRange()) {
				strBuf.append("\t <" + XML_EDGE + " id=\"branch" + edgeCount + "\" source=\"" + convertID(edge.source) + "\" target=\"" + convertID(edge.target) + "\" >\n");
				strBuf.append("\t \t <" + XML_RANGE + ">\n");
				strBuf.append("\t \t \t <" + XML_START + "> " + edge.rangeMin + " </" + XML_START + ">\n");
				strBuf.append("\t \t \t <" + XML_END + "> " + edge.rangeMax + " </" + XML_END + ">\n");
				strBuf.append("\t \t </" + XML_RANGE + ">\n");
				strBuf.append("\t </edge>\n");

			}
			else {
				strBuf.append("\t <" + XML_EDGE + " id=\"branch" + edgeCount + "\" source=\"" + convertID(edge.source) + "\" target=\"" + convertID(edge.target) + "\"/>\n");
			}
			edgeCount++;
		}
		
		strBuf.append("</" + XML_SECTION + ">\n");
		return strBuf.toString();
	}

	@Override
	public String getSuffix() {
		return ".xml";
	}
	


}
