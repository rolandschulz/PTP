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
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPINodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Node;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Process;
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
	private final static String rootTag = "ompi"; //$NON-NLS-1$
	private final static String mapTag = "map"; //$NON-NLS-1$
	private final static String hostTag = "host"; //$NON-NLS-1$
	private final static String processTag = "process"; //$NON-NLS-1$
	private final static String nodeResolveTag = "noderesolve"; //$NON-NLS-1$
	private final static String stdoutTag = "stdout"; //$NON-NLS-1$
	private final static String stderrTag = "stderr"; //$NON-NLS-1$
	private final static String stddiagTag = "stddiag"; //$NON-NLS-1$

	private final static String nameAttribute = "name"; //$NON-NLS-1$
	private final static String resolvedAttribute = "resolved"; //$NON-NLS-1$
	private final static String rankAttribute = "rank"; //$NON-NLS-1$
	private final static String slotsAttribute = "slots"; //$NON-NLS-1$
	private final static String maxSlotsAttribute = "max_slots"; //$NON-NLS-1$
	
	private OpenMPIProcessMapXml13Parser() {
		// Do not allow instances.
	}
	
	private class StackContextHandler extends DefaultHandler {
		Stack<ContextHandler> handlers = new Stack<ContextHandler>();

		public StackContextHandler(ContextHandler documentHandler) {
			handlers.push(documentHandler);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
		 */
		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			notifyStart();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
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

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);
			ContextHandler handler = handlers.peek();
			handler.characters(ch, start, length);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			super.endElement(uri, localName, name);
			//			System.out.println("End element: "+name);
			ContextHandler handler = handlers.pop();
			if (handler != null) {
				handler.finish();
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
		 */
		@Override
		public void error(SAXParseException e) throws SAXException {
			super.error(e);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			super.fatalError(e);
		}
	}

	protected final OpenMPIProcessMap map = new OpenMPIProcessMap();
	protected final StackContextHandler handler = new StackContextHandler(new DocumentHandler());
	protected final ListenerList listeners = new ListenerList();

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
			// Ignore
		}
		/** Called for each argument found at the beginning of the tag. */
		public void setAttribute(String name, String value) throws SAXException {
			throw new UnknownAttributeException(name);
		}
		/** Called when all attributes have been read and tag content is going to be read. */
		public void prepare() throws SAXException {
			// Ignore
		}
		/** Called with characters from the element. */
		public void characters(char[] ch, int start, int length) throws SAXException {
			// Ignore
		}
		/** Called when finished reading the end of the tag. */
		public void finish() throws SAXException {
			// Ignore
		}
	}

	public class DocumentHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase(rootTag)) {
				return new OmpiHandler();
			}
			return super.newElement(name);
		}
	}

	public class OmpiHandler extends ContextHandler {
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase(mapTag)) {
				return new MapHandler();
			} else if (name.equalsIgnoreCase(stdoutTag)) {
				return new StdoutHandler();
			} else if (name.equalsIgnoreCase(stderrTag)) {
				return new StderrHandler();
			} else if (name.equalsIgnoreCase(stddiagTag)) {
				return new StddiagHandler();
			} else {
				return super.newElement(name);
			}
		}

		@Override
		public void finish() throws SAXException {
			super.finish();
			notifyFinish();
			// Throw exception to force stop parsing.
			// Or else, the parser would complain about content not allows after in trailing section.
			throw new ParseInterruptedException();
		}

	}
	
	public class MapHandler extends ContextHandler {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.ContextHandler#newElement(java.lang.String)
		 */
		@Override
		public ContextHandler newElement(String name) throws SAXException {
			if (name.equalsIgnoreCase(hostTag)) {
				return new HostHandler();
			}
			return super.newElement(name);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.ContextHandler#finish()
		 */
		@Override
		public void finish() throws SAXException {
			notifyFinishMap(map.getAttributeManager());
		}
	}

	public abstract class OutputHandler extends ContextHandler {
		private Process process = null;
		private StringBuffer content = new StringBuffer();
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.ContextHandler#setAttribute(java.lang.String, java.lang.String)
		 */
		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase(rankAttribute)) {
				try {
					int rank = Integer.parseInt(value);
					if (rank < 0) {
						throw new InvalidIntegerAttributeException(name, value);
					}
					process = map.getProcesses().get(rank);
					if (process == null) {
						throw new InvalidIntegerAttributeException(name, value);
					}
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else {
				super.setAttribute(name, value);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.ContextHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			content.append(ch, start, length);
		}	
		
		/**
		 * Get the process that generated the output
		 * 
		 * @return process
		 */
		public Process getProcess() {
			return process;
		}
		
		/**
		 * Get the output generated by the process
		 * 
		 * @return string containing output
		 */
		public String getOutput() {
			return new String(content);
		}
	}
	
	public class StdoutHandler extends OutputHandler {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.ContextHandler#finish()
		 */
		@Override
		public void finish() throws SAXException {
			notifyStdout(getProcess(), getOutput());
		}
	}
	
	public class StderrHandler extends OutputHandler {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.ContextHandler#finish()
		 */
		@Override
		public void finish() throws SAXException {
			notifyStderr(getProcess(), getOutput());
		}
	}
	
	public class StddiagHandler extends OutputHandler {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMapXml13Parser.ContextHandler#finish()
		 */
		@Override
		public void finish() throws SAXException {
			notifyStderr(getProcess(), getOutput());
		}	
	}
	
	public class NodeResolveHandler extends ContextHandler {
		private Node node;

		public NodeResolveHandler(Node node) {
			this.node = node;
		}
		
		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase(resolvedAttribute)) {
				node.addResolvedName(value);
			} else {
				super.setAttribute(name, value);
			}
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
			if (name.equalsIgnoreCase(processTag)) {
				return new ProcessHandler(node);
			} else if (name.equalsIgnoreCase(nodeResolveTag)) {
				return new NodeResolveHandler(node);
			}
			return super.newElement(name);
		}

		@Override
		public void setAttribute(String name, String value) throws SAXException {
			if (name.equalsIgnoreCase(nameAttribute)) {
				this.name = value;
			} else if (name.equalsIgnoreCase(slotsAttribute)) {
				try {
					num_slots = Integer.parseInt(value);
					if (num_slots < 0)
						throw new InvalidIntegerAttributeException(name, value);
					has_num_slots = true;
				} catch (NumberFormatException e) {
					throw new InvalidIntegerAttributeException(name, value);
				}
			} else if (name.equalsIgnoreCase(maxSlotsAttribute)) {
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
				throw new MissingAttributeException(nameAttribute);
			}
			if (! has_num_slots) {
				throw new MissingAttributeException(slotsAttribute);
			}
			node = map.getNode(name);
			if (node == null) {
				node = new Node(name);
				map.addNode(node);
			}
			try {
				node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getNumberOfNodesAttributeDefinition().create(Integer.valueOf(num_slots)));
			} catch (IllegalValueException e) {
				// This is not possible
				OpenMPIPlugin.log(e);
			}
			if (has_max_slots) {
				try {
					node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getMaximalNumberOfNodesAttributeDefinition().create(Integer.valueOf(max_slots)));
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
			node.getAttributeManager().addAttribute(OpenMPINodeAttributes.getOversubscribedAttributeDefinition().create(Boolean.valueOf(processes.size() > num_slots)));
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
			if (name.equalsIgnoreCase(rankAttribute)) {
				try {
					rank = Integer.parseInt(value);
					if (rank < 0) {
						throw new InvalidIntegerAttributeException(name, value);
					}
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
			notifyNewProcess(process);
			super.finish();
		}
	}

	public static void parse(InputStream is) throws IOException {
		parse(is, null);
	}

	public static void parse(InputStream is, IOpenMPIProcessMapParserListener listener) throws IOException {
		OpenMPIProcessMapXml13Parser parser = new OpenMPIProcessMapXml13Parser();
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
	}

	public void addListener(IOpenMPIProcessMapParserListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IOpenMPIProcessMapParserListener listener) {
		listeners.remove(listener);
	}

	protected void notifyStart() {
		// Notify listeners that end has been reached.
		for (Object listener : listeners.getListeners()) {
			try {
				((IOpenMPIProcessMapParserListener) listener).start();
			} catch (Exception e) {
				OpenMPIPlugin.log(e);
			}
		}

	}
	
	protected void notifyFinish() {
		// Notify listeners that end has been reached.
		for (Object listener : listeners.getListeners()) {
			try {
				((IOpenMPIProcessMapParserListener) listener).finish();
			} catch (Exception e) {
				OpenMPIPlugin.log(e);
			}
		}

	}
	
	protected void notifyFinishMap(AttributeManager manager) {
		// Notify listeners that end has been reached.
		for (Object listener : listeners.getListeners()) {
			try {
				((IOpenMPIProcessMapParserListener) listener).finishMap(manager);
			} catch (Exception e) {
				OpenMPIPlugin.log(e);
			}
		}

	}

	protected void notifyNewProcess(Process proc) {
		// Notify listeners that end has been reached.
		for (Object listener : listeners.getListeners()) {
			try {
				((IOpenMPIProcessMapParserListener) listener).newProcess(proc);
			} catch (Exception e) {
				OpenMPIPlugin.log(e);
			}
		}

	}

	protected void notifyStdout(Process process, String output) {
		// Notify listeners that end has been reached.
		for (Object listener : listeners.getListeners()) {
			try {
				((IOpenMPIProcessMapParserListener) listener).stdout(process, output);
			} catch (Exception e) {
				OpenMPIPlugin.log(e);
			}
		}

	}
	
	protected void notifyStderr(Process process, String output) {
		// Notify listeners that end has been reached.
		for (Object listener : listeners.getListeners()) {
			try {
				((IOpenMPIProcessMapParserListener) listener).stderr(process, output);
			} catch (Exception e) {
				OpenMPIPlugin.log(e);
			}
		}

	}
	
	public static void main(String[] args) {
		try {
			FileInputStream is = new FileInputStream("xml_sample.txt"); //$NON-NLS-1$
			OpenMPIProcessMapXml13Parser.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
