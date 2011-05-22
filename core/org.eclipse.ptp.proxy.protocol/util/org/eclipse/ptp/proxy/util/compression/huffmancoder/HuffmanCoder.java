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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.eclipse.ptp.proxy.util.messages.Messages;

import org.eclipse.ptp.proxy.util.compression.BitUtils;

class HuffmanCoder {

	/**
	 * Comparator for comparing two nodes based on frequency.
	 */
	static private class FrequencyCompare implements Comparator<HuffmanTree> {
		public FrequencyCompare() {
			// TODO Auto-generated constructor stub
		}

		
		public int compare(HuffmanTree f1, HuffmanTree f2) {
			return f1.frequency - f2.frequency;
		}
	}

	/**
	 * Huffman tree representation.
	 */
	static private final class HuffmanTree {
		int symbolIndex; /* Mapping to the symbol this node represents */
		int treeHeight; /*
								 * Max distance to any leaf under this node from
								 * this node
								 */
		HuffmanTree left; /* left child */
		HuffmanTree right; /* right child */
		int minEncodingLen; /*
									 * Min distance to any leaf under this node
									 * from this node
									 */
		final int frequency; /* Accumulated frequency at this node */

		public HuffmanTree(HuffmanTree n1, HuffmanTree n2, int totalFreq) {
			left = n1;
			right = n2;
			frequency = totalFreq;
			treeHeight = (n1.treeHeight > n2.treeHeight ? n1.treeHeight
					: n2.treeHeight) + 1;
			minEncodingLen = 1 + ((n1.minEncodingLen < n2.minEncodingLen) ? n1.minEncodingLen
					: n2.minEncodingLen);
		}

		public HuffmanTree(int freq, int sIndex) {
			symbolIndex = sIndex;
			frequency = freq;
		}
	}

	/*
	 * Represents encoding of a symbol
	 */
	static private final class Symbol {
		public Symbol() {
			// TODO Auto-generated constructor stub
		}
		/*
		 * The position in the ByteBuffer "encoding" where the encoding for this
		 * symbol starts
		 */
		int encodingindex;
		/* The length of the encoding in number of bits */
		int len;
	}

	/*
	 * Root of the huffman tree.
	 */
	private HuffmanTree huffTree;

	/*
	 * Array of symbols that define the universe of symbols.
	 */
	private final Symbol symbols[];

	/*
	 * A continuous buffer that holds the encoding for all the symbols. Symbols
	 * index into this ByteBuffer to find its encoding.
	 */
	private ByteBuffer encoding;

	/*
	 * Frequency mapping for each symbol.
	 */
	private final int freqtable[];

	/*
	 * Provides symbol to representation mapping and vice - versa.
	 */
	private final ISymbolDefiner symbolTable;

	/* Do caching if minimum encoding length is greater than this */
	private static final int CACHE_TRIGGER = 3;

	/* should not increase CACHE_SIZE_LIMIT above 16. getSomeBits assumes this for efficiency
	 * reasons and is guaranteed to return only 16 bits correctly. Also it is rare that a 
	 * byte huffman coder would have minimum encoding length greater than this.  
	 * */
	private static final int CACHE_SIZE_LIMIT = 16;

	private static final int BYTE_MASK = 0xFF;

	/* do cached decoding for messages larger than this */
	private static final int LARGE_MESSAGE = 8192;

	/**
	 * get some bits (at least 16) from in starting at bit position 'at', byte pos
	 * currentBytePos
	 * 
	 * @param in
	 *            The input byte array
	 * @param currentBytePos
	 *            The starting position to retrieve bits from
	 * @param at
	 *            The offset in the starting byte to retrieve bits
	 * @param len
	 *            The number of bits to retrieve (not used)
	 * @param limit
	 *            The size of the input array.
	 * @return An integer holding the bits in the array.
	 * 
	 */
	static private final int getSomeBits(byte in[], int currentBytePos, int at, int limit) {
		int ret;
		int pos;
		ret = 0;
		pos = currentBytePos;

		ret = ret | (in[pos++] & BYTE_MASK);
		if (pos < limit) {
			ret = ret | ((in[pos++] & BYTE_MASK) << BitUtils.NBITS_PER_BYTE);
		}
		if (pos < limit) {
			ret = ret
					| ((in[pos] & BYTE_MASK) << (BitUtils.NBITS_PER_BYTE << 1));
		}
		return ret >> at;
	}

	/**
	 * Constructs a HuffmanEncoder with given symbolTable and frequencies
	 * 
	 * Constructs a HuffmanEncoder given the symbolTable and input symbol
	 * frequencies.
	 * 
	 * @param symbolTable
	 *            Defines the symbols that make the input.
	 * @param frequencies
	 *            An array of frequencies - ith value corresponds to the the ith
	 *            symbol index defined by symbolTable.
	 */

	HuffmanCoder(ISymbolDefiner sTable, int frequencies[]) {
		symbolTable = sTable;
		freqtable = frequencies;
		symbols = new Symbol[frequencies.length];
		for (int i = 0; i < symbols.length; i++) {
			symbols[i] = new Symbol();
		}
	}

	/**
	 * Assigns Huffman codes for the build tree. Assigns the Huffman codes for
	 * the built Huffman tree.
	 */

	final void assignHuffmanCodes() {
		if (huffTree == null) {
			throw new RuntimeException(
					Messages.getString("HuffmanCoder.0"));  //$NON-NLS-1$
		}
		final int encodinglen = (huffTree.treeHeight >> 3) + 1;
		final ByteBuffer pattern = ByteBuffer.allocate(encodinglen);
		encoding = ByteBuffer.allocate(encodinglen * symbols.length);
		assignHuffmanCodesHelper(huffTree, pattern, 0, 0);
	}

	/**
	 * Helper function for assigning Huffman codes based on recursion.
	 * 
	 * @param node
	 *            The root node of the Huffman tree.
	 * @param pattern
	 *            Buffer used to build the pattern
	 * @param len
	 *            Length in bits of the pattern constructed so far.
	 * @param encodingindex
	 *            Position of the pattern in the ByteBuffer "encoding" for later
	 *            retrieval.
	 * @return Current encoding index.
	 */

	private int assignHuffmanCodesHelper(HuffmanTree node, ByteBuffer pattern,
			int len, int encodingindex) {
		if (node.left != null) {
			BitUtils.resetBit(pattern, len);
			encodingindex = assignHuffmanCodesHelper(node.left, pattern,
					len + 1, encodingindex);
			BitUtils.setBit(pattern, len);
			encodingindex = assignHuffmanCodesHelper(node.right, pattern,
					len + 1, encodingindex);
		} else {
			symbols[node.symbolIndex].encodingindex = encodingindex;
			symbols[node.symbolIndex].len = len;
			encoding.put(pattern.array(), 0, (len >> 3) + 1);
			encodingindex += (len >> 3) + 1;
		}
		return encodingindex;

	}

	/**
	 * Builds the Huffman tree.
	 * 
	 * Builds the Huffman tree based on the given frequencies.
	 */
	final void buildHuffmanTree() {
		final ArrayList<HuffmanTree> queue1 = new ArrayList<HuffmanTree>(
				symbols.length * 2);
		final ArrayList<HuffmanTree> queue2 = new ArrayList<HuffmanTree>(
				symbols.length * 2);
		int i;
		int lowindex1;
		int lowindex2;
		int highindex1;
		int highindex2;
		int nodesleft;
		final int indices[] = new int[4];

		for (i = 0; i < symbols.length; i++) {
			final HuffmanTree t = new HuffmanTree(freqtable[i], i);
			queue1.add(t);
		}

		Collections.sort(queue1, new FrequencyCompare());
		/*
		 * for(i = 0; i < symbols.length; i++) if (queue1.get(i).frequency > 0)
		 * break;
		 */
		i = 0;
		lowindex1 = i;
		lowindex2 = 0;
		highindex1 = symbols.length;
		highindex2 = 0;

		nodesleft = highindex1 - lowindex1 + highindex2 - lowindex2;
		while (nodesleft > 1) {
			indices[0] = lowindex1;
			indices[1] = lowindex2;
			indices[2] = highindex1;
			indices[3] = highindex2;
			combineNodes(queue1, queue2, highindex1 - lowindex1, highindex2
					- lowindex2, indices);
			lowindex1 = indices[0];
			lowindex2 = indices[1];
			highindex1 = indices[2];
			highindex2 = indices[3];
			nodesleft--;
		}
		huffTree = highindex1 - lowindex1 > 0 ? queue1.get(lowindex1) : queue2
				.get(lowindex2);
	}

	/*
	 * picks 2 nodes with lowest frequency from q1 and q1 (since they are sorted
	 * it will be at the front of the queue) and combine them to form a parent
	 * in the appropriate queue
	 * 
	 * @param q1 The first sorted queue
	 * 
	 * @param q2 The second sorted queue
	 * 
	 * @param len1 The size of queue1
	 * 
	 * @param len2 The size of queue2
	 * 
	 * @param indices The positions of low index and high index for the two
	 * queues. Values are also updated into this array.
	 */
	final private void combineNodes(ArrayList<HuffmanTree> q1,
			ArrayList<HuffmanTree> q2, int len1, int len2, int[] indices) {
		HuffmanTree n1;
		HuffmanTree n2;
		HuffmanTree n3 = null;
		HuffmanTree parent;
		int totalfreq;
		int lowindex1 = indices[0];
		int lowindex2 = indices[1];
		int highindex1 = indices[2];
		int highindex2 = indices[3];
		if (len1 > 0) {
			if (len2 > 0) {
				final int freqcomp = q1.get(lowindex1).frequency
						- q2.get(lowindex2).frequency;
				if (freqcomp < 0) {
					n1 = q1.get(lowindex1);
					n2 = q2.get(lowindex2);
					if (len1 > 1) {
						n3 = q1.get(lowindex1 + 1);
					}
					lowindex1++;
				} else {
					n1 = q2.get(lowindex2);
					n2 = q1.get(lowindex1);
					if (len2 > 1) {
						n3 = q2.get(lowindex2 + 1);
					}
					lowindex2++;
				}
				if (n3 == null) {
					if (freqcomp < 0) {
						lowindex2++;
					} else {
						lowindex1++;
					}
				} else {
					if (n2.frequency - n3.frequency > 0) {
						n2 = n3;
						if (freqcomp < 0) {
							lowindex1++;
						} else {
							lowindex2++;
						}
					} else {
						if (freqcomp < 0) {
							lowindex2++;
						} else {
							lowindex1++;
						}
					}
				}
			} else {
				n1 = q1.get(lowindex1);
				n2 = q1.get(lowindex1 + 1);
				lowindex1 += 2;
			}
		} else {
			n1 = q2.get(lowindex2);
			n2 = q2.get(lowindex2 + 1);
			lowindex2 += 2;
		}
		totalfreq = n1.frequency + n2.frequency;
		parent = new HuffmanTree(n1, n2, totalfreq);

		if (highindex1 - lowindex1 == 0
				|| totalfreq > q1.get(highindex1 - 1).frequency) {
			q1.add(parent);
			highindex1++;
		} else {
			q2.add(parent);
			highindex2++;
		}
		indices[0] = lowindex1;
		indices[1] = lowindex2;
		indices[2] = highindex1;
		indices[3] = highindex2;
	}

	/**
	 * Decode the input buffer based on Huffman algorithm.
	 * 
	 * Decodes the input Buffer based on Huffman algorithm.
	 * 
	 * @param in
	 *            The input ByteBuffer to decode.
	 * @param origlen
	 *            The original length of the uncompressed Buffer.
	 * @return The decoded ByteBuffer.
	 */
	ByteBuffer decode(ByteBuffer in, int origlen) {
		if (huffTree == null) {
			throw new RuntimeException(
					Messages.getString("HuffmanCoder.1"));  //$NON-NLS-1$
		}
		if (huffTree.minEncodingLen > HuffmanCoder.CACHE_TRIGGER
				&& huffTree.minEncodingLen < HuffmanCoder.CACHE_SIZE_LIMIT
				&& in.limit() > LARGE_MESSAGE) {
			return decodeCached(in, origlen);
		}

		int currentbitpos = 0;
		int len = 0;
		int currentbytepos;
		final byte resultBuffer[] = new byte[origlen];
		final ByteBuffer result = ByteBuffer.wrap(resultBuffer);
		final byte[] inBuffer = in.array();
		currentbytepos = in.arrayOffset();
		byte currentByte = inBuffer[currentbytepos++];
		HuffmanTree node = huffTree;

		while (true) {
			if (node.left != null) {
				if (currentbitpos == BitUtils.NBITS_PER_BYTE) {
					currentbitpos = 0;
					currentByte = inBuffer[currentbytepos++];
				}
				node = (currentByte & (1 << currentbitpos)) != 0 ? node.right
						: node.left;
				currentbitpos++;
			} else {
				final byte symbolrep[] = symbolTable
						.getSymbolRepresentation(node.symbolIndex);
				if (symbolrep.length == 1) {
					resultBuffer[len++] = symbolrep[0];
				} else {
					System.arraycopy(symbolrep, 0, resultBuffer, len,
							symbolrep.length);
					len += symbolrep.length;
				}
				if (len >= origlen) {
					break;
				}
				node = huffTree;
			}
		}
		return result;
	}

	/*
	 * This version of decode caches the nodes of the huffman tree upto a level
	 * equal to the minimum encoding length so that one can directly start at
	 * this level instead of starting at the root. The first part of the
	 * algorithm fills the cache and once it is filled the second half uses that
	 * cache to speed up lookups.
	 * 
	 * @param in The input buffer to decode
	 * 
	 * @param origlen The original uncompressed length
	 * 
	 * @return The uncompressed buffer.
	 */
	private ByteBuffer decodeCached(ByteBuffer in, int origlen) {
		HuffmanTree node = huffTree;
		int currentbitpos = 0;
		int len = 0;
		final byte[] resultBuffer = new byte[origlen];
		final ByteBuffer result = ByteBuffer.wrap(resultBuffer);
		HuffmanTree cache[] = null;
		int cacheSize = 0;
		int bitLen = 0;
		int cacheFilled = 0;
		int partBits = Integer.MAX_VALUE;
		final int limit = in.limit();
		final byte inBuffer[] = in.array();
		final int offset = in.arrayOffset();
		int currentBytePos = offset;
		int cacheBits = 0;
		byte currentByte = inBuffer[currentBytePos++];

		if (huffTree.minEncodingLen < HuffmanCoder.CACHE_SIZE_LIMIT) {
			cacheBits = huffTree.minEncodingLen;
		} else {
			cacheBits = HuffmanCoder.CACHE_SIZE_LIMIT - 1;
		}
		
		cacheSize = (1 << cacheBits);
		cache = new HuffmanTree[cacheSize];

		while (true) {
			if (node.left != null) {
				if (currentbitpos == BitUtils.NBITS_PER_BYTE) {
					currentbitpos = 0;
					currentByte = inBuffer[currentBytePos++];
				}
				node = (currentByte & (1 << currentbitpos)) != 0 ? node.right
						: node.left;
				currentbitpos++;
				bitLen++;
				if ((bitLen == cacheBits)
						&& partBits != Integer.MAX_VALUE) {
					cache[partBits] = node;
					cacheFilled++;
				}
			} else {
				final byte symbolrep[] = symbolTable
						.getSymbolRepresentation(node.symbolIndex);
				if (symbolrep.length == 1) {
					resultBuffer[len++] = symbolrep[0];
				} else {
					System.arraycopy(symbolrep, 0, resultBuffer, len,
							symbolrep.length);
					len += symbolrep.length;
				}
				if (len >= origlen) {
					break;
				}
				node = huffTree;
				bitLen = 0;

				if (cacheFilled == cacheSize) {
					break;
				}
				partBits = getSomeBits(inBuffer, currentBytePos - 1,
						currentbitpos, limit
						+ offset)
						% cacheSize;
				if (cache[partBits] != null) {
					node = cache[partBits];
					currentbitpos += cacheBits;
					while (currentbitpos >= BitUtils.NBITS_PER_BYTE) {
						currentbitpos -= BitUtils.NBITS_PER_BYTE;
						currentByte = inBuffer[currentBytePos++];
					}
					bitLen = cacheBits;
				}

			}
		}

		if (len < origlen) {
			while (true) {
				if (node.left != null) {
					if (currentbitpos == BitUtils.NBITS_PER_BYTE) {
						currentbitpos = 0;
						currentByte = inBuffer[currentBytePos++];
					}
					node = (currentByte & (1 << currentbitpos)) != 0 ? node.right
							: node.left;
					currentbitpos++;
				} else {
					final byte symbolrep[] = symbolTable
							.getSymbolRepresentation(node.symbolIndex);
					if (symbolrep.length == 1) {
						resultBuffer[len++] = symbolrep[0];
					} else {
						System.arraycopy(symbolrep, 0, resultBuffer, len,
								symbolrep.length);
						len += symbolrep.length;
					}
					if (len >= origlen) {
						break;
					}
					node = huffTree;

					partBits = getSomeBits(inBuffer, currentBytePos - 1,
							currentbitpos, limit
									+ offset)
							% cacheSize;
					node = cache[partBits];
					currentbitpos += cacheBits;
					while (currentbitpos >= BitUtils.NBITS_PER_BYTE) {
						currentbitpos -= BitUtils.NBITS_PER_BYTE;
						currentByte = inBuffer[currentBytePos++];
					}
				}
			}
		}
		return result;
	}

	/**
	 * Encode the input buffer based on Huffman algorithm.
	 * 
	 * Encode the input Buffer based on Huffman algorithm.
	 * 
	 * @param in
	 *            The input ByteBuffer to encode.
	 * @return The encoded ByteBuffer.
	 */
	ByteBuffer encode(ByteBuffer in) {
		if (huffTree == null) {
			throw new RuntimeException(
					Messages.getString("HuffmanCoder.2"));  //$NON-NLS-1$
		}
		int allocsize = in.capacity(), symbolId;
		Symbol symbol;
		int destbitpos = 0;
		byte[] encodedbytearray = new byte[allocsize];
		ByteBuffer encodedbuffer = ByteBuffer.wrap(encodedbytearray);
		final byte[] stable = encoding.array();
		final int stableoffset = encoding.arrayOffset();
		final byte[] inbuffer = in.array();
		final int inoffset = in.arrayOffset();
		int bitssize = ((allocsize - 2) << 3) - huffTree.treeHeight;
		int slen;

		symbolId = symbolTable.nextSymbol(inbuffer, inoffset);
		while (0 <= symbolId) {
			symbol = symbols[symbolId];
			slen = symbol.len;

			if (destbitpos >= bitssize) {
				allocsize += allocsize + symbol.len;
				bitssize = ((allocsize - 2) << 3) - huffTree.treeHeight;
				final byte newbytearray[] = new byte[allocsize];
				encodedbuffer = ByteBuffer.wrap(newbytearray);
				encodedbuffer.put(encodedbytearray);
				encodedbytearray = newbytearray;
			}
			BitUtils.packBits(encodedbytearray, destbitpos, stable,
					stableoffset + symbol.encodingindex, slen);
			destbitpos += slen;
			symbolId = symbolTable.nextSymbol(inbuffer);
		}
		encodedbuffer.limit(destbitpos != 0 ? 1 + (destbitpos >> 3) : 0);
		return encodedbuffer;
	}
}
