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
package org.eclipse.ptp.cell.pdt.xml.wizard.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.AbstractEventElement;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.Event;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventSubgroup;


/**
 * @author Richard Maciel
 *
 */
public class PdtEventTreeContentProvider implements IContentProvider, ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getChildren(Object parentElement) {
		checkParameterValidity(parentElement);
		
		if(parentElement instanceof EventGroupForest) {
			EventGroupForest root = (EventGroupForest)parentElement;
			return root.getVisibleGroups().toArray();
		} else if(parentElement instanceof EventGroup) {
			EventGroup group = (EventGroup)parentElement;
			return group.getSubgroups().toArray();
		} else if(parentElement instanceof EventSubgroup) {
			EventSubgroup subgroup = (EventSubgroup)parentElement;
			return subgroup.getEvents().toArray();
		} 
			// Event class
			return new Object[0];
	}

	public Object getParent(Object element) {
		checkParameterValidity(element);
		
		if(element instanceof EventGroupForest) {
			return null;
		} else if(element instanceof EventGroup) {
			return null;
		} else if(element instanceof EventSubgroup) {
			EventSubgroup subgroup = (EventSubgroup)element;
			return subgroup.getParent();
		}
		
		// Event
		Event event = (Event)element;
		return event.getParent();
	}

	public boolean hasChildren(Object element) {
		checkParameterValidity(element);
		
		if(element instanceof EventGroupForest) {
			EventGroupForest root = (EventGroupForest)element;
			return !root.getVisibleGroups().isEmpty();
		} else if(element instanceof EventGroup) {
			EventGroup group = (EventGroup)element;
			return !group.getSubgroups().isEmpty();
		} else if(element instanceof EventSubgroup) {
			EventSubgroup subgroup = (EventSubgroup)element;
			return !subgroup.getEvents().isEmpty();
		}
		
		return false;
	}

	public Object[] getElements(Object inputElement) {
		checkParameterValidity(inputElement);
		
		// Filter the GENERAL item
		List<EventGroup> eventGroupList = new LinkedList<EventGroup>();
		for (EventGroup eventGroup : ((EventGroupForest)inputElement).getVisibleGroups()) {
			if(!eventGroup.getName().equals(EventGroup.GENERAL_GROUP)) {
				eventGroupList.add(eventGroup);
			}
		}
		
		return eventGroupList.toArray();
	}
	
	private void checkParameterValidity(Object parameter) {
		assert parameter instanceof AbstractEventElement : Messages.PdtEventTreeContentProvider_ValidateParameter_ParameterMustBeDerivedFromAbstractEventElement;
	}

}
