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
package org.eclipse.photran.internal.tests.refactoring.removeunusedvars;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.RemoveUnusedVariablesRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.MarkerBasedRefactoringTestSuite;

/**
 * Unit tests for the Remove Unused Local Variables refactoring.
 *
 * @author Tim Yuvashev
 * @author Jeff Overbey
 */
public class RemoveUnusedTestSuite extends MarkerBasedRefactoringTestSuite<RemoveUnusedVariablesRefactoring>
{
    private static final String DIR = "refactoring-test-code/remove-unused-local-vars";

    public static Test suite() throws Exception
    {
        return new RemoveUnusedTestSuite();
    }

    public RemoveUnusedTestSuite() throws Exception
    {
        super(Activator.getDefault(), "Running Remove Unused Variables refactoring in", DIR, RemoveUnusedVariablesRefactoring.class);
    }

    @Override
    protected boolean configureRefactoring(RemoveUnusedVariablesRefactoring refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        boolean shouldSucceed = Boolean.parseBoolean(markerText[2]);
        return shouldSucceed;
    }
}
