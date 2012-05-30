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
package org.eclipse.ptp.core.elements.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.utils.core.RangeSet;

/**
 * An ElementAttributeManager maintains a mapping between ranges of element ID's and a set of attributes managed by an
 * AttributeManager.
 * 
 * @author greg
 * 
 */
@Deprecated
public class ElementAttributeManager {
	private final Map<RangeSet, AttributeManager> map;

	public ElementAttributeManager() {
		this.map = new HashMap<RangeSet, AttributeManager>();
	}

	public AttributeManager getAttributeManager(Integer id) {
		return map.get(id);
	}

	public RangeSet[] getElementIds() {
		return map.keySet().toArray(new RangeSet[map.size()]);
	}

	public Set<Map.Entry<RangeSet, AttributeManager>> getEntrySet() {
		return map.entrySet();
	}

	public void setAttributeManager(RangeSet ids, AttributeManager attr) {
		map.put(ids, attr);
	}
}
