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
package org.eclipse.photran.internal.tests.refactoring.addemptysubprogram;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.AddEmptySubprogramRefactoring;
import org.eclipse.photran.internal.tests.MarkerBasedRefactoringTestSuite;

/**
 * Unit tests for the Add Empty Subprogram refactoring.
 *
 * @author Jeff Overbey
 */
public class AddEmptySubprogramTestSuite extends MarkerBasedRefactoringTestSuite<AddEmptySubprogramRefactoring>
{
    private static final String DIR = "refactoring-test-code/add-empty-subprogram";

    public static Test suite() throws Exception
    {
        return new AddEmptySubprogramTestSuite();
    }

    public AddEmptySubprogramTestSuite() throws Exception
    {
        super("Adding empty subprogram in", DIR, AddEmptySubprogramRefactoring.class);
    }

    @Override
    protected boolean configureRefactoring(AddEmptySubprogramRefactoring refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        String newName = markerText[2];
        boolean shouldSucceed = Boolean.parseBoolean(markerText[3]);

        refactoring.setName(newName);

        return shouldSucceed;
    }
}
