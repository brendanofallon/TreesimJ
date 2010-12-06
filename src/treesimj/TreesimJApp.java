/*
 * TreesimJApp.java
 */

package treesimj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * The main class of the application, containing the entry point ( the main method ). This class may not exist much longer since
 * it requires the confusing-and-not-really-used JSR 296 (org.jdesktop ..) application model. May be simpler to just nix this
 * whole thing and do it from scratch. We don't really use any of its features, and instead just get stymied when we can't do
 * what we want to. 
 */
public class TreesimJApp {

	static boolean runInputFile;
	static String inputFilename;
	static String propertiesFilePath;
	static Properties props;
	static boolean hasProperties;
	
	static TreesimJApp tjApp = null;
	static TreesimJView tjView;
	
	//We store a few properties, such as the frame size, in the following file
	static final String defaultPropertiesFilename = ".tj_properties.props";
	
	public static final String version="0.8";
	
    /**
     * At startup, see if we should run the input file or display the main window. Either way we create a TreesimJView
     * object, which is required for parsing the settings files (this is not generally a great idea - settings file parsing
     * should be separate, somehow) and running the simulation (ditto). If an apparently legit input file was supplied as 
     * an argument use the TreesimJView to parse the settings and then run, but don't show the window. If no argument was supplied,
     * just show the window. 
     * 
     */
    protected void startup() {
    	tjApp = this;
    	
    	try {
    		tjView = new TreesimJView(props);
    	}
    	catch (NoClassDefFoundError importError) {
    		System.out.println("There was an error while attempting to load the required libraries for TreesimJ and the main window could not be constructed. Please make sure you have an up-to-date version of Java installed.");
    		System.out.println("Required class : " + importError.getMessage());
    	}
    	catch (Exception ex) {
    		System.out.println("There was an error while loading the main TreesimJ window and it could not be constructed.");
    		System.out.println("Error : " + ex);
    	}
    	
    	if (runInputFile) {
    		System.out.println("Running simulation with settings from file : " + inputFilename);
    		File inputFile = new File(inputFilename);
    		tjView.importSettingsFromFile(inputFile, false);
    		tjView.beginSimulationFromGUI(false);
    	}
    	else {
    		tjView.setVisible(true); 
    		
    		//Try to remember what the previous frame size was.... 
			int frameWidth = 650;
			int frameHeight = 560;    		
    		if (props!=null) {
    			try {
    				String frameWidthStr = props.getProperty("frame.width");
    				String frameHeightStr = props.getProperty("frame.height");

    				frameWidth = Integer.parseInt(frameWidthStr);
    				frameHeight = Integer.parseInt(frameHeightStr);
    			}
    			catch (NullPointerException npe) {
    				//No big deal, we just don't have those properties
    			}
    			catch (NumberFormatException nfe) {
    				//We also don't care about this
    			}
    		}
    		tjView.setSize(frameWidth, frameHeight);
    		tjView.askAboutLastRunSettings(); //Ask the user if they want to import settings from the previous run
    	}
        
    }
    
    /**
     * Called on application shutdown, we write some properties to the file on shutdown
     */
    public void shutdown() {	
    	tjView.writeProperties(); //write properties before shutting down
    	tjView.setVisible(false);
    	tjView.dispose();
    	System.exit(0);
    }


    /**
     * A convenient static getter for the application instance.
     * @return the instance of TreesimJApp
     */
    public static TreesimJApp getApplication() {
        return tjApp;
    }

    
    public static TreesimJView getFrame() {
    	return tjView;
    }
    
    /**
     * Load some properties from a file
     * @param string
     */
    private static void loadProperties(String propsPath) {
    	props = new Properties();
    	File propsFile = new File(propsPath);
    	FileInputStream in;
    	try {
    		in = new FileInputStream(propsFile);
    		props.load(in);
    		in.close();
    		hasProperties = true;
    	} catch (FileNotFoundException e) {
    		//System.out.println("Warning: could not find properties file : " + e); Don't scare the user on the first run
    	} catch (IOException e) {
    		System.out.println("Warning: error loading properties : " + e);
    	}
    	
    	String propsFullPath = propsFile.getAbsolutePath();
    	props.setProperty("props.path", propsFullPath);
    	String propsName = propsFile.getName();
    	props.setProperty("props.filename", propsName);
    }


    public static void launchApplication() {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			TreesimJApp app = new TreesimJApp();
    			app.startup();
    		}
    	});
    }	

    
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
    	
    	if (args.length>0) {

    		//If the user specifies an argument ending in xml, we assume it is a settings
    		//file and attempt to execute it without opening a new window
    		if (args[0].endsWith("xml")) {
    			runInputFile = true;
    			inputFilename = args[0];
    		}
    		
    		//search for a properties file
    		for(int i=0; i<args.length; i++) {
    			if (args[i].endsWith(".props")) {	
    				loadProperties(args[i]);
    			}
    		}
    		
    	}
    	
		if (! hasProperties) {
			loadProperties(defaultPropertiesFilename);
		}
    	
		//We try to catch exceptions thrown in the event dispatch thread so we can handle them in a user-friendly
		//way.. but it's very debateable if this works. 
		System.setProperty("sun.awt.exception.handler",DefaultExceptionHandler.class.getName());
		
        launchApplication();
    }




}
