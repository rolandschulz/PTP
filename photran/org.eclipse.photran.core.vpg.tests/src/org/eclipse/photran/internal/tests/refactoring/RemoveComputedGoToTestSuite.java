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

import org.eclipse.photran.internal.core.refactoring.RemoveComputedGoToRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Remove computedGoto refactoring.
 *
 * @author Rui Wang
 */
public class RemoveComputedGoToTestSuite extends PhotranRefactoringTestSuiteFromMarkers<RemoveComputedGoToRefactoring>
{
    private static final String DIR = "refactoring-test-code/remove-computed-goto";

    public static Test suite() throws Exception
    {
        return new RemoveComputedGoToTestSuite();
    }

    /**
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    public RemoveComputedGoToTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Remove computed goto refactoring in",
              DIR,
              RemoveComputedGoToRefactoring.class);
    }
}
