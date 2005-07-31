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
package org.eclipse.ptp.ui.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.ui.model.IContainer;
import org.eclipse.ptp.ui.model.IElement;

/**
 * @author clement chu
 *
 */
public abstract class Container extends Element implements IContainer {
	protected Map elementMap = new HashMap();
	protected IElement[] sortedArray = new IElement[0];
	
	public Container(String id, boolean selected) {
		super(id, selected);
	}
	
	public boolean contains(String id) {
		return elementMap.containsKey(id);
	}
	
	public void add(IElement element) {
		if (!contains(element.getID()))		
			elementMap.put(element.getID(), element);
	}
	public void remove(IElement element) {
		remove(element.getID());
	}
	public void remove(String id) {
		if (contains(id))
			elementMap.remove(id);
	}
		
	public abstract IElement[] get();
	
	public IElement[] getSorted() {
		if (sortedArray.length != size())
			refresh();
		
		return sortedArray;		
	}	
	
	public IElement get(String id) {
		return (IElement)elementMap.get(id);
	}
	public IElement get(int index) {
		return getSorted()[index];
	}
	public String getElementID(int index) {
		return get(index).getID();
	}
	public void clearAll() {
		elementMap.clear();
		sortedArray = new IContainer[0];
	}
	public int size() {
		return elementMap.size();
	}
	public void refresh() {
		IElement[] sortingElements = get();
		sort(sortingElements);
		sortedArray = sortingElements;
	}
	
	private void quickSort(IElement element[], int low, int high) {
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

	private void swap(IElement element[], int i, int j) {
		IElement tempElement;
		tempElement = element[i];
		element[i] = element[j];
		element[j] = tempElement;
	}

	protected void sort(IElement element[]) {
		quickSort(element, 0, element.length - 1);
	}
}
