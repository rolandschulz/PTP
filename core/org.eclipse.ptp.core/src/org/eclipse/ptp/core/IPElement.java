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
package org.eclipse.ptp.core;

import java.util.Collection;
import org.eclipse.search.ui.ISearchPageScoreComputer;

/**
 * This is the generic parallel element class which all the specific classes
 * extend, like Machine, Node, Job, etc. This base class maintains a name for
 * each entity that extends it and handles parent/child operations. A key is
 * also maintained for each parallel element which is used in storing these
 * elements in hash tables and such.
 * 
 * @author Nathan DeBardeleben
 */
public interface IPElement extends ISearchPageScoreComputer {
	// public String NAME_TAG = "";

	/**
	 * Element tag for an error / undefined type
	 */
	public static final int P_TYPE_ERROR = -1;
	
	/**
	 * Element tag for Universe Elements
	 */
	public static final int P_UNIVERSE = 10;

	/**
	 * Element tag for Machine Elements
	 */
	public static final int P_MACHINE = 11;

	/**
	 * Element tag for Node Elements
	 */
	public static final int P_NODE = 12;

	/**
	 * Element tag for Job Elements
	 */
	public static final int P_JOB = 13;

	/**
	 * Element tag for Process Elements
	 */
	public static final int P_PROCESS = 14;

	/**
	 * Returns a name for this Element so it can be distinguished from other
	 * Elements as well as printed out easily.
	 * 
	 * @return This Element's name
	 */
	public String getElementName();

	/**
	 * Returns the element type (such as P_UNIVERSE, P_MACHINE, etc.) for this
	 * Element. This type is usually set through the constructor of the
	 * implementing class of this interface.
	 * 
	 * @return The element type of this Element which can be compared with the
	 *         public statics of this interface.
	 * @see P_UNIVERSE
	 * @see P_MACHINE
	 * @see P_NODE
	 * @see P_JOB
	 * @see P_PROCESS
	 */
	public int getElementType();

	/**
	 * Returns an Element array of the children of this Element. If this Element
	 * does not yet have any children, then null is returned.
	 * 
	 * @return An Element array of the children of this Element, null if there
	 *         are none
	 */
	public IPElement[] getChildren();

	/**
	 * If this Element has a parent then this method returns it, else it returns
	 * null.
	 * 
	 * @return The parent Element of this Element, null if there is none
	 */
	public IPElement getParent();

	/*
	 * public IPUniverse getPUniverse(); public IPMachine getPMachine(); public
	 * IPJob getPRoot();
	 */

	/**
	 * Adds an Element as a child of this Element creating a parent-child
	 * relationship between the two.
	 * 
	 * @param member
	 *            The Element to add as a child to this Element
	 */
	public void addChild(IPElement member);

	/**
	 * Locate a child Element of this Element and remove it as a child, breaking
	 * the parent-child relationship between the two. If the proposed child
	 * member is not found as a child of this Element then no action is taken.
	 * 
	 * @param member
	 *            The Element to remove as a child of this Element
	 */
	public void removeChild(IPElement member);

	/**
	 * Remove all children Elements from this Element.
	 */
	public void removeChildren();

	/**
	 * Finds an child Element of this Element by searching by the name of the
	 * Element. If found, the Element object is returned, else null is returned.
	 * 
	 * @param elementName
	 *            The name of the child Element to find
	 * @return The Element object if found, else null
	 */
	public IPElement findChild(String elementName);

	/**
	 * Returns the number of children of this Element.
	 * 
	 * @return The number of children of this Element
	 */
	public int size();

	/**
	 * This method really doesn't make sense anymore, it needs to be fixed. <br>
	 * TODO: <i>SLATED FOR REMOVAL</i>
	 * 
	 * @return I have no idea, T/F obviously though :)
	 */
	public boolean isAllStop();

	/**
	 * Returns a sorted Element array of this Element's children. Left open to
	 * the implementer of this interface as to how the sorting is done and what
	 * the sorting is performed on.
	 * 
	 * @return Sorted Element array of the children of this Element, null if
	 *         there are none
	 */
	public IPElement[] getSortedChildren();

	/**
	 * Returns an int version of the ID for this Element
	 * 
	 * @return The ID for this Element
	 */
	public int getID();
	
	/**
	 * Returns a String version of the ID for this Element
	 * 
	 * @return The ID for this Element as a String
	 */
	public String getIDString();

	/**
	 * Returns the children of this Element as a Collection or null if there are
	 * no children.
	 * 
	 * @return The children of this Element or null if there are none
	 */
	public Collection getCollection();

	/**
	 * Returns true if this Element has children Elements, else returns false.
	 * 
	 * @return True if this Element has children Elements, else false
	 */
	public boolean hasChildren();
}
