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
package org.eclipse.photran.internal.tests.refactoring.safedelete;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.SafeDeleteRefactoring;
import org.eclipse.photran.internal.tests.MarkerBasedRefactoringTestSuite;

/**
 * Unit tests for the Safe Delete refactoring.
 *
 * @author Jeff Overbey
 */
public class SafeDeleteTestSuite extends MarkerBasedRefactoringTestSuite<SafeDeleteRefactoring>
{
    private static final String DIR = "refactoring-test-code/safe-delete";

    public static Test suite() throws Exception
    {
        return new SafeDeleteTestSuite();
    }

    public SafeDeleteTestSuite() throws Exception
    {
        super("Performing safe deletion in", DIR, SafeDeleteRefactoring.class);
    }

    @Override
    protected boolean configureRefactoring(SafeDeleteRefactoring refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        boolean shouldSucceed = Boolean.parseBoolean(markerText[2]);
        return shouldSucceed;
    }
}
