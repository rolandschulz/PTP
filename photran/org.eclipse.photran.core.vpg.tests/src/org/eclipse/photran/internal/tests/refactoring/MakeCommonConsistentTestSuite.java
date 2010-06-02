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
import org.eclipse.photran.internal.core.refactoring.CommonVarNamesRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Common Variable Name refactoring.
 *
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang - Modified to add support for marker based testing, documentation
 */

public class MakeCommonConsistentTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<CommonVarNamesRefactoring>
{
    private static final String DIR = "refactoring-test-code/make-common-var-names-consistent";

    public static Test suite() throws Exception
    {
        return new MakeCommonConsistentTestSuite();
    }

    public MakeCommonConsistentTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Common var names refactoring in",
              DIR,
              CommonVarNamesRefactoring.class);
    }

    /**
     * Overridden method to allow marker to hold entity names
     * 
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, entityName1,
     *                entityName2, ..., result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    @Override
    protected boolean initializeRefactoring(CommonVarNamesRefactoring refactoring,
                                            IFile file,
                                            TextSelection selection,
                                            String[] markerText)
    {   
        boolean result = super.initializeRefactoring(refactoring, file, selection, markerText);
        
        //loop to collect any entity fields from the marker text
        for (int i = 4; i < markerText.length-1; i++)
        {
            String entityToAdd = markerText[i]; //entity field(s) are just before result
            
            if (!entityToAdd.equals(""))
                refactoring.modifyNewName(i - 4, markerText[i]);
        }
        
        return result;
    }
}
