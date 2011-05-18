/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation
 *    Benjamin Lindner (ben@benlabs.net) - Attribute Definitions and Mapping (bug 316671)
 *    Albert L. Rossi (arossi@ncsa.illinois.edu) added call to EventAttributeListener
 *                    to handle potential New Job event mapping of batch Id with
 *                    client-generated jobSubId (update); 10/11/2010
 *******************************************************************************/

package org.eclipse.ptp.rm.proxy.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent.Level;
import org.eclipse.ptp.rm.proxy.core.element.ElementManager;
import org.eclipse.ptp.rm.proxy.core.element.IElement;
import org.eclipse.ptp.rm.proxy.core.event.IEventFactory;
import org.eclipse.ptp.rm.proxy.core.messages.Messages;
import org.eclipse.ptp.rm.proxy.core.parser.IParser;
import org.eclipse.ptp.utils.core.RangeSet;

/**
 * @author rschulz
 * 
 */
public class Controller {

	/**
	 * @since 2.0
	 */
	public static interface ErrorHandler {
		void handle(Level level, String msg);
	}

	/**
	 * @since 2.0
	 */
	public static interface EventArgumentsHandler {
		void handle(List<String> eventArgs);
	}

	class FilterData {
		String key;
		Pattern pattern;

		public FilterData(String key, String pattern) {
			this.key = key;
			this.pattern = Pattern.compile(pattern);
		}
	}

	private Queue<String> debugFiles = null;
	private ErrorHandler errorHandler = null;
	private EventArgumentsHandler eventArgumentsHandler = null;
	private final String command;
	private final IEventFactory eventFactory;
	private final IParser parser;
	private int baseID = 0;

	private Controller parentController = null;
	public ElementManager currentElements = new ElementManager();
	private FilterData filter = null;

	private String elementKeyID;
	private String elementParentKeyID;

	private List<IAttributeDefinition<?, ?, ?>> AttributeDefinitions;
	private List<String> requiredAttributeKeys;
	private List<List<Object>> ParserKeyMap;
	private List<List<Object>> ParserValueMap;
	private List<List<Object>> ProtocolKeyMap;
	private List<List<Object>> ProtocolValueMap;

	private static ExecutorService fPool = Executors.newCachedThreadPool();

	/**
	 * Instantiates a new controller.
	 * 
	 * @param command
	 *            the executable to be run for each update
	 * @param eventFactory
	 *            the event factory to create events to be sent to the client
	 * @param parser
	 *            the parser for the output of the command
	 * @since 2.0
	 */
	public Controller(String command, IEventFactory eventFactory, IParser parser) {
		this.command = command;
		this.eventFactory = eventFactory;
		this.parser = parser;
	}

	/**
	 * Instantiates a new controller.
	 * 
	 * @param command
	 *            the executable to be run for each update
	 * @param eventFactory
	 *            the event factory to create events to be sent to the client
	 * @param parser
	 *            the parser for the output of the command
	 * @param parentController
	 *            the controller of the parent type according to the proxy
	 *            protocol
	 * @since 2.0
	 */
	public Controller(String command, IEventFactory eventFactory, IParser parser, Controller parentController) {
		this(command, eventFactory, parser);
		this.parentController = parentController;
	}

	/**
	 * @since 2.0
	 */
	public List<IAttributeDefinition<?, ?, ?>> getAttributeDefinitions() {
		return AttributeDefinitions;
	}

	/**
	 * Run the command and parse the output.
	 * 
	 * @return the list of elements in the program output
	 * @throws Execption
	 */
	public Set<IElement> parse() throws Exception {

		if (debugFiles != null) {
			String file = debugFiles.poll();
			InputStream is = new BufferedInputStream(new FileInputStream(new File(file)));
			debugFiles.add(file);

			/*
			 * throws Exception to allow any Exception from different parsers
			 */
			try {
				return parser.parse(requiredAttributeKeys, AttributeDefinitions, ParserKeyMap, ParserValueMap, is, elementKeyID,
						elementParentKeyID);
			} finally {
				is.close();
			}
		}
		Process p = Runtime.getRuntime().exec(command);
		final InputStream is = new BufferedInputStream(p.getInputStream());
		final InputStream err = new BufferedInputStream(p.getErrorStream());
		Callable<Set<IElement>> parseThread = new Callable<Set<IElement>>() {
			public Set<IElement> call() throws Exception {
				Set<IElement> ret = parser.parse(requiredAttributeKeys, AttributeDefinitions, ParserKeyMap, ParserValueMap, is,
						elementKeyID, elementParentKeyID);
				try {
					while (is.read(new byte[1024]) > -1) {
					} /* read any additional unparsed output to prevent dead-lock */
				} catch (IOException e) {
					// ignore
				}
				return ret;
			}
		};
		Future<Set<IElement>> ret = fPool.submit(parseThread);
		byte buf[] = new byte[1024];
		int length;
		while ((length = err.read(buf)) > -1) {
			if (errorHandler != null) {
				errorHandler.handle(Level.WARNING, command + ": " + new String(buf, 0, length)); //$NON-NLS-1$
			}
		}
		p.waitFor();
		int exitValue = p.exitValue();
		if (exitValue != 0 && errorHandler != null) {
			errorHandler.handle(Level.ERROR, MessageFormat.format(Messages.getString("Controller.0"), command, exitValue)); //$NON-NLS-1$
		}
		return ret.get();
	}

	/**
	 * @since 2.0
	 */
	public void setAttributeDefinitions(List<IAttributeDefinition<?, ?, ?>> definitions) throws IOException, Exception {
		this.AttributeDefinitions = definitions;
	}

	/**
	 * Sets baseID. Should only be used if no parent controller has been given
	 * to the constructor.
	 * 
	 * @param baseID
	 *            baseID
	 */
	public void setBaseID(int baseID) {
		if (parentController != null) {
			throw new AssertionError("Illegal to specify baseID and parentController!"); //$NON-NLS-1$
		}
		this.baseID = baseID;
	}

	/**
	 * Activates debug mode and sets the debug files
	 * 
	 * @param files
	 *            list of files to iterate over in debug mode
	 */
	public void setDebug(String... files) {
		debugFiles = new LinkedList<String>(Arrays.asList(files));
	}

	/**
	 * @since 2.0
	 */
	public void setElementKeyID(String key) {
		elementKeyID = key;
	}

	/**
	 * @since 2.0
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * @since 2.0
	 */
	public void setEventArgumentsHandler(EventArgumentsHandler eventArgumentsHandler) {
		this.eventArgumentsHandler = eventArgumentsHandler;
	}

	/**
	 * Acctivate filters for the output to be sent to the client
	 * 
	 * @param key
	 *            attribute name to match
	 * @param pattern
	 *            regular expression pattern to match against attribute value
	 */
	public void setFilter(String key, String pattern) {
		filter = new FilterData(key, pattern);
	}

	/**
	 * @since 2.0
	 */
	public void setParentKeyID(String key) {
		elementParentKeyID = key;
	}

	/**
	 * @since 2.0
	 */
	public void setParserKeyMap(List<List<Object>> ParserKeyMap) throws IOException, Exception {
		this.ParserKeyMap = ParserKeyMap;
	}

	/**
	 * @since 2.0
	 */
	public void setParserValueMap(List<List<Object>> ParserValueMap) throws IOException, Exception {
		this.ParserValueMap = ParserValueMap;
	}

	/**
	 * @since 2.0
	 */
	public void setProtocolKeyMap(List<List<Object>> ProtocolKeyMap) throws IOException, Exception {
		this.ProtocolKeyMap = ProtocolKeyMap;
	}

	/**
	 * @since 2.0
	 */
	public void setProtocolValueMap(List<List<Object>> ProtocolValueMap) throws IOException, Exception {
		this.ProtocolValueMap = ProtocolValueMap;
	}

	/**
	 * @since 2.0
	 */
	public void setRequiredAttributeKeys(List<String> requiredAttributeKeys) throws IOException, Exception {
		this.requiredAttributeKeys = requiredAttributeKeys;
	}

	/**
	 * Run the command and parse the output. Generates all the events for
	 * changes since the last update.
	 * 
	 * @return the list of events generated by this update
	 * @throws Exception
	 */
	public List<IProxyEvent> update() throws Exception {
		Set<IElement> eList = parse();
		eList = filterElements(eList);

		currentElements.update(eList);

		ElementManager addedElements = currentElements.getAddedElements();
		ElementManager removedElements = currentElements.getRemovedElements();
		ElementManager changedElements = currentElements.getChangedElements();

		List<IProxyEvent> events = new ArrayList<IProxyEvent>();
		List<List<String>> allNewArgs = addedElements.serializeSplittedByParent(ProtocolKeyMap, ProtocolValueMap); // all
		// Elements
		// split
		// by
		// ParentKey

		for (List<String> newEventArgs : allNewArgs) { // loop over different
			// parents
			// change first element of string list (contains parent) from
			// (parent) key to (parent) ID
			newEventArgs.set(0, Integer.toString(getParentIDFromKey(newEventArgs.get(0))));
			/*
			 * special processing of new job event - alr 10/11/2010
			 */
			if (eventArgumentsHandler != null) {
				eventArgumentsHandler.handle(newEventArgs);
			}
			events.add(eventFactory.createNewEvent(newEventArgs.toArray(new String[0])));
		}
		RangeSet removedIDs = removedElements.getElementIDsAsRange();
		if (removedIDs.size() > 0) {
			// System.err.println("eventArgsRemoveRange -> " + removedIDs);
			events.add(eventFactory.createRemoveEvent(new String[] { removedIDs.toString() }));
		}

		if (changedElements.size() > 0) {
			List<String> changedArgs = changedElements.serialize(ProtocolKeyMap, ProtocolValueMap);
			// System.out.println("changedArgs:"+changedArgs);
			events.add(eventFactory.createChangeEvent(changedArgs.toArray(new String[0])));
		}
		return events;

	}

	private Set<IElement> filterElements(Set<IElement> elements) {
		if (filter == null) {
			return elements;
		}
		Set<IElement> ret = new HashSet<IElement>();
		for (IElement t : elements) {
			// System.err.println("filter:"+filter.pattern+","+filter.key+","+t.getAttribute(filter.key));
			if (filter.pattern.matcher(t.getAttribute(filter.key)).matches()) {
				ret.add(t);
			}
		}
		return ret;
	}

	private int getParentIDFromKey(String parentKey) {
		// get ParentID
		int parentID = 0;
		if (parentController != null) {
			parentID = parentController.currentElements.getElementIDByKey(parentKey);
			// System.err.println(parentKey+":"+parentID);
		} else {
			parentID = baseID;
		}
		return parentID;
	}

}
