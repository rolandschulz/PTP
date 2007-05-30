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
import java.util.List;
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
	private Map<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>> map = 
		new HashMap<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>>();
	
	public AttributeManager() {
	}
	
	public AttributeManager(IAttribute<?,?,?>[] attrs) {
		for (IAttribute<?,?,?> attr : attrs) {
			addAttribute(attr);
		}
	}

	public void addAttribute(IAttribute<?,?,?> attr) {
		map.put(attr.getDefinition(), attr);
	}

	/**
	 * Add new attributes to the attributes being managed. Handle array
	 * attributes specially: the new array is appended to an existing array, 
	 * if any.
	 * 
	 * @param attrs
	 */
	public void addAttributes(IAttribute<?,?,?>[] attrs) {
		for (IAttribute<?,?,?> attr : attrs) {
			IAttributeDefinition<?,?,?> def = attr.getDefinition();
			if (map.containsKey(def) && attr instanceof ArrayAttribute) {
				ArrayAttribute<?> arrAttr = (ArrayAttribute<?>) attr;
				addAttributeToArrayAttribute(arrAttr);
			} else {
				addAttribute(attr);
			}
		}
	}

	/**
	 * @param attr
	 * @param def
	 */
	private <T extends Comparable<? super T>>
	void addAttributeToArrayAttribute(ArrayAttribute<T> attr) {
		ArrayAttributeDefinition<T> def = attr.getDefinition();
		ArrayAttribute<T> exAttr = (ArrayAttribute<T>)map.get(def);
		final List<T> value = attr.getValue();
		exAttr.addAll(value);
	}

	public IAttribute<?,?,?> getAttribute(String id) {
		for (Map.Entry<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>> entry : map.entrySet()) {
			if (entry.getKey().getId().equals(id)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public <T, A extends IAttribute<T,A,D>, D extends IAttributeDefinition<T,A,D>>
	A getAttribute(D def) {
		return (A) this.getAttribute(def.getId());
	}

	public IAttribute<?,?,?>[] getAttributes() {
		return map.values().toArray(new IAttribute[map.size()]);
	}
	
	public Map<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>> getMap() {
		return map;
	}
	
	public Set<IAttributeDefinition<?,?,?>> getKeySet() {
		return map.keySet();
	}
	
	public void removeAttribute(IAttribute<?,?,?> attr) {
		map.remove(attr.getDefinition());
	}
	
	public String[] toStringArray() {
		ArrayList<String> res = new ArrayList<String>();
		
		for (Map.Entry<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>> entry : map.entrySet()) {
			IAttribute<?,?,?> attr = entry.getValue();
			if (attr instanceof ArrayAttribute) {
				List<?> arrObj = ((ArrayAttribute<?>)attr).getValue();
				for (Object obj : arrObj) {
					res.add(entry.getKey().getId() + "=" + obj.toString());					
				}
			} else {
				res.add(entry.getKey().getId() + "=" + attr.getValueAsString());
			}
		}
		
		return res.toArray(new String[res.size()]);
	}
}
