/*******************************************************************************
 * Copyright (c) 2010 Zeeshan Ansari, Mark Chen, Burim Isai, Waseem Sheikh, Mumtaz Vauhkonen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zeeshan Ansari
 *     Mark Chen
 *     Burim Isai
 *     Waseem Sheihk
 *     Mumtaz Vauhkonen
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.IfConstructStatementConversionRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the IF Statement/Construct refactoring.
 * 
 * @author Zeeshan Ansari
 * @author Mark Chen
 * @author Mumtaz Vauhkonrn
 * @author Burim Isai
 * @author Waseem Sheikh
 */
public class IfConstructStatementConversionRefactoringTestSuite extends
    PhotranRefactoringTestSuiteFromMarkers<IfConstructStatementConversionRefactoring>
{
    private static final String DIR = "refactoring-test-code/if-construct-statement-conversion";

    public static Test suite() throws Exception
    {
        return new IfConstructStatementConversionRefactoringTestSuite();
    }

    public IfConstructStatementConversionRefactoringTestSuite() throws Exception
    {
        super(Activator.getDefault(), "Running If Construct Statement Conversion refactoring in",
            DIR, IfConstructStatementConversionRefactoring.class);
    }

    @Override
    protected boolean configureRefactoring(IfConstructStatementConversionRefactoring refactoring,
        IFile file, TextSelection selection, String[] markerText)
    {
        boolean shouldSucceed = super
            .configureRefactoring(refactoring, file, selection, markerText);
        String includeOptionalElse;
        String testType;
        testType = markerText[4];

        if (testType.equals("IfStmtToIfConstruct"))
        {
            includeOptionalElse = markerText[5];
            if (includeOptionalElse.equals("TRUE")) refactoring.setAddEmptyElseBlock();
        }
        
        return shouldSucceed;
    }
}
