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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.ui.model.IContainer;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
/**
 * @author clement chu
 *
 */
public class ElementSet extends Container implements IElementSet {	
	//only store the set id;
	private final String MATCHSET_KEY = "matchset";
	
	public ElementSet(IElement parent, String id, String name) {
		this(parent, id, name, false);
	}
	public ElementSet(IElement parent, String id, String name, boolean selected) {
		super(parent, id, name, selected, IContainer.ELEMENT_TYPE);
		setData(MATCHSET_KEY, new ArrayList());
	}
	public IElementHandler getElementHandler() {
		return (IElementHandler)parent;
	}
	
	public boolean isRootSet() {
		return (id.equals(IElementHandler.SET_ROOT_ID));
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
	public void addMatchSet(String setId) {
		List setList = getMatchSetList();
		if (!setList.contains(setId))
			setList.add(setId);
	}
	public void removeMatchSet(String setId) {
		List setList = getMatchSetList();
		if (setList.contains(setId))
			setList.remove(setId);
	}
	public String[] getMatchSets() {
		List setList = getMatchSetList();
		return (String[])setList.toArray(new String[setList.size()]);
	}
	public boolean containOtherSets() {
		List setList = getMatchSetList();
		return (setList.size()>0);
	}
	public boolean isContainSets(String set_id) {
		List setList = getMatchSetList();
		return setList.contains(set_id);
	}
	
	private List getMatchSetList() {
		return (List)getData(MATCHSET_KEY);
	}
}
