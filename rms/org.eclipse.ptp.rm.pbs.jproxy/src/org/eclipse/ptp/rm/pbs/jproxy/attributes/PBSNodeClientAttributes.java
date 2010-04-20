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
package org.eclipse.ptp.rm.pbs.jproxy.attributes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.proxy.core.attributes.IElementAttributes;

/**
 * The Class defining the map that links the xml tag names to the attributes-ids
 * needs to be in the classpath of the jproxy. The proxy client defines the same
 * attribute-id constants. Obviously one shouldn't define those at two places.
 * This is only a temporary solution.
 */

public class PBSNodeClientAttributes extends PBSNodeProtocolAttributes
		implements IElementAttributes {
	private static final Map<String, String> nodeStatusMap = new HashMap<String, String>();
	static {
		nodeStatusMap.put("job-exclusive", "UP"); //$NON-NLS-1$ //$NON-NLS-2$
		nodeStatusMap.put("free", "UP"); //$NON-NLS-1$ //$NON-NLS-2$
		nodeStatusMap.put("down,offline", "DOWN"); //$NON-NLS-1$ //$NON-NLS-2$
		nodeStatusMap.put("down", "DOWN"); //$NON-NLS-1$ //$NON-NLS-2$
		nodeStatusMap.put("offline", "DOWN"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static final Map<String, DefaultValueMap<String, String>> valueMap = new HashMap<String, DefaultValueMap<String, String>>();
	static {
		valueMap.put(
				"state", new DefaultValueMap<String, String>("UNKNOWN", nodeStatusMap)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// public static Map<String, String> getNodeStatusMap() {
	// return nodeStatusMap;
	// }

	private static final Map<String, String> xmlTag_AttributeID_Map = new HashMap<String, String>();
	static {
		xmlTag_AttributeID_Map.put("name", NAME_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("state", STATE_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("np", NP_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("properties", PROPERTIES_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("ntype", NTYPE_ATTR_ID); //$NON-NLS-1$
		// somehow breaks the UI population, even after escaping the value as to
		// not interfer with the protocol encoding.
		// xmlTag_AttributeID_Map.put("status" , STATUS_ATTR_ID );
	}

	private static final String key = "name"; //$NON-NLS-1$

	private static final String parent_key = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.pbs.attributes.IElementAttributes#getKey()
	 */
	public String getKey() {
		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.pbs.attributes.IElementAttributes#getParentKey
	 * ()
	 */
	public String getParentKey() {
		return parent_key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.jproxy.attributes.IElementAttributes#getValueMap()
	 */
	public Map<String, DefaultValueMap<String, String>> getValueMap() {
		return valueMap;
	}

	public Map<String, String> getXmltagAttributeID_Map() {
		return xmlTag_AttributeID_Map;
	}
}
