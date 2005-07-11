/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.ui.model.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
import org.eclipse.ptp.debug.ui.model.IGroupManager;


/**
 * @author clement chu
 *
 */
public class GroupManager implements IGroupManager {
	private Map groups = new HashMap();
	private IElementGroup[] sortedElementGroups = new IElementGroup[0];
	
	public GroupManager() {
		//create root 
		addGroup(new ElementGroup(GROUP_ROOT_ID));
	}
	public IElementGroup getGroupRoot() {
		if (size() == 0)
			addGroup(new ElementGroup(GROUP_ROOT_ID));
		
		return getGroup(GROUP_ROOT_ID);
	}
	public void addGroup(IElementGroup aGroup) {
		groups.put(aGroup.getID(), aGroup);
	}
	public void removeGroup(IElementGroup aGroup) {
		aGroup.clearAll();
		groups.remove(aGroup.getID());
	}
	public void removeGroup(String id) {
		((IElementGroup)groups.remove(id)).clearAll();
	}
	public IElementGroup[] getGroups() {
		return (IElementGroup[])groups.values().toArray(new IElementGroup[size()]);
	}
	public IElementGroup[] getSortedGroups() {
		if (sortedElementGroups.length != size())
			refresh();
		
		return sortedElementGroups;
	}
	public IElementGroup getGroup(String id) {
		return (IElementGroup)groups.get(id);
	}
	public IElementGroup getGroup(int index) {
		return getSortedGroups()[index];
	}
	public String getGroupID(int index) {
		return getGroup(index).getID();
	}	
	public int size() {
		return groups.size();
	}
	public void clearAll() {
		for (Iterator i=groups.values().iterator(); i.hasNext();) {
			((IElementGroup)i.next()).clearAll();
		}
		groups.clear();
		sortedElementGroups = new IElementGroup[0];
	}
	public void refresh() {
		IElementGroup[] sortingElementGroups = getGroups();
		GroupManager.sort(sortingElementGroups);
		sortedElementGroups = sortingElementGroups;
	}

	private static void quickSort(IElement element[], int low, int high) {
		int lo = low;
		int hi = high;
		int mid;
		if (high > low) {
			mid = Integer.parseInt(element[(low + high) / 2].getID());
			while (lo <= hi) {
				while ((lo < high) && (Integer.parseInt(element[lo].getID()) < mid))
					++lo;
				while ((hi > low) && (Integer.parseInt(element[hi].getID()) > mid))
					--hi;
				if (lo <= hi) {
					swap(element, lo, hi);
					++lo;
					--hi;
				}
			}
			if (low < hi)
				quickSort(element, low, hi);
			if (lo < high)
				quickSort(element, lo, high);
		}
	}

	private static void swap(IElement element[], int i, int j) {
		IElement tempElement;
		tempElement = element[i];
		element[i] = element[j];
		element[j] = tempElement;
	}

	public static void sort(IElement element[]) {
		quickSort(element, 0, element.length - 1);
	}
}
