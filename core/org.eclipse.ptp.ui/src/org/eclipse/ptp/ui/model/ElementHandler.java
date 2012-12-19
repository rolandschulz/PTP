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
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author clement chu
 * 
 */
public class ElementHandler implements IElementHandler {
	private final BitSet fRegisteredElements = new BitSet();
	private final Map<String, IElementSet> fSets = new HashMap<String, IElementSet>();

	public ElementHandler() {
		fSets.put(SET_ROOT_ID, new ElementSet(SET_ROOT_ID, SET_ROOT_ID));
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#createSet(java.lang.String, java.lang.String, java.util.BitSet)
	 */
	@Override
	public IElementSet createSet(String id, String name, BitSet elements) {
		IElementSet set = fSets.get(id);
		if (set == null) {
			set = new ElementSet(id, name);
			set.addElements(elements);
			fSets.put(id, set);
		}
		return set;
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getRegistered()
	 */
	@Override
	public BitSet getRegistered() {
		return (BitSet) fRegisteredElements.clone();
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSet(java.lang.String)
	 */
	@Override
	public IElementSet getSet(String id) {
		return fSets.get(id);
	}

	/**
	 * @since 7.0
	 */
	@Override
	public IElementSet[] getSets() {
		return fSets.values().toArray(new IElementSet[0]);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#getSetsContaining(int)
	 */
	@Override
	public IElementSet[] getSetsContaining(int element) {
		List<IElementSet> sets = new ArrayList<IElementSet>();
		for (IElementSet set : fSets.values()) {
			if (set.contains(element)) {
				sets.add(set);
			}
		}
		return sets.toArray(new IElementSet[0]);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#isRegistered(int)
	 */
	@Override
	public boolean isRegistered(int index) {
		return fRegisteredElements.get(index);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#register(java.util.BitSet)
	 */
	@Override
	public void register(BitSet elements) {
		fRegisteredElements.or(elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#removeAllRegistered()
	 */
	@Override
	public void removeAllRegistered() {
		fRegisteredElements.clear();
	}

	/**
	 * @since 7.0
	 */
	@Override
	public IElementSet removeSet(String id) {
		return fSets.remove(id);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#size()
	 */
	@Override
	public int size() {
		return fSets.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#totalRegistered()
	 */
	@Override
	public int totalRegistered() {
		return fRegisteredElements.size();
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.model.IElementHandler#unRegister(java.util.BitSet)
	 */
	@Override
	public void unRegister(BitSet elements) {
		fRegisteredElements.andNot(elements);
	}
}
