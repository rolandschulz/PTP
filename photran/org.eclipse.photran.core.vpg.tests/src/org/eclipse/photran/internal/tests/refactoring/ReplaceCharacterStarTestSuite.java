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
import org.eclipse.photran.internal.core.refactoring.ReplaceCharacterStarRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Replace Character* with Character(len=) refactoring.
 *
 * @author Mariano Mendez (Fortran)
 * @author Esfar Huq (Java)
 */
public class ReplaceCharacterStarTestSuite extends PhotranRefactoringTestSuiteFromMarkers<ReplaceCharacterStarRefactoring>
{
    private static final String DIR = "refactoring-test-code/replace-character-star";

    public static Test suite() throws Exception
    {
        return new ReplaceCharacterStarTestSuite();
    }

    /**
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    public ReplaceCharacterStarTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Replace CHARACTER* refactoring in",
              DIR,
              ReplaceCharacterStarRefactoring.class);
    }
}
