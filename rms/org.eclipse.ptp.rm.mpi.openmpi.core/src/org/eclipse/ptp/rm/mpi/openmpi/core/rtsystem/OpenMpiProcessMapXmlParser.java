package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OpenMpiProcessMapXmlParser {
	private OpenMpiProcessMapXmlParser() {
		// Do not allow instances.
	}

	private class StackContextHandler extends DefaultHandler {
		Stack<ContextHandler> handlers = new Stack<ContextHandler>();

		public StackContextHandler(ContextHandler documentHandler) {
			handlers.push(documentHandler);
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			System.out.println("Start document");
		}

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
			System.out.println("End document");
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);
			System.out.println(MessageFormat.format("Start element {0} {1} {2}", uri, localName, name));
			ContextHandler handler = handlers.peek();
			ContextHandler childHandler = null;
			if (handler != null) {
				childHandler = handler.newElement(name);
				if (childHandler != null) {
					childHandler.prepare();
					for (int i = 0; i < attributes.getLength(); i++) {
						String attr_name = attributes.getLocalName(i);
						String attr_value = attributes.getValue(i);
						handler.setAttribute(attr_name, attr_value);
					}
				}
			}
			handlers.push(childHandler);
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			super.endElement(uri, localName, name);
			System.out.println(MessageFormat.format("End elemen {0} {1} {2}", uri, localName, name));
			ContextHandler handler = handlers.pop();
			if (handler != null) {
				handler.finish();
			}
		}
	}

	protected final OpenMpiProcessMap map = new OpenMpiProcessMap();
	protected final StackContextHandler handler = new StackContextHandler(new DocumentHandler());

	private class UnknownElementException extends SAXException {
		private static final long serialVersionUID = 1L;

		public UnknownElementException(String name) {
			super(NLS.bind("Unknown XML element: {0}", name));
		}
	}

	private class UnknownAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public UnknownAttributeException(String name) {
			super(NLS.bind("Unknown XML element: {0}", name));
		}
	}
	
	private class InvalidIntegerAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public InvalidIntegerAttributeException(String name, String value) {
			super(NLS.bind("Attribute {0} is not an valid integer ({1})", name, value));
		}
	}

	private abstract class ContextHandler {
		public ContextHandler newElement(String name) throws SAXException {
			throw new UnknownElementException(name);
		}
		public void setAttribute(String name, String value) throws SAXException {
			throw new UnknownAttributeException(name);
		}
		public void prepare() throws SAXException {
			// Empty
		}
		public void finish() throws SAXException {
			// Empty
		}
	}

	private class DocumentHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("map")) {
				return new MapHandler();
			} else if (name.equalsIgnoreCase("allocation")) {
					return null; // ignore
			} else {
				return super.newElement(name);
			}
		}
	}

	public class MapHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("host")) {
				return new HostHandler();
			} else {
				return super.newElement(name);
			}
		}
		
	}

	public class HostHandler extends ContextHandler {
		private String name;
		private int num_slots = 0;
		private int max_slots = 0;
		
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("process")) {
				return new ProcessHandler();
			} else {
				return super.newElement(name);
			}
		}
		
		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase("name")) {
				name = value;
			} else if (name.equalsIgnoreCase("slots")) {
				try {
					num_slots = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else if (name.equalsIgnoreCase("max_slots")) {
				try {
					max_slots = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else {
				super.setAttribute(name, value);
			}
		}
		
		@Override
		public void finish() throws SAXException {
			super.finish();
//			MappedNode node = new MappedNode();
//			map.mappedNodes.add(node);
//			node.
		}
	}

	public class ProcessHandler extends ContextHandler {
	}

	public static OpenMpiProcessMap parse(FileInputStream is) throws IOException {
		OpenMpiProcessMapXmlParser parser = new OpenMpiProcessMapXmlParser();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();			
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(is, parser.handler);
			return parser.map;
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("xml_sample.txt");
			OpenMpiProcessMap map = OpenMpiProcessMapXmlParser.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
