/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation

*******************************************************************************/

package org.eclipse.ptp.rm.proxy.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.rm.proxy.core.attributes.AttributeDefinition;
import org.eclipse.ptp.rm.proxy.core.element.ElementManager;
import org.eclipse.ptp.rm.proxy.core.element.IElement;
import org.eclipse.ptp.rm.proxy.core.event.IEventFactory;
import org.eclipse.ptp.rm.proxy.core.parser.IParser;
import org.eclipse.ptp.utils.core.RangeSet;


public class Controller {

	private Queue<String> debugFiles = null;
	private String command;
	private IEventFactory eventFactory;
	private IParser parser;
	private int baseID=0;
	private Controller parentController=null;
	
	public ElementManager currentElements = new ElementManager();
	private FilterData filter = null;
	private AttributeDefinition attrDef;
	
	private Controller(String command, AttributeDefinition attrDef, IEventFactory eventFactory, IParser parser) { 
		/*Ugly to have to pass class - stupid java doesn't allow T.class - 
		 */
		this.command = command;
		this.attrDef = attrDef;
		this.eventFactory = eventFactory;
		this.parser = parser;
	}
	
	 public Controller(String command, AttributeDefinition attrDef, IEventFactory eventFactory, IParser parser, int baseID) {
		 this(command, attrDef, eventFactory, parser);
	     this.baseID = baseID;	 
	 }
	 
	 public Controller(String command, AttributeDefinition attrDef, IEventFactory eventFactory, IParser parser, Controller parentController) {
		 this(command, attrDef, eventFactory, parser);
	     this.parentController = parentController;	 
	 }
	 
	public void setDebug(String...files){
		debugFiles = new LinkedList<String>(Arrays.asList(files));
	}
	
	
	public List<IProxyEvent> update() {
		InputStream is = null;
		if (debugFiles!=null) {
			String file = debugFiles.poll();
			is = readFile(file);
			debugFiles.add(file);
		} else {
			is = readProgramOutput(command);
		}
		
		Set<IElement> eList = parser.parse(attrDef, is);
		eList = filterElements(eList);  
		
		currentElements.update(eList);
		
		
		ElementManager addedElements = currentElements.getAddedElements();
		ElementManager removedElements = currentElements.getRemovedElements();
		ElementManager changedElements = currentElements.getChangedElements();
		
		List<IProxyEvent> events = new ArrayList<IProxyEvent>();
		List<List<String>> allNewArgs = addedElements.serializeSplittedByParent();  //all Elements split by ParentKey
		
		for (List<String> newEventArgs :  allNewArgs) {  //loop over different parents
			//change first element from (parent) key to (parent) ID 
			newEventArgs.set(0,Integer.toString(getParentIDFromKey(newEventArgs.get(0)))); 
//			System.out.println("newEventArgs:" + newEventArgs);
			events.add(eventFactory.createNewEvent(newEventArgs.toArray(new String[0])));
		}
		RangeSet removedIDs = removedElements.getElementIDsAsRange();
		if (removedIDs.size() > 0) {
//			System.err.println("eventArgsRemoveRange -> " + removedIDs);
			events.add(eventFactory.createRemoveEvent(new String[]{removedIDs.toString()}));
		}
		
		if (changedElements.size()>0) {
			List<String> changedArgs = changedElements.serialize();
//			System.out.println("changedArgs:"+changedArgs);
			events.add(eventFactory.createChangeEvent(changedArgs.toArray(new String[0])));
		}	
		return events;

	}

	private int getParentIDFromKey(String parentKey) {
		//get ParentID
		int parentID = 0;
		if (parentController != null ) {
			parentID = parentController.currentElements.getElementIDByKey(parentKey);
//			System.err.println(parentKey+":"+parentID);
		} else {
			parentID=baseID;
		}
		return parentID;
	}
	
	private Set<IElement> filterElements(Set<IElement> elements) {
		if (filter==null) 
			return elements;
		Set<IElement> ret = new HashSet<IElement>();
		for (IElement t : elements) {
//			System.err.println("filter:"+filter.pattern+","+filter.key+","+t.getAttribute(filter.key));
			if (filter.pattern.matcher(t.getAttribute(filter.key)).matches())
				ret.add(t);
		}
		return ret;
	}


	private InputStream readProgramOutput(String string) {
		InputStream stdOut = null;
		String[] args = string.split(" "); //$NON-NLS-1$
		try {
			Process p = Runtime.getRuntime().exec(args);

			// read the standard output of the command
			stdOut = new BufferedInputStream(p.getInputStream());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return stdOut;
	}
	
	private InputStream readFile(String path) {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(new File(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return in; 
	}
	
	public void setFilter(String key, String pattern) {
		filter = new FilterData(key,pattern);
	}
	
	class FilterData {
		public FilterData(String key, String pattern) {
			this.key = key;
			this.pattern = Pattern.compile(pattern);
		}
		String key;
		Pattern pattern;
	}
	


	
	
	


}
