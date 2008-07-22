package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiNodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMpiProcessMap.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class OpenMpiProcessMapXml13Parser {
	private OpenMpiProcessMapXml13Parser() {
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
			System.out.println("Start element: "+name);
			ContextHandler handler = handlers.peek();
			ContextHandler childHandler = null;
			if (handler != null) {
				childHandler = handler.newElement(name);
				if (childHandler != null) {
					childHandler.start();
					for (int i = 0; i < attributes.getLength(); i++) {
						String attr_name = attributes.getQName(i);
						String attr_value = attributes.getValue(i);
						childHandler.setAttribute(attr_name, attr_value);
					}
					childHandler.prepare();
				}
			}
			handlers.push(childHandler);
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			super.endElement(uri, localName, name);
			System.out.println("End element: "+name);
			ContextHandler handler = handlers.pop();
			if (handler != null) {
				handler.finish();
			}
		}
		
		@Override
		public void error(SAXParseException e) throws SAXException {
			// TODO Auto-generated method stub
			super.error(e);
		}
		
		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			// TODO Auto-generated method stub
			System.out.println(e.getClass());
			
			super.fatalError(e);
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
			super(NLS.bind("Unknown XML attribute: {0}", name));
		}
	}

	private class MissingAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public MissingAttributeException(String name) {
			super(NLS.bind("Missing XML attribute: {0}", name));
		}
	}

	private class InvalidIntegerAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public InvalidIntegerAttributeException(String name, String value) {
			super(NLS.bind("Attribute {0} is not an valid integer ({1})", name, value));
		}
	}

	private class ParseInterruptedException extends SAXException {
		private static final long serialVersionUID = 1L;		
	}
	
	private abstract class ContextHandler {
		public ContextHandler newElement(String name) throws SAXException {
			throw new UnknownElementException(name);
		}
		/** Called when reading the beginning of the tag. */
		public void start() throws SAXException {
			// Empty
		}
		/** Called for each argument found at the beginning of the tag. */
		public void setAttribute(String name, String value) throws SAXException {
			throw new UnknownAttributeException(name);
		}
		/** Called when all attributes have been read and tag content is going to be read. */
		public void prepare() throws SAXException {
			// Empty
		}
		/** Called when finished reading the end of the tag. */
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

		@Override
		public void finish() throws SAXException {
			// Throw exception to force stop parsing.
			// Or else, the parser would complain about content not allows after in trailing section.
			throw new ParseInterruptedException();
		}
	}

	public class HostHandler extends ContextHandler {
		private int host_counter = 0;

		private String name;
		private boolean has_num_slots = false;
		private boolean has_max_slots = false;
		private int num_slots = 0;
		private int max_slots = 0;

		private Node node;
		private List<OpenMpiProcessMap.Process> processes = new ArrayList<OpenMpiProcessMap.Process>();

		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("process")) {
				return new ProcessHandler(node);
			} else {
				return super.newElement(name);
			}
		}

		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase("name")) {
				this.name = value;
			} else if (name.equalsIgnoreCase("slots")) {
				try {
					num_slots = Integer.parseInt(value);
					if (num_slots < 0) {
						throw new InvalidIntegerAttributeException(name, value);
					}
					has_num_slots = true;
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else if (name.equalsIgnoreCase("max_slots")) {
				try {
					max_slots = Integer.parseInt(value);
					if (max_slots < 0) {
						throw new InvalidIntegerAttributeException(name, value);
					}
					if (max_slots != 0) {
						has_max_slots = true;
					}
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else {
				super.setAttribute(name, value);
			}
		}

		@Override
		public void prepare() throws SAXException {
			if (name == null) {
				throw new MissingAttributeException("name");
			}
			if (! has_num_slots) {
				throw new MissingAttributeException("slots");
			}
			node = new Node(host_counter++, name);
			map.addNode(node);
			try {
				node.getAttributeManager().addAttribute(OpenMpiNodeAttributes.getNumberOfNodesAttributeDefinition().create(num_slots));
			} catch (IllegalValueException e) {
				// This is not possible
				PTPCorePlugin.log(e);
			}
			if (has_max_slots) {
				try {
					node.getAttributeManager().addAttribute(OpenMpiNodeAttributes.getMaximalNumberOfNodesAttributeDefinition().create(max_slots));
				} catch (IllegalValueException e) {
					// This is not possible
					PTPCorePlugin.log(e);
				}
			}
			super.prepare();
		}
		@Override
		public void finish() throws SAXException {
			/*
			 * Count how many processes are running on the host and check if the number is greater than the allowed one.
			 * If yes, then the node is oversubcribed.
			 */
			assert node != null;
			node.getAttributeManager().addAttribute(OpenMpiNodeAttributes.getOversubscribedDefinition().create(processes.size() > num_slots));
			super.finish();
		}
	}

	public class ProcessHandler extends ContextHandler {
		private Node node;
		private int rank;
		private boolean has_rank = false;

		public ProcessHandler(Node node) {
			this.node = node;
		}

		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase("rank")) {
				try {
					rank = Integer.parseInt(value);
					if (rank < 0) {
						throw new InvalidIntegerAttributeException(name, value);
					}
					has_rank = true;
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else {
				super.setAttribute(name, value);
			}
		}

		@Override
		public void finish() throws SAXException {
			OpenMpiProcessMap.Process process = new OpenMpiProcessMap.Process(node, rank, Integer.toString(rank), 1);
			map.addProcess(process);
			super.finish();
		}
	}

	public static OpenMpiProcessMap parse(InputStream is) throws IOException {
		OpenMpiProcessMapXml13Parser parser = new OpenMpiProcessMapXml13Parser();
		try {
			BufferedInputStream bis = new BufferedInputStream(is);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(bis, parser.handler);
		} catch (ParseInterruptedException e) {
			// this is ok and expected
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		return parser.map;
	}

	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("xml_sample.txt");
			OpenMpiProcessMap map = OpenMpiProcessMapXml13Parser.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
