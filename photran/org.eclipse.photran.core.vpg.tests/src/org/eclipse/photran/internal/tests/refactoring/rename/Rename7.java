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

public class Rename7 extends RenameTestSuite
{
    private String filename = "rename7.f90";
    
    private Ident[] vars = new Ident[] {
        var(filename, "v1", new LineCol[] { lc(6,12), lc(8,18), lc(12,6) }),
        var(filename, "v2", new LineCol[] { lc(6,16), lc(8,22), lc(12,10) }),
        var(filename, "v3", new LineCol[] { lc(6,20), lc(8,28) }),
        var(filename, "v4", new LineCol[] { lc(6,24), lc(8,41) }),
        var(filename, "v5", new LineCol[] { lc(6,28), lc(8,45) }),
        var(filename, "common1", new LineCol[] { lc(8,9), /*lc(8,32),*/ lc(10,7) }),
    };
    
    private Ident bdn = var(filename, "bdn", new LineCol[] { lc(2,12), lc(14,16) });
    
    public static Test suite() throws Exception
    {
        return new Rename7();
    }
    
    public Rename7() throws Exception
    {
        startTests("Renaming local variables: common blocks and block data");
        
        addSuccessTests(bdn, "zzzzz");
        for (Ident v : vars)
        	addSuccessTests(bdn, v.getName());

        for (Ident v1 : vars)
        {
            addSuccessTests(v1, "zzzzz");
            
            for (Ident v2 : vars)
            	addPreconditionTests(v1, v2.getName());
        }

        endTests();
    }
}
