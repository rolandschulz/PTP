/*******************************************************************************
/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.refactoring.ReverseLoopRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Reverse Loop refactoring.
 *
 * @author Ashley Kasza
 */
public class ReverseLoopRefactoringTestSuite extends PhotranRefactoringTestSuiteFromMarkers<ReverseLoopRefactoring>
{
    private static final String DIR = "refactoring-test-code/reverse-loop";

    public static Test suite() throws Exception
    {
        return new ReverseLoopRefactoringTestSuite();
    }

    public ReverseLoopRefactoringTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Reverse Loop refactoring in",
              DIR,
              ReverseLoopRefactoring.class);
    }

    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
       return false;
    }
}
