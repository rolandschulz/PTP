/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPINodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIProcessMapXml13Parser {
	private OpenMPIProcessMapXml13Parser() {
		// Do not allow instances.
	}
	
	/*
	 * Attempt to clean up quasi-XML output by 1.3.0 
	 * 
	 * We know the following:
	 * - the XML is malformed because it doesn't include a root element
	 * - XML between the <map> and </map> tags should be well formed
	 * - <noderesolve> elements precede the <map>
	 * - there may be garbage preceding the first <noderesolve> and after the </map>
	 * 
	 * We need to:
	 * - inject a root element
	 * - ignore anything prior to the first and after the last XML tags
	 * 
	 * To simplify the parsing (so we don't need to build a full XML parser) we assume
	 * that:
	 * - the XML begins with 0 or more <noderesolve> elements followed by a <map> element
	 * - <noderesolve> elements are always empty-element tags
	 * - there is only one <map> element
	 * - the final XML tag is </map>
	 */
	private class OpenMPI130InputStream extends FilterInputStream {
		private final static int PROLOG = 0;
		private final static int START_TAG = 1;
		private final static int XML = 2;
		private final static int EPILOG = 3;
	
		private int state = PROLOG;
		//private Queue<Integer> saved = new ConcurrentLinkedQueue<Integer>();
		private StringBuffer prolog = new StringBuffer();
		private StringBuffer epilog = new StringBuffer(6);
		
		protected OpenMPI130InputStream(InputStream in) {
			super(in);
		}

		/* (non-Javadoc)
		 * @see java.io.FilterInputStream#read()
		 */
		@Override
		public int read() throws IOException {
			while (state != EPILOG) {
				int ch = super.read();
				if (ch < 0) {
					return -1;
				}
				switch (state) {
				case PROLOG:
					// Ignore anything except a less-than
					if (ch == '<') {
						state = START_TAG;
						prolog.append((char)ch);
					}
					break;
					
				case START_TAG:
					// Find matching greater-than. If we encounter
					// another less-than, assume that this is the new 
					// starting point
					switch (ch) {
					case '<':
						prolog.delete(0, prolog.length());
						break;
						
					case '>':
						state = XML;
						prolog.insert(0, "<ompi>"); //$NON-NLS-1$
						break;
					}
					prolog.append((char)ch);
					break;
				
				case XML:
					epilog.append((char)ch);
					ch = epilog.charAt(0);
					if (epilog.length() >= 6) {
						if (epilog.substring(epilog.length()-6).equals("</map>")) { //$NON-NLS-1$
							epilog.append("</ompi>"); //$NON-NLS-1$
							state = EPILOG;
							break;
						}
					}
					if (prolog.length() > 0) {
						ch = prolog.charAt(0);
						prolog.deleteCharAt(0);
						return ch;
					}
					epilog.deleteCharAt(0);
					return ch;
				}
			}
			
			if (prolog.length() > 0) {
				int ch = prolog.charAt(0);
				prolog.deleteCharAt(0);
				return ch;
			}
			if (epilog.length() > 0) {
				int ch = epilog.charAt(0);
				epilog.deleteCharAt(0);
				return ch;
			}
			return -1;
		}

		/* (non-Javadoc)
		 * @see java.io.FilterInputStream#read(byte[], int, int)
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (len == 0) {
				return 0;
			}

			int pos = off;
			
			for (int i = 0; i < len && available() > 0; i++) {
				int ch = read();
				if (ch < 0) {
					return -1;
				}
				b[pos++] = (byte) (ch & 0xff);
			}
			
			return pos - off;
		}

		/* (non-Javadoc)
		 * @see java.io.FilterInputStream#markSupported()
		 */
		@Override
		public boolean markSupported() {
			return false;
		}

		/* (non-Javadoc)
		 * @see java.io.FilterInputStream#available()
		 */
		@Override
		public int available() throws IOException {
			return state == PROLOG ? 1 : prolog.length() + epilog.length();
		}
		
	}

	private class StackContextHandler extends DefaultHandler {
		Stack<ContextHandler> handlers = new Stack<ContextHandler>();

		public StackContextHandler(ContextHandler documentHandler) {
			handlers.push(documentHandler);
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			// Notify listeners that parsing has started
			for (Object listener : listeners.getListeners()) {
				try {
					((IOpenMpiProcessMapXml13ParserListener ) listener).startDocument();
				} catch (Exception e) {
					OpenMPIPlugin.log(e);
				}
			}
		}

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);
			//			System.out.println("Start element: "+name);
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
			//			System.out.println("End element: "+name);
			ContextHandler handler = handlers.pop();
			if (handler != null) {
				handler.finish();
			}
		}

		@Override
		public void error(SAXParseException e) throws SAXException {
			super.error(e);
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			super.fatalError(e);
		}
	}

	protected final OpenMPIProcessMap map = new OpenMPIProcessMap();
	protected final StackContextHandler handler = new StackContextHandler(new DocumentHandler());
	protected final ListenerList listeners = new ListenerList();

	public static interface IOpenMpiProcessMapXml13ParserListener {
		void startDocument();
		void endDocument();
	}

	public class UnknownElementException extends SAXException {
		private static final long serialVersionUID = 1L;

		public UnknownElementException(String name) {
			super(NLS.bind(Messages.OpenMPIProcessMapXml13Parser_Exception_UnknownElement, name));
		}
	}

	public class UnknownAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public UnknownAttributeException(String name) {
			super(NLS.bind(Messages.OpenMPIProcessMapXml13Parser_Exception_UnknownAttribute, name));
		}
	}

	public class MissingAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public MissingAttributeException(String name) {
			super(NLS.bind(Messages.OpenMPIProcessMapXml13Parser_Exception_MissingAttribute, name));
		}
	}

	public class InvalidIntegerAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public InvalidIntegerAttributeException(String name, String value) {
			super(NLS.bind(Messages.OpenMPIProcessMapXml13Parser_Exception_AttributeNotInteger, name, value));
		}
	}

	public class ParseInterruptedException extends SAXException {
		private static final long serialVersionUID = 1L;
	}

	public abstract class ContextHandler {
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

	public class DocumentHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("ompi")) { //$NON-NLS-1$
				return new OmpiHandler();
			}
			return super.newElement(name);
		}
	}

	public class OmpiHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("map")) { //$NON-NLS-1$
				return new MapHandler();
			} else if (name.equalsIgnoreCase("noderesolve")) { //$NON-NLS-1$
				return new NodeResolveHandler();
			} else if (name.equalsIgnoreCase("allocation")) { //$NON-NLS-1$
				return null; // ignore
			} else {
				return super.newElement(name);
			}
		}

		@Override
		public void finish() throws SAXException {
			super.finish();
			// Notify listeners that end has been reached.
			for (Object listener : listeners.getListeners()) {
				try {
					((IOpenMpiProcessMapXml13ParserListener ) listener).endDocument();
				} catch (Exception e) {
					OpenMPIPlugin.log(e);
				}
			}

			// Throw exception to force stop parsing.
			// Or else, the parser would complain about content not allows after in trailing section.
			throw new ParseInterruptedException();
		}

	}
	
	public class MapHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("host")) { //$NON-NLS-1$
				return new HostHandler();
			}
			return super.newElement(name);
		}
	}

	public class NodeResolveHandler extends ContextHandler {
		private String nodeName = null;
		private String resolvedName = null;
		
		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase("name")) { //$NON-NLS-1$
				nodeName = value;
			} else if (name.equalsIgnoreCase("resolved")) { //$NON-NLS-1$
				resolvedName = value;
			} else {
				super.setAttribute(name, value);
			}
		}

		@Override
		public void finish() throws SAXException {
			if (nodeName == null) {
				throw new MissingAttributeException("name"); //$NON-NLS-1$
			}
			if (resolvedName == null) {
				throw new MissingAttributeException("resolved"); //$NON-NLS-1$
			}
			
			Node node = map.getNode(resolvedName);
			if (node == null) {
				node = new Node(resolvedName);
				map.addNode(node);
			}
			node.addResolvedName(nodeName);
			super.finish();
		}
	}

	public class HostHandler extends ContextHandler {
		private String name;
		private boolean has_num_slots = false;
		private boolean has_max_slots = false;
		private int num_slots = 0;
		private int max_slots = 0;

		private Node node;
		private List<OpenMPIProcessMap.Process> processes = new ArrayList<OpenMPIProcessMap.Process>();

		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("process")) { //$NON-NLS-1$
				return new ProcessHandler(node);
			}
			return super.newElement(name);
		}

		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase("name")) { //$NON-NLS-1$
				this.name = value;
			} else if (name.equalsIgnoreCase("slots")) { //$NON-NLS-1$
				try {
					num_slots = Integer.parseInt(value);
					if (num_slots < 0)
						throw new InvalidIntegerAttributeException(name, value);
					has_num_slots = true;
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else if (name.equalsIgnoreCase("max_slots")) { //$NON-NLS-1$
				try {
					max_slots = Integer.parseInt(value);
					if (max_slots < 0)
						throw new InvalidIntegerAttributeException(name, value);
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
				throw new MissingAttributeException("name"); //$NON-NLS-1$
			}
			if (! has_num_slots) {
				throw new MissingAttributeException("slots"); //$NON-NLS-1$
			}
			node = map.getNode(name);
			if (node == null) {
				node = new Node(name);
				map.addNode(node);
			}
			try {
				node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getNumberOfNodesAttributeDefinition().create(num_slots));
			} catch (IllegalValueException e) {
				// This is not possible
				OpenMPIPlugin.log(e);
			}
			if (has_max_slots) {
				try {
					node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getMaximalNumberOfNodesAttributeDefinition().create(max_slots));
				} catch (IllegalValueException e) {
					// This is not possible
					OpenMPIPlugin.log(e);
				}
			}
			super.prepare();
		}
		@Override
		public void finish() throws SAXException {
			/*
			 * Count how many processes are running on the host and check if the number is greater than the allowed one.
			 * If yes, then the node is oversubscribed.
			 */
			assert node != null;
			node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getOversubscribedAttributeDefinition().create(processes.size() > num_slots));
			super.finish();
		}
	}

	public class ProcessHandler extends ContextHandler {
		private Node node;
		private int rank;

		public ProcessHandler(Node node) {
			this.node = node;
		}

		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase("rank")) { //$NON-NLS-1$
				try {
					rank = Integer.parseInt(value);
					if (rank < 0)
						throw new InvalidIntegerAttributeException(name, value);
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else {
				super.setAttribute(name, value);
			}
		}

		@Override
		public void finish() throws SAXException {
			OpenMPIProcessMap.Process process = new OpenMPIProcessMap.Process(node, rank, Integer.toString(rank), 1);
			map.addProcess(process);
			super.finish();
		}
	}

	public static OpenMPIProcessMap parse(InputStream is) throws IOException {
		return parse(is, null);
	}

	public static OpenMPIProcessMap parse(InputStream is, IOpenMpiProcessMapXml13ParserListener listener) throws IOException {
		OpenMPIProcessMapXml13Parser parser = new OpenMPIProcessMapXml13Parser();
		if (listener != null) {
			parser.addListener(listener);
		}
		try {
			OpenMPI130InputStream ois = parser.new OpenMPI130InputStream(is);
			BufferedInputStream bis = new BufferedInputStream(ois);
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

	public void addListener(IOpenMpiProcessMapXml13ParserListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IOpenMpiProcessMapXml13ParserListener listener) {
		listeners.remove(listener);
	}

	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("xml_sample.txt"); //$NON-NLS-1$
			OpenMPIProcessMap map = OpenMPIProcessMapXml13Parser.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
