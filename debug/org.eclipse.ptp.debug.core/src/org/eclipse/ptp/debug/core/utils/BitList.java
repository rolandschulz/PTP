/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.debug.core.utils;

import java.util.BitSet;

public class BitList {

	private BitSet bitSet;
	
	public BitList() {
		bitSet = new BitSet();
	}
	
	public int cardinality() {
		return bitSet.cardinality();
	}

	public void clear(int index) {
		bitSet.clear(index);
	}

	public void clear(int[] indexs) {
		for (int i = 0; i < indexs.length; i++)
			bitSet.clear(indexs[i]);
	}

	public void set(int index) {
		bitSet.set(index);
	}

	public void set(int[] indexs) {
		for (int i = 0; i < indexs.length; i++)
			bitSet.set(indexs[i]);
	}
	
	public boolean get(int index) {
		return bitSet.get(index);
	}
	
	public int nextSetBit(int index) {
		return bitSet.nextSetBit(index);
	}
	
	public int[] toArray() {
		int[] retValue = new int[bitSet.cardinality()];
		for(int i = bitSet.nextSetBit(0), j = 0; i >= 0; i = bitSet.nextSetBit(i+1), j++) {
			retValue[j] = i;
		}
		return retValue;
	}
	
	public BitList copy() {
		BitList retVal = new BitList();
		retVal.bitSet = (BitSet) bitSet.clone();
		return retVal;
	}
}
