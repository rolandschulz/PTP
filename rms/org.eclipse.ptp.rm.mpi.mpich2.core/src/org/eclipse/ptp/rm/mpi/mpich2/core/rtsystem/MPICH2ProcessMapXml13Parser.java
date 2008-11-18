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
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2NodeAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem.MPICH2ProcessMap.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2ProcessMapXml13Parser {
	private MPICH2ProcessMapXml13Parser() {
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
			// Notify listeners that parsing has started
			for (Object listener : listeners.getListeners()) {
				try {
					((IOpenMpiProcessMapXml13ParserListener ) listener).startDocument();
				} catch (Exception e) {
					MPICH2Plugin.log(e);
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
			// TODO Auto-generated method stub
			super.error(e);
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			// TODO Auto-generated method stub
			//			System.out.println(e.getClass());

			super.fatalError(e);
		}
	}

	protected final MPICH2ProcessMap map = new MPICH2ProcessMap();
	protected final StackContextHandler handler = new StackContextHandler(new DocumentHandler());
	protected final ListenerList listeners = new ListenerList();

	public static interface IOpenMpiProcessMapXml13ParserListener {
		void startDocument();
		void endDocument();
	}

	private class UnknownElementException extends SAXException {
		private static final long serialVersionUID = 1L;

		public UnknownElementException(String name) {
			super(NLS.bind(Messages.MPICH2ProcessMapXml13Parser_Exception_UnknownElement, name));
		}
	}

	private class UnknownAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public UnknownAttributeException(String name) {
			super(NLS.bind(Messages.MPICH2ProcessMapXml13Parser_Exception_UnknownAttribute, name));
		}
	}

	private class MissingAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public MissingAttributeException(String name) {
			super(NLS.bind(Messages.MPICH2ProcessMapXml13Parser_Exception_MissingAttribute, name));
		}
	}

	private class InvalidIntegerAttributeException extends SAXException {
		private static final long serialVersionUID = 1L;

		public InvalidIntegerAttributeException(String name, String value) {
			super(NLS.bind(Messages.MPICH2ProcessMapXml13Parser_Exception_AttributeNotInteger, name, value));
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
			if (name.equalsIgnoreCase("map")) //$NON-NLS-1$
				return new MapHandler();
			else if (name.equalsIgnoreCase("allocation")) //$NON-NLS-1$
				return null; // ignore
			else
				return super.newElement(name);
		}
	}

	public class MapHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("host")) //$NON-NLS-1$
				return new HostHandler();
			else
				return super.newElement(name);
		}

		@Override
		public void finish() throws SAXException {
			super.finish();
			// Notify listeners that end has been reached.
			for (Object listener : listeners.getListeners()) {
				try {
					((IOpenMpiProcessMapXml13ParserListener ) listener).endDocument();
				} catch (Exception e) {
					MPICH2Plugin.log(e);
				}
			}

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
		private List<MPICH2ProcessMap.Process> processes = new ArrayList<MPICH2ProcessMap.Process>();

		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase("process")) //$NON-NLS-1$
				return new ProcessHandler(node);
			else
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
			if (name == null)
				throw new MissingAttributeException("name"); //$NON-NLS-1$
			if (! has_num_slots)
				throw new MissingAttributeException("slots"); //$NON-NLS-1$
			node = new Node(host_counter++, name);
			map.addNode(node);
			try {
				node.getAttributeManager().addAttribute(MPICH2NodeAttributes.getNumberOfNodesAttributeDefinition().create(num_slots));
			} catch (IllegalValueException e) {
				// This is not possible
				MPICH2Plugin.log(e);
			}
			if (has_max_slots) {
				try {
					node.getAttributeManager().addAttribute(MPICH2NodeAttributes.getMaximalNumberOfNodesAttributeDefinition().create(max_slots));
				} catch (IllegalValueException e) {
					// This is not possible
					MPICH2Plugin.log(e);
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
			node.getAttributeManager().addAttribute(MPICH2NodeAttributes.getOversubscribedAttributeDefinition().create(processes.size() > num_slots));
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
			if (name.equalsIgnoreCase("rank")) { //$NON-NLS-1$
				try {
					rank = Integer.parseInt(value);
					if (rank < 0)
						throw new InvalidIntegerAttributeException(name, value);
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
			MPICH2ProcessMap.Process process = new MPICH2ProcessMap.Process(node, rank, Integer.toString(rank), 1);
			map.addProcess(process);
			super.finish();
		}
	}

	public static MPICH2ProcessMap parse(InputStream is) throws IOException {
		return parse(is, null);
	}

	public static MPICH2ProcessMap parse(InputStream is, IOpenMpiProcessMapXml13ParserListener listener) throws IOException {
		MPICH2ProcessMapXml13Parser parser = new MPICH2ProcessMapXml13Parser();
		if (listener != null) {
			parser.addListener(listener);
		}
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

	public void addListener(IOpenMpiProcessMapXml13ParserListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IOpenMpiProcessMapXml13ParserListener listener) {
		listeners.remove(listener);
	}

	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("xml_sample.txt"); //$NON-NLS-1$
			MPICH2ProcessMap map = MPICH2ProcessMapXml13Parser.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
