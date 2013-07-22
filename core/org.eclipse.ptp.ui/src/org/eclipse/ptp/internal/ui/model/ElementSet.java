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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @author clement chu
 * 
 */
public class ElementSet implements IElementSet {
	private final String fId;
	private final String fName;
	private final BitSet fElements = new BitSet();
	private final BitSet fSelectedElements = new BitSet();

	private final List<String> matchSetList = new ArrayList<String>();

	/**
	 * @since 7.0
	 */
	public ElementSet(String id, String name) {
		fId = id;
		fName = name;
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#addElements(java.util.BitSet)
	 */
	@Override
	public void addElements(BitSet elements) {
		fElements.or(elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#addMatchSet(java.lang.String)
	 */
	@Override
	public void addMatchSet(String setID) {
		if (!containsMatchSet(setID)) {
			matchSetList.add(setID);
		}
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#contains(java.util.BitSet)
	 */
	@Override
	public BitSet contains(BitSet elements) {
		BitSet result = (BitSet) fElements.clone();
		result.and(elements);
		return result;
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#contains(int)
	 */
	@Override
	public boolean contains(int element) {
		return fElements.get(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#containsMatchSet(java.lang.String)
	 */
	@Override
	public boolean containsMatchSet(String setID) {
		return matchSetList.contains(setID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#getElement(int)
	 */
	@Override
	public int getElement(int index) {
		int pos = -1;
		while (index-- >= 0) {
			pos = fElements.nextSetBit(pos + 1);
		}
		return pos;
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#getID()
	 */
	@Override
	public String getID() {
		return fId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#getMatchSetIDs()
	 */
	@Override
	public String[] getMatchSetIDs() {
		return matchSetList.toArray(new String[0]);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#getSelected()
	 */
	@Override
	public BitSet getSelected() {
		return (BitSet) fSelectedElements.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementSet#isRootSet()
	 */
	@Override
	public boolean isRootSet() {
		return (fId.equals(IElementHandler.SET_ROOT_ID));
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#isSelected(int)
	 */
	@Override
	public boolean isSelected(int index) {
		return fSelectedElements.get(index);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#removeElement(int)
	 */
	@Override
	public void removeElement(int index) {
		fElements.clear(index);
		fSelectedElements.clear(index);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#removeElements(java.util.BitSet)
	 */
	@Override
	public void removeElements(BitSet elements) {
		fElements.andNot(elements);
		fSelectedElements.andNot(elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#removeMatchSet(java.lang.String)
	 */
	@Override
	public void removeMatchSet(String setID) {
		matchSetList.remove(setID);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#setSelected(int, boolean)
	 */
	@Override
	public void setSelected(int index, boolean selected) {
		if (fElements.get(index)) {
			if (selected) {
				fSelectedElements.set(index);
			} else {
				fSelectedElements.clear(index);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.model.IElementSet#size()
	 */
	@Override
	public int size() {
		return fElements.cardinality();
	}
}
