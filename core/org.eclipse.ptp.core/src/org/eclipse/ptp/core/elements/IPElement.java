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

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;

public interface IPElement extends IAdaptable {
	
	/**
	 * Sets the attribute value.
	 * 
	 * @param attrValue
	 */
	public void addAttribute(IAttribute<?,?,?> attrValue);

	/**
	 * Sets the attribute values.
	 * 
	 * @param attrValues
	 */
	public void addAttributes(IAttribute<?,?,?>[] attrValues);

	/**
	 * Returns the unique ID for this Element
	 * 
	 * @return The ID for this Element as a String
	 */
	public String getID();

	/**
	 * Returns a name for this Element. An Element name does not need to
	 * be unique. It is primarily used for UI display purposes.
	 * 
	 * @return This Element's name
	 */
	public String getName();
	
	/**
	 * Returns the attribute corresponding to the attribute definition.
	 * 
	 * @param attrDef
	 * @return The attribute for this attribute definition
	 */
	public <T, A extends IAttribute<T,A,D>, D extends IAttributeDefinition<T,A,D>>
	A getAttribute(D attrDef);
	
	/**
	 * Returns the attribute corresponding to the attribute definition ID.
	 * 
	 * @param attrDefId
	 * @return The attribute for this attribute definition
	 */
	public IAttribute<?,?,?> getAttribute(String attrDefId);

	/**
	 * Returns an array containing all attributes.
	 * 
	 * @return An array of attributes for this element
	 */
	public IAttribute<?,?,?>[] getAttributes();

	/**
	 * Get all the attribute definitions that this element knows about.
	 * 
	 * @return array of IAttributeDefinition keys
	 */
	public IAttributeDefinition<?,?,?>[] getAttributeKeys();

	/**
	 * Returns a map containing attribute name, attribute pairs
	 * 
	 * @return map
	 */
	public Map<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>> getAttributeMap();

	/**
	 * Removes the attribute value.
	 * 
	 * @param attrValue
	 */
	public void removeAttribute(IAttribute<?,?,?> attrValue);


	/**
	 * Returns the number of children of this Element.
	 * 
	 * @return The number of children of this Element
	 */
	public int size();
}
