/*******************************************************************************
 * Copyright (c) 2010 Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, 
 * Balaji Ambresh Rajkumar and Paramvir Singh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, Balaji Ambresh Rajkumar
 * and Paramvir Singh - Initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.RemoveAssignedGotoRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Remove Assigned Goto refactoring.
 *
 * * @author Andrea Dranberg
 * @author John Hammonds
 * @author Rajashekhar Arasanal
 * @author Balaji Ambresh Rajkumar
 * @author Paramvir Singh
 */
public class RemoveAssignedGotoTestSuite
     extends PhotranRefactoringTestSuiteFromMarkers<RemoveAssignedGotoRefactoring>
{
    private static final String DIR = "refactoring-test-code/remove-assign-gotos";

    public static Test suite() throws Exception
    {
        return new RemoveAssignedGotoTestSuite();
    }

    public RemoveAssignedGotoTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Remove Assigned Goto refactoring in",
              DIR,
              RemoveAssignedGotoRefactoring.class);
    }

    /**
     * Overridden method to allow refactoring to process the 'isDefaultCaseRequired' 
     * field of the marker.
     * 
     * MARKER FORMAT: !<<<<< startLine, startCol, endLine, endCol, isDefaultCaseRequired, result
     * 
     * Note: result is either pass, fail-initial, or fail-final
     */
    @Override
    protected boolean configureRefactoring(RemoveAssignedGotoRefactoring refactoring,
                                           IFile file,
                                           TextSelection selection,
                                           String[] markerText)
    {
        boolean shouldSucceed = super.configureRefactoring(refactoring, file, selection, markerText);
        refactoring.setDefaultSelected(Boolean.parseBoolean(markerText[4]));
        
        return shouldSucceed;
    }
    
    /**
     * Method that prevents compilation of test files we know aren't supposed to compile
     */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
        return ! (fileContainingMarker.getName().equalsIgnoreCase("one_label_no_goto_no_address.f90") ||
        fileContainingMarker.getName().equalsIgnoreCase("integer_label_assign_and_assignment.f90"));
    }
}
