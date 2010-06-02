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
import org.eclipse.photran.internal.core.refactoring.InterchangeLoopsRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Interchange Loops refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang - Modified to allow for marker based testing, documentation
 * 
 * NOTE: Refactoring of loops is incorrect at the moment**
 */
public class InterchangeLoopsTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<InterchangeLoopsRefactoring>
{
    private static final String DIR = "refactoring-test-code/interchange-loops";

    public static Test suite() throws Exception
    {
        return new InterchangeLoopsTestSuite();
    }

    /**
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    public InterchangeLoopsTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Interchange loop refactoring in",
              DIR,
              InterchangeLoopsRefactoring.class);
    }

    /**
     * Method that prevents the compilation of any test we know don't compile
     */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
       return false;
    }
}
