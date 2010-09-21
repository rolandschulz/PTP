/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.UnrollLoopRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Test suite for loop unrolling refactoring
 * 
 * @author Ashley Kasza
 */
public class UnrollLoopRefactoringTestSuite extends PhotranRefactoringTestSuiteFromMarkers<UnrollLoopRefactoring>
{
    private static final String DIR = "refactoring-test-code/unroll-loop";

    public static Test suite() throws Exception
    {
        return new UnrollLoopRefactoringTestSuite();
    }

    public UnrollLoopRefactoringTestSuite() throws Exception
    {
        super(Activator.getDefault(),
            "Running Loop Unrolling refactoring in",
            DIR,
            UnrollLoopRefactoring.class);
    }

    @Override
    protected boolean initializeRefactoring(UnrollLoopRefactoring refactoring, IFile file,
        TextSelection selection, String[] markerText)
    {
        boolean result = super.initializeRefactoring(refactoring, file, selection, markerText);

        String s = markerText[4]; // file field(s) are just before result
        String s2 = markerText[5];
        refactoring.setLoopUnrollNumber(s);
        refactoring.setComplete(s2.equals("true"));
        // }

        return result;
    }

    /** Prevents the compilation and running of tests we know don't compile or run */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
        String filename = fileContainingMarker.getName();
        return !filename.equalsIgnoreCase("decrementLoopPartial.f90")
            && !filename.equalsIgnoreCase("testSimpleLoop.f90");
    }
}
