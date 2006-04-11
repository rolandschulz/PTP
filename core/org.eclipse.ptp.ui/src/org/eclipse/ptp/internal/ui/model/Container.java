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
package org.eclipse.ptp.internal.ui.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.ui.model.IContainer;
import org.eclipse.ptp.ui.model.IElement;

/**
 * @author clement chu
 *
 */
public abstract class Container extends Element implements IContainer {
	protected Map dataMap = new HashMap();
	protected Map elementMap = new HashMap();
	protected IElement[] sortedArray = new IElement[0];
	protected int store_element_type = ELEMENT_TYPE; 
	
	/** Constructor
	 * @param parent
	 * @param id set ID
	 * @param name name of set
	 * @param cur_type type of set
	 */
	public Container(IElement parent, String id, String name, int cur_type) {
		super(parent, id, name);
		this.store_element_type = cur_type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#setData(java.lang.String, java.lang.Object)
	 */
	public void setData(String key, Object data) {
		dataMap.put(key, data);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#getData(java.lang.String)
	 */
	public Object getData(String key) {
		return dataMap.get(key);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElement#getParent()
	 */
	public IElement getParent() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#contains(java.lang.String)
	 */
	public boolean contains(String id) {
		return elementMap.containsKey(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#add(org.eclipse.ptp.ui.model.IElement)
	 */
	public void add(IElement element) {
		if (!contains(element.getID()))		
			elementMap.put(element.getID(), element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#remove(org.eclipse.ptp.ui.model.IElement)
	 */
	public void remove(IElement element) {
		remove(element.getID());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#remove(java.lang.String)
	 */
	public void remove(String id) {
		if (contains(id))
			elementMap.remove(id);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#get()
	 */
	public abstract IElement[] get();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#getSorted()
	 */
	public IElement[] getSorted() {
		if (sortedArray.length != size())
			refresh(store_element_type);
		
		return sortedArray;		
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#get(java.lang.String)
	 */
	public IElement get(String id) {
		return (IElement)elementMap.get(id);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#get(int)
	 */
	public IElement get(int index) {
		return getSorted()[index];
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#getElementID(int)
	 */
	public String getElementID(int index) {
		return get(index).getID();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#clearAll()
	 */
	public void clearAll() {
		elementMap.clear();
		sortedArray = new IContainer[0];
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#size()
	 */
	public int size() {
		return elementMap.size();
	}
	/**
	 * @param type
	 */
	public void refresh(int type) {
		IElement[] sortingElements = get();
		sort(sortingElements, type);
		sortedArray = sortingElements;
	}
	
	/** Quick sort given elements by thier ID
	 * @param element elements being sorted
	 * @param low
	 * @param high
	 */
	private void quickSortNum(IElement element[], int low, int high) {
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
				quickSortNum(element, low, hi);
			if (lo < high)
				quickSortNum(element, lo, high);
		}
	}

	/** QUick sort given elements by their text
	 * @param element elements being sorted
	 * @param low
	 * @param high
	 */
	private void quickSortText(IElement[] element, int low, int high) {
		int lo = low;
		int hi = high;
		String mid = "";
		if (high > low) {
			mid = element[(low + high) / 2].getID();
			while (lo <= hi) {
				while ((lo < high) && (element[lo].getID().compareTo(mid)) < 0)
					++lo;
				while ((hi > low) && (element[hi].getID().compareTo(mid)) > 0)
					--hi;
				if (lo <= hi) {
					swap(element, lo, hi);
					++lo;
					--hi;
				}
			}
			if (low < hi)
				quickSortText(element, low, hi);
			if (lo < high)
				quickSortText(element, lo, high);
		}
	}	
	/** Swap elements
	 * @param element
	 * @param i
	 * @param j
	 */
	private void swap(IElement[] element, int i, int j) {
		IElement tempElement;
		tempElement = element[i];
		element[i] = element[j];
		element[j] = tempElement;
	}

	/** Sort elements by given type
	 * @param element elements being to sort
	 * @param type
	 */
	protected void sort(IElement[] element, int type) {
		//set sort will ignore the root
		if (type == SET_TYPE)
			quickSortText(element, 0, element.length - 1);
		else
			quickSortNum(element, 0, element.length - 1);
	}
}
