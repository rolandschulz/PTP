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

import org.eclipse.photran.internal.core.refactoring.AddIdentifierToEndRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Add Identifier to END refactoring.
 *
 * @author Matthew Fotzler
 */
public class AddIdentifierToEndTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<AddIdentifierToEndRefactoring>
{
    private static final String DIR = "refactoring-test-code/add-identifier-to-end";

    public static Test suite() throws Exception
    {
        return new AddIdentifierToEndTestSuite();
    }

    public AddIdentifierToEndTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Add identifier to END refactoring in",
              DIR,
              AddIdentifierToEndRefactoring.class);
    }
}
