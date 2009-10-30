/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.core.tests.preservation;

import junit.framework.TestCase;

import org.eclipse.rephraserengine.internal.core.preservation.Interval;

/**
 *
 * @author Jeff Overbey
 */
public class IntervalTestCase extends TestCase
{
    private Interval width1at0 = new Interval(0, 1);
    private Interval width3at1 = new Interval(1, 4);
    private Interval negWidth20 = new Interval(-4, 16);
    private Interval empty1 = new Interval(3, 3);
    private Interval empty2 = new Interval(5, 5);

    public void testCtorAndToString()
    {
        assertEquals("[0, 1)",    width1at0.toString());
        assertEquals("[1, 4)",    width3at1.toString());
        assertEquals("[-4, 16)",  negWidth20.toString());
        assertEquals("[3, 3)",    empty1.toString());
        assertEquals("[5, 5)", empty2.toString());
    }

    public void testAccessors()
    {
        assertEquals(-4, negWidth20.lb);
        assertEquals(16, negWidth20.ub);
        assertEquals( 3, empty1.lb);
        assertEquals( 3, empty1.ub);
        assertEquals( 5, empty2.lb);
        assertEquals( 5, empty2.ub);
    }

    public void testCardinality()
    {
        assertEquals(1, width1at0.cardinality());
        assertEquals(3, width3at1.cardinality());
        assertEquals(20, negWidth20.cardinality());
        assertEquals(0, empty1.cardinality());
        assertEquals(0, empty2.cardinality());
    }

    public void testComparison()
    {
        assertFalse(width3at1.isLessThan(width1at0));
        assertTrue(width1at0.isLessThan(width3at1));

        assertFalse(width3at1.isLessThan(negWidth20));

        assertTrue(empty1.isLessThan(empty2));
        assertFalse(empty2.isLessThan(empty1));
    }

    public void testSubset()
    {
        assertFalse(width3at1.isSubsetOf(width1at0));
        assertFalse(width1at0.isSubsetOf(width3at1));

        assertFalse(negWidth20.isSubsetOf(width3at1));
        assertTrue(width3at1.isSubsetOf(negWidth20));

        assertTrue(empty1.isSubsetOf(width3at1));
        assertTrue(empty1.isSubsetOf(negWidth20));
        assertTrue(empty1.isSubsetOf(empty2));
        assertTrue(empty1.isSubsetOf(empty1));
        assertTrue(empty2.isSubsetOf(width3at1));
        assertTrue(empty2.isSubsetOf(negWidth20));
        assertTrue(empty2.isSubsetOf(empty2));
        assertTrue(empty2.isSubsetOf(empty1));
    }

    public void testOffset()
    {
        assertEquals(new Interval( 3,  4), width1at0.plus(3));
        assertEquals(new Interval( 4,  7), width3at1.plus(3));
        assertEquals(new Interval(-1, 19), negWidth20.plus(3));
        assertEquals(new Interval( 6,  6), empty1.plus(3));
        assertEquals(new Interval( 8,  8), empty2.plus(3));
    }
}
