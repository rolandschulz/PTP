/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
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

import org.eclipse.photran.internal.core.refactoring.EncapsulateVariableRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the New Encapsulate Variable refactoring.
 *
 * @author Rui Wang
 * @author Esfar Huq - Modified to support marker based testing, documentation
 */
public class EncapsulateVariableTestSuite extends PhotranRefactoringTestSuiteFromMarkers<EncapsulateVariableRefactoring>
{
    private static final String DIR = "refactoring-test-code/encapsulate-variable";

    public static Test suite() throws Exception
    {
        return new EncapsulateVariableTestSuite();
    }

    /**
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    public EncapsulateVariableTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Encapsulate variable refactoring in",
              DIR,
              EncapsulateVariableRefactoring.class);
    }
}
