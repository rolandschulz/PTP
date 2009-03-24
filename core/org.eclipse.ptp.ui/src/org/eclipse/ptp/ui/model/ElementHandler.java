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
import java.util.List;

/**
 * @author clement chu
 *
 */
public class ElementHandler extends ElementSet implements IElementHandler {
	private List<IElement> registerList = new ArrayList<IElement>();
	
	public ElementHandler() {
		super(null, SET_ROOT_ID, SET_ROOT_ID);
		//create root 
		addElements(new IElement[] { new ElementSet(this, SET_ROOT_ID, SET_ROOT_ID) });
	}
	public IElementSet getSetRoot() {
		return (IElementSet)getElementByID(SET_ROOT_ID);
	}
	public IElementSet[] getSetsWithElement(String id) {
		List<IElementSet> aList = new ArrayList<IElementSet>();
		for (IElement element : getElements()) {
			if (((IElementSet)element).getElementByID(id) != null) {
				aList.add((IElementSet)element);
			}
		}
		return (IElementSet[])aList.toArray(new IElementSet[0]);
	}
	public boolean containsRegister(IElement element) {
		return registerList.contains(element);
	}
	public void addToRegister(IElement[] elements) {
		for (IElement element : elements) {
			if (element != null && !containsRegister(element)) {
				element.setRegistered(true);
				registerList.add(element);
			}
		}
	}
	public void removeFromRegister(IElement[] elements) {
		for (IElement element : elements) {
			if (element != null && registerList.remove(element))
				element.setRegistered(false);
		}
	}
	public IElement[] getRegistered() {
		return registerList.toArray(new IElement[0]);
	}
	public void removeAllRegistered() {
		registerList.clear();
	}
	public int totalRegistered() {
		return registerList.size();
	}
}
