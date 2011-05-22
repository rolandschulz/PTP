/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.util.compression.huffmancoder;

import java.nio.ByteBuffer;

/** Implements the ISymbolDefiner interface for a byte level mapping. */
final class ByteSymbolTable implements ISymbolDefiner {
	private final byte[][] symbolIndex;
	private int curr;

	/** Default Initializer */
	ByteSymbolTable() {
		symbolIndex = new byte[256][];
		for (int i = 0; i < 256; i++) {
			symbolIndex[i] = new byte[1];
			symbolIndex[i][0] = (byte) i;
		}
		curr = 0;
	}

	/**
	 * Gets the byte representation corresponding to given symbolId
	 * 
	 * Gets the raw representation corresponding to symbolId. This is the
	 * reverse mapping of nextSymbol.
	 * 
	 * @param symbolId
	 *            The symbolId of the mapping.
	 * @return The raw byte array representation of the symbolId
	 */
	
	public byte[] getSymbolRepresentation(int index) {
		return symbolIndex[index];
	}

	/**
	 * Gets the next symbol in byte array based upon the internal position set.
	 * The internal position is subsequently incremented.
	 * 
	 * @param in
	 *            The input byte array.
	 * @return The index/id of the symbol at current position in byte array.
	 *         Returns -1 if position is out of bounds.
	 */
	
	public int nextSymbol(byte[] in) {
		if (curr < in.length) {
			return in[curr++] & 0x000000FF;
		}
		return -1;
	}

	/**
	 * Gets the symbol at specified position Returns the symbol index present at
	 * position 'at' in input buffer in. Also sets the internal position at this
	 * index to 'at' + 1.
	 * 
	 * @param in
	 *            The input byte array.
	 * @param at
	 *            The position for which the next symbol index is returned.
	 * @return The index/id of the symbol at position 'at' in byte array 'in'.
	 *         If position is out of bounds returns -1.
	 */

	
	public int nextSymbol(byte[] in, int at) {
		if (at < in.length) {
			curr = at + 1;
			return in[at] & 0x000000FF;
		}
		return -1;
	}

	/**
	 * Gets the next symbol index
	 * 
	 * Returns the next symbol index in the input buffer in. This method is a
	 * one-to-one mapping between the representation and the symbol index.
	 * 
	 * @param in
	 *            The input ByteBuffer.
	 * @return The index of the next Symbol. -1 if there are none.
	 */
	
	public int nextSymbol(ByteBuffer in) {
		if (in.position() < in.limit()) {
			return in.get() & 0x000000FF;
		}
		return -1;
	}
}
