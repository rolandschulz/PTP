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

import org.eclipse.ptp.proxy.util.compression.IEncoder;
import org.eclipse.ptp.proxy.util.messages.Messages;

public final class HuffmanByteCompress implements IEncoder {
	private static final int NSYMBOLS = 256;

	private HuffmanCoder huffmanCoder;
	private ISymbolDefiner symbolTable;
	private boolean freqUpdated;
	private final int[] frequencies;
	private final int[] accFrequencies;
	private int bytesAccumulated;

	/**
	 * Constructs a HuffmanByteCompress object.
	 * 
	 * Constructs a HuffmanByteCompress object. The default object assumes equal
	 * frequencies for all symbols.
	 */
	public HuffmanByteCompress() {
		symbolTable = new ByteSymbolTable();
		freqUpdated = true;
		frequencies = new int[NSYMBOLS];
		accFrequencies = new int[NSYMBOLS];
		for (int i = 0; i < frequencies.length; i++) {
			frequencies[i] = accFrequencies[i] = 1;
		}
		symbolTable = new ByteSymbolTable();
		huffmanCoder = new HuffmanCoder(symbolTable, frequencies);
		huffmanCoder.buildHuffmanTree();
		huffmanCoder.assignHuffmanCodes();
		/* do not send the frequency table as receiver assumes the default */
		freqUpdated = false;
		bytesAccumulated = 0;
	}

	/**
	 * Constructs a HuffmanByteCompress object with initial frequencies.
	 * 
	 * Constructs a HuffmanByteCompress object with given initial frequencies
	 * 
	 * @param frequencies
	 *            Frequency values for each of the 256 byte values.
	 */
	public HuffmanByteCompress(int[] freq) {
		if (freq == null || freq.length != NSYMBOLS) {
			throw new IllegalArgumentException(
					Messages.getString("HuffmanByteCompress.0")); //$NON-NLS-1$
		}
		frequencies = new int[NSYMBOLS];
		accFrequencies = new int[NSYMBOLS];
		System.arraycopy(freq, 0, frequencies, 0, NSYMBOLS);
		symbolTable = new ByteSymbolTable();
		huffmanCoder = new HuffmanCoder(symbolTable, frequencies);
		huffmanCoder.buildHuffmanTree();
		huffmanCoder.assignHuffmanCodes();
		freqUpdated = true;
		bytesAccumulated = 0;
		for (int i = 0; i < NSYMBOLS; i++) {
			accFrequencies[i] = 1;
		}
	}

	/**
	 * Updates the internal frequency table to reflect the symbols in input
	 * buffer "in"
	 * 
	 * @param in
	 *            Accumulates the bytes in "in" and updates the frequencies
	 *            accordingly.
	 */
	private void accumulateFrequencies(ByteBuffer in) {
		final byte b[] = in.array();
		final int offset = in.arrayOffset();
		final int limit = in.limit();
		for (int i = 0; i < limit; i++) {
			accFrequencies[b[i + offset] & 0xFF]++;
		}
		bytesAccumulated += limit;
	}

	/**
	 * Compresses the input ByteBuffer
	 * 
	 * Compresses the input ByteBuffer in based on Huffman coding
	 * 
	 * @param in
	 *            The input buffer to be compressed with limit set to the number
	 *            of bytes in input.
	 * @return The buffer encoded by Huffman encoding.
	 */
	
	public ByteBuffer apply(ByteBuffer in) {
		ByteBuffer encodedbuffer;
		ByteBuffer result;
		int len;

		if (in == null || in.limit() <= 0) {
			throw new IllegalArgumentException(
					Messages.getString("HuffmanByteCompress.1")); //$NON-NLS-1$
		}

		accumulateFrequencies(in);

		encodedbuffer = huffmanCoder.encode(in);
		len = encodedbuffer.limit() + (freqUpdated ? 1024 : 0) + 4;
		result = ByteBuffer.allocate(len).putInt(in.limit());
		if (freqUpdated) {
			for (final int frequencie : frequencies) {
				result.putInt(frequencie);
			}
		}
		freqUpdated = false;
		result.put(encodedbuffer.array(), 0, encodedbuffer.limit()).rewind();

		return result;
	}

	/**
	 * Gets the number of bytes accumulated in the frequency table. This is
	 * reset to 0 on calling updateHuffmanTable.
	 * 
	 * @return The number of bytes accumulated before a call to
	 *         updateHuffmanTable
	 */
	public int getBytesAccumulated() {
		return bytesAccumulated;
	}

	/**
	 * Returns a boolean which indicates whether the freq table will be included
	 * in the buffer returned by apply.
	 * 
	 * @return Whether a subsequent call to apply will include the frequency
	 *         table. This might be changed by a call to updateHuffmanTable
	 */
	public boolean getIncludeTableFlag() {
		return freqUpdated;
	}

	/**
	 * Updates the huffman table with current accumulated frequencies.
	 * 
	 */
	public void updateHuffmanTable() {
		symbolTable = new ByteSymbolTable();

		System.arraycopy(accFrequencies, 0, frequencies, 0, NSYMBOLS);
		for (int i = 0; i < NSYMBOLS; i++) {
			accFrequencies[i] = 1;
		}
		huffmanCoder = new HuffmanCoder(symbolTable, frequencies);
		huffmanCoder.buildHuffmanTree();
		huffmanCoder.assignHuffmanCodes();
		freqUpdated = true;
		bytesAccumulated = 0;
	}

	/**
	 * Updates the Huffman table with given frequencies.
	 * 
	 * @param freq
	 *            The frequency array to update the internal frequencies to.
	 */
	public void updateHuffmanTable(int freq[]) {
		if (freq == null || freq.length != NSYMBOLS) {
			throw new IllegalArgumentException(
					Messages.getString("HuffmanByteCompress.0")); //$NON-NLS-1$
		}
		System.arraycopy(freq, 0, frequencies, 0, NSYMBOLS);
		symbolTable = new ByteSymbolTable();
		huffmanCoder = new HuffmanCoder(symbolTable, frequencies);
		huffmanCoder.buildHuffmanTree();
		huffmanCoder.assignHuffmanCodes();
		freqUpdated = true;
		for (int i = 0; i < NSYMBOLS; i++) {
			accFrequencies[i] = 1;
		}
		bytesAccumulated = 0;
	}
}
