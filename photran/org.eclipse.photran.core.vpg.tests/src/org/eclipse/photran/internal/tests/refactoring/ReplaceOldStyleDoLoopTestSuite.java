/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Esfar Huq, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.refactoring.ReplaceOldStyleDoLoopRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Replace Old Style Do Loop refactoring.
 *
 * @author Esfar Huq
 */
public class ReplaceOldStyleDoLoopTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<ReplaceOldStyleDoLoopRefactoring>
{
    private static final String DIR = "refactoring-test-code/replace-old-style-do-loop";

    public static Test suite() throws Exception
    {
        return new ReplaceOldStyleDoLoopTestSuite();
    }

    /**
     * Overridden method
     * 
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    public ReplaceOldStyleDoLoopTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Replace old style do loop refactoring in",
              DIR,
              ReplaceOldStyleDoLoopRefactoring.class);
    }

    /**
     * Prevents compilation of any tests we know don't compile
     */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
       if (fileContainingMarker.getName().contains("test5"))
           return false; // Test 5 does not compile, so don't try
       else
           return true; // All others do
    }
}
