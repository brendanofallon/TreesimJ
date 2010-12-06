package population;

import java.io.Serializable;
import java.util.ArrayList;


import cern.jet.random.engine.RandomEngine;
import dnaModels.DNASequence;
import fitnessProviders.FitnessProvider;

/**
 * A single individual, with references to exactly one parent and zero or more offspring. The root individual always has parent null.
 * Individuals have a FitnessProvider element which they query to determine their fitness, and zero or more additional 
 * inheritable items. 
 * 
 * @author brendan
 *
 */
public class Locus implements Serializable {

	ArrayList<Inheritable> inheritables; //Additional non fitness-providing inheritable items
	FitnessProvider fitnessData; //Describes fitness
	double relativeFitness; 	//Fitness relative to population average in this generation - set & used by Population
	long id;					//An almost certainly unique id
	ArrayList<Locus> offspring; //List of all offspring individuals
	Locus parent;			//Parent individual
	RandomEngine rng;
	String label;	
	int depth = -1; //Handy for use when traversing big trees
	int originPop = -1; 		//In multiple population models, the pop number in which this individual was created. -1 signals nothing has been set. 
	
	boolean preserve; //A flag to indicate whether or not this individual can be cleared from the population. Used for serial-tree sampling
	
	public Locus(RandomEngine rng) {
		inheritables = null;
		offspring = new ArrayList<Locus>(2);
		parent = null;
		relativeFitness = 1.0;
		this.rng = rng;
		if (rng != null)
			id = rng.nextLong();
		else
			id = 0;
	}
	
	public void setOriginPopulation(int num) {
		originPop = num;
	}
	
	public int getOriginPop() {
		return originPop;
	}
	
	public void addInheritable(Inheritable item) {
		if (inheritables == null) 
			inheritables = new ArrayList<Inheritable>(2);
		inheritables.add(item);
	}
	
	public void setLabel(String l) {
		label = l;
	}
	
	public String getLabel() {
		return label;
	}
	
	/**
	 * A flag to mark this individual for non-removal from the population. This is used by the SerialTreeSampler to collect serial trees
	 * @return
	 */
	public boolean isPreserve() {
		return preserve;
	}

	public void setPreserve(boolean preserve) {
		this.preserve = preserve;
	}
	
	/**
	 * A flag that is used by a few Statistics to keep track of an individuals depth in a genealogy. At some point we could
	 * implement an annotation-type system for Individuals, and thus generalize the ability to set a key=value label for Individuals
	 * @return
	 */
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int d) {
		depth =d ;
	}

	
	/**
	 * Returns index of a particular offspring, or -1 if kid is not an immediate descendant of this individual
	 * @param kid
	 * @return
	 */
	public Integer offspringIndex(Locus kid) {
		Integer index = offspring.indexOf(kid);
		if (index==-1) {
			return null;
		}
		else
			return index;
	}
	
	/**
	 * Fetches the DNA sequence used to calculate fitness, or if it doesn't exist, the first DNA sequence that
	 * exists in inheritables, or null if there's no DNA anywhere.
	 * @return
	 */
	public DNASequence getPrimaryDNA() {
		if (fitnessData.getSubstrate() instanceof DNASequence) {
			return (DNASequence)fitnessData.getSubstrate();
		}
		else {
			if (inheritables != null) {
				for(Inheritable item : inheritables) {
					if (item instanceof DNASequence)
						return (DNASequence)item;
				}
			}
		}
		return null;
	}
	
	public long getID() {
		return id;
	}
	
	public String getReadableID() {
		StringBuffer buf = new StringBuffer("i");
		String num = String.valueOf(Math.abs(id)).substring(0, 5);
		buf.append(num);
		if (originPop>-1) {
			buf.append("_p" + String.valueOf(originPop));
		}

		return buf.toString();
	}
	
	public void setID(long newID) {
		id = newID;
	}
	
	public boolean isLeaf() {
		return offspring.size()==0;
	}
	
	public boolean isTip() {
		return isLeaf();
	}
	
	//Used by Population to determine if individuals reproduce. 
	public double getRelFitness() {
		return relativeFitness;
	}
	
	public void setRelFitness(double val) {
		relativeFitness = val;
	}
	
	public void setFitnessProvider(FitnessProvider fitness) {
		fitnessData = fitness;
	}
	
	public double getFitness() {
		return fitnessData.getFitness();
	}
	
	public int getNumInheritables() {
		if (inheritables == null)
			return 0;
		return inheritables.size();
	}
	
	public Inheritable getInheritable(int which) {
		if (inheritables == null)
			return null;
		return inheritables.get(which);
	}
	
	//Removes this individual from the offspring list, if it's in there
	public boolean removeOffspring(Locus kid) {
		return offspring.remove(kid);
	}
	
	/**
	 * Remove this individual from the population and all ancestors of this individual, as
	 * long as the ancestors all have exactly one offspring (leading 'up to' this individual).  
	 * We must be aware that some individuals, potentially ancestors of this individual, are 'preserved', 
	 * meaning that they should not be removed from the population (this is for compatibility with 
	 * serial-sampling stuff, which is not implemented in a release version)
	 * 
	 */
	public void removeFromPop() {
		if (preserve)
			return;
		
		int depth = 0;
		Locus ref = this;
		
		while(ref.getParent().numOffspring()==1 && !ref.getParent().isPreserve()) {
			ref = ref.getParent();
			if (ref.getParent()==null) {
				System.out.println("Individual has no parent at depth: " + depth);
				System.out.println("ID is : " + ref.getReadableID());
			}
			depth++;
		}

		
		ref.getParent().removeOffspring(ref);
		ref.setParent(null);
	}
	
	/**
	 * Return the number of generations (Individuals / nodes in the tree) between this individual and 
	 * the root
	 * @return
	 */
	public int distToRoot() {
		int dist = 0;
		Locus ref = this;
		while(ref.getParent() != null) {
			dist++;
			ref = ref.getParent();
		}
		return dist;
	}
	
	/**
	 * Calls mutate on the fitness data and all inheritables. 
	 */
	public void mutate() {
		fitnessData.mutate();
		if (inheritables != null) {
			for(Inheritable item : inheritables) {
				item.mutate();
			}
		}
	}
	
	public void setParent(Locus par) {
		parent = par;
	}
	
	public void addOffspring(Locus kid) {
		offspring.add(kid);
	}
	
	public ArrayList<Locus> getOffspring() {
		return offspring;
	}
	
	public Locus getOffspring(int which) {
		return offspring.get(which);
	}
	
	public int numOffspring() {
		return offspring.size();
	}
	
	public Locus getParent() {
		return parent;
	}
	
	
	/**
	 * Returns an individual with the same data AND the same parents and offspring as this
	 * Note that data is cloned, but parents and offspring are just references
	 */
	public Locus getCompleteCopy() {
		Locus ind = new Locus(rng);
		ind.copyDataFrom(this);
		ind.setParent( parent );
		for(Locus kid : offspring)
			ind.addOffspring(kid);
		
		return ind;
	}
	
	/**
	 * Creates a clone of this individual and copies all data to the new individual using .copyDataFrom(this)
	 * @return
	 */
	public Locus getDataCopy() {
		Locus ind = new Locus(rng);
		ind.copyDataFrom(this);
		return ind;
	}
	
	/**
	 * This just passes references from the parent data to the offspring
	 * @param parent
	 */
	public void inheritFrom(Locus parent) {
		fitnessData = parent.getFitnessData();
		
		if (inheritables != null) {
			inheritables.clear();
			for(Inheritable item : inheritables) {
				inheritables.add(item);
			}
		}
	}
	
	
	/**
	 * Copies data from source individual ind. 
	 * @param ind
	 */
	public void copyDataFrom(Locus ind) {
		if (inheritables != null) {
			inheritables.clear();
			for(Inheritable item : inheritables) {
				inheritables.add(item.getCopy());
			}
		}
		originPop = ind.originPop;
		fitnessData = ind.getFitnessData().getCopy();
	}
	
	public FitnessProvider getFitnessData() {
		return fitnessData;
	}
	
//	public ArrayList<Inheritable> getOtherInheritables() {
//		return inheritables;
//	}
	
	
}
