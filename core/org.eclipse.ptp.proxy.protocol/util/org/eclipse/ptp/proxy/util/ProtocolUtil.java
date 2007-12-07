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

import java.math.BigInteger;
import java.nio.CharBuffer;

import org.eclipse.ptp.proxy.packet.ProxyPacket;

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
		if (address.startsWith("-")) {
			negative = true;
			index++;
		}
		if (address.startsWith("0x", index) || address.startsWith("0X", index)) {
			index += 2;
			radix = 16;
		} else if (address.startsWith("#", index)) {
			index ++;
			radix = 16;
		} else if (address.startsWith("0", index) && address.length() > 1 + index) {
			index ++;
			radix = 8;
		}

		if (index > 0) {
			address = address.substring(index);
		}
		if (negative) {
			address = "-" + address;
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
	public static byte[] decodeBytes(String str) {
		int len = str.length()/2;
		byte[] strBytes = new byte[len];
		
		for (int i = 0, p = 0; i < len; i++, p += 2) {
			byte c = (byte) ((Character.digit(str.charAt(p), 16) & 0xf) << 4);
			c |= (byte) ((Character.digit(str.charAt(p+1), 16) & 0xf));
			strBytes[i] = c;
		}
		
		return strBytes;
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
		
		for (int i = 0 ; i < len ; i++) {
			if (i < rem)
				res[i] = '0';
			else
				res[i] = str.charAt(i - rem);
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
			str = "";
		} else {
			len = str.length();
		}
		
		return encodeIntVal(len, ProxyPacket.PACKET_ARG_LEN_SIZE) + ":" + str;		
	}	

}
