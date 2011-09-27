/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Serializer {
	static final char[] BASE64_ALPHABET = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
	};

	static final byte[] BASE64_ALPHABET_INVERSE = {
		62, 0, 0, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0,
		0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
		10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
		0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
		36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
	};

	public static String serialize(Object o) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		GZIPOutputStream zipStream = new GZIPOutputStream(buffer);
		ObjectOutputStream out = new ObjectOutputStream(zipStream);
		out.writeObject(o);
		out.close();
		return encodeBase64(buffer.toByteArray(), 0, buffer.size());
	}
	/**
	 * To deserialize an object from other osgi bundle class loader, i.e. class from other plugin
	 * @param data
	 * @param cl
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserialize(String data, final ClassLoader cl) throws IOException, ClassNotFoundException{
		if(cl!=null){
			
			byte[] buffer = decodeBase64(data);
			ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
			GZIPInputStream zipStream = new GZIPInputStream(stream);
			ObjectInputStream in = new ObjectInputStream(zipStream){
				public Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
						ClassNotFoundException {
				
					try{
						return cl.loadClass(desc.getName());
					}catch(Exception e){
						
					}
					return super.resolveClass(desc);
				}
			};
			
			return in.readObject();
			
		}else{
			return deserialize(data);
		}
	}
	
	public static Object deserialize(String data) throws IOException, ClassNotFoundException {
		byte[] buffer = decodeBase64(data);
		ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
		GZIPInputStream zipStream = new GZIPInputStream(stream);
		ObjectInputStream in = new ObjectInputStream(zipStream);
		return in.readObject();
	}

	static String encodeBase64(byte[] data, int offset, int length) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		StringBuilder buffer = new StringBuilder();
		int remainder = length % 3;
		int i;
		for (i = offset; i < offset + length - remainder; i += 3) {
			int value = (in.read() << 16) + (in.read() << 8) + in.read();
			buffer.append(BASE64_ALPHABET[(value >>> 18) & 0x3f]);
			buffer.append(BASE64_ALPHABET[(value >>> 12) & 0x3f]);
			buffer.append(BASE64_ALPHABET[(value >>> 6) & 0x3f]);
			buffer.append(BASE64_ALPHABET[value & 0x3f]);
		}
		switch (remainder) {
		case 2:
			int value = (in.read() << 16) + (in.read() << 8);
			buffer.append(BASE64_ALPHABET[(value >>> 18) & 0x3f]);
			buffer.append(BASE64_ALPHABET[(value >>> 12) & 0x3f]);
			buffer.append(BASE64_ALPHABET[(value >>> 6) & 0x3f]);
			buffer.append('=');
			break;
		case 1:
			value = (in.read() << 16);
			buffer.append(BASE64_ALPHABET[(value >>> 18) & 0x3f]);
			buffer.append(BASE64_ALPHABET[(value >>> 12) & 0x3f]);
			buffer.append('=');
			buffer.append('=');
			break;
		}
		return buffer.toString();
	}

	static byte[] decodeBase64(String data) {
		int paddingLength = 0;
		for (int i = 1; i < 3; i++) {
			if (data.charAt(data.length() - i) == '=') {
				paddingLength++;
			} else {
				break;
			}
		}
		int length = (data.length() / 4 * 3) - paddingLength;
		int bytesDecoded = 0;
		byte[] buffer = new byte[length];
		int i;
		int charactersToRead = paddingLength > 0 ? data.length() - 4 : data.length();
		for (i = 0; i < charactersToRead; i+= 4) {
			int value = (BASE64_ALPHABET_INVERSE[data.charAt(i) - 43] << 18)
				+ (BASE64_ALPHABET_INVERSE[data.charAt(i + 1) - 43] << 12)
				+ (BASE64_ALPHABET_INVERSE[data.charAt(i + 2) - 43] << 6)
				+ BASE64_ALPHABET_INVERSE[data.charAt(i + 3) - 43];
			buffer[bytesDecoded++] = Integer.valueOf((value >>> 16) & 0xff).byteValue();
			buffer[bytesDecoded++] = Integer.valueOf((value >>> 8) & 0xff).byteValue();
			buffer[bytesDecoded++] = Integer.valueOf(value & 0xff).byteValue();
		} 
		switch (paddingLength) {
		case 2:
			int value = (BASE64_ALPHABET_INVERSE[data.charAt(i) - 43] << 18)
				+ (BASE64_ALPHABET_INVERSE[data.charAt(i + 1) - 43] << 12);
			buffer[bytesDecoded++] = (byte) ((value >>> 16) & 0xff);
			break;
		case 1:
			value = (BASE64_ALPHABET_INVERSE[data.charAt(i) - 43] << 18)
				+ (BASE64_ALPHABET_INVERSE[data.charAt(i + 1) - 43] << 12)
				+ (BASE64_ALPHABET_INVERSE[data.charAt(i + 2) - 43] << 6);
			buffer[bytesDecoded++] = (byte) ((value >>> 16) & 0xff);
			buffer[bytesDecoded++] = (byte) ((value >>> 8) & 0xff);
			break;
		}
		return buffer;
	}
}
