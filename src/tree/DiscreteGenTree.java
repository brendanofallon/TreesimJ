package tree;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import population.Individual;


/** 
 * A tree of Individuals, in which each level in the tree represents one generation. Under
 * normal circumstances, almost every node in the tree will have exactly 1 descendent, except for
 * (approximately) (#tips-1) nodes that will have two descendents. Since individuals know their parents
 * as well as offspring, we cal easily traverse up and down the tree. 
 * 
 * Note that there is no real concept of 'branch length' here - all branches are assumed to be 
 * one generation long. By 'distance', we mean the number of nodes traversed from Individual A to B
 * @author brendan
 *
 */
public class DiscreteGenTree {

	private Individual root = null;
	
	public DiscreteGenTree(Individual root) {
		this.root = root;
	}
	
	/**
	 * Returns the total number of nodes
	 * @return
	 */
	public int getTotalNodes() {
		if (root==null)
			return 0;
		else 
			return subTreeSize(root);
	}
	
	/**
	 * The total number of tips (individuals with zero offspring)
	 * @return
	 */
	public int getNumLeaves() {
		if (root==null)
			return 0;
		else 
			return numDescendentLeaves(root);		
	}
	
	/**
	 * Obtain a newick representation of this tree
	 * @return
	 */
	public String getNewick() {
		StringBuffer str = new StringBuffer("(");
		int i;
		for(i=0; i<(root.numOffspring()-1); i++) {
			str.append( getNewickSubtree(root.getOffspring(i), 1) );
			str.append(", ");
		}
		str.append( getNewickSubtree(root.getOffspring(i), 1)  );
		
		str.append(")");
		
		if (root.getLabel() != null) 
			str.append("[" + root.getLabel() +"]");
		str.append(";");
		return str.toString();
	}
	
	public void printTree() {
		if (root==null) {
			System.out.println("Tree has no root.");
		}
		else {
			printDescendents(root, 0);
		}
	}
	
	public Individual getRoot() {
		return root;
	}

	
	/**
	 * Returns the maximum distance from any tip to the root of the tree
	 * @return
	 */
	public int getMaxHeight() {
		ArrayList<Individual> tips = this.getTips();
		int maxHeight = 0;
		int i;
		for(i=0; i<tips.size(); i++)
			if ( getDistToRoot( tips.get(i) ) > maxHeight )
				maxHeight = getDistToRoot( tips.get(i) );
		
		return maxHeight;
	}
	
	
	/**
	 * Poorly named, returns an arraylist of all depths at which a node appears that has more than
	 * one offspring (These would be the 'node times' in a normal, coalescent tree)
	 * @return
	 */
	public ArrayList<Double> getNodeTimes() {
		Stack<Individual> nodes = new Stack<Individual>();
		nodes.add(root);
		double maxHeight = getMaxHeight();
		ArrayList<Double> times = new ArrayList<Double>();

		while(nodes.size()>0) {
			Individual ind = nodes.pop();
			if (ind.numOffspring()>1)
				times.add( maxHeight- getDistToRoot(ind) );
			nodes.addAll( ind.getOffspring());
		}
		
		Collections.sort(times);
		return times;
	}
	
	
	
	/////////////// Newick String Building Methods /////////////////////////////////
	private static boolean hasKids(String subtree) {
		if (subtree.indexOf(",")<0)
			return false;
		else
			return true;
	}
//	private static ArrayList<String> getOffspringStrings(int startIndex, String treeStr) {
//		ArrayList<String> children = new ArrayList<String>();
//		int lastParen = matchingParenIndex(startIndex, treeStr);
//		
//		//Scan along subtree string, create new strings separated by commas *at 'highest level'*
//		//not all commas, of course
//		int i=startIndex+1;
//		StringBuffer cstr = new StringBuffer();
//		
//		int count = 0;
//		for(i=startIndex+1; i<lastParen; i++) {	
//			if (treeStr.charAt(i)=='(')
//				count++;
//			if (treeStr.charAt(i)==')')
//				count--;
//			if (treeStr.charAt(i)==',' && count==0) {
//				children.add(cstr.toString());
//				cstr = new StringBuffer();
//			}
//			else	
//				cstr.append( treeStr.charAt(i));
//			
//		}
//		
//		children.add(cstr.toString());
//		
//		return children;
//	}
	
	/**
	 * Returns a string representing 
	 */
//	private static String extractSubtreeString(int startIndex, String treeStr) {	
//		int lastParen = matchingParenIndex(startIndex, treeStr);
//		int lastPos = Math.min( treeStr.indexOf(",", lastParen),  treeStr.indexOf(")", lastParen+1) );
//		System.out.println("Last paren index : " + lastParen);
//		System.out.println("First , or ) after lastParen : " + lastPos);
//		String subtree = treeStr.substring(startIndex, lastPos);
//		return subtree;
//	}
//	
//	//Returns the distance of the subtree from a parent (just the number following the final colon) 
//	private static double subTreeDistFromParent(String subtree) {
//		int lastColon = subtree.lastIndexOf(":");
//		int lastParen = subtree.lastIndexOf(")");
//		if (lastParen<lastColon)
//			lastParen=subtree.length();
//		if (lastColon > lastParen)
//			System.err.println("Uh-oh, found bad values for branch length positions on this substring : " + subtree);
//		String branchLengthStr = subtree.substring(lastColon+1, lastParen);
//		double branchLength = Double.valueOf(branchLengthStr);
//		return branchLength;
//	}
	
//	private static int matchingParenIndex(int firstPos, String str) {
//		int i = firstPos;
//		if (str.charAt(i) != '(') {
//			System.err.println("Got a non-paren for first index in find matching paren");
//			System.err.println(" Char is : |" + str.charAt(i) +"|");
//			return -1;
//		}
//		int count = 0;
//		for(i=firstPos; i<str.length(); i++) {
//			if (str.charAt(i) == '(')
//				count++;
//			if (str.charAt(i) == ')')
//				count--;
//			if (count==0)
//				return i;
//		}
//		
//		System.err.println("Couldn't find matching paren for this string : |" + str + "|");
//		System.err.println(" First paren index : " + firstPos);
//		return -1;
//	}
	
	
	//Returns distance from Individual to farthest out tip (=totaldepth - IndividualDepth)
//	public static double getIndividualHeight(Individual n, DiscreteGenTree tree) {
//		double totalDepth = getMaxDescendentDepth( tree.getRoot() );
//		double IndividualDepth = getDistToRoot(n);
//		return totalDepth - IndividualDepth;
//	}
	
	//Returns depth of leaf that is 'farthest' (toward tips) fom n
//	public static double getMaxDescendentDepth(Individual n) {
//		ArrayList<Double> depths = getDescendentDepthList(n);
//		int i;
//		double maxDepth = 0;
//		//System.out.println("Found depths : ");
//		for(i=0; i<depths.size(); i++) {
//			//System.out.println( depths.get(i));
//			if (depths.get(i) > maxDepth)
//				maxDepth = depths.get(i);
//		}
//		//System.out.println(" Returning max dist from root : " + maxDepth);
//		return maxDepth;
//	}
	
	
	//Returns the depth of the leaf Individual that is 'closest' (toward root) to n	
//	public static double getMinDescendentDepth(Individual n) {
//		ArrayList<Double> depths = getDescendentDepthList(n);
//		int i;
//		double minDepth = Double.MAX_VALUE;
//		//System.out.println("Found depths : ");
//		for(i=0; i<depths.size(); i++) {
//			//System.out.println( depths.get(i));
//			if (depths.get(i) < minDepth)
//				minDepth = depths.get(i);
//		}
//		//System.out.println(" Returning min dist from root : " + minDepth);
//		return minDepth;
//	}
	
	public static int getDistToRoot(Individual n) {
		int dist = 0;
		Individual p = n;
		while( p != null) {
			dist++;
			p = p.getParent();
		}
		
		return dist;
	}

	public static int getDistToParent(Individual n) {
		int dist = 1;
		Individual p = n.getParent();
		while( p.numOffspring()==1) {
			dist++;
			p = p.getParent();
		}
		
		return dist;
	}

	/**
	 * Returns next 'node' down in tree, that is, a tip or  the next individual with >1 offspring
	 * @param n node to return the next descendant node of
	 * @return the next node down in tree
	 */
	public static Individual getNextNode(Individual n) {
		Individual ref = n;
		while(ref.numOffspring()==1)
			ref = n.getOffspring(0);
		
		return ref;
	}

	private static int lineageCountSubtree(Individual n, double tDepth) {
		double thisDepth = getDistToRoot(n);
		double parentDepth = getDistToRoot( n.getParent() );
		if (thisDepth >= tDepth && parentDepth <= tDepth)
			return 1;
		
		if (thisDepth < tDepth) {
			int count = 0;
			int i;
			for(i=0; i<n.numOffspring(); i++)
				count += lineageCountSubtree(n.getOffspring(i), tDepth);
			return count;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Returns an array list of all the tips of the tree
	 * @return
	 */
	public ArrayList<Individual> getTips() {
		return getTips(root);
	}
	
	/**
	 * Returns an array list of all the tips of the tree descending from Individual n
	 * @return
	 */
	private static ArrayList<Individual> getTips(Individual n) {
		Stack<Individual> nodes = new Stack<Individual>();
		nodes.add(n);
		ArrayList<Individual> tips = new ArrayList<Individual>();

		while(nodes.size()>0) {
			Individual ind = nodes.pop();
			if (ind.numOffspring()==0)
				tips.add( ind );
			nodes.addAll( ind.getOffspring());
		}
		
		return tips;
	}
	
	/**
	 * The number distance Individual n is from the root
	 * @param n
	 * @return
	 */
	public static int getNodesToRoot(Individual n) {
		Individual ref = n;
		int sum  = 0;
		while(ref.getParent() != null) {
			if (ref.numOffspring()>1)
				sum++;
			ref = ref.getParent();
		}
		return sum;
	}
	
	/**
	 * The number of tips descending from Individual n
	 * @param n
	 * @return
	 */
	public static int getNumTips(Individual n) {
		Stack<Individual> nodes = new Stack<Individual>();
		nodes.push(n);
		int numTips = 0;
		while(nodes.size()>0) {
			Individual ind = nodes.pop();
			if (ind.numOffspring()==0)
				numTips++;
			nodes.addAll( ind.getOffspring());
		}
		
		return numTips;
	}
	
	/**
	 * Returns a newick string representing the subtree starting from individual n.
	 * @param n The Individual marking the top of the subtree
	 * @param depth The depth of individual n, used to calculate branch lengths
	 * @return The newick string representing the subtree
	 */
	private static String getNewickSubtree(Individual n, int depth) {
		while(n.numOffspring()==1) {
			n = n.getOffspring(0);
			depth++;
		}
		
		if (n.numOffspring()==0) {
			StringBuffer buf = new StringBuffer(n.getReadableID() );
			if (n.getLabel() != null)
				buf.append( "[" + n.getLabel() + "]");
			buf.append(":" + depth );
			return buf.toString();
		}

		//Must be more than one offspring
		StringBuffer str = new StringBuffer("(");
		int i;
		for(i=0; i<(n.numOffspring()-1); i++) {
			str.append( getNewickSubtree(n.getOffspring(i), 1) );
			str.append(", ");
		}
		str.append( getNewickSubtree(n.getOffspring(i), 1));
		str.append(")");
		if (n.getLabel() != null) 
			str.append("[" + n.getLabel() + "]");
		str.append(":" + depth);
		return str.toString();

	}	
	
	//Returns a list of the distances to the root of all the tips that descend 
	//from this Individual
	public static ArrayList<Integer> getDescendentDepthList(Individual n) {
		ArrayList<Integer> depths = new ArrayList<Integer>();
		if (n.numOffspring()==0) {
			depths.add( getDistToRoot(n) );
		}
		else {
			int i;
			for(i=0; i<n.numOffspring(); i++) 
				depths.addAll( getDescendentDepthList(n.getOffspring(i)) );
		}
		return depths;
	}
	
	
	/**
	 * Emits a representation of the tree to stdout, can be useful for debugging
	 * @param n
	 * @param padding
	 */
	private static void printDescendents(Individual n, int padding) {
		int i;
		for(i=0; i<padding; i++)
			System.out.print("  ");
		System.out.println("Individual id : " + n.getID() + " children : " + n.numOffspring() );
		for(i=0; i<n.numOffspring(); i++ ) {
			printDescendents(n.getOffspring(i), padding+2);
		}
		
	}

	/**
	 * Number of leaves descending from Individual n
	 * @param n
	 * @return
	 */
	private static int numDescendentLeaves(Individual n) {
		int count = 0;
		int i;
		int k = n.numOffspring();
		if (k==0)
			return 1;
		else {
			for(i=0; i<k; i++)
				count += numDescendentLeaves(n.getOffspring(i));
			return count;
		}
		
	}
	
	/**
	 * Total number of individuals descending from Individual n
	 * @param n Individual marking the top of the subtree
	 * @return
	 */
	private static int subTreeSize(Individual n) {
		int count = 1;
		int i;
		for(i=0; i<n.numOffspring(); i++)
			count += subTreeSize(n.getOffspring(i));
		
		return count;
	}
	
	
}

