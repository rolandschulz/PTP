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
package org.eclipse.ptp.core.elementcontrols;

import org.eclipse.ptp.core.elements.IPElement;
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
public interface IPElementControl extends IPElement, ISearchPageScoreComputer {
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
	 * Element tag for Resource Manager Elements
	 */
	public static final int P_RESOURCE_MANAGER = 15;
	
	/**
	 * Element tag for Queue Elements
	 */
	public static final int P_QUEUE = 16;

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
	 * If this Element has a parent then this method returns it, else it returns
	 * null.
	 * 
	 * @return The parent Element of this Element, null if there is none
	 */
	public IPElementControl getParent();

	/*
	 * public IPUniverse getPUniverse(); public IPMachine getPMachine(); public
	 * IPJob getPRoot();
	 */

	/**
	 * Returns true if this Element has children Elements, else returns false.
	 * 
	 * @return True if this Element has children Elements, else false
	 */
	public boolean hasChildren();
}
