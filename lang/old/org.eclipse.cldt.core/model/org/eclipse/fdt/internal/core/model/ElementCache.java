package org.eclipse.fdt.internal.core.model;

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

import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.IOpenable;
import org.eclipse.fdt.internal.core.util.LRUCache;
import org.eclipse.fdt.internal.core.util.OverflowingLRUCache;

/**
 * An LRU cache of <code>CElements</code>.
 * 
 * This class is similar to the JDT ElementCache class.
 */
public class ElementCache extends OverflowingLRUCache {

	/**
	 * Constructs a new element cache of the given size.
	 */
	public ElementCache(int size) {
		super(size);
	}
	/**
	 * Constructs a new element cache of the given size.
	 */
	public ElementCache(int size, int overflow) {
		super(size, overflow);
	}
	/**
	 * Returns true if the element is successfully closed and
	 * removed from the cache, otherwise false.
	 *
	 * <p>NOTE: this triggers an external removal of this element
	 * by closing the element.
	 */
	protected boolean close(LRUCacheEntry entry) {
		IOpenable element = (IOpenable) entry._fKey;
		try {
			if (element.hasUnsavedChanges()) {
				return false;
			}
			element.close();
			return true;
		} catch (CModelException npe) {
			return false;
		}
	}
		/**
		 * Returns a new instance of the reciever.
		 */
		protected LRUCache newInstance(int size, int overflow) {
			return new ElementCache(size, overflow);
		}
}
