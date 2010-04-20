/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation
 *    Benjamin Lindner (ben@benlabs.net)

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

public class PBSQueueClientAttributes extends PBSQueueProtocolAttributes
		implements IElementAttributes {
	private static final Map<String, String> xmlTag_AttributeID_Map = new HashMap<String, String>();
	static {
		xmlTag_AttributeID_Map.put("name", NAME_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("queue_type", TYPE_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("total_jobs", TOTAL_JOBS_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("state_count", STATE_COUNT_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("mtime", MTIME_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("enabled", ENABLED_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put("started", STARTED_ATTR_ID); //$NON-NLS-1$

		xmlTag_AttributeID_Map.put(
				"resources_max__walltime", RES_MAX_WALLTIME_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put(
				"resources_default__nodes", RES_DEFAULT_NODES_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put(
				"resources_default__walltime", RES_DEFAULT_WALLTIME_ATTR_ID); //$NON-NLS-1$
		xmlTag_AttributeID_Map.put(
				"resources_assigned__nodect", RES_ASSIGNED_NODECT_ATTR_ID); //$NON-NLS-1$
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

	public Map<String, DefaultValueMap<String, String>> getValueMap() {
		return null;
	}

	public Map<String, String> getXmltagAttributeID_Map() {
		return xmlTag_AttributeID_Map;
	}
}