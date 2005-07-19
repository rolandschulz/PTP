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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
/**
 * @author clement chu
 *
 */
public class ElementGroup extends Parent implements IElementGroup {
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
	public IElement[] getSelectedElements() {
		List selectedElements = new ArrayList();
		for (Iterator i=elementMap.values().iterator(); i.hasNext();) {
			IElement element = (IElement)i.next();
			if (element.isSelected())
				selectedElements.add(element);
		}
		return (IElement[])selectedElements.toArray(new IElement[selectedElements.size()]);
	}
	public void removeAllSelected() {
		for (Iterator i=elementMap.values().iterator(); i.hasNext();) {
			IElement element = (IElement)i.next();
			if (element.isSelected())
				element.setSelected(false);
		}
	}
	public void select(int index, boolean selectIt) {
		IElement element = getElement(index);
		element.setSelected(selectIt);
	}
	public void select(int index) {
		IElement element = getElement(index);
		element.setSelected(!element.isSelected());
	}
	public void setAllSelect(boolean selectIt) {
		for (Iterator i=elementMap.values().iterator(); i.hasNext();) {
			((IElement)i.next()).setSelected(selectIt);
		}
	}
	
	public IElement[] get() {
		return getElements();
	}
	public IElement[] getSortedElements() {
		return getSorted();		
	}
	public IElement[] getElements() {
		return (IElement[])elementMap.values().toArray(new IElement[elementMap.size()]);
	}
	public IElement getElement(String id) {
		return get(id);
	}
	public IElement getElement(int index) {
		return get(index);
	}
}
