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

package org.eclipse.photran.internal.tests.refactoring.introimplicitnone.failing;

import junit.framework.Test;

import org.eclipse.photran.internal.core.refactoring.IntroImplicitNoneRefactoring;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;
import org.eclipse.photran.internal.tests.failing.Activator;

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
}
