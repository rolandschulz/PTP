/**
 * 
 */
package org.eclipse.ptp.core.tests;

import static org.junit.Assert.*;

import org.eclipse.ptp.core.util.RangeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author greg
 *
 */
public class RangeSetTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#RangeSet()}.
	 */
	@Test
	public final void testRangeSet() {
		System.out.println("testRangeSet()");
		RangeSet r = new RangeSet();
		assertEquals(r.toString(), "");
	}
	
	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#RangeSet(java.lang.String)}.
	 */
	@Test
	public final void testRangeSetString() {
		System.out.println("testRangeSetString()");
		RangeSet r = new RangeSet("10,30-31,40,44-68,90");
		assertEquals(r.toString(), "10,30-31,40,44-68,90");
		r = new RangeSet("30-31,10,44-68,90,40");
		assertEquals(r.toString(), "10,30-31,40,44-68,90");
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#RangeSet(int)}.
	 */
	@Test
	public final void testRangeSetInt() {
		System.out.println("testRangeSetInt()");
		RangeSet r = new RangeSet(45);
		assertEquals(r.toString(), "45");
	}
	
	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#RangeSet(int, int)}.
	 */
	@Test
	public final void testRangeSetIntInt() {
		System.out.println("testRangeSetIntInt()");
		RangeSet r = new RangeSet(45, 67);
		assertEquals(r.toString(), "45-67");
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#add(int)}.
	 */
	@Test
	public final void testAddInt() {
		System.out.println("testAddInt()");
		RangeSet r = new RangeSet(45, 67);
		assertEquals(r.toString(), "45-67");
		r.add(44);
		assertEquals(r.toString(), "44-67");
		r.add(68);
		assertEquals(r.toString(), "44-68");
		r.add(50);
		assertEquals(r.toString(), "44-68");
		r.add(30);
		assertEquals(r.toString(), "30,44-68");
		r.add(31);
		assertEquals(r.toString(), "30-31,44-68");
		r.add(40);
		assertEquals(r.toString(), "30-31,40,44-68");
		r.add(42);
		assertEquals(r.toString(), "30-31,40,42,44-68");
		r.add(41);
		assertEquals(r.toString(), "30-31,40-42,44-68");
		r.add(10);
		assertEquals(r.toString(), "10,30-31,40-42,44-68");
		r.add(90);
		assertEquals(r.toString(), "10,30-31,40-42,44-68,90");
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#add(int, int)}.
	 */
	@Test
	public final void testAddIntInt() {
		System.out.println("testAddIntInt()");
		RangeSet r = new RangeSet(45, 67);
		r.add(2);
		r.add(65, 67);
		assertEquals(r.toString(), "2,45-67");
		r.add(65, 90);
		assertEquals(r.toString(), "2,45-90");
		r.add(10, 20);
		assertEquals(r.toString(), "2,10-20,45-90");
		r.add(30, 45);
		assertEquals(r.toString(), "2,10-20,30-90");
		r.add(2, 40);
		assertEquals(r.toString(), "2-90");
		r.add(100, 110);
		r.add(200, 300);
		r.add(99, 120);
		assertEquals(r.toString(), "2-90,99-120,200-300");
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#remove(int)}.
	 */
	@Test
	public final void testRemoveInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#remove(int, int)}.
	 */
	@Test
	public final void testRemoveIntInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#inRange(int)}.
	 */
	@Test
	public final void testInRange() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#size()}.
	 */
	@Test
	public final void testSize() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#toArray()}.
	 */
	@Test
	public final void testToArray() {
		System.out.println("testToArray()");
		RangeSet r = new RangeSet(45, 67);
		r.add(44);
		r.add(68);
		r.add(50);
		r.add(30);
		r.add(31);
		r.add(40);
		r.add(10);
		r.add(90);
		int v[] = new int[]{10,30,31,40,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,90};
		assertEquals(r.toArray(), v);	
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#toString()}.
	 */
	@Test
	public final void testToString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eclipse.ptp.core.util.RangeSet#add(int)}.
	 */
	@Test
	public final void testFindIndex() {
		System.out.println("testFindIndex()");
		RangeSet r = new RangeSet("10,30-31,40,44-68,90");
		assertEquals(r.findIndex(1), -1);
		assertEquals(r.findIndex(10), 0);
		assertEquals(r.findIndex(11), -2);
		assertEquals(r.findIndex(20), -2);
		assertEquals(r.findIndex(29), -2);
		assertEquals(r.findIndex(30), 1);
		assertEquals(r.findIndex(31), 1);
		assertEquals(r.findIndex(50), 3);
		assertEquals(r.findIndex(100), -6);		
	}
}
