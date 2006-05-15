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
package org.eclipse.ptp.rm.core.attributes;

/**
 * Maintain the relationship between an attribute's value and its description.
 * Specifies a strict-weak ordering of itself and other attributes. Provide a
 * string representation of the attribute.
 * 
 * @author rsqrd
 * 
 */
public interface IAttribute extends Comparable {
	/**
	 * Does this attribute contain attribute as a subset of attributes.
	 * 
	 * @param attribute
	 * @return true if descriptions are the same and components of attribute are subset
	 * of this attribute
	 */
	boolean contains(IAttribute attribute);

	/**
	 * @return the description to which this attribute refers
	 */
	IAttrDesc getDescription();
	
	/**
	 * @return the components of this attribute
	 */
	IAttrComponent[] getComponents();
}
