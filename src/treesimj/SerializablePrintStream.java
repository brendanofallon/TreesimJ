package treesimj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;

/**
 * PrintStreams are not in general Serializable, which means we can't write them to a file for saving the simulation state. This
 * wrapper enables Serialization by re-creating the printstream whenever this file is read back in. 
 * @author brendan
 *
 */
public class SerializablePrintStream implements Serializable {

	transient PrintStream ps;
	boolean isSysOut = false;
	File outFile;
	
	public SerializablePrintStream(File outputFile) throws FileNotFoundException {
		this.outFile = outputFile;
		ps = new PrintStream(new FileOutputStream(outputFile));
	}
	
	public SerializablePrintStream(PrintStream out) {
		if (out == System.out) {
			isSysOut = true;
			ps = out;
		}
		else {
			throw new IllegalArgumentException("Can't create a non-System.out Serializable print stream for object " + out);
		}
		outFile = null;
	}
	
	public PrintStream getPrintStream() {
		return ps;
	}
	
	/**
	 * Close the print stream associated with this object
	 */
	public void close() {
		ps.close();
	}
	
	/**
	 * Returns true if this object is encapsulating System.out
	 * @return
	 */
	public boolean isSystemOut() {
		return isSysOut;
	}
	
	/**
	 * This is called by the VM whenever this object is de-serialized, it re-creates the non-serializable PrintStream from the
	 * stored object
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException  {
	     in.defaultReadObject();
	     if (isSysOut) {
	    	 ps = System.out;
	     }
	     else {
	    	 ps = new PrintStream(new FileOutputStream(outFile));
	     }
	     
	}
	
	public void println(Object line) {
		ps.println(line);
	}
	
	public void println() {
		ps.println();
	}
	
	public void print(Object x) {
		ps.print(x);
	}
	
}
