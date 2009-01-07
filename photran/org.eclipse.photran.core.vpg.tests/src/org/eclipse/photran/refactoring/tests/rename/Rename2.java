/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

public class Rename2 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN RENAME2.F90, AND
    // GROUP THEM ACCORDING TO WHICH ONES SHOULD BE RENAMED TOGETHER
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String filename = "rename2.f90";
    
    private Ident[] vars = new Ident[]
    {
        var(filename, "Main", new LineCol[] { lc(2,9), lc(27,13) }),
        var(filename, "one", new LineCol[] { lc(4,16), lc(12,14), lc(16,11), lc(20,11) }),
        var(filename, "two", new LineCol[] { lc(5,27), lc(10,13), lc(13,14), lc(17,14) }),
        var(filename, "three", new LineCol[] { lc(6,16), lc(14,9), lc(18,9) }),
        var(filename, "four", new LineCol[] { lc(10,21), lc(15,14), lc(19,14) })
    };
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new Rename2();
    }
    
    public Rename2() throws Exception
    {
        startTests("Renaming program with comments and line continuations");
        for (String name : new String[] { "z", "a_really_really_long_name" })
            for (Ident var : vars)
                addSuccessTests(var, name);
        endTests();
    }
}
