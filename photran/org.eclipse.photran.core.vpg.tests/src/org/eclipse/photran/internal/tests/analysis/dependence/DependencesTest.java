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

import org.eclipse.photran.internal.core.analysis.dependence.LoopDependences;
import org.eclipse.photran.internal.tests.BaseTestCase;

/**
 * Unit tests for the Dependences class, which collects array dependences in a perfect loop nest.
 *
 * @author Jeff Overbey
 */
public class DependencesTest extends BaseTestCase
{
    public void testPage38() throws Exception
    {
        LoopDependences deps = dependences(
            "DO I = 1, N\n" +
            "    A(I + 1) = A(I) + B(I)     ! S1\n" +
            "ENDDO\n");

        assertEquals("[a(1*i+1)]", deps.getWrites().toString());
        assertEquals("[a(1*i+0), i, b(1*i+0), i]", deps.getReads().toString());

        assertEquals("[Anti-dependence from a(1*i+0) to a(1*i+1), Flow dependence from a(1*i+1) to a(1*i+0)]",
            deps.getDependences().toString());
    }

    public void testPage39() throws Exception
    {
        LoopDependences deps = dependences(
            "DO I = 1, N\n" +
            "    A(I + 2) = A(I) + B(I)     ! S1\n" +
            "ENDDO\n");

        assertEquals("[a(1*i+2)]", deps.getWrites().toString());
        assertEquals("[a(1*i+0), i, b(1*i+0), i]", deps.getReads().toString());

        assertEquals("[Anti-dependence from a(1*i+0) to a(1*i+2), Flow dependence from a(1*i+2) to a(1*i+0)]",
            deps.getDependences().toString());
    }

    public void testFalse1() throws Exception
    {
        System.out.println("START");
        LoopDependences deps = dependences(
            "DO I = 1, N\n" +
            "    A(2*I) = A(2*I + 1)\n" +
            "ENDDO\n");

        assertEquals("[a(2*i+0)]", deps.getWrites().toString());
        assertEquals("[a(2*i+1), i]", deps.getReads().toString());

        assertTrue(deps.getDependences().isEmpty());
        System.out.println("DONE");
    }

    public void testPage49() throws Exception
    {
        LoopDependences deps = dependences(
            "DO I = 1, N\n" +
            "    A(I + 1) = F(I)     ! S1\n" +
            "    F(I + 1) = A(I)     ! S2\n" +
            "ENDDO\n");

        assertEquals("[a(1*i+1), f(1*i+1)]", deps.getWrites().toString());
        assertEquals("[f(1*i+0), i, a(1*i+0), i]", deps.getReads().toString());

        assertEquals("[Anti-dependence from f(1*i+0) to f(1*i+1), " +
                      "Flow dependence from a(1*i+1) to a(1*i+0), " +
                      "Anti-dependence from a(1*i+0) to a(1*i+1), " +
                      "Flow dependence from f(1*i+1) to f(1*i+0)]",
            deps.getDependences().toString());
    }

    public void testFalse2() throws Exception
    {
        LoopDependences deps = dependences(
            "DO I = 1, N\n" +
            "    A(-1*I) = A(I)\n" +
            "ENDDO\n");

        assertEquals("[a(-1*i+0)]", deps.getWrites().toString());
        assertEquals("[a(1*i+0), i]", deps.getReads().toString());

        assertTrue(deps.getDependences().isEmpty());
    }

    // FIXME
    public void testEvenOdd() throws Exception
    {
        LoopDependences deps = dependences(
            "DO I = 1, N\n" +
            "    A(2*I) = A(2*I+3)\n" +
            "ENDDO\n");

        assertEquals("[a(2*i+0)]", deps.getWrites().toString());
        assertEquals("[a(2*i+3), i]", deps.getReads().toString());

        System.out.println(deps.getDependences());
        assertTrue(deps.getDependences().isEmpty());
    }
}
