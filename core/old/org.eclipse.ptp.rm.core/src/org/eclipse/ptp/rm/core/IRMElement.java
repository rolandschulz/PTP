/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rm.core;

import org.eclipse.ptp.rm.core.attributes.IAttrDesc;
import org.eclipse.ptp.rm.core.attributes.IAttribute;

/**
 * Provide the status information, i.e. attributes, for the ARM's associated
 * machine Set and provide specific attributes for a given attribute description
 * 
 * @author rsqrd
 */
public interface IRMElement {
	/**
	 * @return the unique id (unique for the objects of the subclass of
	 *         IRMElement)
	 */
	int getID();

	/**
	 * @return the name of this element
	 */
	String getName();

	/**
	 * @return the list of attributes for this element (unordered)
	 */
	IAttribute[] getAttributes();

	/**
	 * @param description
	 * @return the attribute associated with the attribute description
	 */
	IAttribute getAttribute(IAttrDesc description);

	/**
	 * set the attribute associated with the attribute description
	 * 
	 * @param description
	 * @param attribute
	 */
	void setAttribute(IAttrDesc description, IAttribute attribute);
}
