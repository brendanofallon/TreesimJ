package tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import population.Locus;


/**
 * A class that takes a tree / arg and decomposes it into a number of edges and nodes, so it can be written to XML easily
 * @author brendan
 *
 */
public class GraphDecomposer {
	
	protected List<GraphNode> nodes;
	protected List<Branch> edges;

	public GraphDecomposer() {
		//Do we need to do anything? May be nice to be stateless
	}
	
	/**
	 * Create nodes and edges corresponding to the structure of the given tree. These can then
	 * be accessed from getNodes and getEdges
	 * @param tree
	 */
	public void parseGraph(DiscreteGenTree tree) {
		nodes = new ArrayList<GraphNode>();
		edges = new ArrayList<Branch>();
		
		List<Locus> tips = tree.getTips();
		//List<Integer> breakpoints = tree.collectBreakPoints();
		
		//Traverse through each tip, adding as many nodes/branches as we can
		
		for(Locus tip : tips) {
			GraphNode tipNode = new GraphNode(0, tip.getID());
			
			addBranchesAndNodes(tip, tipNode, nodes, edges);
			nodes.add(tipNode);
		}
		
		String newick = tree.getNewick();
		System.out.println("Tree: " + newick);
		
		System.out.println("Found " + edges.size() + " branches:" );
		for(Branch branch : edges) {
			System.out.println(branch);
		}
		
		System.out.println("Found " + nodes.size() + " nodes");
		for(GraphNode node : nodes) {
			System.out.println(node);
		}
	}

	/**
	 * Obtain the set of nodes from the most recent tree/arg parsed, or null if no tree or arg has ever been parsed
	 * @return
	 */
	public List<GraphNode> getNodes() {
		return nodes;
	}
	
	/**
	 * Obtain the set of edges from the most recent tree/arg parsed, or null if no tree or arg has ever been parsed
	 * @return
	 */
	public List<Branch> getEdges() {
		return edges;
	}
	
	/**
	 * Traverse recursively ROOTWARD in tree from given tip, creating branches and nodes as we go and
	 * adding them to the list provided. 
	 * @param tip
	 * @param tipNode
	 * @param nodes
	 * @param branches
	 */
	private void addBranchesAndNodes(Locus tip, GraphNode tipNode, List<GraphNode> nodes,
			List<Branch> branches) {
		
		Stack<BranchBundle> stack = new Stack<BranchBundle>();
				
		BranchBundle bundle = getBranchNodePair(tip, tipNode);
		stack.push(bundle);
		
		//Awesome rootward recursion algorithm below
		while( ! stack.isEmpty() ) { 
			bundle = stack.pop();
			Branch branch = bundle.branch;
			GraphNode target = bundle.targetNode;
			Locus targetLocus = bundle.locus;
		
			branches.add(branch);
			
			//If we've already added the target, then stop heading rootward
			if (getNodeForID(nodes, targetLocus.getID())==null) {
				nodes.add(target);
				if (targetLocus.getParent()!=null && targetLocus.numOffspring()>1) {
					BranchBundle newBundle = getBranchNodePair(targetLocus, target);
					stack.push(newBundle);
				}
				
				if (targetLocus.hasRecombination()) {
					//TODO not complete! May be very wrong!
					Locus recombLocus = targetLocus.getRecombinationPartner();
					GraphNode recombGraphNode = new GraphNode(target.height, recombLocus.getID());
					BranchBundle recomBundle = getBranchNodePair(recombLocus, recombGraphNode);
					//Need some way of specifying range information!
					stack.push(recomBundle);
				}
			}
			

		}
	}

	private GraphNode getNodeForID(List<GraphNode> nodes, long id) {
		for(GraphNode node : nodes) {
			if (node.id == id) {
				return node;
			}
		}
		return null;
	}
	
	/**
	 * Given a source node, this creates a new branch extending in the rootward direction until it hits either a 
	 * coalescent or recombination node. It then returns a branchBundle with the branch, targetNode (the rootward node)
	 * and the Locus corresponding to the coalescent or recombination event. 
	 * 
	 * @param sourceLocus Locus in tree to start the branch
	 * @param source GraphNode corresponding to source
	 * @return
	 */
	private BranchBundle getBranchNodePair(Locus sourceLocus, GraphNode graphSource) {
		 //Stores a branch and the rootward node it reaches
		
		double length = 1;
		Locus ref = sourceLocus.getParent();
		while(ref.getParent()!= null && ref.getOffspring().size()==1 && !ref.hasRecombination()) {
			length++;
			ref = ref.getParent();
		}
		
		ref.setLabel( ref.getReadableID());
		GraphNode graphTarget = new GraphNode(graphSource.height+length, ref.getID());
		Branch branch = new Branch(graphSource, graphTarget);
	
		return new BranchBundle(branch, graphTarget, ref);
	}
	
	
	/**
	 * Nothing more than a container for a branch, a target node, and the locus corresponding to
	 * the target node
	 * @author brendan
	 *
	 */
	class BranchBundle {
		
		Branch branch;
		GraphNode targetNode;
		Locus locus;
		
		public BranchBundle(Branch branch, GraphNode target, Locus loc) {
			this.branch = branch;
			this.targetNode = target;
			this.locus = loc;
		}
	}
	
	
	/**
	 * Represents a single branch of the ARG. We collect all of these from the tree, then
	 * emit them all as XML
	 * @author brendan
	 *
	 */
	class Branch {
		
		GraphNode source;
		GraphNode target;
				
		public Branch(GraphNode source, GraphNode target) {
			this.source = source;
			this.target = target;
		}
		
		public String toString() {
			return "branch:\t source=" + source.id + "\t target=" + target.id;
		}
	}
	
	class GraphNode {
		
		 double height;
		 long id;
		 
		 public GraphNode(double height, long id) {
			 this.height = height;
			 this.id = id;
		 }

		public String toString() {
			return "Node: id=" + id + "\theight=" + height;
		}
	}

}
