package treesimj;

/**
 * These listen for changes in the progress of a long running task, like a simulation.
 * @author brendan
 *
 */
public interface ProgressListener {
	
	public void setProgress(int prog, String str); //Called to set progress of current task. Prog should be from 0..100, String can be null
	
	public void done(Object source); //Called when task is finished. Source should be the task that is now done
}
