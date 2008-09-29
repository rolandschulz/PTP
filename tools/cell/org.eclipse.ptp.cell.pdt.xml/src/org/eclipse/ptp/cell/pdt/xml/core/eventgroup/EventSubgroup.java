/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.core.eventgroup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Richard Maciel
 *
 */
public class EventSubgroup extends AbstractEventElement {
	/*public static int SPE_TYPE = 1;
	public static int PPE_TYPE = 2;
	public static int GENERIC_TYPE = 3;*/
	
	protected Set<Event> events;
	protected String id;
	
	protected EventGroup parent;
	
	/*protected int subgroupType;
	
	public int getSubgroupType() {
		return subgroupType;
	}

	public void setSubgroupType(int subgroupType) {
		this.subgroupType = subgroupType;
	}*/

	public EventGroup getParent() {
		return parent;
	}

	public void setParent(EventGroup parent) {
		this.parent = parent;
	}

	public EventSubgroup() {
		events = new HashSet<Event>();
	}
	
	public Set<Event> getEvents() {
		return events;
	}

	public void setEvents(Set<Event> events) {
		this.events = events;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void addEvent(Event event) {
		events.add(event);
	}
	
	public Iterator<Event> getEventIterator() {
		return events.iterator();
	}
	
}
