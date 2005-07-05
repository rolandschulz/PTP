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
package org.eclipse.ptp.debug.ui.model;

import java.util.HashMap;
import java.util.Map;


/**
 * @author clement chu
 *
 */
public class GroupManager {
	private Map groups = new HashMap(1);
	
	public void addGroup(ElementGroup aGroup) {
		groups.put(aGroup.getText(), aGroup);
	}
	public void removeGroup(ElementGroup aGroup) {
		aGroup.clearAll();
		groups.remove(aGroup.getText());
	}
	public void removeGroup(String key) {
		((ElementGroup)groups.remove(key)).clearAll();
	}
	public ElementGroup[] getGroups() {
		return (ElementGroup[])groups.values().toArray(new ElementGroup[size()]);
	}
	public ElementGroup[] getSortedGroups() {
		ElementGroup[] sortGroupElements = getGroups();
		GroupManager.sort(sortGroupElements);
		return sortGroupElements;		
	}
	public ElementGroup getGroup(String key) {
		return (ElementGroup)groups.get(key);
	}
	public int size() {
		return groups.size();
	}
	public void shutdown() {
		groups.clear();
	}

	private static void quickSort(Element element[], int low, int high) {
		int lo = low;
		int hi = high;
		int mid;
		if (high > low) {
			mid = element[(low + high) / 2].getID();
			while (lo <= hi) {
				while ((lo < high) && (element[lo].getID() < mid))
					++lo;
				while ((hi > low) && (element[hi].getID() > mid))
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

	private static void swap(Element element[], int i, int j) {
		Element tempElement;
		tempElement = element[i];
		element[i] = element[j];
		element[j] = tempElement;
	}

	public static void sort(Element element[]) {
		quickSort(element, 0, element.length - 1);
	}
}
