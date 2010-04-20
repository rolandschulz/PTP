/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation

 *******************************************************************************/
package org.eclipse.ptp.rm.proxy.core.attributes;

import java.util.Set;

import org.eclipse.ptp.rm.proxy.core.element.Element;

/* This could be an abstract class of IEelementAttributes.
 * currently not because it is not clear whether IEelementAttributes will be
 * shared with client
 */
/**
 * Defines the Attributes for one element type All data is in the Proxy specific
 * Data classes implementing IElementAttributes
 * 
 */
public class AttributeDefinition {
	// private Set<String> requiredAttributes = new HashSet<String>();
	// //required attributes
	// private String key, parentKey;
	/** The attrs. */
	private IElementAttributes attrs = null;

	/**
	 * Instantiates a new attribute definition.
	 * 
	 * @param attrs
	 *            the attribute definition
	 */
	public AttributeDefinition(IElementAttributes attrs) {
		this.attrs = attrs;
	}

	/**
	 * Creates the element.
	 * 
	 * @return the element
	 */
	public Element createElement() {
		return new Element(this);
	}

	/**
	 * Equals.
	 * 
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	public boolean equals(AttributeDefinition o) {
		return o.getRequiredAttributes().equals(getRequiredAttributes())
				&& o.getKey().equals(getKey())
				&& o.getParentKey().equals(getParentKey());
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return attrs.getKey();
	}

	/**
	 * Gets the mapped value.
	 * 
	 * @param attr
	 *            the attr
	 * @param value
	 *            the value
	 * @return the mapped value
	 */
	public String getMappedValue(String attr, String value) {
		if (attrs.getValueMap() != null
				&& attrs.getValueMap().containsKey(attr)) {
			return attrs.getValueMap().get(attr).get(value);
		} else {
			return value; // unmapped
		}
	}

	// public Map<String, String> getXmltagAttributeID_Map() {
	// return attrs.getXmltagAttributeID_Map();
	// }

	/**
	 * Gets the parent key.
	 * 
	 * @return the parent key
	 */
	public String getParentKey() {
		return attrs.getParentKey();
	}

	/**
	 * Gets the protocol key.
	 * 
	 * @param attr
	 *            the attr
	 * @return the protocol key
	 */
	public String getProtocolKey(String attr) {
		return attrs.getXmltagAttributeID_Map().get(attr);
	}

	/**
	 * Gets the required attributes.
	 * 
	 * @return the required attributes
	 */
	public Set<String> getRequiredAttributes() {
		// List<String> ret = new Vector<String>();
		// for (Entry<String, Boolean> attr : attributes.entrySet()) {
		// if (attr.getValue()) ret.add(attr.getKey());
		// }
		return attrs.getXmltagAttributeID_Map().keySet();
	}

	/**
	 * Checks for attribute.
	 * 
	 * @param attr
	 *            the attr
	 * @return true, if successful
	 */
	public boolean hasAttribute(String attr) {
		// IDEA: return also optionalAttributes if available
		return getRequiredAttributes().contains(attr);
	}
}
