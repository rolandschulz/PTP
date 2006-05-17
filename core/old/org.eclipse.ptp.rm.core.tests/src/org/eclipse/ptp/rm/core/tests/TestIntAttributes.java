/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.rm.core.tests;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.core.attributes.IAttrDesc;
import org.eclipse.ptp.rm.core.attributes.IAttribute;
import org.eclipse.ptp.rm.core.attributes.IntAttrDesc;


public class TestIntAttributes extends TestCase {

	/*
	 * Test method for 'org.eclipse.ptp.resourcemanager.attributes.IntAttrDesc.IntAttrDesc(String, String)'
	 */
	public void testIntAttrDesc() {
		IntAttrDesc subject = new IntAttrDesc("dog", "The dog is a canine");
		assertEquals("dog", subject.getName());
		assertEquals("The dog is a canine", subject.getDescription());
	}

	public void testEquals() {
		IntAttrDesc s1 = new IntAttrDesc("dog", "The dog is a canine");
		IntAttrDesc s1a = new IntAttrDesc("dog", "The dog is a canine");
		IntAttrDesc s2 = new IntAttrDesc("doggy", "The dog is a canine");
		IntAttrDesc s3 = new IntAttrDesc("dog", "The dog is a canine for sure");
		
		assertTrue(s1.equals(s1));
		assertTrue(s1.equals(s1a));
		assertTrue(s1a.equals(s1));
		assertFalse(s1.equals(s2));
		assertFalse(s2.equals(s1));
		
		// equals only on name
		assertTrue(s1.equals(s3));
		assertTrue(s3.equals(s1));
	}
	
	/*
	 * Test method for 'org.eclipse.ptp.resourcemanager.attributes.AbstractAttrDesc.compareTo(Object)'
	 */
	public void testCompareTo() {
		IntAttrDesc s1 = new IntAttrDesc("dog", "The dog is a canine");
		IntAttrDesc s1a = new IntAttrDesc("dog", "The dog is a canine");
		IntAttrDesc s2 = new IntAttrDesc("doggy", "The dog is a canine");
		IntAttrDesc s3 = new IntAttrDesc("dog", "The dog is a canine for sure");

		int s1Cs1 = s1.compareTo(s1);
		assertEquals(0, s1Cs1);
		
		int s1Cs1a = s1.compareTo(s1a);
		assertEquals(0, s1Cs1a);

		int s1Cs2 = s1.compareTo(s2);
		// compares only on name, not description
		assertEquals(-1, s1Cs2);
		
		int s2Cs1 = s2.compareTo(s1);
		assertEquals(1, s2Cs1);

		int s1Cs3 = s1.compareTo(s3);
		// compares only on name, not description
		assertEquals(0, s1Cs3);
		
		int s3Cs1 = s3.compareTo(s1);
		// compares only on name, not description
		assertEquals(0, s3Cs1);
	}

	/*
	 * Test method for 'org.eclipse.ptp.resourcemanager.attributes.AbstractAttrDesc.createAttribute(String)'
	 */
	public void testCreateAttributeString() {
		IAttrDesc d1 = new IntAttrDesc("dog", "A dog is a canine");
		IAttribute a1 = d1.createAttribute("42");
		assertEquals("42", a1.toString());
	}

	/*
	 * Test method for 'org.eclipse.ptp.resourcemanager.attributes.AbstractAttrDesc.createAttribute(String[])'
	 */
	public void testCreateAttributeStringArray() {
		IAttrDesc d1 = new IntAttrDesc("dog", "A dog is a canine");
		IAttribute a1 = d1.createAttribute(new String[]{"42", "123", "75", "42"});
		assertEquals("(42 75 123)", a1.toString());
	}
	
	public void testIntAttributeComparisons() {
		
		// Lexigraphical comparisons of integer attributes.
		
		IAttrDesc d1 = new IntAttrDesc("dog", "A dog is a canine");
		IAttrDesc d1a = new IntAttrDesc("dog", "A dog is a canine");

		IAttribute a1 = d1.createAttribute(new String[]{"42", "123", "75", "42"});
		
		IAttribute a1a = d1.createAttribute(new String[]{"123", "75", "42"});
		assertEquals(0, a1.compareTo(a1a));
		assertTrue(a1.equals(a1a));
		assertTrue(a1a.equals(a1));
		
		IAttribute a1d1a = d1a.createAttribute(new String[]{"123", "75", "42"});
		assertEquals(0, a1.compareTo(a1d1a));
		assertTrue(a1.equals(a1d1a));
		assertTrue(a1d1a.equals(a1));

		IAttrDesc d2 = new IntAttrDesc("doggy", "A dog is a canine");
		IAttribute a1d2 = d2.createAttribute(new String[]{"123", "75", "42"});
		assertEquals(-1, a1.compareTo(a1d2));
		assertEquals(1, a1d2.compareTo(a1));
		assertFalse(a1.equals(a1d2));
		assertFalse(a1d2.equals(a1));

		IAttribute a2 = d1.createAttribute(new String[]{"420", "123", "75", "420"});
		assertEquals(-1, a1.compareTo(a2));
		assertEquals(1, a2.compareTo(a1));
		assertFalse(a1.equals(a2));
		assertFalse(a2.equals(a1));

		IAttribute a3 = d1.createAttribute(new String[]{"42", "123", "75", "800"});
		assertEquals(-1, a1.compareTo(a3));
		assertEquals(1, a3.compareTo(a1));
		assertFalse(a1.equals(a3));
		assertFalse(a3.equals(a1));
		
		IAttribute a4 = d1.createAttribute("41");
		assertEquals(1, a1.compareTo(a4));
		assertEquals(-1, a4.compareTo(a1));
		assertFalse(a1.equals(a4));
		assertFalse(a4.equals(a1));

		IAttribute a5 = d1.createAttribute("42");
		assertEquals(1, a1.compareTo(a5));
		assertEquals(-1, a5.compareTo(a1));
		assertFalse(a1.equals(a5));

		IAttribute a6 = d1.createAttribute("43");
		assertEquals(-1, a1.compareTo(a6));
		assertEquals(1, a6.compareTo(a1));
		assertFalse(a1.equals(a6));
		assertFalse(a6.equals(a1));
	}

}
