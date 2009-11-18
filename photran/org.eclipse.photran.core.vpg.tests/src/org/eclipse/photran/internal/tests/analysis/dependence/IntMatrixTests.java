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
package org.eclipse.photran.internal.tests.analysis.dependence;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.analysis.dependence.GeneralizedGCDTest.IntMatrix;
import org.eclipse.photran.internal.core.analysis.dependence.GeneralizedGCDTest.IntVector;
import org.eclipse.photran.internal.core.analysis.dependence.GeneralizedGCDTest.IntMatrix.Reduce;

/**
 *
 * @author Jeff Overbey
 */
public class IntMatrixTests extends TestCase
{
    public void testToString()
    {
        int rows = 3;
        int cols = 5;
        IntMatrix m = IntMatrix.zero(rows, cols);
        for (int row = 1; row <= rows; row++)
            for (int col = 1; col <= cols; col++)
                m.set(row, col, 10*row + col);
        String actual = m.toString();
        String expected =
                "   11   12   13   14   15\n" +
                "   21   22   23   24   25\n" +
                "   31   32   33   34   35\n";
        assertEquals(expected, actual);

        IntMatrix copy = IntMatrix.copyFrom(m);

        m.swapColumns(2, 3);
        String actual2 = m.toString();
        String expected2 =
                "   11   13   12   14   15\n" +
                "   21   23   22   24   25\n" +
                "   31   33   32   34   35\n";
        assertEquals(expected2, actual2);

        m.set(1, 2, 77777);
        m.set(2, 3, 88888);
        m.set(2, 4, 99999);

        assertEquals(expected, copy.toString());

        int[] ints = new int[] {
                3, 4, 5,
                6, 7, 8,
                -1, 0, 1,
                4, 3, 2,
                1, 0, 0
        };
        IntMatrix m3 = IntMatrix.create(5, 3, ints);
        String actual3 = m3.toString();
        String expected3 =
                "    3    4    5\n" +
                "    6    7    8\n" +
                "   -1    0    1\n" +
                "    4    3    2\n" +
                "    1    0    0\n";
        assertEquals(expected3, actual3);
        assertTrue(m3.equalsUnwrapped(ints));
    }

    /** Wolfe p. 113 */
    public void testReduceRow()
    {
        IntMatrix a = IntMatrix.create(1, 3,
            6, -4, 14);
        Reduce r = a.reduce();
        assertTrue(r.getColumnEchelonForm().equalsUnwrapped(2, 0, 0));
        assertTrue(r.getUnimodularMatrix().equalsUnwrapped(
            1, 2, -1,
            1, 3,  2,
            0, 0,  1));
    }

    /** Wolfe p. 116 */
    public void testReduceMatrix()
    {
        IntMatrix a = IntMatrix.create(3, 3,
            3,  2, -1,
            2, -2,  5,
            0,  0,  0);
        Reduce r = a.reduce();
        assertTrue(r.getColumnEchelonForm().equalsUnwrapped(
            -1, 0, 0,
             5, 1, 0,
             0, 0, 0));
        assertTrue(r.getUnimodularMatrix().equalsUnwrapped(
            0,  1, -8,
            0, -2, 17,
            1, -1, 10));

        IntVector t = r.getColumnEchelonForm().forwardSubstitute(9, 7, 0);
        assertEquals(-9, t.get(1));
        assertEquals(52, t.get(2));
    }

    public void testForwardSubstitute()
    {
        IntMatrix u = IntMatrix.create(3, 3,
            -15, 0, 0,
            -8, -4, 0,
             3,  2, 1);
        int[] c = new int[] { -15, -16, 15 };
        IntVector x = u.forwardSubstitute(c);
        assertEquals(3, x.size());
        assertEquals(1, x.get(1));
        assertEquals(2, x.get(2));
        assertEquals(8, x.get(3));
    }

    /** Wolfe p. 226 */
    public void testSolve0()
    {
        IntMatrix a = IntMatrix.create(4, 4,
            1, -1, 0, 0,
            0,  0, 1, -1,
            0,  0, 0,  0,
            0,  0, 0,  0);
        IntVector t = a.solve(-1, 0, 0, 0);
        assertEquals(4, t.size());
        assertEquals(-1, t.get(1));
        assertEquals(0, t.get(2));
        assertEquals(0, t.get(3));
        assertEquals(0, t.get(4));
    }

    public void testSolve1()
    {
        IntMatrix a = IntMatrix.create(4, 4,
            1, -1, 1, -1,
            0,  0, 1, -1,
            0,  0, 0,  0,
            0,  0, 0,  0);
        IntVector t = a.solve(-1, 0, 0, 0);
        assertEquals(4, t.size());
        assertEquals(-1, t.get(1));
        assertEquals(0, t.get(2));
        assertEquals(0, t.get(3));
        assertEquals(0, t.get(4));
    }

    // http://tutorial.math.lamar.edu/Classes/LinAlg/SolvingSystemsOfEqns.aspx
    public void testSolve2()
    {
        IntMatrix a = IntMatrix.create(3, 3,
            -2, 1, -1,
             1, 2,  3,
             3, 0,  1);
        IntVector t = a.solve(4, 13, -1);
        assertEquals(3, t.size());
        assertEquals(-1, t.get(1));
        assertEquals(4, t.get(2));
        assertEquals(2, t.get(3));
    }

    // http://tutorial.math.lamar.edu/Classes/LinAlg/SolvingSystemsOfEqns.aspx
    public void testSolve3()
    {
        IntMatrix a = IntMatrix.create(3, 3,
            1, -2, 3,
            -1, 1, -2,
            2, -1, 3);
        // No solutions
        IntVector t = a.solve(-2, 3, 1);
        assertNull(t);
    }

    // http://tutorial.math.lamar.edu/Classes/LinAlg/SolvingSystemsOfEqns.aspx
    public void testSolve4()
    {
        IntMatrix a = IntMatrix.create(3, 3,
            1, -2, 3,
            -1, 1, -2,
            2, -1, 3);
        // Infinite solutions
        IntVector t = a.solve(-2, 3, -7);
        assertEquals(3, t.size());
        assertEquals(-4, t.get(1));
        assertEquals(-1, t.get(2));
        assertEquals(0, t.get(3));
    }
}
