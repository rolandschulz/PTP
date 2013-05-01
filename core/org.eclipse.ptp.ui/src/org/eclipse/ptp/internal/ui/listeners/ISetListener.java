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
package org.eclipse.ptp.internal.ui.listeners;

import java.util.BitSet;

import org.eclipse.ptp.internal.ui.model.IElementSet;

/**
 * @author clement chu
 * 
 */
public interface ISetListener {
	/**
	 * Add element to set event
	 * 
	 * @param set
	 *            Set
	 * @param elements
	 *            elements to add to set
	 * @since 7.0
	 */
	public void addElementsEvent(IElementSet set, BitSet elements);

	/**
	 * Change set event
	 * 
	 * @param currentSet
	 *            current Set
	 * @param preSet
	 *            previous Set
	 */
	public void changeSetEvent(IElementSet currentSet, IElementSet preSet);

	/**
	 * Create set event
	 * 
	 * @param set
	 *            new Set
	 * @param elements
	 *            add elements into a given Set
	 * @since 7.0
	 */
	public void createSetEvent(IElementSet set, BitSet elements);

	/**
	 * Delete set event
	 * 
	 * @param set
	 *            Set
	 */
	public void deleteSetEvent(IElementSet set);

	/**
	 * Remove elements event
	 * 
	 * @param set
	 *            Set
	 * @param elements
	 *            elements to remove from set
	 * @since 7.0
	 */
	public void removeElementsEvent(IElementSet set, BitSet elements);
}
