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

import org.eclipse.photran.internal.core.refactoring.MinOnlyListRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Min Only List refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang - Modified to allow for marker based filtering, documentation
 */
public class MinOnlyListTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<MinOnlyListRefactoring>
{
    private static final String DIR = "refactoring-test-code/min-only-list";

    public static Test suite() throws Exception
    {
        return new MinOnlyListTestSuite();
    }

    /**
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    public MinOnlyListTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Min only list refactoring in",
              DIR,
              MinOnlyListRefactoring.class);
    }
}
