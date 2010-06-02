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
import org.eclipse.photran.internal.core.refactoring.AddOnlyToUseStmtRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Add Only to Use refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang - Modified to add support for marker based testing, documentation
 */

public class AddOnlyToUseTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<AddOnlyToUseStmtRefactoring>
{
    private static final String DIR = "refactoring-test-code/add-only-to-use-stmt";

    public static Test suite() throws Exception
    {
        return new AddOnlyToUseTestSuite();
    }

    public AddOnlyToUseTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Add only to use refactoring in",
              DIR,
              AddOnlyToUseStmtRefactoring.class);
    }

    /**
     * Overridden method to allow marker to hold multiple file names
     * 
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol,
     *                       filename1, filname2, ..., result
     * Note: result is either pass, fail-initial, or fail-final
     */
    
    @Override
    protected boolean initializeRefactoring(AddOnlyToUseStmtRefactoring refactoring,
                                            IFile file,
                                            TextSelection selection,
                                            String[] markerText)
    {
        boolean result = super.initializeRefactoring(refactoring, file, selection, markerText);
        
        //loop to collect multiple files
        for (int i = 4; i < markerText.length-1; i++)
        {
            String fileToAdd = markerText[i]; //file field(s) are just before result
            if (!fileToAdd.equals(""))
                refactoring.addToOnlyList(fileToAdd);
        }
        
        return result;
    }
    
    /**
     * Method that prevents compilation of test files we know aren't supposed to compile
     */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
        return !fileContainingMarker.getName().equalsIgnoreCase("test6.f90");
    }
}
