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
package org.eclipse.photran.internal.tests.refactoring.rename;

import junit.framework.Test;

import org.eclipse.photran.internal.core.util.LineCol;

public class Rename5 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN RENAME3.F90, AND
    // GROUP THEM ACCORDING TO WHICH ONES SHOULD BE RENAMED TOGETHER
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String filename = "rename5.f90";
    
    private Ident intrinsic1 = var(filename, "selected_int_kind", new LineCol[] { lc(2,14) });
    private Ident intrinsic2 = var(filename, "null", new LineCol[] { lc(29,33) });
    
    //private Ident outerThrice = var(filename, "thrice", new LineCol[] { lc(2,45) });
    //private Ident outerThriceImpliedResult = var(filename, "thrice", new LineCol[] { lc(4,3) });
    private Ident outerThriceFunctionAndResult = var(filename, "thrice", new LineCol[] { lc(2,45), lc(4,3), lc(5,14),
        lc(17,12), lc(17,23), lc(45,20)}); // If statement function removed
    private Ident outerThriceParam = var(filename, "n", new LineCol[] { lc(2,52), lc(3,17) });
    
    private Ident typeType = var(filename, "type", new LineCol[] { lc(7,8), lc(9,12), lc(27,10), lc(29,10), lc(37,9) });
    private Ident typeComponent = var(filename, "a", new LineCol[] { lc(8,16), lc(35,7), lc(45,9) });
    
    //private Ident innerThrice = var(filename, "thrice", new LineCol[] { lc(12,3), lc(17,12), lc(17,23), lc(45,20) });
    //private Ident innerThriceParam = var(filename, "n", new LineCol[] { lc(12,10), lc(12,17) });
    
    private Ident localN = var(filename, "n", new LineCol[] { lc(14,3), lc(17,30), lc(17,34) });
    
    //private Ident fFunction = var(filename, "f", new LineCol[] { lc(17,38), lc(17,44), lc(23,30), lc(45,27), lc(46,16) });
    //private Ident fResult = var(filename, "f", new LineCol[] { lc(47,5), lc(45,16), lc(45,29) });
    private Ident fFunctionAndResult = var(filename, "f", new LineCol[] { lc(17,38), lc(17,44), lc(23,30), lc(45,27), lc(46,16), lc(43,5), lc(45,16), lc(45,29) });
    private Ident fParam = var(filename, "a", new LineCol[] { lc(23,33), lc(25,16), lc(32,19), lc(35,11), lc(37,14) });
    private Ident fx = var(filename, "x", new LineCol[] { lc(27,19), lc(31,15), lc(35,5), lc(37,5), lc(39,10) });
    private Ident fp = var(filename, "p", new LineCol[] { lc(29,28), lc(39,5) });
    
    private Ident[] notRenameable = new Ident[]
    {
        intrinsic1, intrinsic2,
        outerThriceParam, /*innerThriceParam,*/ fParam,
        typeComponent,
    };
    
    private Ident[] renameable = new Ident[]
    {
        outerThriceFunctionAndResult,
        typeType,
        /*innerThrice,*/
        localN,
        fFunctionAndResult,
        fx,
        fp,
    };
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new Rename5();
    }
    
    public Rename5() throws Exception
    {
        //startTests("Renaming program with a shadowing statement function, implied results, and pointers");
        startTests("Renaming program with implied results and pointers");
        for (Ident var : notRenameable)
            addPreconditionTests(var, "z");
        for (Ident var : renameable)
            addSuccessTests(var, "z");
        endTests();
    }
}
