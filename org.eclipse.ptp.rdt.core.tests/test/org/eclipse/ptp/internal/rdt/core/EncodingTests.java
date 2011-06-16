/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core;

import java.util.Arrays;

import junit.framework.TestCase;


public class EncodingTests extends TestCase {
	public void testFullRange() 
	{
		byte[] data = new byte[256];
		for (int i = 0; i < data.length; i++) {
			data[0] = (byte) (i - 128);
		}
		String encoded = Serializer.encodeBase64(data, 0, data.length);
		byte[] decoded = Serializer.decodeBase64(encoded);
		assertTrue(Arrays.equals(data, decoded));
	}

	public void testZeroPad() 
	{
		byte[] data = new byte[] { 1, 2, 3, };
		String encoded = Serializer.encodeBase64(data, 0, data.length);
		byte[] decoded = Serializer.decodeBase64(encoded);
		assertTrue(Arrays.equals(data, decoded));
	}
	
	public void testOnePad() 
	{
		byte[] data = new byte[] { 1, 2, };
		String encoded = Serializer.encodeBase64(data, 0, data.length);
		byte[] decoded = Serializer.decodeBase64(encoded);
		assertTrue(Arrays.equals(data, decoded));
	}

	public void testTwoPad() 
	{
		byte[] data = new byte[] { 1 };
		String encoded = Serializer.encodeBase64(data, 0, data.length);
		byte[] decoded = Serializer.decodeBase64(encoded);
		assertTrue(Arrays.equals(data, decoded));
	}
	
	public void testZeroPad2() 
	{
		byte[] data = new byte[] { -84, -19, 0 };
		String encoded = Serializer.encodeBase64(data, 0, data.length);
		byte[] decoded = Serializer.decodeBase64(encoded);
		assertTrue(Arrays.equals(data, decoded));
	}
	
}
