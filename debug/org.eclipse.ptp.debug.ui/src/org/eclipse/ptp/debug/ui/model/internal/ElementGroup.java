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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
/**
 * @author clement chu
 *
 */
public class ElementGroup extends Element implements IElementGroup {
	private Map elements = new HashMap();
	private IElement[] sortedElements = new IElement[0];
	private static int group_counter = 1;
	
	public ElementGroup(boolean selected) {
		this(group_counter++, selected);
	}
	public ElementGroup(int num) {
		this(String.valueOf(num), false);
	}
	public ElementGroup(int num, boolean selected) {
		this(String.valueOf(num), selected);
	}
	public ElementGroup(String id) {
		this(id, false);
	}
	public ElementGroup(String id, boolean selected) {
		super(id, selected);
	}
	public IElement[] getElements() {
		return (IElement[])elements.values().toArray(new IElement[elements.size()]);
	}	
	public IElement[] getSortedElements() {
		if (sortedElements.length != size())
			refresh();
		
		return sortedElements;		
	}
	public IElement[] getSelectedElements() {
		List selectedElements = new ArrayList();
		for (Iterator i=elements.values().iterator(); i.hasNext();) {
			IElement element = (IElement)i.next();
			if (element.isSelected())
				selectedElements.add(element);
		}
		return (IElement[])selectedElements.toArray(new IElement[selectedElements.size()]);
	}
	public void addElement(IElement element) {
		elements.put(element.getID(), element);
	}
	public void removeElement(IElement element) {
		elements.remove(element.getID());
	}
	public IElement getElement(String id) {
		return (IElement)elements.get(id);
	}
	public IElement getElement(int index) {
		return getSortedElements()[index];
	}
	public String getElementID(int index) {
		return getElement(index).getID();
	}
	public void removeAllSelected() {
		for (Iterator i=elements.values().iterator(); i.hasNext();) {
			IElement element = (IElement)i.next();
			if (element.isSelected())
				element.setSelected(false);
		}
	}
	public void select(int index) {
		IElement element = getElement(index);
		element.setSelected(!element.isSelected());
	}
	public void setAllSelect(boolean select) {
		for (Iterator i=elements.values().iterator(); i.hasNext();) {
			((IElement)i.next()).setSelected(select);
		}
	}
	
	public void clearAll() {
		elements.clear();
		sortedElements = new IElement[0];
	}
	public int size() {
		return elements.size();
	}
	public void refresh() {
		IElement[] sortingElements = getElements();
		GroupManager.sort(sortingElements);
		sortedElements = sortingElements;
	}
}
