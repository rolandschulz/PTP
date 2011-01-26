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

/**
 * This is the generic parallel element class which all the specific classes
 * extend, like Machine, Node, Job, etc. This base class maintains a name for
 * each entity that extends it and handles parent/child operations. A key is
 * also maintained for each parallel element which is used in storing these
 * elements in hash tables and such.
 * 
 * @author Nathan DeBardeleben
 */
public interface IPElementControl extends IPElement {

	/**
	 * If this Element has a parent then this method returns it, else it returns
	 * null.
	 * 
	 * @return The parent Element of this Element, null if there is none
	 */
	public IPElementControl getParent();
}
