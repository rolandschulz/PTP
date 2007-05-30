/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.core.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a range of numbers inclusive of max and min.
 *
 */
public class Range implements Iterable<String> {
	private Integer min;
	private Integer max;
	private Integer curr;
	
	public Range(int val) {
		this(val, val);
	}
	
	public Range(int min, int max) throws IllegalArgumentException {
		if (min > max) {
			throw new IllegalArgumentException();
		}
		this.min = min;
		this.max = max;
	}
	
	public int getMinValue() {
		return min;
	}
	
	public int getMaxValue() {
		return max;
	}
	
	public void setMinValue(int val) {
		min = val;
	}
	
	public void setMaxValue(int val) {
		max = val;
	}
	
	public boolean inRange(int val) {
		return val >= min && val <= max;
	}
	
	public int size() {
		return max - min + 1;
	}
	
	public String toString() {
		if (min == max)
			return "" + min;
		
		return min + "-" + max;
	}

	public Iterator<String> iterator() {
		curr = min;
		
		return new Iterator<String>() {
			public boolean hasNext() {
				return curr <= max;
			}

			public String next() {
				if (hasNext()) {
					return (curr++).toString();
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}

