package xml;

public class TJXMLException extends Exception {

	String source;
	String message;
	
	public TJXMLException(String source, String message) {
		super(message);
		this.message = message;
		this.source = source;
	}
	
	public String toString() {
		return source + " : " + message;
	}
}
