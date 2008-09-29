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

import org.eclipse.core.runtime.IPath;


/**
 * Represents a group of PDT events, that contains subgroups.
 * 
 * @author Richard Maciel
 *
 */
public class EventGroup extends AbstractEventElement {	
	public static String GENERAL_GROUP = "GENERAL"; //$NON-NLS-1$
	
	protected Set<EventSubgroup> subgroups;
	
	
	protected Float yStart;
	protected Float yEnd;
	protected Integer color;
	protected String id; 
	protected IPath associatedPath;
	
	public EventGroup() {
		subgroups = new HashSet<EventSubgroup>();
	}
	
	public Set<EventSubgroup> getSubgroups() {
		return subgroups;
	}
	
	public void setSubgroups(Set<EventSubgroup> subgroups) {
		this.subgroups = subgroups;
	}
	
	public void addSubgroup(EventSubgroup subgroup) {
		subgroups.add(subgroup);
	}
	
	public Iterator<EventSubgroup> getSubgroupIterator() {
		return subgroups.iterator();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IPath getAssociatedPath() {
		return associatedPath;
	}

	public void setAssociatedPath(IPath associatedPath) {
		this.associatedPath = associatedPath;
	}

	public Float getYStart() {
		return yStart;
	}

	public void setYStart(Float start) {
		yStart = start;
	}

	public Float getYEnd() {
		return yEnd;
	}

	public void setYEnd(Float end) {
		yEnd = end;
	}

	public Integer getColor() {
		return color;
	}

	public void setColor(Integer color) {
		this.color = color;
	}
}
