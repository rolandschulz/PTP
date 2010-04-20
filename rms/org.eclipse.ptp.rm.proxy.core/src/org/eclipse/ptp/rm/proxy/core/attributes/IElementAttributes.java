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

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface IElementAttributes.
 */
public interface IElementAttributes {

	/**
	 * The Class DefaultValueMap.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 */
	public class DefaultValueMap<K, V> extends HashMap<K, V> {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 7519098365827806311L;

		/** The default value. */
		public V defaultValue;

		/**
		 * Instantiates a new default value map.
		 * 
		 * @param defaultValue
		 *            the default value
		 */
		public DefaultValueMap(V defaultValue) {
			this.defaultValue = defaultValue;
		}

		/**
		 * Instantiates a new default value map.
		 * 
		 * @param defaultValue
		 *            the default value
		 * @param map
		 *            the map
		 */
		public DefaultValueMap(V defaultValue, Map<K, V> map) {
			super(map);
			this.defaultValue = defaultValue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.HashMap#get(java.lang.Object)
		 */
		@Override
		public V get(Object key) {
			if (containsKey(key)) {
				return super.get(key);
			} else {
				return defaultValue;
			}
		}

	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public abstract String getKey();

	/**
	 * Gets the parent key.
	 * 
	 * @return the parent key
	 */
	public abstract String getParentKey();

	/**
	 * Gets the value map.
	 * 
	 * @return the value map
	 */
	public abstract Map<String, DefaultValueMap<String, String>> getValueMap();

	/* Map between xml-tag (key) and communication protocol keyword (value) */
	/**
	 * Gets the xmltag attribute i d_ map.
	 * 
	 * @return the xmltag attribute i d_ map
	 */
	public abstract Map<String, String> getXmltagAttributeID_Map();

}