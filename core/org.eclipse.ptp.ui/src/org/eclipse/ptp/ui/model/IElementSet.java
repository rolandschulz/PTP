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

/**
 * @author clement chu
 *
 */
public interface IElementSet extends IElement {
	/**
	 * Removes Element from this set
	 * @param element id 
	 */
	public void removeElement(String id);
	
	/**
	 * Adds given elements to this set
	 * @param elements
	 */
	public void addElements(IElement[] elements);

	/**
	 * Removes given elements from this set
	 * @param elements
	 */
	public void removeElements(IElement[] elements);

	/**
	 * Returns an array of elements of this set
	 * @return an array of elements
	 */
	public IElement[] getElements();

	/**
	 * Returns whether this set is root or not
	 * @return true if this set is root
	 */
	public boolean isRootSet();

	/**
	 * Returns element by given element id
	 * @param id element id
	 * @return element 
	 */
	public IElement getElementByID(String id);
	
	/**
	 * Returns element by given element name
	 * @param name element name
	 * @return element
	 */
	public IElement getElementByName(String name);

	/**
	 * Returns element by given index
	 * @param index 
	 * @return element
	 */
	public IElement getElement(int index);
	
	/**
	 * Returns total elements of this set
	 * @return total elements of this set
	 */
	public int size();
	
	/**
	 * Remove all the elements in this set
	 */
	public void clean();
	
	/**
	 * Returns whether this set contains given element
	 * @param element
	 * @return whether this set contains given element
	 */
	public boolean contains(IElement element);
	
	/**
	 * Returns whether this set contains given element id
	 * @param id
	 * @return whether this set contains given element id
	 */
	public boolean contains(String id);
	
	/**
	 * Store a list which set also contains the same element 
	 * Adds to match set of given set id
	 * @param setID set id
	 */
	public void addMatchSet(String setID);
	
	/**
	 * Removes match sets of given set id
	 * @param setID set id
	 */
	public void removeMatchSet(String setID);
	
	/**
	 * Returns whether given set id contains in match list
	 * @param setID
	 * @return true if given set id contains in match list
	 */
	public boolean containsMatchSet(String setID);
	
	/**
	 * Returns an array of match set id
	 * @return an array of match set id
	 */
	public String[] getMatchSetIDs();
	
	/**
	 * Returns position of given element id
	 * @param id element id
	 * @return position of given element id
	 */
	public int findIndexByID(String id);

	/**
	 * Returns position of given element name
	 * @param name element name
	 * @return position of given element name
	 */
	public int findIndexByName(String name);
}
