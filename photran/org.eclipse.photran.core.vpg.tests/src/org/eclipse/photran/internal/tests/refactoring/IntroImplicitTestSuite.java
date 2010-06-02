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
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.IntroImplicitNoneRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Introduce Implicit None refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq 
 * @author Rui Wang - Modified to allow for marker based testing, documentation
 */

public class IntroImplicitTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<IntroImplicitNoneRefactoring>
{
    private static final String DIR = "refactoring-test-code/intro-implicit-none";

    public static Test suite() throws Exception
    {
        return new IntroImplicitTestSuite();
    }

    public IntroImplicitTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Intro implicit refactoring in",
              DIR,
              IntroImplicitNoneRefactoring.class);
    }

    /**
     * Overridden method to simplify functionality
     * 
     * MARKER FORMAT: !<<<<< startLine, startCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    
    @Override
    protected boolean configureRefactoring(IntroImplicitNoneRefactoring refactoring,
                                           IFile file,
                                           TextSelection selection,
                                           String[] markerText)
    {
        return super.configureRefactoring(refactoring, file, selection, markerText);
    }
    
    /**
     * Method that prevents the compilation of any test we know don't compile
     */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
       return !fileContainingMarker.getName().equalsIgnoreCase("implicit-subprog.f90");
    }
}
