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

import org.eclipse.ptp.proxy.util.compression.IDecoder;
import org.eclipse.ptp.proxy.util.messages.Messages;

public class HuffmanByteUncompress implements IDecoder {
	private static final int NSYMBOLS = 256;

	private HuffmanCoder huffmanCoder;
	private final ISymbolDefiner symbolTable;
	private boolean freqUpdated;

	/**
	 * Constructs a HuffmanByteUncompress object
	 * 
	 * Constructs a HuffmanByteUncompress object.
	 * 
	 * @param frequencies
	 *            Frequency values for each of the 256 byte values.
	 */
	public HuffmanByteUncompress() {
		symbolTable = new ByteSymbolTable();
	}

	/**
	 * Uncompresses the input ByteBuffer
	 * 
	 * Uncompresses the input ByteBuffer "in" based on Huffman coding
	 * 
	 * @param in
	 *            The input buffer to be compressed.
	 * @return The uncompressed buffer using Huffman encoding.
	 */

	public ByteBuffer apply(ByteBuffer in) {
		if (in == null) {
			throw new IllegalArgumentException(Messages.getString("HuffmanByteUncompress.0")); //$NON-NLS-1$
		}
		int uncompressedlen;
		final int frequencies[] = new int[NSYMBOLS];

		in.position(0);
		uncompressedlen = in.getInt();

		if (freqUpdated) {
			for (int i = 0; i < NSYMBOLS; i++) {
				frequencies[i] = in.getInt();
			}
		} else if (huffmanCoder == null) {
			for (int i = 0; i < NSYMBOLS; i++) {
				frequencies[i] = 1;
			}
			freqUpdated = true;
		}
		if (freqUpdated) {
			huffmanCoder = new HuffmanCoder(symbolTable, frequencies);
			huffmanCoder.buildHuffmanTree();
			/* no need to assign huffman codes */

			freqUpdated = false;
		}
		final ByteBuffer result = huffmanCoder.decode(in.slice(), uncompressedlen);
		result.rewind();
		return result;
	}

	/**
	 * Notifies Huffman frequency table is updated in the incoming stream
	 * 
	 * When this method is called, the next call to apply expects a frequency
	 * table in the header information of the input compressed stream.
	 * Subsequent calls to apply assume no frequency table.
	 */
	public void notifyFrequencyUpdate() {
		freqUpdated = true;
	}

}
