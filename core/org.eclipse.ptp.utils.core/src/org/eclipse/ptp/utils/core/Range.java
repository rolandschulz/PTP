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

package org.eclipse.ptp.utils.core;

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
	private boolean empty;

	/**
	 * @since 4.0
	 */
	public Range() {
		empty = true;
	}

	public Range(int val) {
		this(val, val);
	}

	public Range(int min, int max) throws IllegalArgumentException {
		if (min > max) {
			throw new IllegalArgumentException();
		}
		this.min = min;
		this.max = max;
		this.empty = false;
	}

	/**
	 * @since 4.0
	 */
	public void clear() {
		empty = true;
	}

	/**
	 * @since 4.0
	 */
	public boolean contains(int val) {
		return !empty && val >= min && val <= max;
	}

	/**
	 * @since 4.0
	 */
	public Range contains(Range r) {
		Range newRange = new Range();
		if (r.getMaxValue() >= min && r.getMinValue() <= max) {
			newRange.setMinValue(r.getMinValue() < min ? min : r.getMinValue());
			newRange.setMaxValue(r.getMaxValue() > max ? max : r.getMaxValue());
		}
		return newRange;
	}

	public int getMaxValue() {
		return max;
	}

	public int getMinValue() {
		return min;
	}

	/**
	 * @since 4.0
	 */
	public boolean isEmpty() {
		return empty;
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

	public void setMaxValue(int val) {
		max = val;
		empty = false;
	}

	public void setMinValue(int val) {
		min = val;
		empty = false;
	}

	public int size() {
		return empty ? 0 : max - min + 1;
	}

	@Override
	public String toString() {
		if (min == max) {
			return "" + min; //$NON-NLS-1$
		}

		return min + "-" + max; //$NON-NLS-1$
	}
}
