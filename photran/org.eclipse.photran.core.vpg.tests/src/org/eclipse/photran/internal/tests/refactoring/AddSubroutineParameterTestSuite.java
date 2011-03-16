/*******************************************************************************
 * Copyright (c) 2010 Joe Handzik, Joe Gonzales, Marc Celani, and Jason Patel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Joe Handzik, Joe Gonzales, Marc Celani, and Jason Patel - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.AddSubroutineParameterRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * 
 * @author Marc Celani, Joe Handzik, Joe Gonzalez, Jason Patel
 */
public class AddSubroutineParameterTestSuite extends
    PhotranRefactoringTestSuiteFromMarkers<AddSubroutineParameterRefactoring>
{
    private static final String DIR = "refactoring-test-code/add-subroutine-parameter";

    public static Test suite() throws Exception
    {
        return new AddSubroutineParameterTestSuite();
    }

    public AddSubroutineParameterTestSuite() throws Exception
    {
        super(Activator.getDefault(), "Running Add Subroutine Parameter refactoring in", DIR,
            AddSubroutineParameterRefactoring.class);
    }

    @Override
    protected boolean configureRefactoring(AddSubroutineParameterRefactoring refactoring,
        IFile file, TextSelection selection, String[] markerText)
    {
        boolean shouldSucceed = super
            .configureRefactoring(refactoring, file, selection, markerText);
        refactoring.setDeclaration(markerText[4].replaceAll(";", ","));
        refactoring.setPosition(Integer.parseInt(markerText[5]));
        if (!markerText[6].equals("_dont_call")) refactoring.setDefaultValue(markerText[6]);

        return shouldSucceed;
    }
}