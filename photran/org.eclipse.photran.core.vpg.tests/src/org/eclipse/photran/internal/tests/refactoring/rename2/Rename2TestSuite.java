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
package org.eclipse.photran.internal.tests.refactoring.rename2;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.RenameRefactoring2;
import org.eclipse.photran.internal.tests.MarkerBasedRefactoringTestSuite;

/**
 * Unit tests for the preservation-based Rename refactoring.
 *
 * @author Jeff Overbey
 */
public class Rename2TestSuite extends MarkerBasedRefactoringTestSuite<RenameRefactoring2>
{
    private static final String DIR = "refactoring-test-code/rename2";

    public static Test suite() throws Exception
    {
        return new Rename2TestSuite();
    }

    public Rename2TestSuite() throws Exception
    {
        super("Renaming (alternate) in", DIR, RenameRefactoring2.class);
    }

    @Override
    protected boolean configureRefactoring(RenameRefactoring2 refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        String newName = markerText[2];
        boolean shouldSucceed = Boolean.parseBoolean(markerText[3]);

        refactoring.setNewNameForIdentifier(newName);

        return shouldSucceed;
    }
}
