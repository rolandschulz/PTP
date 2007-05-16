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
public class ElementHandler extends Container implements IElementHandler {
	//only store the register set id;
	private final String REGISTERED_KEY = "registeredset";

	/** Constructor
	 * 
	 */
	public ElementHandler() {
		super(null, SET_ROOT_ID, SET_ROOT_ID, IContainer.SET_TYPE);
		//store registered list
		setData(REGISTERED_KEY, new ArrayList<IElement>());
		
		//create root 
		add(new ElementSet(this, SET_ROOT_ID, SET_ROOT_ID));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSetRoot()
	 */
	public IElementSet getSetRoot() {
		if (size() == 0)
			add(new ElementSet(this, SET_ROOT_ID, SET_ROOT_ID));
		
		return (IElementSet)get(SET_ROOT_ID);
	}
	public void clearAll() {
		for (IElementSet eSet : get().toArray(new IElementSet[0])) {
			eSet.clearAll();
		}
		super.clearAll();
	}
	protected IElement[] getElements() {
		return get().toArray(new IElementSet[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSortedSets()
	 */
	public IElementSet[] getSortedSets() {
		return (IElementSet[])getSorted();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSets()
	 */
	public IElementSet[] getSets() {
		return get().toArray(new IElementSet[0]);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSet(java.lang.String)
	 */
	public IElementSet getSet(String id) {
		return (IElementSet)get(id);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSet(int)
	 */
	public IElementSet getSet(int index) {
		return (IElementSet)get(index);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSetsWithElement(java.lang.String)
	 */
	public IElementSet[] getSetsWithElement(String id) {
		List<IElementSet> aList = new ArrayList<IElementSet>();
		for (IElementSet set : getSortedSets()) {
			if (set.contains(id))
				aList.add(set);
		}
		return (IElementSet[])aList.toArray(new IElementSet[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#containsRegisterElement(org.eclipse.ptp.ui.model.IElement)
	 */
	public boolean containsRegisterElement(IElement element) {
		return getRegisteredSetList().contains(element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#addRegisterElement(org.eclipse.ptp.ui.model.IElement)
	 */
	public void addRegisterElement(IElement element) {
		List<IElement> setList = getRegisteredSetList();
		if (!setList.contains(element))
			setList.add(element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#removeRegisterElement(org.eclipse.ptp.ui.model.IElement)
	 */
	public void removeRegisterElement(IElement element) {
		List<IElement> setList = getRegisteredSetList();
		if (setList.contains(element))
			setList.remove(element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getRegisteredElements()
	 */
	public IElement[] getRegisteredElements() {
		return (IElement[])getRegisteredSetList().toArray(new IElement[0]);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#removeAllRegisterElements()
	 */
	public void removeAllRegisterElements() {
		getRegisteredSetList().clear();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementHandler#totalRegisterElements()
	 */
	public int totalRegisterElements() {
		return getRegisteredSetList().size();
	}
	
	/** Get registered set list
	 * @return list of registered set
	 */
	@SuppressWarnings("unchecked")
	private List<IElement> getRegisteredSetList() {
		return (List<IElement>)getData(REGISTERED_KEY);
	}	
}
