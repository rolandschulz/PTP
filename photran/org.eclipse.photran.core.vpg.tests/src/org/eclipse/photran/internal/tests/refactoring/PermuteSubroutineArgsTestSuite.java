/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Fotzler, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.PermuteSubroutineArgsRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Test suite for the change subroutine signature refactoring.
 * 
 * @author Matthew Fotzler
 */
public class PermuteSubroutineArgsTestSuite extends PhotranRefactoringTestSuiteFromMarkers<PermuteSubroutineArgsRefactoring>
{
    private static final String DIR = "refactoring-test-code/permute-subroutine-args";

    public PermuteSubroutineArgsTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Permute Subroutine Arguments refactoring in",
              DIR,
              PermuteSubroutineArgsRefactoring.class);
    }

    public static Test suite() throws Exception
    {
        return new PermuteSubroutineArgsTestSuite();
    }
    
    // Marker format is !<<<<< startRow,startCol,endRow,endCol,[sigma],pass/fail-initial/fail-final
    // where sigma is the desired permutation of the subroutine arguments given by the selection
    @Override
    protected boolean initializeRefactoring(PermuteSubroutineArgsRefactoring refactoring,
                                            IFile file,
                                            TextSelection selection,
                                            String[] markerText)
    {
        boolean result = super.initializeRefactoring(refactoring, file, selection, markerText);
        List<Integer> sigma = new ArrayList<Integer>();

        for (int i = 4; i < markerText.length-1; i++)
        {
            String nextElement = markerText[i];
            
            sigma.add(Integer.parseInt(nextElement));
        }
        
        refactoring.setSigma(sigma);
        
        return result;
    }

    /** Prevents the compilation of tests we know don't compile */
    @Override protected boolean shouldCompile(IFile fileContainingMarker)
    {
       return false;
    }
}
