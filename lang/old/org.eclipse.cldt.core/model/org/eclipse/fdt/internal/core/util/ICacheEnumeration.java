package org.eclipse.fdt.internal.core.util;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import java.util.Enumeration;

/**
 * The <code>ICacheEnumeration</code> is used to iterate over both the keys 
 * and values in an LRUCache.  The <code>getValue()</code> method returns the 
 * value of the last key to be retrieved using <code>nextElement()</code>.  
 * The <code>nextElement()</code> method must be called before the 
 * <code>getValue()</code> method.
 *
 * <p>The iteration can be made efficient by making use of the fact that values in 
 * the cache (instances of <code>LRUCacheEntry</code>), know their key.  For this reason,
 * Hashtable lookups don't have to be made at each step of the iteration.
 *
 * <p>Modifications to the cache must not be performed while using the
 * enumeration.  Doing so will lead to an illegal state.
 *
 * @see LRUCache
 * 
 * This interface is similar to the JDT ICacheEnumeration interface.
 */
public interface ICacheEnumeration extends Enumeration {
	/**
	 * Returns the value of the previously accessed key in the enumeration.
	 * Must be called after a call to nextElement().
	 *
	 * @return Value of current cache entry
	 */
	public Object getValue();
}
