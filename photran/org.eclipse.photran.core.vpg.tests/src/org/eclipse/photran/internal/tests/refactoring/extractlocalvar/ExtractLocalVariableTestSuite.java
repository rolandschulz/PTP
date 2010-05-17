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
package org.eclipse.photran.internal.tests.refactoring.extractlocalvar;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.ExtractLocalVariableRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Extract Local Variable refactoring.
 *
 * @author Jeff Overbey
 */
public class ExtractLocalVariableTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<ExtractLocalVariableRefactoring>
{
    private static final String DIR = "refactoring-test-code/extract-local-variable";

    public static Test suite() throws Exception
    {
        return new ExtractLocalVariableTestSuite();
    }

    public ExtractLocalVariableTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Extract Local Variable refactoring in",
              DIR,
              ExtractLocalVariableRefactoring.class);
    }

    @Override
    protected boolean initializeRefactoring(ExtractLocalVariableRefactoring refactoring,
                                            IFile file,
                                            TextSelection selection,
                                            String[] markerText)
    {
        super.initializeRefactoring(refactoring, file, selection, markerText);
        return !file.getName().contains("FAILINITIAL");
    }

    @Override
    protected boolean configureRefactoring(ExtractLocalVariableRefactoring refactoring,
                                           IFile file,
                                           TextSelection selection,
                                           String[] markerText)
    {
        String newName = markerText[4];
        boolean shouldSucceed = Boolean.parseBoolean(markerText[5]);

        refactoring.setDecl(newName);

        return shouldSucceed;
    }
}
