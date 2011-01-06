package statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import population.Locus;

public class NodeCount extends Statistic {
	
	public static final String identifier = "Tree node total";
	
	@Override
	public Statistic getNew(Options ops) {
		return new NodeCount();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void collect(Collectible pop) {
		List<Locus> loci = pop.getList();
		double count = getNodeCount(loci);
		values.add(count);
		
	}

	@Override
	public String getDescription() {
		return "Count of all nodes (individuals) in population tree";
	}

	public boolean showOnScreenLog() {
		return true;
	}
	
	
	/**
	 * Counts all nodes (all current loci and ancestors) that exist. 
	 * @return
	 */
	private int getNodeCount(List<Locus> sample) {
		int depth = 0;
		
		Set<Locus> parentSet = new HashSet<Locus>();
		
		int nodeTotal = 0;

		while(sample.size()>1) {
//			if (getCurrentGenNumber()%100==0 && depth%10==0)
//				System.out.println("Ancestors at depth " + depth + " : " + sample.size());
			depth++;
			nodeTotal += sample.size();
			List<Locus> parents = new ArrayList<Locus>();
			parents.clear();
			parentSet.clear();
			
			for(int i=0; i<sample.size(); i++) {
				//If the parent is not already in the list, add it
				if (! parentSet.contains(sample.get(i).getParent())) {
					parents.add(sample.get(i).getParent());
					parentSet.add(sample.get(i).getParent());
				}
				
				//If this sample has a recombination, then we also add it's recomb. partner's parent. 
				if (sample.get(i).hasRecombination()) {
					Locus partner = sample.get(i).getRecombinationPartner();
					if (partner == null) {
						System.out.println("Yikes! Recomb. partner is null!");
					}
					if (partner.getParent()==null) {
						System.out.println("This node: " + sample.get(i).getReadableID() + " partner: " + partner.getReadableID());
						System.out.println("Yikes, recomb. partner's parent is null!");
					}
					if (! parentSet.contains( sample.get(i).getRecombinationPartner().getParent())) {
						parents.add( sample.get(i).getRecombinationPartner().getParent() );
						parentSet.add( sample.get(i).getRecombinationPartner().getParent() );
					}
				}
			}
			//System.out.println("tree size at depth " + depth + " : " + sample.size() );
			sample = parents;
		}
		
		//System.out.println("..done");
		
//		if (getCurrentGenNumber()%200==0) {
//			System.out.println("Current gen: "+ getCurrentGenNumber() + " FC depth : " + depth + " total nodes: " + nodeTotal);
//		}
		return nodeTotal;
	}
}
