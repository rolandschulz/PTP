package org.eclipse.cldt.internal.core.util;
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
 *	The <code>LRUCacheEnumerator</code> returns its elements in 
 *	the order they are found in the <code>LRUCache</code>, with the
 *	most recent elements first.
 *
 *	Once the enumerator is created, elements which are later added 
 *	to the cache are not returned by the enumerator.  However,
 *	elements returned from the enumerator could have been closed 
 *	by the cache.
 *
 *  This class is similar to the JDT LRUCacheEnumerator class. 
 */
public class LRUCacheEnumerator implements Enumeration {
	/**
	 *	Current element;
	 */
	protected LRUEnumeratorElement fElementQueue;

	public static class LRUEnumeratorElement {
		/**
		 *	Value returned by <code>nextElement()</code>;
		 */
		public Object fValue;
		
		/**
		 *	Next element
		 */
		public LRUEnumeratorElement fNext;

		/**
		 * Constructor
		 */
		public LRUEnumeratorElement(Object value) {
			fValue = value;
		}
	}
	/**
	 *	Creates a CacheEnumerator on the list of <code>LRUEnumeratorElements</code>.
	 */
	public LRUCacheEnumerator(LRUEnumeratorElement firstElement) {
		fElementQueue = firstElement;
	}
	/**
	 * Returns true if more elements exist.
	 */
	public boolean hasMoreElements() {
		return fElementQueue != null;
	}
	/**
	 * Returns the next element.
	 */
	public Object nextElement() {
		Object temp = fElementQueue.fValue;
		fElementQueue = fElementQueue.fNext;
		return temp;
	}
}

