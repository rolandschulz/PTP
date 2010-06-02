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

import org.eclipse.photran.internal.core.refactoring.MakePrivateEntityPublicRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Private to Public refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang - Modified to allow for marker based testing, documentation
 */

public class MakePrivateEntityPublicTestCase
     extends PhotranRefactoringTestSuiteFromMarkers<MakePrivateEntityPublicRefactoring>
{
    private static final String DIR = "refactoring-test-code/make-private-entity-public";

    public static Test suite() throws Exception
    {
        return new MakePrivateEntityPublicTestCase();
    }

    /**
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, result
     * 
     * Note: result is of the form pass, fail-initial, or fail-final
     */
    public MakePrivateEntityPublicTestCase() throws Exception
    {
        super(Activator.getDefault(),
              "Running Make private entity public refactoring in",
              DIR,
              MakePrivateEntityPublicRefactoring.class);
    }
}
