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
package org.eclipse.ptp.core.elements;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;

public interface IPElement extends IAdaptable {

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
	 * Returns a name for this Element so it can be distinguished from other
	 * Elements as well as printed out easily.
	 * 
	 * @return This Element's name
	 */
	public String getElementName();
	
	/**
	 * Returns the attribute corresponding to the attribute ID.
	 * 
	 * @param attrId
	 * @return The attribute for this ID
	 */
	public Object getAttribute(String attrId);
	
	/**
	 * Returns the attribute corresponding to the attribute definition.
	 * 
	 * @param attrDef
	 * @return The attribute for this attribute definition
	 */
	public IAttribute getAttribute(IAttributeDefinition attrDef);
	
	/**
	 * Returns a list of the attribute names
	 * 
	 * @return attribute names
	 */
	public String[] getAttributeKeys();

	/**
	 * Sets the attribute value corresponding to the attribute ID.
	 * 
	 * @param attrId
	 * @param attrValue
	 */
	public void setAttribute(String attrId, IAttribute attrValue);

	/**
	 * Sets the string attribute value corresponding to the attribute ID.
	 * 
	 * @param attrId
	 * @param attrValue
	 */
	public void setAttribute(String attrId, String attrValue);
	
	/**
	 * Sets the Object attribute value corresponding to the attribute ID.
	 * 
	 * @param attrId
	 * @param attrValue
	 */
	public void setAttribute(String attrId, Object attrValue);

}
