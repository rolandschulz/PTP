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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.KeywordCaseRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Keyword Case refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang - Modified to allow for marker based testing, documentation
 */

public class KeywordCaseTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<KeywordCaseRefactoring>
{
    private static final String DIR = "refactoring-test-code/keyword-case";

    public static Test suite() throws Exception
    {
        return new KeywordCaseTestSuite();
    }

    public KeywordCaseTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Keyword case refactoring in",
              DIR,
              KeywordCaseRefactoring.class);
    }

    /**
     * Overridden method to allow refactoring to process the 'isLowerCase' field of the marker
     * 
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, isLower, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    @Override
    protected boolean configureRefactoring(KeywordCaseRefactoring refactoring,
                                           IFile file,
                                           TextSelection selection,
                                           String[] markerText)
    {
        boolean shouldSucceed = super.configureRefactoring(refactoring, file, selection, markerText);
        
        //the fifth field of the marker (index 4) should hold current lowercase status
        if(markerText[4].equals("false"))
            refactoring.setLowerCase(false);
        else
            refactoring.setLowerCase(true);
        
        return shouldSucceed;
    }
    
}
