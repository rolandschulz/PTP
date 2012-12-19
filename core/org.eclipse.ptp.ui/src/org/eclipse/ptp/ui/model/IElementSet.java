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

import java.util.BitSet;

/**
 * @author clement chu
 * 
 */
public interface IElementSet {

	/**
	 * @since 7.0
	 */
	public String getID();

	/**
	 * @since 7.0
	 */
	public String getName();

	/**
	 * Adds given elements to this set
	 * 
	 * @param elements
	 * @since 7.0
	 */
	public void addElements(BitSet elements);

	/**
	 * Store a list which set also contains the same element
	 * Adds to match set of given set id
	 * 
	 * @param setID
	 *            set id
	 */
	public void addMatchSet(String setID);

	/**
	 * Returns whether this set contains given element
	 * 
	 * @param element
	 * @return whether this set contains given element
	 * @since 7.0
	 */
	public boolean contains(int element);

	/**
	 * Returns the set of elements contained in this set
	 * 
	 * @since 7.0
	 */
	public BitSet contains(BitSet elements);

	/**
	 * Returns whether given set id contains in match list
	 * 
	 * @param setID
	 * @return true if given set id contains in match list
	 */
	public boolean containsMatchSet(String setID);

	/**
	 * Returns an array of match set id
	 * 
	 * @return an array of match set id
	 */
	public String[] getMatchSetIDs();

	/**
	 * Returns whether this set is root or not
	 * 
	 * @return true if this set is root
	 */
	public boolean isRootSet();

	/**
	 * Test if element is selected
	 * 
	 * @return true if element is selected
	 * @since 7.0
	 */
	public boolean isSelected(int index);

	/**
	 * @since 7.0
	 */
	public BitSet getSelected();

	/**
	 * Removes Element from this set
	 * 
	 * @param index
	 *            remove the element at index from the set
	 * @since 7.0
	 */
	public void removeElement(int index);

	/**
	 * Removes given elements from this set
	 * 
	 * @param elements
	 * @since 7.0
	 */
	public void removeElements(BitSet elements);

	/**
	 * Removes match sets of given set id
	 * 
	 * @param setID
	 *            set id
	 */
	public void removeMatchSet(String setID);

	/**
	 * Set element state to selected
	 * 
	 * @param selected
	 * @since 7.0
	 */
	public void setSelected(int index, boolean selected);

	/**
	 * Returns total elements of this set
	 * 
	 * @return total elements of this set
	 */
	public int size();
}
