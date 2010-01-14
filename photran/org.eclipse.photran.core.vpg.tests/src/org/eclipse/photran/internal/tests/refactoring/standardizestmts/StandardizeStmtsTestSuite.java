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
package org.eclipse.photran.internal.tests.refactoring.standardizestmts;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.DataToParameterRefactoring;
import org.eclipse.photran.internal.core.refactoring.StandardizeStatementsRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.MarkerBasedRefactoringTestSuite;

/**
 * Unit tests for the Standardize Statements refactoring.
 *
 * @author Tim Yuvashev
 * @author Jeff Overbey
 */
public class StandardizeStmtsTestSuite extends MarkerBasedRefactoringTestSuite<StandardizeStatementsRefactoring>
{
    private static final String DIR = "refactoring-test-code/standardize-stmts";

    public static Test suite() throws Exception
    {
        return new StandardizeStmtsTestSuite();
    }

    public StandardizeStmtsTestSuite() throws Exception
    {
        super(Activator.getDefault(), "Running Standardize Statements refactoring in", DIR, StandardizeStatementsRefactoring.class);
    }

    @Override
    protected boolean configureRefactoring(StandardizeStatementsRefactoring refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        boolean shouldSucceed = Boolean.parseBoolean(markerText[2]);
        return shouldSucceed;
    }
}
