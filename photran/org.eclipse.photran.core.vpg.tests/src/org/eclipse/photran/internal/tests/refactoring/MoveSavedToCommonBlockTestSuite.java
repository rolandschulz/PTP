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
import org.eclipse.photran.internal.core.refactoring.MoveSavedToCommonBlockRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Moved Saved to Common Block refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang - Modified to add support for marker based testing, documentation
 */
public class MoveSavedToCommonBlockTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<MoveSavedToCommonBlockRefactoring>
{
    private static final String DIR = "refactoring-test-code/move-saved-to-common-block";

    public static Test suite() throws Exception
    {
        return new MoveSavedToCommonBlockTestSuite();
    }

    /**
     * Overridden method
     * 
     * MARKER FORMAT: !<<<<< startLine, startCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    public MoveSavedToCommonBlockTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Move saved to common block refactoring in",
              DIR,
              MoveSavedToCommonBlockRefactoring.class);
    }
    
    /**
     * Method that prevents the compilation of tests that we know don't compile
     */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
        //don't compile the second test case
        return !fileContainingMarker.getName().equalsIgnoreCase("test2.f90");
    }
}
