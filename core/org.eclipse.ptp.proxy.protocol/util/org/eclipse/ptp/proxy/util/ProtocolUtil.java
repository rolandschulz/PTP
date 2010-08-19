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
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.util.messages.Messages;

public class ProtocolUtil {
	/**
	 * @since 5.0
	 */
	public final static int TYPE_STRING_ATTR = 0;
	/**
	 * @since 5.0
	 */
	public final static int TYPE_INTEGER = 1;
	/**
	 * @since 5.0
	 */
	public final static int TYPE_BITSET = 2;
	/**
	 * @since 5.0
	 */
	public final static int TYPE_STRING = 3;
	/**
	 * @since 5.0
	 */
	public final static int TYPE_INTEGER_ATTR = 4;
	/**
	 * @since 5.0
	 */
	public final static int TYPE_BOOLEAN_ATTR = 5;

	/**
	 * Decode a string into a BigInteger
	 * 
	 * @param address
	 * @return BigInteger
	 */
	@Deprecated
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
	 * Convert as sequence of hexadecimal values to a Java byte array.
	 * 
	 * @param str
	 * @return byte array
	 */
	@Deprecated
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
	 * Convert a proxy representation of a string to a String. A string is
	 * represented by a length in varint format followed by the string
	 * characters. If the characters are multibyte, then the length will reflect
	 * this. If length is 0, then the string is null and should be skipped,
	 * otherswise length = strlen(string) + 1;
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
	public static String decodeString(ByteBuffer buf, CharsetDecoder decoder) throws IOException {
		String result = null;
		VarInt strLen = new VarInt(buf);
		if (!strLen.isValid()) {
			throw new IOException(Messages.getString("ProtocolUtil.0")); //$NON-NLS-1$
		}
		int len = strLen.getValue() - 1;
		if (len >= 0) {
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
		return result;
	}

	/**
	 * Convert a proxy representation of a string into a Java String
	 * 
	 * @param buf
	 * @param start
	 * @return proxy string converted to Java String
	 */
	@Deprecated
	public static String decodeString(CharBuffer buf, int start) {
		int end = start + ProxyPacket.PACKET_ARG_LEN_SIZE;
		int len = Integer.parseInt(buf.subSequence(start, end).toString(), 16);
		start = end + 1; // Skip ':'
		end = start + len;
		return buf.subSequence(start, end).toString();
	}

	/**
	 * Convert a proxy representation of a string attribute into a Java String.
	 * Assumes that the type byte as been removed from the buffer.
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
	public static String decodeStringAttributeType(ByteBuffer buf, CharsetDecoder decoder) throws IOException {
		String result = ""; //$NON-NLS-1$

		String key = decodeString(buf, decoder);
		if (key != null) {
			result = key;
		}
		String value = decodeString(buf, decoder);
		if (value != null) {
			if (result.length() > 0) {
				result += "="; //$NON-NLS-1$
			}
			result += value;
		}
		return result;
	}

	/**
	 * Convert an integer to it's proxy representation
	 * 
	 * @param val
	 * @param len
	 * @return proxy representation
	 */
	@Deprecated
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
	@Deprecated
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

	/**
	 * Encode a string attribute type and place the encoded bytes in buffer
	 * list.
	 * 
	 * @param bufs
	 *            list of buffers containing encoded string
	 * @param attribute
	 *            string attribute
	 * @param charset
	 *            charset to map characters into bytes
	 * @throws IOException
	 * @since 5.0
	 */
	public static void encodeStringAttributeType(List<ByteBuffer> bufs, String attribute, Charset charset) throws IOException {
		String[] kv = attribute.split("="); //$NON-NLS-1$
		bufs.add(encodeType(TYPE_STRING_ATTR));
		encodeString(bufs, kv[0], charset);
		if (kv.length == 1) {
			bufs.add(new VarInt(0).getBytes());
		} else {
			encodeString(bufs, kv[1], charset);
		}
	}

	/**
	 * Encode a string type and place the encoded bytes in buffer list.
	 * 
	 * @param bufs
	 *            list of buffers containing encoded string
	 * @param str
	 *            string to encode
	 * @param charset
	 *            charset to map characters into bytes
	 * @since 5.0
	 */
	public static void encodeStringType(List<ByteBuffer> bufs, String str, Charset charset) {
		bufs.add(encodeType(TYPE_STRING));
		encodeString(bufs, str, charset);
	}

	/**
	 * Encode a string and place the encoded bytes in buffer list.
	 * 
	 * @param bufs
	 *            list of buffers containing encoded string
	 * @param str
	 *            string to encode
	 * @param charset
	 *            charset to map characters into bytes
	 */
	private static void encodeString(List<ByteBuffer> bufs, String str, Charset charset) {
		bufs.add(new VarInt(str.length()).getBytes());
		try {
			bufs.add(ByteBuffer.wrap(str.getBytes(charset.name())));
		} catch (UnsupportedEncodingException e) {
			bufs.add(ByteBuffer.wrap(str.getBytes()));
		}
	}

	/**
	 * Encode an attribute type in a byte buffer
	 * 
	 * @param type
	 * @return
	 */
	private static ByteBuffer encodeType(int type) {
		ByteBuffer b = ByteBuffer.allocate(1).put((byte) type);
		b.rewind();
		return b;
	}

}
