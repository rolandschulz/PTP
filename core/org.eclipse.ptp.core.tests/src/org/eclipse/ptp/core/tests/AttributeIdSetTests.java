/*******************************************************************************
* Copyright (c) 2010 Los Alamos National Laboratory and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* 	LANL - Initial API and implementation
*******************************************************************************/

package org.eclipse.ptp.core.tests;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.AttributeIdSet;


/**
 * @author Randy M. Roberts
 *
 */
public class AttributeIdSetTests extends TestCase {
	
	private static void assertComplete(AttributeIdSet<IntegerAttribute> idSet) {
		BitSet expected = idSet.getIdSet();
		BitSet actual = new BitSet();
		for (IntegerAttribute i1 : idSet.getAttributes()) {
			BitSet bs1 = idSet.getIdSet(i1);
			actual.or(bs1);
		}
		assertEquals(expected, actual);
	}
	
	private static void assertDisjoint(AttributeIdSet<IntegerAttribute> idSet) {
		for (IntegerAttribute i1 : idSet.getAttributes()) {
			BitSet bs1 = idSet.getIdSet(i1);
			for (IntegerAttribute i2 : idSet.getAttributes()) {
				if (i1.equals(i2)) {
					continue;
				}
				BitSet bs2 = idSet.getIdSet(i2);
				boolean intersects = bs1.intersects(bs2);
				if (intersects) {
					fail(bs1 + " and " + bs2 + " are not disjoint id sets");
				}
			}
		}
	}

	/**
	 * @param bitset
	 * @param is
	 */
	private static void set(BitSet bitset, int[] is) {
		for (int i : is) {
			bitset.set(i);
		}
	}
	
	private IntegerAttributeDefinition definition;
	
	private AttributeIdSet<IntegerAttribute> testingIdSet;
	
	@Override
	public void setUp() {
		try {
			testingIdSet = new AttributeIdSet<IntegerAttribute>();
			final BitSet ids = new BitSet();
			definition = new IntegerAttributeDefinition("xxx", "name", "description", true, -42);
			IntegerAttribute attr;
			
			attr = definition.create(1);
			ids.set(0, 5);
			ids.set(100, 105);
			testingIdSet.setAttribute(attr, ids);
			
			attr = definition.create(3);
			ids.clear();
			ids.set(200, 205);
			ids.set(300, 305);
			testingIdSet.setAttribute(attr, ids);
			
			attr = definition.create(5);
			ids.clear();
			set(ids, new int[]{1,101,201,301,401});
			testingIdSet.setAttribute(attr, ids);
			
		} catch (IllegalValueException e) {
			fail(e.getMessage());
		}
	}
	
	@Override
	public void tearDown() {
		testingIdSet = null;
		definition = null;
	}
	
	public void testClearAttributes() throws IllegalValueException {
		BitSet clearedSet = new BitSet();
		set(clearedSet, new int[]{2,102,202,302,402});
		testingIdSet.clearAttributes(clearedSet);
		
		assertDisjoint(testingIdSet);
		assertComplete(testingIdSet);

		BitSet expected;
		BitSet actual;

		expected = new BitSet();
		set(expected, new int[]{0, 1, 3, 4, 100, 101, 103, 104, 200, 201, 203, 204, 300, 301, 303, 304, 401});
		actual = testingIdSet.getIdSet();
		assertEquals(expected, actual);
		
		expected = new BitSet();
		set(expected, new int[]{0, 3, 4, 100, 103, 104});
		actual = getIdSet(testingIdSet, 1);
		assertEquals(expected, actual);
		
		expected = new BitSet();
		set(expected, new int[]{200, 203, 204, 300, 303, 304});
		actual = getIdSet(testingIdSet, 3);
		assertEquals(expected, actual);
		
		expected = new BitSet();
		set(expected, new int[]{1, 101, 201, 301, 401});
		actual = getIdSet(testingIdSet, 5);
		assertEquals(expected, actual);
		
		// let's do another clearAttributes
		clearedSet.clear();
		clearedSet.set(200, 400);
		testingIdSet.clearAttributes(clearedSet);
		
		assertDisjoint(testingIdSet);
		assertComplete(testingIdSet);

		expected.clear();
		set(expected, new int[]{0, 1, 3, 4, 100, 101, 103, 104, 401});
		actual = testingIdSet.getIdSet();
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{0, 3, 4, 100, 103, 104});
		actual = getIdSet(testingIdSet, 1);
		assertEquals(expected, actual);
		
		actual = getIdSet(testingIdSet, 3);
		assertTrue(actual.isEmpty());
		
		expected = new BitSet();
		set(expected, new int[]{1, 101, 401});
		actual = getIdSet(testingIdSet, 5);
		assertEquals(expected, actual);
		
		Set<IntegerAttribute> actualAttrs = testingIdSet.getAttributes();
		Set<IntegerAttribute> expectedAttrs = new HashSet<IntegerAttribute>(
				Arrays.asList(definition.create(1),	definition.create(5)));
		assertEquals(expectedAttrs, actualAttrs);
	}
	
	public void testGetAttributes() throws IllegalValueException {
		Set<IntegerAttribute> expected = new HashSet<IntegerAttribute>();
		expected.add(definition.create(1));
		expected.add(definition.create(3));
		expected.add(definition.create(5));
		Set<IntegerAttribute> actual = testingIdSet.getAttributes();
		assertEquals(expected, actual);
	}
	
	public void testGetAtttribute() throws IllegalValueException {
		IntegerAttribute ia;
		ia = testingIdSet.getAttribute(1);		
		assertEquals(definition.create(5), ia);
		
		ia = testingIdSet.getAttribute(2);		
		assertEquals(definition.create(1), ia);
		
		ia = testingIdSet.getAttribute(500);		
		assertNull(ia);
	}

	public void testSubset() throws IllegalValueException {
		final BitSet expected = new BitSet();
		BitSet actual;

		BitSet subSet = new BitSet();
		subSet.set(3, 5);
		subSet.set(103, 201);
		AttributeIdSet<IntegerAttribute> results = testingIdSet.getSubset(subSet);

		assertDisjoint(results);
		assertComplete(results);

		set(expected, new int[]{3,4,103,104,200});
		actual = results.getIdSet();
		assertEquals(expected, actual);

		expected.clear();
		set(expected, new int[]{3,4,103,104});
		actual = getIdSet(results, 1);
		assertEquals(expected, actual);

		expected.clear();
		set(expected, new int[]{200});
		actual = getIdSet(results, 3);
		assertEquals(expected, actual);

		actual = getIdSet(results, 5);
		assertTrue(actual.isEmpty());
	}

	public void testValidSetupByAttr1() throws IllegalValueException {
		BitSet expected = new BitSet();
		set(expected, new int[]{0, 2, 3, 4, 100, 102, 103, 104});
		
		BitSet actual = getIdSet(testingIdSet, 1);
		assertEquals(expected, actual);
	}
	
	public void testValidSetupByAttr2() throws IllegalValueException {
		BitSet actual = getIdSet(testingIdSet, 2);
		assertTrue(actual.isEmpty());
	}
	
	public void testValidSetupByAttr3() throws IllegalValueException {
		BitSet expected = new BitSet();
		set(expected, new int[]{200, 202, 203, 204, 300, 302, 303, 304});
		
		BitSet actual = getIdSet(testingIdSet, 3);
		assertEquals(expected, actual);
	}

	public void testValidSetupByAttr5() throws IllegalValueException {
		BitSet expected = new BitSet();
		set(expected, new int[]{1,101,201,301,401});
		
		BitSet actual = getIdSet(testingIdSet, 5);
		assertEquals(expected, actual);
	}

	public void testValidSetupIsComplete() {
		assertComplete(testingIdSet);
	}

	public void testValidSetupIsDisjoint() {
		assertDisjoint(testingIdSet);
	}
	
	public void testValidSetupTotalBitSet() {
		BitSet expected = new BitSet();
		set(expected, new int[]{0, 1, 2, 3, 4, 100, 101, 102, 103, 104, 200, 201, 202, 203, 204, 300, 301, 302, 303, 304, 401});
		assertEquals(expected, testingIdSet.getIdSet());
	}
	
	private BitSet getIdSet(AttributeIdSet<IntegerAttribute> idSet, int value) throws IllegalValueException {
		return idSet.getIdSet(definition.create(value));
	}
}
