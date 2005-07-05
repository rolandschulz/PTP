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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * @author clement chu
 *
 */
public class ElementGroup extends Element {
	private static final String GROUP_PREFIX = "Group";
	private Map elements = new HashMap(0);
	
	public ElementGroup(int id, boolean selected) {
		super(id, selected);
	}
	public ElementGroup(int id) {
		this(id, false);
	}
	public String getText() {
		return GROUP_PREFIX + " " + (id + 1);
	}
	public Element[] getElements() {
		return (Element[])elements.values().toArray(new Element[elements.size()]);
	}	
	public Element[] getSortedElements() {
		Element[] sortElements = getElements();
		GroupManager.sort(sortElements);
		return sortElements;		
	}
	public Element[] getSelectedElements() {
		List selectedElements = new ArrayList();
		for (Iterator i=elements.values().iterator(); i.hasNext();) {
			Element element = (Element)i.next();
			if (element.isSelected())
				selectedElements.add(element);
		}
		return (Element[])selectedElements.toArray(new Element[selectedElements.size()]);
	}
	public void addElement(Element element) {
		elements.put(new Integer(element.getID()), element);
	}
	public void removeElement(Element element) {
		elements.remove(new Integer(element.getID()));
	}
	public Element getElementByID(int id) {
		return (Element)elements.get(new Integer(id));
	}
	public Element getElement(int index) {
		return getSortedElements()[index];
	}
	public int getElementID(int index) {
		return getElement(index).getID();
	}
	public void removeAllSelected() {
		for (Iterator i=elements.values().iterator(); i.hasNext();) {
			Element element = (Element)i.next();
			if (element.isSelected())
				element.setSelected(false);
		}
	}
	public void select(int index) {
		Element element = getElement(index);
		element.setSelected(!element.isSelected());
	}
	
	public void clearAll() {
		elements.clear();
	}
	public int size() {
		return elements.size();
	}
}
