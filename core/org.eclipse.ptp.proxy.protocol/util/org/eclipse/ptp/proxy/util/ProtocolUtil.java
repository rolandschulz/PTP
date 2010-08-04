/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.proxy.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.util.messages.Messages;

public class ProtocolUtil {
	public final static int HEXADECIMAL = 0;
	public final static int OCTAL = 1;
	public final static int BINARY = 2;
	public final static int DECIMAL = 3;
	public final static int RAW = 4;
	public final static int NATURAL = 5;

	public final static int FLOAT = 10;
	public final static int ADDRESS = 11;
	public final static int INSTRUCTION = 12;
	public final static int CHAR = 13;
	public final static int STRING = 14;
	public final static int UNSIGNED = 15;

	private final static int FLAG_NORMAL = 0x80000000;
	private final static int FLAG_NOSTORE = 0x40000000;
	private final static int LENGTH_MASK = 0x3fffffff;
	private final static int ID_MASK = 0x7fffffff;

	/**
	 * Decode a string into a BigInteger
	 * 
	 * @param address
	 * @return BigInteger
	 */
	public static BigInteger decodeAddress(String address) {
		int index = 0;
		int radix = 10;
		boolean negative = false;

		// Handle zero length
		address = address.trim();
		if (address.length() == 0) {
			return BigInteger.ZERO;
		}

		// Handle minus sign, if present
		if (address.startsWith("-")) { //$NON-NLS-1$
			negative = true;
			index++;
		}
		if (address.startsWith("0x", index) || address.startsWith("0X", index)) { //$NON-NLS-1$ //$NON-NLS-2$
			index += 2;
			radix = 16;
		} else if (address.startsWith("#", index)) { //$NON-NLS-1$
			index++;
			radix = 16;
		} else if (address.startsWith("0", index) && address.length() > 1 + index) { //$NON-NLS-1$
			index++;
			radix = 8;
		}

		if (index > 0) {
			address = address.substring(index);
		}
		if (negative) {
			address = "-" + address; //$NON-NLS-1$
		}
		try {
			return new BigInteger(address, radix);
		} catch (NumberFormatException e) {
			// ...
			// What can we do ???
		}
		return BigInteger.ZERO;
	}

	/**
	 * Convert a proxy representation of an attribute into a Java String
	 * 
	 * Attributes are key/value pairs of the form "key=value". They are encoded
	 * into a pair of length/value pairs, with the first length/value pair being
	 * the key of a key=value pair and the second length/value pair being the
	 * value of a key=value pair or a stand-alone string's value.
	 * 
	 * @param buf
	 *            buffer containing bytes to decode
	 * @param decoder
	 *            charset decoder
	 * @return attribute converted to Java String
	 * @throws IOException
	 *             if decoding failed
	 * @since 5.0
	 */
	public static String decodeAttribute(ByteBuffer buf, CharsetDecoder decoder, StringTable stringTable) throws IOException {
		String result = ""; //$NON-NLS-1$

		String key = decodeString(buf, decoder, stringTable);
		if (key != null) {
			result = key;
		}
		String value = decodeString(buf, decoder, stringTable);
		if (value != null) {
			if (result.length() > 0) {
				result += "="; //$NON-NLS-1$
			}
			result += value;
		}
		return result;
	}

	/**
	 * Convert as sequence of hexadecimal values to a Java byte array.
	 * 
	 * @param str
	 * @return byte array
	 */
	public static byte[] decodeBytes(String str) {
		int len = str.length() / 2;
		byte[] strBytes = new byte[len];

		for (int i = 0, p = 0; i < len; i++, p += 2) {
			byte c = (byte) ((Character.digit(str.charAt(p), 16) & 0xf) << 4);
			c |= (byte) ((Character.digit(str.charAt(p + 1), 16) & 0xf));
			strBytes[i] = c;
		}

		return strBytes;
	}

	/**
	 * Convert a proxy representation of a string to a String.
	 * 
	 * In order to reduce message traffic, only the first occurrence of a string
	 * is included in the packet. When a unique string is seen, it may be added
	 * to a string table. Subsequent references to the same string will use the
	 * index of the string in the string table, rather than the actual string.
	 * 
	 * A string is represented as a length field followed by an optional value.
	 * The length field is an unsigned 32 bit integer encoded as a varint. The
	 * field is interpreted as follows:
	 * 
	 * <pre>
	 *       +----------------------------+
	 * Bit # | 31 | 30 |  Remaining Bits  |
	 *       +----------------------------+
	 * Value | E  | S  | Depends on flags |
	 *       +----------------------------+
	 * 
	 * E = 0 
	 * 		The value is omitted. Bits 30-0 = string table ID + 1
	 * E = 1
	 *      The value is the actual string with bits 29-0 set to the length of 
	 *      the string excluding any null termination.
	 * E = 1, S = 1
	 *      Don't add the string to the string table.
	 *      
	 * If all the bits are 0, the value is omitted. The string should be skipped.
	 * </pre>
	 * 
	 * @param buf
	 *            byte buffer containing string to be decoded
	 * @param decoder
	 *            charset decoder
	 * @return decoded string or null if the field should be skipped
	 * @throws IOException
	 *             if the string can't be decoded
	 * 
	 * @since 5.0
	 */
	public static String decodeString(ByteBuffer buf, CharsetDecoder decoder, StringTable stringTable) throws IOException {
		String result = ""; //$NON-NLS-1$
		VarInt strLen = new VarInt(buf);
		if (!strLen.isValid()) {
			throw new IOException(Messages.getString("ProtocolUtil.0")); //$NON-NLS-1$
		}
		int flags = strLen.getValue();
		if (flags == 0) {
			return null;
		}
		if ((flags & FLAG_NORMAL) == FLAG_NORMAL) {
			/*
			 * Normal string. Decode and insert into string table if required.
			 */
			int len = flags & LENGTH_MASK;

			if (len > 0) {
				ByteBuffer strBuf = buf.slice();
				try {
					strBuf.limit(len);
				} catch (IllegalArgumentException e) {
					throw new IOException(e.getMessage());
				}
				try {
					CharBuffer chars = decoder.decode(strBuf);
					result = chars.toString();
				} catch (CharacterCodingException e) {
					throw new IOException(e.getMessage());
				}
				try {
					buf.position(buf.position() + len);
				} catch (IllegalArgumentException e) {
					throw new IOException(e.getMessage());
				}
			}
			if ((flags & FLAG_NOSTORE) == 0) {
				stringTable.put(result);
			}
		} else {
			/*
			 * String table entry
			 */
			result = stringTable.get((flags & ID_MASK) - 1);
		}
		return result;
	}

	/**
	 * Convert a proxy representation of a string into a Java String
	 * 
	 * @param buf
	 * @param start
	 * @return proxy string converted to Java String
	 */
	public static String decodeString(CharBuffer buf, int start) {
		int end = start + ProxyPacket.PACKET_ARG_LEN_SIZE;
		int len = Integer.parseInt(buf.subSequence(start, end).toString(), 16);
		start = end + 1; // Skip ':'
		end = start + len;
		return buf.subSequence(start, end).toString();
	}

	/**
	 * Convert an integer to it's proxy representation
	 * 
	 * @param val
	 * @param len
	 * @return proxy representation
	 */
	public static String encodeIntVal(int val, int len) {
		char[] res = new char[len];
		String str = Integer.toHexString(val);
		int rem = len - str.length();

		for (int i = 0; i < len; i++) {
			if (i < rem) {
				res[i] = '0';
			} else {
				res[i] = str.charAt(i - rem);
			}
		}
		return String.valueOf(res);
	}

	/**
	 * Encode a string into it's proxy representation
	 * 
	 * @param str
	 * @return proxy representation
	 */
	public static String encodeString(String str) {
		int len;

		if (str == null) {
			len = 0;
			str = ""; //$NON-NLS-1$
		} else {
			len = str.length();
		}

		return encodeIntVal(len, ProxyPacket.PACKET_ARG_LEN_SIZE) + ":" + str; //$NON-NLS-1$	
	}

}
