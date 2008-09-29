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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * This class represents a configuration for the PDT tool. Thru this class
 * is possible to access the groups of events, subgroups and the events themselves
 * 
 * To generate the XML file from this class, just use the PdtXmlFileGenerator.
 * 
 * @author Richard Maciel
 *
 */
public class EventGroupForest extends AbstractEventElement{
	protected Set<EventGroup> visibleGroups;
	protected Set<EventGroup> invisibleGroups;
	
	/**
	 * Get only "visible" groups - groups which the user can select
	 * @return
	 */
	public Set<EventGroup> getVisibleGroups() {
		return visibleGroups;
	}
	
	/**
	 * Set only "visible" groups - groups which the user can select
	 * @return
	 */
	public void setVisibleGroups(Set<EventGroup> groups) {
		this.visibleGroups = groups;
	}
	
	@Override
	public String getName() {
		// It has a fixed name
		return "root"; //$NON-NLS-1$
	}
	
	@Override
	public void setName(String name) {
	}
	
	@Override
	public void setActive(Boolean active) {
	}
	
	@Override
	public Boolean getActive() {
		return true;
	}
	
	/*@Override
	protected Object clone() throws CloneNotSupportedException {
		EventGroupForest newForest = new EventGroupForest();
		newForest.setName(this.name);
		newForest.setVisibleGroups(this.visibleGroups);
		
		return newForest;
	}*/
	
	/**
	 * Return the "invisible" groups - groups which the user cannot be select (i.e. are always selected)
	 * @return
	 */
	public Set<EventGroup> getInvisibleGroups() {
		return invisibleGroups;
	}
	
	/**
	 * Set the "invisible" groups - - groups which the user cannot be select (i.e. are always selected)
	 * @param groups
	 */
	public void setInvisibleGroups(Set<EventGroup> groups) {
		invisibleGroups = groups;
	}
	
	/**
	 * Return a merge of "invisible" and "visible" groups
	 * @return
	 */
	public Set<EventGroup> getGroupsUnion() {
		HashSet<EventGroup> union = new HashSet<EventGroup>(visibleGroups);
		union.addAll(invisibleGroups);
		
		return union;
	}
	
	public String [] toStringVector() {
		List<String> groupElem = new LinkedList<String>();
		
		for (EventGroup group : visibleGroups) {
			groupElem.add(group.getName() + "=" + group.getActive()); //$NON-NLS-1$
			
			for (EventSubgroup subgroup : group.getSubgroups()) {
				groupElem.add(group.getName() + "." + subgroup.getName() + "=" + subgroup.getActive());//$NON-NLS-1$ //$NON-NLS-2$
				
				for (Event event : subgroup.getEvents()) {
					groupElem.add(group.getName() + "." + subgroup.getName() + "." + event.getName() +  //$NON-NLS-1$ //$NON-NLS-2$
							"=" + event.getActive());//$NON-NLS-1$
				}
			}
		}
		String [] groupArr = new String[groupElem.size()];
		groupElem.toArray(groupArr);
		
		return groupArr;
		//return //(String [])groupElem.toArray();
	}
}
