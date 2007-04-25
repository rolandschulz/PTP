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

import java.util.HashMap;
import java.util.Map;

public class AttributeManager {
	private Map<String, IAttribute>	map = new HashMap<String, IAttribute>();
	
	public AttributeManager() {
	}
	
	public AttributeManager(IAttribute[] attrs) {
		for (IAttribute attr : attrs) {
			setAttribute(attr);
		}
	}

	public void setAttribute(IAttribute attr) {
		map.put(attr.getDefinition().getId(), attr);
	}

	public IAttribute getAttribute(String id) {
		return map.get(id);
	}
	
	public IAttribute[] getAttributes() {
		return map.values().toArray(new IAttribute[map.size()]);
	}
	
	public <T> T[] toArray(T[] a) {
		return map.values().toArray(a);
	}
}
