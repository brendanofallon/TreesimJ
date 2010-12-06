package xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Base class for all things that can write themselves to XML. This is pretty much
 * every functional element in the game (fitness models, mutation models, site models, statistics, etc...)
 * 
 * @author brendan
 *
 */
public abstract class XMLParseable implements Serializable {

	protected String XMLBlockName;
	protected ArrayList<KeyValuePair> xmlAttrs = null;
	protected ArrayList<XMLParseable> children = null;
	
	public XMLParseable(String blockName) {
		XMLBlockName = blockName;
	}
	
	/**
	 * Add a new xml node as a child of this node
	 * @param child
	 */
	protected void addXMLChild(XMLParseable child) {
		if (children == null) {
			children = new ArrayList<XMLParseable>();
		}

		children.add(child);
	}
	
	/**
	 * Add a new xml attribute to this node. If there is already an attribute with the given key the old key is
	 * over written by the new key. 
	 * @param key
	 * @param value
	 */
	protected void addXMLAttr(String key, String value) {
		if (xmlAttrs == null) {
			xmlAttrs = new ArrayList<KeyValuePair>();
		}
	
		//If we already have an attribute with the specified key, we over write the old value with
		//the specified value.
		for(int i=0; i<xmlAttrs.size(); i++) {
			if (xmlAttrs.get(i).key.equals(key)) {
				xmlAttrs.get(i).value = value;
				return;
			}
		}

		xmlAttrs.add(new KeyValuePair(key, value));
	}
	
	public String getBlockName() {
		return XMLBlockName;
	}

	/**
	 * Write the XML associated with this node, including all attributes and child nodes, to the given XML stream writer
	 * @param writer
	 * @param prefix
	 * @throws XMLStreamException
	 */
	public void writeXMLBlock(XMLStreamWriter writer, String prefix) throws XMLStreamException {
		writer.writeCharacters(prefix);
		writer.writeStartElement(XMLBlockName);
		//System.out.println("Writing block : " + XMLBlockName);
		if (xmlAttrs != null) {
			for(KeyValuePair kv : xmlAttrs) {
				writer.writeAttribute(kv.key, kv.value);
			}
		}

		
		if (children != null) {
			writer.writeCharacters("\n");
			for(XMLParseable child : children) {
				child.writeXMLBlock(writer, "\t"+prefix);
				writer.writeCharacters("\n");
			}
		}

		
		writer.writeEndElement();
	}
	

	
	public void writeXMLBlock(XMLStreamWriter writer) throws XMLStreamException {
		writeXMLBlock(writer, "");
	}
	
	public static class Utils {
		
		public static Hashtable<String, String> makeAttributeMap(XMLStreamReader reader) {
			Hashtable<String, String> map = new Hashtable<String, String>();
			for(int i=0; i<reader.getAttributeCount(); i++) {
				String key = reader.getAttributeLocalName(i);
				String val = reader.getAttributeValue(i);
				map.put(key, val);
			}
			
			return map;
		}
		
		public static  String getAttributeForKey(XMLStreamReader reader, String key) {
			for(int i=0; i<reader.getAttributeCount(); i++) {
				String name = reader.getAttributeLocalName(i);
				if (name == key)
					return reader.getAttributeValue(i);

			}
			return null;
		}
	}
	
	protected class KeyValuePair implements Serializable {
		String key;
		String value;
		
		public KeyValuePair(String k, String v) {
			key = k;
			value = v;
		}
	}
}
