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
public class ElementSet extends Container implements IElementSet {	
	//only store the set id;
	private final String MATCHSET_KEY = "matchset";
	
	/** Constructor
	 * @param parent Parent element
	 * @param id element ID
	 * @param name name of element
	 */
	public ElementSet(IElement parent, String id, String name) {
		super(parent, id, name, IContainer.ELEMENT_TYPE);
		setData(MATCHSET_KEY, new ArrayList());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#getElementHandler()
	 */
	public IElementHandler getElementHandler() {
		return (IElementHandler)parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#isRootSet()
	 */
	public boolean isRootSet() {
		return (id.equals(IElementHandler.SET_ROOT_ID));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IContainer#get()
	 */
	public IElement[] get() {
		return getElements();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#getSortedElements()
	 */
	public IElement[] getSortedElements() {
		return getSorted();		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#getElements()
	 */
	public IElement[] getElements() {
		return (IElement[])elementMap.values().toArray(new IElement[elementMap.size()]);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#getElement(java.lang.String)
	 */
	public IElement getElement(String id) {
		return get(id);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#getElement(int)
	 */
	public IElement getElement(int index) {
		return get(index);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#addMatchSet(java.lang.String)
	 */
	public void addMatchSet(String setId) {
		List setList = getMatchSetList();
		if (!setList.contains(setId))
			setList.add(setId);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#removeMatchSet(java.lang.String)
	 */
	public void removeMatchSet(String setId) {
		List setList = getMatchSetList();
		if (setList.contains(setId))
			setList.remove(setId);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#getMatchSets()
	 */
	public String[] getMatchSets() {
		List setList = getMatchSetList();
		return (String[])setList.toArray(new String[setList.size()]);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#containOtherSets()
	 */
	public boolean containOtherSets() {
		List setList = getMatchSetList();
		return (setList.size()>0);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElementSet#isContainSets(java.lang.String)
	 */
	public boolean isContainSets(String set_id) {
		List setList = getMatchSetList();
		return setList.contains(set_id);
	}
	
	/** Get mactch set list
	 * @return list of match set
	 */
	private List getMatchSetList() {
		return (List)getData(MATCHSET_KEY);
	}
}
