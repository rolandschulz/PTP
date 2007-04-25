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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a range of numbers. Note: ranges are ALWAYS disjoint and
 * kept in a sorted list.
 *
 */
public class RangeSet implements Iterable<Integer> {
	private ArrayList<Range>	rangeList = new ArrayList<Range>(0);
	private Iterator<Range>		rangeListIter;
	private Iterator<Integer>	rangeIter;
	
	public RangeSet() {
		
	}
	
	public RangeSet(String str) {
		if (str != null) {
			String[] ranges = str.split(",");
			for (String range : ranges) {
				String[] vals = range.split("-");
				if (vals.length == 1) {
					try {
						add(Integer.parseInt(vals[0]));
					} catch (NumberFormatException e) {
					}
				} else {
					try {
						add(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
					} catch (NumberFormatException e) {
					}					
				}
			}
		}
	}
	
	public RangeSet(int val) {
		this(val, val);
	}
	
	public RangeSet(int min, int max) {
		rangeList.add(new Range(min, max));
	}
	
	public void add(int val) {
		int pos = 0;
		
		if (rangeList.size() > 0) {
			pos = findIndex(val);
			
			if (pos >= 0)
				return;
			
			pos = -(pos + 1);
	    }
	    
        rangeList.add(pos, new Range(val));  
        fixupRanges();
	}
	
	public void add(int min, int max) {
		if (min == max) {
			add(min);
			return;
		} 
		
		if (min > max) {
			return;
		}
		
		if (rangeList.size() == 0) {
			rangeList.add(new Range(min,max));
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
	    		 * max is already covered by a range.
	    		 * Need to merge ranges.
	    		 */
	    		rhigh = rangeList.get(ihigh);
	    		rlow.setMaxValue(rhigh.getMaxValue());
	    	} else {
		    	/*
		    	 * max falls outside a range.
		    	 * Extend current range.
		    	 */
	    		rlow.setMaxValue(max);
	    		ihigh = -(ihigh+2);
	    	}
	    	
	    	/*
	    	 * Remove ranges
	    	 */
    		for (; ihigh != ilow; ihigh--) {
    			rangeList.remove(ilow+1);
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
		    			rangeList.remove(ilow+1);
		    		}
	    		}
	    	}
	    }
	    
	    fixupRanges();
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
	
	public void remove(int val) {
		
	}
	
	public void remove(int min, int max) {
		
	}
	
	public boolean inRange(int val) {
		return findIndex(val) >= 0;
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
	
	public String toString() {
		String str = "";
		
		for (int i = 0; i < rangeList.size(); i++) {
			if (i > 0)
				str += ",";
			str += rangeList.get(i).toString();
		}
		
		return str;
	}
	
	public int findIndex(int val) {
	    int low = 0;
	    int high = rangeList.size() - 1;
	    
	    while (low <= high) {
	        int pos = (low == high) ? low : ((low + high) / 2);
	        Range r = rangeList.get(pos);

	        if (r.inRange(val)) {
	            return pos;
	        } else if (val < r.getMinValue()) {
	            high = pos - 1;
	        } else if (val > r.getMaxValue()) {
	            low = pos + 1;
	        }
	    }
	    
	    return -(low+1);
	}

	public Iterator<Integer> iterator() {
		rangeListIter = rangeList.iterator();
		
		if (rangeListIter.hasNext())
			rangeIter = rangeListIter.next().iterator();
		else
			rangeIter = null;
		
		return new Iterator<Integer>() {
			public boolean hasNext() {
				return rangeIter != null && (rangeIter.hasNext() || rangeListIter.hasNext());
			}

			public Integer next() {
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
}
	

