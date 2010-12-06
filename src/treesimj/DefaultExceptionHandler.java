package treesimj;

import javax.swing.JOptionPane;

/**
 * A default exception handler, meant to catch and appropriate inform the user of all exceptions not caught.
 * It must have a no-argument constructor to operate. 
 * @author brendan
 *
 */
public class DefaultExceptionHandler {
	
	public DefaultExceptionHandler() {	}
	
	public void handle(Throwable aThrowable){
	    System.err.println("Error: " + aThrowable.toString() );
	}
}
