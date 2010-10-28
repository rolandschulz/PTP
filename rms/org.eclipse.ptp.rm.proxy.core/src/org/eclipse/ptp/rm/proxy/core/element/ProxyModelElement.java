/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation
 *    Benjamin Lindner (ben@benlabs.net) - Attribute Definitions and Mapping (bug 316671)
 
 *******************************************************************************/

package org.eclipse.ptp.rm.proxy.core.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;

// TODO: Auto-generated Javadoc
/**
 * The Class Element.
 * @since 2.0
 */
public class ProxyModelElement implements IElement {

	/** The element id. */
	private int elementID;

	private String keyID = null; 
	private String parentkeyID = null;
	
	// private String parentElementID;
	/** The attributes. */
	private Map<String, String> attributes = new HashMap<String, String>();

	/** The attr def. */
	private final List<String> requiredAttributeKeys;
	private final List<IAttributeDefinition<?,?,?>> AttributeDefinitions;

	
	public ProxyModelElement(List<String> requiredAttributeKeys,List<IAttributeDefinition<?,?,?>> AttributeDefinitions,String keyID,String parentkeyID) {
		this.requiredAttributeKeys = requiredAttributeKeys;
		this.AttributeDefinitions = AttributeDefinitions;		
		this.keyID = keyID;
		this.parentkeyID = parentkeyID;
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
		if (other == null || !(other instanceof ProxyModelElement)) {
			return false;
		}
		return attributes.equals(((ProxyModelElement) other).attributes);
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
		if (keyID==null) return null;
		return attributes.get(keyID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.pbs.jproxy.core.IElement#getParentKey()
	 */
	public String getParentKey() {
		if (parentkeyID==null) return null;
		return attributes.get(parentkeyID);
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
		for (String k : requiredAttributeKeys) {
			if (!attributes.containsKey(k)) {
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
	public void setAttribute(String key, String value,Boolean NotValidError)
		throws UnknownValueExecption {
		
		Boolean attrfound = false;
		
		// test whether the attribute with the KEY key is defined
		if (NotValidError) {
			for (IAttributeDefinition<?,?,?> attr : AttributeDefinitions) {
				if (key.equals(attr)) {
					attrfound = true;
				}
			}
			if (!attrfound) throw new UnknownValueExecption(key+" : "+value);
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
			ret.add(e.getKey() + "=" + //$NON-NLS-1$
					e.getValue());
		}
		return ret;
	}

	public Collection<String> toStringArrayMap(List<List<Object>> KeyMap,List<List<Object>> ValueMap,boolean preserve) {
		List<String> ret = new ArrayList<String>();

		List<String> originalentries = new ArrayList<String>();
		List<String> modifiedentries = new ArrayList<String>();

		for (Entry<String, String> e : attributes.entrySet()) { // Attributes
			String newkey = e.getKey();
			String newvalue = e.getValue();
			String k = newkey;
			String v = newvalue;
			boolean modified = false;
			
			for (List<Object> ke : KeyMap) {
				Pattern kp = (Pattern) ke.get(0);
				if (kp.matcher(k).matches()) {
					newkey = (String) ke.get(1);					
					modified = true;
					break;
				}
			}

			for (List<Object> ve : ValueMap) {
				Pattern kp = (Pattern) ve.get(0);
				Pattern vp = (Pattern) ve.get(1);

				if (kp.matcher(k).matches() && vp.matcher(v).matches()) {
					newvalue = (String) ve.get(2);					
					modified = true;
					break;
				}
			}
	
			if (modified) {
				modifiedentries.add(newkey + "=" + newvalue);
				if (preserve) originalentries.add(k + "=" + attributes.get(k));
			} else {
				originalentries.add(k + "=" + attributes.get(k));				
			}

		}

		ret.add(Integer.toString(getElementID())); // ElementID
		ret.add(Integer.toString(modifiedentries.size()+originalentries.size())); // Size

		ret.addAll(modifiedentries); 
		ret.addAll(originalentries); 
		

		return ret;
	}

}
