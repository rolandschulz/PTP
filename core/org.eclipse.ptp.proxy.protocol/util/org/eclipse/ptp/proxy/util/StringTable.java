/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.proxy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @since 5.0
 */
public class StringTable {
	private class StringEntry {
		private final String fEntry;
		private final int fIndex;

		public StringEntry(String str, int index) {
			fEntry = str;
			fIndex = index + 1;
		}

		/**
		 * @return the entry
		 */
		public String getEntry() {
			return fEntry;
		}

		/**
		 * @return the index
		 */
		public int getIndex() {
			return fIndex;
		}
	}

	private final Map<String, StringEntry> fMap = new HashMap<String, StringEntry>();
	private final ArrayList<StringEntry> fList = new ArrayList<StringEntry>();

	/**
	 * Flush all entries from the string table and reset the string index to 0
	 */
	public void flush() {
		fMap.clear();
		fList.clear();
	}

	/**
	 * Look up a string given it's index in the table
	 * 
	 * @param index
	 *            index returned by {@link #get(int)}
	 * @return string at the given index position, or null if no entry exists
	 * @throws IndexOutOfBoundsException
	 *             if index < 1 or index > size()
	 */
	public String get(int index) throws IndexOutOfBoundsException {
		StringEntry entry = fList.get(index - 1);
		return entry.getEntry();
	}

	/**
	 * Look up a string's index in the table
	 * 
	 * @param string
	 *            string to look up
	 * @return index of string in the table
	 * @throws NoSuchElementException
	 *             if the string could not be found
	 */
	public int get(String str) throws NoSuchElementException {
		StringEntry entry = fMap.get(str);
		if (entry == null) {
			throw new NoSuchElementException(str);
		}
		return entry.getIndex();
	}

	/**
	 * Insert a new string into the string table. Has no effect if the string
	 * already exists.
	 * 
	 * @param string
	 *            string to insert
	 * @return index of string in the table if the string already exists, or
	 *         -index if the string is inserted in the table for the first time
	 */
	public int put(String str) {
		StringEntry entry = fMap.get(str);
		if (entry == null) {
			entry = new StringEntry(str, fList.size());
			fMap.put(str, entry);
			fList.add(entry);
			return -entry.getIndex();
		}
		return entry.getIndex();
	}

	/**
	 * Returns the number of entries in the table
	 * 
	 * @return the number of entries in the table
	 */
	public int size() {
		return fList.size();
	}
}
