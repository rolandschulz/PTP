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

package org.eclipse.ptp.proxy.util.compression;

import java.nio.ByteBuffer;

/**
 * This class defines some common methods and constants required for any
 * Encoding.
 */
public class BitUtils {
	/** A constant equal to the number of bits per byte */
	public static final int NBITS_PER_BYTE = 8;

	static private int mask1[] = { 0xFF, 0x7F, 0x3F, 0x1F, 0xF, 0x7, 0x3, 0x1 };

	static private int mask2[] = { 0x0, 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F,
			0x7F };

	/**
	 * Get a bit from the input ByteBuffer
	 * 
	 * Returns the bit at bit position pos in ByteBuffer in.
	 * 
	 * @param in
	 *            The input ByteBuffer
	 * @param pos
	 *            The bit position in the given ByteBuffer
	 * @return Returns non-zero if the bit is set otherwise return zero.
	 */
	public static final int getBit(ByteBuffer in, int pos) {
		return in.get(pos >> 3) & (1 << (pos % NBITS_PER_BYTE));
	}

	/**
	 * Pack bits into destination ByteBuffer
	 * 
	 * pack len bits from src into dest starting at bit destpos pos in dest and
	 * byte srcpos in src.
	 * 
	 * @param dest
	 *            The destination ByteBuffer
	 * @param destpos
	 *            The bit position in the destination ByteBuffer to start
	 *            packing.
	 * @param src
	 *            The source byte array.
	 * @param srcpos
	 *            The source byte position in the src byte array.
	 * @param len
	 *            The number of bits in source array to copy.
	 */
	public static final void packBits(byte[] dest, int destpos, byte[] src,
			int srcpos, int len) {
		int cbyte;
		byte srcbyte;
		int destbytepos = destpos >> 3;
		final int offset = destpos - (destbytepos << 3);
		final int m1 = mask1[offset];
		final int m2 = mask2[offset];
		final int endpos = len == 0 ? srcpos : srcpos + (len >> 3) + 1;
		while (srcpos < endpos) {
			srcbyte = src[srcpos++];
			cbyte = dest[destbytepos];

			cbyte &= m2; /* set higher bits to 0 */
			cbyte |= (srcbyte & m1) << offset; /*
												 * insert lower bits of src at
												 * high bits in dest
												 */

			dest[destbytepos++] = (byte) cbyte;
			dest[destbytepos] = (byte) (srcbyte >> (NBITS_PER_BYTE - offset)); /*
																				 * put
																				 * the
																				 * remaining
																				 * bits
																				 * into
																				 * the
																				 * next
																				 * byte
																				 * location
																				 */
		}
	}

	/**
	 * Sets the bit in given input to zero
	 * 
	 * Sets the pos bit in input to zero
	 * 
	 * @param in
	 *            The input ByteBuffer
	 * @param pos
	 *            The position in the input ByteBuffer to set.
	 */

	public static final void resetBit(ByteBuffer in, int pos) {
		in.put(pos >> 3,
				(byte) (in.get(pos >> 3) & ~(1 << (pos % NBITS_PER_BYTE))));
	}

	/**
	 * Sets the bit in given input to one
	 * 
	 * Sets the pos bit in input to one
	 * 
	 * @param input
	 *            The input ByteBuffer
	 * @param pos
	 *            The position in the input ByteBuffer to set.
	 */

	public static final void setBit(ByteBuffer input, int pos) {
		input.put(pos >> 3,
				(byte) (input.get(pos >> 3) | (1 << (pos % NBITS_PER_BYTE))));
	}
}
