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
import org.eclipse.ptp.core.elements.attributes.AttributeIndexSet;


/**
 * @author Randy M. Roberts
 *
 */
public class AttributeIndexSetTests extends TestCase {
	
	private static void assertComplete(AttributeIndexSet<IntegerAttribute> indexSet) {
		BitSet expected = indexSet.getIndexSet();
		BitSet actual = new BitSet();
		for (IntegerAttribute i1 : indexSet.getAttributes()) {
			BitSet bs1 = indexSet.getIndexSet(i1);
			actual.or(bs1);
		}
		assertEquals(expected, actual);
	}
	
	private static void assertDisjoint(AttributeIndexSet<IntegerAttribute> indexSet) {
		for (IntegerAttribute i1 : indexSet.getAttributes()) {
			BitSet bs1 = indexSet.getIndexSet(i1);
			for (IntegerAttribute i2 : indexSet.getAttributes()) {
				if (i1.equals(i2)) {
					continue;
				}
				BitSet bs2 = indexSet.getIndexSet(i2);
				boolean intersects = bs1.intersects(bs2);
				if (intersects) {
					fail(bs1 + " and " + bs2 + " are not disjoint index sets");
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
	
	private AttributeIndexSet<IntegerAttribute> testingIndexSet;
	
	@Override
	public void setUp() {
		try {
			testingIndexSet = new AttributeIndexSet<IntegerAttribute>();
			final BitSet indices = new BitSet();
			definition = new IntegerAttributeDefinition("xxx", "name", "description", true, -42);
			IntegerAttribute attr;
			
			attr = definition.create(1);
			indices.set(0, 5);
			indices.set(100, 105);
			testingIndexSet.setIndicesOfAttribute(attr, indices);
			
			attr = definition.create(3);
			indices.clear();
			indices.set(200, 205);
			indices.set(300, 305);
			testingIndexSet.setIndicesOfAttribute(attr, indices);
			
			attr = definition.create(5);
			indices.clear();
			set(indices, new int[]{1,101,201,301,401});
			testingIndexSet.setIndicesOfAttribute(attr, indices);
			
		} catch (IllegalValueException e) {
			fail(e.getMessage());
		}
	}
	
	@Override
	public void tearDown() {
		testingIndexSet = null;
		definition = null;
	}
	
	public void testAndAttrs() throws IllegalValueException {
		IntegerAttribute attr = definition.create(1);
		BitSet newIndices = new BitSet();
		newIndices.set(99, 103);
		testingIndexSet.addIndicesToAttribute(attr, newIndices);
		
		assertDisjoint(testingIndexSet);
		assertComplete(testingIndexSet);

		BitSet expected;
		BitSet actual;

		expected = new BitSet();
		set(expected, new int[]{0, 1, 2, 3, 4, 99, 100, 101, 102, 103, 104, 200, 201, 202, 203, 204, 300, 301, 302, 303, 304, 401});
		actual = testingIndexSet.getIndexSet();
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{0, 2, 3, 4, 99, 100, 101, 102, 103, 104});
		actual = getIndexSet(testingIndexSet, 1);
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{200, 202, 203, 204, 300, 302, 303, 304});
		actual = getIndexSet(testingIndexSet, 3);
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{1,201,301,401});
		actual = getIndexSet(testingIndexSet, 5);
		assertEquals(expected, actual);		
	}
	
	public void testClearIndices() throws IllegalValueException {
		BitSet clearedSet = new BitSet();
		set(clearedSet, new int[]{2,102,202,302,402,1000});
		testingIndexSet.clearIndices(clearedSet);
		
		assertDisjoint(testingIndexSet);
		assertComplete(testingIndexSet);

		BitSet expected;
		BitSet actual;

		expected = new BitSet();
		set(expected, new int[]{0, 1, 3, 4, 100, 101, 103, 104, 200, 201, 203, 204, 300, 301, 303, 304, 401});
		actual = testingIndexSet.getIndexSet();
		assertEquals(expected, actual);
		
		expected = new BitSet();
		set(expected, new int[]{0, 3, 4, 100, 103, 104});
		actual = getIndexSet(testingIndexSet, 1);
		assertEquals(expected, actual);
		
		expected = new BitSet();
		set(expected, new int[]{200, 203, 204, 300, 303, 304});
		actual = getIndexSet(testingIndexSet, 3);
		assertEquals(expected, actual);
		
		expected = new BitSet();
		set(expected, new int[]{1, 101, 201, 301, 401});
		actual = getIndexSet(testingIndexSet, 5);
		assertEquals(expected, actual);
		
		// let's do another clearAttributes
		clearedSet.clear();
		clearedSet.set(200, 400);
		testingIndexSet.clearIndices(clearedSet);
		
		assertDisjoint(testingIndexSet);
		assertComplete(testingIndexSet);

		expected.clear();
		set(expected, new int[]{0, 1, 3, 4, 100, 101, 103, 104, 401});
		actual = testingIndexSet.getIndexSet();
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{0, 3, 4, 100, 103, 104});
		actual = getIndexSet(testingIndexSet, 1);
		assertEquals(expected, actual);
		
		actual = getIndexSet(testingIndexSet, 3);
		assertTrue(actual.isEmpty());
		
		expected = new BitSet();
		set(expected, new int[]{1, 101, 401});
		actual = getIndexSet(testingIndexSet, 5);
		assertEquals(expected, actual);
		
		Set<IntegerAttribute> actualAttrs = testingIndexSet.getAttributes();
		Set<IntegerAttribute> expectedAttrs = new HashSet<IntegerAttribute>(
				Arrays.asList(definition.create(1),	definition.create(5)));
		assertEquals(expectedAttrs, actualAttrs);
	}
	
	public void testClearIndicesForAttribute() throws IllegalValueException {
		IntegerAttribute attr = definition.create(1);
		BitSet clearedSet = new BitSet();
		// 300 is not part of attr 1's set, so it shouldn't be cleared
		set(clearedSet, new int[]{3, 4, 100, 102, 300, 1000});
		testingIndexSet.clearIndicesForAttribute(attr, clearedSet);
		
		assertDisjoint(testingIndexSet);
		assertComplete(testingIndexSet);

		BitSet expected;
		BitSet actual;

		expected = new BitSet();
		// notice 300 is still here
		set(expected, new int[]{0, 1, 2, 101, 103, 104, 200, 201, 202, 203, 204, 300, 301, 302, 303, 304, 401});
		actual = testingIndexSet.getIndexSet();
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{0, 2, 103, 104});
		actual = getIndexSet(testingIndexSet, 1);
		assertEquals(expected, actual);

		actual = getIndexSet(testingIndexSet, 2);
		assertTrue(actual.isEmpty());

		expected = new BitSet();
		// notice 300 is still here
		set(expected, new int[]{200, 202, 203, 204, 300, 302, 303, 304});
		actual = getIndexSet(testingIndexSet, 3);
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{1,101,201,301,401});
		actual = getIndexSet(testingIndexSet, 5);
		assertEquals(expected, actual);		
	}
	
	public void testGetAttributes() throws IllegalValueException {
		Set<IntegerAttribute> expected = new HashSet<IntegerAttribute>();
		expected.add(definition.create(1));
		expected.add(definition.create(3));
		expected.add(definition.create(5));
		Set<IntegerAttribute> actual = testingIndexSet.getAttributes();
		assertEquals(expected, actual);
	}
	
	public void testGetAtttribute() throws IllegalValueException {
		IntegerAttribute ia;
		ia = testingIndexSet.getAttribute(1);		
		assertEquals(definition.create(5), ia);
		
		ia = testingIndexSet.getAttribute(2);		
		assertEquals(definition.create(1), ia);
		
		ia = testingIndexSet.getAttribute(500);		
		assertNull(ia);
	}

	public void testSetAttrsSubset() throws IllegalValueException {
		IntegerAttribute attr = definition.create(1);
		BitSet newIndices = new BitSet();
		newIndices.set(101, 103);
		testingIndexSet.setIndicesOfAttribute(attr, newIndices);
		
		assertDisjoint(testingIndexSet);
		assertComplete(testingIndexSet);

		BitSet expected;
		BitSet actual;

		expected = new BitSet();
		set(expected, new int[]{1, 101, 102, 200, 201, 202, 203, 204, 300, 301, 302, 303, 304, 401});
		actual = testingIndexSet.getIndexSet();
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{101, 102});
		actual = getIndexSet(testingIndexSet, 1);
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{200, 202, 203, 204, 300, 302, 303, 304});
		actual = getIndexSet(testingIndexSet, 3);
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{1,201,301,401});
		actual = getIndexSet(testingIndexSet, 5);
		assertEquals(expected, actual);		
	}

	public void testSubset() throws IllegalValueException {
		final BitSet expected = new BitSet();
		BitSet actual;

		BitSet subSet = new BitSet();
		subSet.set(3, 5);
		subSet.set(103, 201);
		AttributeIndexSet<IntegerAttribute> results = testingIndexSet.getSubset(subSet);

		assertDisjoint(results);
		assertComplete(results);

		set(expected, new int[]{3,4,103,104,200});
		actual = results.getIndexSet();
		assertEquals(expected, actual);

		expected.clear();
		set(expected, new int[]{3,4,103,104});
		actual = getIndexSet(results, 1);
		assertEquals(expected, actual);

		expected.clear();
		set(expected, new int[]{200});
		actual = getIndexSet(results, 3);
		assertEquals(expected, actual);

		actual = getIndexSet(results, 5);
		assertTrue(actual.isEmpty());
	}

	public void testTotalClearAttribute() throws IllegalValueException {
		IntegerAttribute attr = definition.create(1);
		testingIndexSet.clearAttribute(attr);
		
		assertDisjoint(testingIndexSet);
		assertComplete(testingIndexSet);

		BitSet expected;
		BitSet actual;

		expected = new BitSet();
		set(expected, new int[]{1, 101, 200, 201, 202, 203, 204, 300, 301, 302, 303, 304, 401});
		actual = testingIndexSet.getIndexSet();
		assertEquals(expected, actual);

		actual = getIndexSet(testingIndexSet, 1);
		assertTrue(actual.isEmpty());

		expected = new BitSet();
		set(expected, new int[]{200, 202, 203, 204, 300, 302, 303, 304});
		actual = getIndexSet(testingIndexSet, 3);
		assertEquals(expected, actual);

		expected = new BitSet();
		set(expected, new int[]{1,101,201,301,401});
		actual = getIndexSet(testingIndexSet, 5);
		assertEquals(expected, actual);		
	}

	public void testValIndexSetupByAttr1() throws IllegalValueException {
		BitSet expected = new BitSet();
		set(expected, new int[]{0, 2, 3, 4, 100, 102, 103, 104});
		
		BitSet actual = getIndexSet(testingIndexSet, 1);
		assertEquals(expected, actual);
	}
	
	public void testValIndexSetupByAttr2() throws IllegalValueException {
		BitSet actual = getIndexSet(testingIndexSet, 2);
		assertTrue(actual.isEmpty());
	}
	
	public void testValIndexSetupByAttr3() throws IllegalValueException {
		BitSet expected = new BitSet();
		set(expected, new int[]{200, 202, 203, 204, 300, 302, 303, 304});
		
		BitSet actual = getIndexSet(testingIndexSet, 3);
		assertEquals(expected, actual);
	}

	public void testValIndexSetupByAttr5() throws IllegalValueException {
		BitSet expected = new BitSet();
		set(expected, new int[]{1,101,201,301,401});
		
		BitSet actual = getIndexSet(testingIndexSet, 5);
		assertEquals(expected, actual);
	}

	public void testValIndexSetupIsComplete() {
		assertComplete(testingIndexSet);
	}

	public void testValIndexSetupIsDisjoint() {
		assertDisjoint(testingIndexSet);
	}
	
	public void testValIndexSetupTotalBitSet() {
		BitSet expected = new BitSet();
		set(expected, new int[]{0, 1, 2, 3, 4, 100, 101, 102, 103, 104, 200, 201, 202, 203, 204, 300, 301, 302, 303, 304, 401});
		assertEquals(expected, testingIndexSet.getIndexSet());
	}
	
	private BitSet getIndexSet(AttributeIndexSet<IntegerAttribute> indexSet, int value) throws IllegalValueException {
		return indexSet.getIndexSet(definition.create(value));
	}
}
