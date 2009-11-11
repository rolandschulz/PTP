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

import org.eclipse.photran.internal.core.analysis.dependence.VariableReference;
import org.eclipse.photran.internal.tests.BaseTestCase;

/**
 * 
 * @author Jeff Overbey
 */
public class ArrayReferenceTest extends BaseTestCase
{
    public void test1DLHS() throws Exception
    {
        assertEquals("a(2*i+3)", VariableReference.fromLHS(assignment("a(2* i  +3) = 0")).toString());
        assertEquals("a(2*i+3)", VariableReference.fromLHS(assignment("A(2* i  +3) = 0")).toString());
        assertEquals("a(1*i+2)", VariableReference.fromLHS(assignment("a(i + 2) = 0")).toString());
        assertEquals("a(3)",     VariableReference.fromLHS(assignment("a(3) = 0")).toString());
        assertEquals("a(1*i+0)", VariableReference.fromLHS(assignment("a(i) = 0")).toString());
        assertEquals("a(2*i+0)", VariableReference.fromLHS(assignment("a(2* i) = 0")).toString());
        assertEquals("a",        VariableReference.fromLHS(assignment("a(s(n/3)) = 0")).toString());
        assertEquals("a(2*i+5)", VariableReference.fromLHS(assignment("a(5+2*i) = 0")).toString());
        assertEquals("a(1*i+5)", VariableReference.fromLHS(assignment("a(5+i) = 0")).toString());
        assertEquals("a(-1*i+5)", VariableReference.fromLHS(assignment("a(-i+5) = 0")).toString());
        assertEquals("a(-1*i+0)", VariableReference.fromLHS(assignment("a(-i) = 0")).toString());
        //assertEquals("a(-1*i+5)", VariableReference.fromLHS(assignment("a(5+-i) = 0")).toString());
    }

    public void test3DLHS() throws Exception
    {
        assertEquals("a(2*i+0, 6, 1*j+10)", VariableReference.fromLHS(assignment("a ( 2*i, 6 , j + 10 ) = 0")).toString());
    }

    public void testRHS() throws Exception
    {
        assertEquals("[b(3), c(6*i+0), i, d, a]",
            VariableReference.fromRHS(assignment("a ( 2*i, 6 , j + 10 ) = b(3) + c(6*i) + 3 * d * a")).toString());
    }
}
