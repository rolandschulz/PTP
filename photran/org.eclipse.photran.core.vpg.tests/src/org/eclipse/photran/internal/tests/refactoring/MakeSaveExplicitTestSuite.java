/*******************************************************************************
 * Copyright (c) 2010 Stephen Downs, Robert Samblanet, Kevin Schilling, Jon 
 * Woolwine, and Chad Zamzow
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Stephen Downs, Robert Samblanet, Kevin Schilling, 
 *      Jon Woolwine, and Chad Zamzow
 *******************************************************************************/

package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.photran.internal.core.refactoring.MakeSaveExplicitRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Make Save Explicit refactoring.
 *
 * @author Stephen Downs
 * @author Robert Samblanet
 * @author Kevin Schilling
 * @author Jon Woolwine
 * @author Chad Zamzow
 */
public class MakeSaveExplicitTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<MakeSaveExplicitRefactoring>
{
    private static final String DIR = "refactoring-test-code/make-save-explicit";

    public static Test suite() throws Exception
    {
        return new MakeSaveExplicitTestSuite();
    }

    public MakeSaveExplicitTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Make Save Attribute Explicit refactoring in",
              DIR,
              MakeSaveExplicitRefactoring.class);
    }
}