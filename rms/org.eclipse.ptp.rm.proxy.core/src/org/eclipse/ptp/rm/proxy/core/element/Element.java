/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation

 *******************************************************************************/

package org.eclipse.ptp.rm.proxy.core.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.ptp.rm.proxy.core.attributes.AttributeDefinition;

// TODO: Auto-generated Javadoc
/**
 * The Class Element.
 */
public class Element implements IElement {

	/** The element id. */
	private int elementID;
	// private String parentElementID;
	/** The attributes. */
	private Map<String, String> attributes = new HashMap<String, String>();

	/** The attr def. */
	private final AttributeDefinition attrDef;

	public Element(AttributeDefinition attrDef) {
		this.attrDef = attrDef;
	}

	/*
	 * Compares all attributes Preferable IElement does not contain time values
	 * which change at each update e.g. it should store the time when the job
	 * finishes instead of the remaining time but if time values are stored they
	 * have to be compared in equals - otherwise wrong values are displayed
	 */
	/**
	 * /* (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals()
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Element)) {
			return false;
		}
		return attributes.equals(((Element) other).attributes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IElement#getAttribute(java.lang.String
	 * )
	 */
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.pbs.jproxy.core.IElement#getElementID()
	 */
	public int getElementID() {
		return this.elementID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.pbs.jproxy.core.IElement#toStringArray()
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.pbs.jproxy.core.IElement#getKey()
	 */
	public String getKey() {
		return attributes.get(attrDef.getKey());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.pbs.jproxy.core.IElement#getParentKey()
	 */
	public String getParentKey() {
		return attributes.get(attrDef.getParentKey());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return attributes.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.pbs.jproxy.core.IElement#isComplete()
	 */
	public boolean isComplete() {
		Set<String> reqAttrs = attrDef.getRequiredAttributes();
		for (String key : reqAttrs) {
			if (!attributes.containsKey(key)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IElement#setAttribute(java.lang.String
	 * , java.lang.String)
	 */
	public void setAttribute(String key, String value)
			throws UnknownValueExecption {
		if (!attrDef.hasAttribute(key)) {
			throw new UnknownValueExecption();
		}
		attributes.put(key, value);
	}

	public void setAttributes(IElement element) {
		attributes = new HashMap<String, String>(element.getAttributes());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.core.IElement#setElementID(java.lang.String
	 * )
	 */
	public void setElementID(int elementID) {
		this.elementID = elementID;
	}

	@Override
	public String toString() {
		return toStringArray().toString();
	}

	public Collection<String> toStringArray() {
		Collection<String> ret = new ArrayList<String>();
		ret.add(Integer.toString(getElementID())); // ElementID
		ret.add(Integer.toString(attributes.size())); // Size
		for (Entry<String, String> e : attributes.entrySet()) { // Attributes
			ret.add(attrDef.getProtocolKey(e.getKey()) + "=" + //$NON-NLS-1$
					attrDef.getMappedValue(e.getKey(), e.getValue()));
		}
		return ret;
	}

}
