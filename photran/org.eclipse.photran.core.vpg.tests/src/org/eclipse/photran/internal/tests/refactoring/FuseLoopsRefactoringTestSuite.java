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
import org.eclipse.photran.internal.core.refactoring.FuseLoopsRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Fuse Loops refactoring.
 * 
 * @author Ashley Kasza
 */
public class FuseLoopsRefactoringTestSuite extends PhotranRefactoringTestSuiteFromMarkers<FuseLoopsRefactoring>
{
    private static final String DIR = "refactoring-test-code/fuse-loops";
    
    public static Test suite() throws Exception
    {
        return new FuseLoopsRefactoringTestSuite();
    }
    
    public FuseLoopsRefactoringTestSuite() throws Exception
    {
        super(Activator.getDefault(),
            "Running Loop Fusion refactoring in",
            DIR,
            FuseLoopsRefactoring.class);
    }

    /** Prevents the compilation and running of tests we know don't preserve behavior */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
       return false;
    }
}
