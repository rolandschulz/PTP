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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An attribute manager is responsible for managing a set of attribute definition ID's and attributes. It is used where groups of
 * attributes are needed.
 * 
 * @author greg
 * 
 */
@Deprecated
public class AttributeManager {
	private final Map<IAttributeDefinition<?, ?, ?>, IAttribute<?, ?, ?>> map = Collections
			.synchronizedMap(new HashMap<IAttributeDefinition<?, ?, ?>, IAttribute<?, ?, ?>>());

	public AttributeManager() {
	}

	/**
	 * @since 4.0
	 */
	public AttributeManager(IAttribute<?, ?, ?> attr) {
		addAttribute(attr);
	}

	public AttributeManager(IAttribute<?, ?, ?>[] attrs) {
		for (IAttribute<?, ?, ?> attr : attrs) {
			addAttribute(attr);
		}
	}

	/**
	 * @param attr
	 */
	public void addAttribute(IAttribute<?, ?, ?> attr) {
		map.put(attr.getDefinition(), attr);
	}

	/**
	 * Add new attributes to the attributes being managed. Handle array attributes specially: the new array is appended to an
	 * existing array, if any.
	 * 
	 * @param attrs
	 */
	public void addAttributes(IAttribute<?, ?, ?>[] attrs) {
		synchronized (map) {
			for (IAttribute<?, ?, ?> attr : attrs) {
				IAttributeDefinition<?, ?, ?> def = attr.getDefinition();
				if (map.containsKey(def) && attr instanceof ArrayAttribute) {
					ArrayAttribute<?> arrAttr = (ArrayAttribute<?>) attr;
					addAttributeToArrayAttribute(arrAttr);
				} else {
					addAttribute(attr);
				}
			}
		}
	}

	/**
	 * @param attr
	 * @param def
	 */
	@SuppressWarnings("unchecked")
	private <T extends Comparable<? super T>> void addAttributeToArrayAttribute(ArrayAttribute<T> attr) {
		synchronized (map) {
			ArrayAttributeDefinition<T> def = attr.getDefinition();
			ArrayAttribute<T> exAttr = (ArrayAttribute<T>) map.get(def);
			final List<T> value = attr.getValue();
			exAttr.addAll(value);
		}
	}

	/**
	 * @param <T>
	 * @param <A>
	 * @param <D>
	 * @param def
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> A getAttribute(D def) {
		return (A) this.getAttribute(def.getId());
	}

	/**
	 * @param id
	 * @return
	 */
	public IAttribute<?, ?, ?> getAttribute(String id) {
		synchronized (map) {
			for (Map.Entry<IAttributeDefinition<?, ?, ?>, IAttribute<?, ?, ?>> entry : map.entrySet()) {
				if (entry.getKey().getId().equals(id)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * @return
	 */
	public IAttribute<?, ?, ?>[] getAttributes() {
		synchronized (map) {
			return map.values().toArray(new IAttribute[map.size()]);
		}
	}

	/**
	 * @return
	 */
	public IAttribute<?, ?, ?>[] getDisplayAttributes() {
		List<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();

		synchronized (map) {
			for (IAttribute<?, ?, ?> attr : map.values()) {
				if (attr.getDefinition().getDisplay()) {
					attrs.add(attr);
				}
			}
		}

		return attrs.toArray(new IAttribute[attrs.size()]);
	}

	/**
	 * @return
	 */
	public IAttributeDefinition<?, ?, ?>[] getKeys() {
		synchronized (map) {
			return map.keySet().toArray(new IAttributeDefinition[0]);
		}
	}

	/**
	 * @param attr
	 */
	public void removeAttribute(IAttribute<?, ?, ?> attr) {
		synchronized (map) {
			map.remove(attr.getDefinition());
		}
	}

	@Override
	public String toString() {
		return map.toString();
	}

	/**
	 * @return
	 */
	public String[] toStringArray() {
		ArrayList<String> res = new ArrayList<String>();

		synchronized (map) {
			for (Map.Entry<IAttributeDefinition<?, ?, ?>, IAttribute<?, ?, ?>> entry : map.entrySet()) {
				IAttribute<?, ?, ?> attr = entry.getValue();
				if (attr instanceof ArrayAttribute) {
					List<?> arrObj = ((ArrayAttribute<?>) attr).getValue();
					for (Object obj : arrObj) {
						res.add(entry.getKey().getId() + "=" + obj.toString()); //$NON-NLS-1$
					}
				} else {
					res.add(entry.getKey().getId() + "=" + attr.getValueAsString()); //$NON-NLS-1$
				}
			}
		}

		return res.toArray(new String[res.size()]);
	}
}
