/*******************************************************************************
 * Copyright (c) 2010 Los Alamos National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	LANL - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.util;

import java.util.BitSet;
import java.util.Iterator;

/**
 * @author Randy M. Roberts
 * @since 2.0
 * 
 */
public class BitSetIterable implements Iterable<Integer> {

	private class BitSetIterator implements Iterator<Integer> {

		private int index;

		public BitSetIterator(int i) {
			index = bitSet.nextSetBit(i);
		}

		public boolean hasNext() {
			return index >= 0;
		}

		public Integer next() {
			int tmpIndex = index;
			index = bitSet.nextSetBit(index + 1);
			return tmpIndex;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public static BitSetIterable iterate(BitSet bitSet) {
		return new BitSetIterable(bitSet);
	}

	private final BitSet bitSet;

	public BitSetIterable(BitSet bitSet) {
		this.bitSet = bitSet;
	}

	public Iterator<Integer> iterator() {
		return new BitSetIterator(0);
	}

}
