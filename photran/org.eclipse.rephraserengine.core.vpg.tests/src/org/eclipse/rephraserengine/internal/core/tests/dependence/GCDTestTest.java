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
package org.eclipse.rephraserengine.internal.core.tests.dependence;

import junit.framework.TestCase;

import org.eclipse.rephraserengine.core.analysis.dependence.GCDTest;

/**
 *
 * @author Jeff Overbey
 */
public class GCDTestTest extends TestCase
{
    /**
     * DO I = 1, N
     *     A(I + 1) = A(I) + B(I)     ! S1
     * ENDDO
     */
    public void testPage38()
    {
        assertTrue(test(
            1,
            //          Constant term
            //          |  Coefficient of I
            //          |  |
            //          V  V
            new int[] { 1, 1 },
            new int[] { 0, 1 }));
    }

    /**
     * DO I = 1, N
     *     A(I + 2) = A(I) + B(I)     ! S1
     * ENDDO
     */
    public void testPage39()
    {
        assertTrue(test(
            1,
            //          Constant term
            //          |  Coefficient of I
            //          |  |
            //          V  V
            new int[] { 2, 1 },
            new int[] { 0, 1 }));
    }

    /**
     * DO I = 1, N
     *     A(2*I) = A(2*I + 1)
     * ENDDO
     */
    public void testFalse()
    {
        assertFalse(test(
            1,
            //          Constant term
            //          |  Coefficient of I
            //          |  |
            //          V  V
            new int[] { 0, 2 },
            new int[] { 1, 2 }));
    }

    private boolean test(int n, int[] a, int[] b)
    {
        return new GCDTest().test(n, null, null, a, b, null).dependenceMightExist();
    }
}
