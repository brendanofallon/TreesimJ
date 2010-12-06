package dnaModels;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Stack;

/**
 * This class may be used at some point to recycle the BitSets that are used to construct DNA sequences, 
 * and thus to reduce the amount of new object allocation that occurs during reproduction. Currently
 * this has not been implemented. 
 * 
 * @author brendan
 *
 */
public class BitSetPool {

	ArrayList<BitSet> inUse;
	Stack<BitSet> free;
	 
	int allocs = 0;
	int frees = 0;
	int capacity;
	
	public BitSetPool(int capacity) {
		this.capacity = capacity;
		inUse = new ArrayList<BitSet>();
		free = new Stack<BitSet>();
		
		for(int i=0; i<capacity; i++) {
			free.push(new BitSet());
		}
	}
	
	
	public BitSet getNew() {
		BitSet set;
		if (free.size()>0) {
			set = free.pop();
			
		}
		else {
			set = new BitSet();
			System.out.println("No free space left to give, grabbing more space from heap");
		}
		
		inUse.add(set);
		allocs++;
		
		while (free.size()>capacity) {
			free.pop();
		}
		
		if (allocs % 10==0) {
			System.out.println("Allocations : " + allocs + " frees : " + frees + " in use: " + inUse.size() + " free store : " + free.size() + " total size : " + (inUse.size()+free.size()) );
		}
		return set;
	}
	
	public void retire(BitSet retiree) {
		boolean inThere = inUse.remove(retiree);
		if (!inThere) {
			System.err.println("Tried to remove a bitset that wasn't in Use pool");
		}
		free.push(retiree);
		
		frees++;
	}
}
