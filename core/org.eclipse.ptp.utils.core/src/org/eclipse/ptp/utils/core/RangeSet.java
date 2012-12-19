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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Represents a range of numbers. Note: ranges are ALWAYS disjoint and kept in a
 * sorted list.
 * 
 */
public class RangeSet implements Iterable<String> {
	private final ArrayList<Range> rangeList = new ArrayList<Range>(0);
	private Iterator<Range> rangeListIter;
	private Iterator<String> rangeIter;

	public RangeSet() {

	}

	/**
	 * @param indices
	 * @since 2.0
	 */
	public RangeSet(BitSet indices) {
		if (indices.isEmpty()) {
			return;
		}

		BitSetIterable iitb = new BitSetIterable(indices);
		Iterator<Integer> iit = iitb.iterator();
		int firstVal = iit.next();

		int low = firstVal;
		int high = firstVal;

		while (iit.hasNext()) {
			int val = iit.next();
			if (val == (high + 1)) {
				++high;
			} else {
				add(low, high);
				low = val;
				high = val;
			}
		}
		add(low, high);
	}

	public RangeSet(int val) {
		this(val, val);
	}

	public RangeSet(int min, int max) {
		rangeList.add(new Range(min, max));
	}

	public RangeSet(String str) {
		if (str != null) {
			String[] ranges = str.split(","); //$NON-NLS-1$
			for (String range : ranges) {
				String[] vals = range.split("-"); //$NON-NLS-1$
				if (vals.length == 1) {
					add(Integer.parseInt(vals[0]));
				} else {
					add(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
				}
			}
		}
	}

	/**
	 * @since 4.0
	 */
	public BitSet getBits() {
		BitSet bits = new BitSet(size());
		for (Range r : rangeList) {
			bits.set(r.getMinValue(), r.getMaxValue());
		}
		return bits;
	}

	/**
	 * Add value to the set
	 * 
	 * @param val
	 */
	public void add(int val) {
		int pos = 0;

		if (rangeList.size() > 0) {
			pos = findIndex(val);

			if (pos >= 0) {
				return;
			}

			pos = -(pos + 1);
		}

		rangeList.add(pos, new Range(val));
		fixupRanges();
	}

	/**
	 * Add values in the range [min, max] to the set
	 * 
	 * @param min
	 * @param max
	 */
	public void add(int min, int max) {
		internalAdd(min, max);
		fixupRanges();
	}

	/**
	 * Add all the values contained in the RangeSet to this set
	 * 
	 * @param set
	 * @since 4.0
	 */
	public void add(RangeSet set) {
		for (Range range : set.getRanges()) {
			add(range.getMinValue(), range.getMaxValue());
		}
		fixupRanges();
	}

	/**
	 * @since 4.0
	 */
	public void clear() {
		rangeList.clear();
	}

	/**
	 * Check if val is in the set
	 * 
	 * @param val
	 * @return true if val is in the set
	 * @since 4.0
	 */
	public boolean contains(int val) {
		return findIndex(val) >= 0;
	}

	private void contains(Range val, RangeSet set) {
		for (Range r : rangeList) {
			Range nr = r.contains(val);
			set.add(nr.getMinValue(), nr.getMaxValue());
		}
	}

	/**
	 * Find the common values between the supplied set and this set.
	 * 
	 * @param set
	 * @return RangeSet containing common values
	 * @since 4.0
	 */
	public RangeSet contains(RangeSet set) {
		RangeSet newSet = new RangeSet();
		for (Range r : set.getRanges()) {
			contains(r, newSet);
		}
		return newSet;
	}

	/**
	 * Find the index position of val in the list of ranges using a binary search. Returns a positive value if val is already in the
	 * list, or a negative number representing the position+1 in the list that val would be located.
	 * 
	 * @param val
	 * @return
	 */
	private int findIndex(int val) {
		int low = 0;
		int high = rangeList.size() - 1;

		while (low <= high) {
			int pos = (low == high) ? low : ((low + high) / 2);
			Range r = rangeList.get(pos);

			if (r.contains(val)) {
				return pos;
			} else if (val < r.getMinValue()) {
				high = pos - 1;
			} else if (val > r.getMaxValue()) {
				low = pos + 1;
			}
		}

		return -(low + 1);
	}

	private void fixupRanges() {
		for (int pos = rangeList.size() - 2; pos >= 0; pos--) {
			Range r1 = rangeList.get(pos);
			Range r2 = rangeList.get(pos + 1);
			if (r1.getMaxValue() >= r2.getMinValue() - 1) {
				r1.setMaxValue(r2.getMaxValue());
				rangeList.remove(pos + 1);
			}
		}
	}

	/**
	 * Get the list of ranges in this set. These ranges are guaranteed to be sorted and non-overlapping. i.e. there will always be
	 * at least one element between each range in the list.
	 * 
	 * @return sorted list of non-overlapping ranges in this set
	 * 
	 * @since 4.0
	 */
	public List<Range> getRanges() {
		return rangeList;
	}

	private void internalAdd(int min, int max) {
		if (min == max) {
			add(min);
			return;
		}

		if (min > max) {
			return;
		}

		if (rangeList.size() == 0) {
			rangeList.add(new Range(min, max));
			return;
		}

		Range rlow = null;
		Range rhigh = null;

		int ilow = findIndex(min);
		int ihigh = findIndex(max);

		if (ilow >= 0) {
			/*
			 * Quick check to see if subset
			 */
			if (ilow == ihigh) {
				return;
			}

			/*
			 * min is already covered by a range
			 */
			rlow = rangeList.get(ilow);

			if (ihigh >= 0) {
				/*
				 * max is already covered by a range. Need to merge ranges.
				 */
				rhigh = rangeList.get(ihigh);
				rlow.setMaxValue(rhigh.getMaxValue());
			} else {
				/*
				 * max falls outside a range. Extend current range.
				 */
				rlow.setMaxValue(max);
				ihigh = -(ihigh + 2);
			}

			/*
			 * Remove ranges
			 */
			for (; ihigh != ilow; ihigh--) {
				rangeList.remove(ilow + 1);
			}
		} else {
			/*
			 * min falls outside a range
			 */
			ilow = -(ilow + 1);

			if (ihigh >= 0) {
				/*
				 * max falls inside a range
				 */
				rhigh = rangeList.get(ihigh);
				rhigh.setMinValue(min);

				/*
				 * Remove ranges
				 */
				for (; ihigh != ilow; ihigh--) {
					rangeList.remove(ilow);
				}
			} else {
				ihigh = -(ihigh + 1);

				if (ilow == ihigh) {
					rangeList.add(ilow, new Range(min, max));
				} else {
					rlow = rangeList.get(ilow);
					rlow.setMinValue(min);
					rlow.setMaxValue(max);

					/*
					 * Remove ranges
					 */
					for (; ihigh > ilow + 1; ihigh--) {
						rangeList.remove(ilow + 1);
					}
				}
			}
		}
	}

	public Iterator<String> iterator() {
		rangeListIter = rangeList.iterator();

		if (rangeListIter.hasNext()) {
			rangeIter = rangeListIter.next().iterator();
		} else {
			rangeIter = null;
		}

		return new Iterator<String>() {
			public boolean hasNext() {
				return rangeIter != null && (rangeIter.hasNext() || rangeListIter.hasNext());
			}

			public String next() {
				if (hasNext()) {
					if (!rangeIter.hasNext()) {
						rangeIter = rangeListIter.next().iterator();
					}
					return rangeIter.next();
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public void remove(int val) {
		int pos = findIndex(val);
		if (pos >= 0) {
			Range r = rangeList.get(pos);
			int min = r.getMinValue();
			int max = r.getMaxValue();
			if (max == min) {
				rangeList.remove(pos);
			} else if (min == val) {
				r.setMinValue(val + 1);
			} else {
				r.setMaxValue(val - 1);
				if (max > val) {
					Range r2 = new Range(val + 1, max);
					rangeList.add(pos + 1, r2);
				}
			}
		}
	}

	public void remove(int from, int to) {
		ListIterator<Range> it = rangeList.listIterator();
		while (it.hasNext()) {
			Range r = it.next();
			int min = r.getMinValue();
			int max = r.getMaxValue();
			if (min >= from && max <= to) {
				it.remove();
			} else if (r.contains(from) || r.contains(to)) {
				if (from > min) {
					r.setMaxValue(from - 1);
					if (to < max) {
						Range r2 = new Range(to + 1, max);
						it.add(r2);
					}
				} else if (to < max) {
					r.setMinValue(to + 1);
				}
			}
		}
	}

	/**
	 * @since 4.0
	 */
	public void remove(RangeSet set) {
		for (Range r : set.getRanges()) {
			remove(r.getMinValue(), r.getMaxValue());
		}
	}

	public int size() {
		int nels = 0;
		for (Range r : rangeList) {
			nels += r.size();
		}
		return nels;
	}

	public int[] toArray() {
		int elt = 0;
		int[] vals = new int[size()];

		for (Range r : rangeList) {
			for (int i = r.getMinValue(); i <= r.getMaxValue(); i++) {
				vals[elt++] = i;
			}
		}

		return vals;
	}

	@Override
	public String toString() {
		String str = ""; //$NON-NLS-1$

		for (int i = 0; i < rangeList.size(); i++) {
			if (i > 0) {
				str += ","; //$NON-NLS-1$
			}
			str += rangeList.get(i).toString();
		}

		return str;
	}
}
