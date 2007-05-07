/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An attribute manager is responsible for managing a set of attribute definition ID's and
 * attributes. It is used where groups of attributes are needed.
 * 
 * @author greg
 *
 */
public class AttributeManager {
	private Map<String, IAttribute>	map = new HashMap<String, IAttribute>();
	
	public AttributeManager() {
	}
	
	public AttributeManager(IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			addAttribute(attr);
		}
	}

	public void addAttribute(IAttribute attr) {
		map.put(attr.getDefinition().getId(), attr);
	}

	/**
	 * Add new attributes to the attributes being managed. Handle array
	 * attributes specially: the new array is appended to an existing array, 
	 * if any.
	 * 
	 * @param attrs
	 */
	public void addAttributes(IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			String id = attr.getDefinition().getId();
			if (map.containsKey(id) && attr instanceof ArrayAttribute) {
				@SuppressWarnings("unchecked")
				ArrayAttribute exAttr = (ArrayAttribute)map.get(id);
				@SuppressWarnings("unchecked")
				final Comparable[] value = ((ArrayAttribute)attr).getValue();
				exAttr.addAll(value);
			} else {
				addAttribute(attr);
			}
		}
	}

	public IAttribute getAttribute(String id) {
		return map.get(id);
	}
	
	public IAttribute getAttribute(IAttributeDefinition def) {
		return getAttribute(def.getId());
	}

	public IAttribute[] getAttributes() {
		return map.values().toArray(new IAttribute[map.size()]);
	}
	
	public Set<Map.Entry<String, IAttribute>> getEntrySet() {
		return map.entrySet();
	}
	
	public Set<String> getKeySet() {
		return map.keySet();
	}
	
	public void removeAttribute(IAttribute attr) {
		map.remove(attr.getDefinition().getId());
	}
	
	public String[] toStringArray() {
		ArrayList<String> res = new ArrayList<String>();
		
		for (Map.Entry<String, IAttribute> entry : map.entrySet()) {
			IAttribute attr = entry.getValue();
			if (attr instanceof ArrayAttribute) {
				@SuppressWarnings("unchecked")
				Object[] arrObj = ((ArrayAttribute)attr).getValue();
				for (Object obj : arrObj) {
					res.add(entry.getKey() + "=" + obj.toString());					
				}
			} else {
				res.add(entry.getKey() + "=" + attr.getValueAsString());
			}
		}
		
		return res.toArray(new String[res.size()]);
	}
}
