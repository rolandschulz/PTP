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
package org.eclipse.ptp.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author clement chu
 *
 */
public class ElementSet extends Element implements IElementSet {
	private Map<String,IElement> elementMap = new HashMap<String,IElement>();
	private List<IElement> elementList = new ArrayList<IElement>();
	private List<String> matchSetList = new ArrayList<String>();
	private int number_of_elements = 0;
	
	public ElementSet(IElement parent, String id, String name) {
		super(parent, id, name, null);
	}
	public boolean isRootSet() {
		return (id.equals(IElementHandler.SET_ROOT_ID));
	}
	public void addElements(IElement[] elements) {
		for (IElement element : elements) {
			if (contains(element.getID()))
				continue;

			if (!isRootSet()) {
				IElementSet[] sets = ((IElementHandler)getParent()).getSetsWithElement(element.getID());
				for (IElementSet set : sets) {
					set.addMatchSet(getID());
					addMatchSet(set.getID());
				}
			}
			elementMap.put(element.getID(), element);
			elementList.add(element);
			number_of_elements++;
		}
		sorting();
	}
	private void sorting() {
		Collections.sort(elementList);
	}
	public IElement getElement(int index) {
		if (index < 0 || index >= size())
			return null;
		return elementList.get(index);
	}
	private int binarySearch(String name) {
		int lo = 0;
		int hi = number_of_elements - 1;
		int mid = 0;
		while (lo <= hi) {
			mid = (lo + hi) / 2;
			int comp_res = compare(getElement(mid), name);
			if (comp_res == 0)
				return mid;
			else if (comp_res > 0)
				hi = mid - 1;
			else
				lo = ++mid;
		}
		return -mid - 1;
	}
	public IElement getElementByID(String id) {
		return elementMap.get(id);
	}
	public IElement getElementByName(String name) {
		int index = binarySearch(name);
		if (index < number_of_elements)
			return elementList.get(index);
		return null;
	}
	public int findIndexByID(String id) {
		IElement element = getElementByID(id);
		if (element != null) {
			return findIndexByName(element.getName());
		}
		return -1;
	}
	public int findIndexByName(String name) {
		int index = binarySearch(name);
		if (index < number_of_elements)
			return index;
		return -1;
	}
	public IElement[] getElements() {
		return elementList.toArray(new IElement[0]);
	}
	public void removeElements(IElement[] elements) {
		for (IElement element : elements) {
			removeElement(element.getID());
		}
	}
	public void removeElement(String id) {
		IElement elmenet = elementMap.remove(id);
		elementList.remove(elmenet);
		number_of_elements--;

		if (!isRootSet()) {
			IElementSet[] sets = ((IElementHandler)getParent()).getSetsWithElement(id);
			for (IElementSet set : sets) {
				set.removeMatchSet(getID());
				removeMatchSet(set.getID());
			}
		}
	}
	private int compare(IElement e1, String val) {
		return compareTo(e1.getName(), val);
	}
	public int compareTo(String s1, String s2) {
		return s1.compareTo(s2);
	}
	public int size() {
		return number_of_elements;
	}
	public void clean() {
		elementMap.clear();
		elementList.clear();
		matchSetList.clear();
		number_of_elements = 0;
	}
	public boolean contains(IElement element) {
		return contains(element.getID());
	}
	public boolean contains(String id) {
		return elementMap.containsKey(id);
	}
	
	public void addMatchSet(String setID) {
		if (!containsMatchSet(setID)) {
			matchSetList.add(setID);
		}
	}
	public void removeMatchSet(String setID) {
		matchSetList.remove(setID);
	}
	public boolean containsMatchSet(String setID) {
		return matchSetList.contains(setID);
	}
	public String[] getMatchSetIDs() {
		return matchSetList.toArray(new String[0]);
	}
}
