/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.photran.internal.core.refactoring.SafeDeleteInternalSubprogramRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Safe Delete Internal Subprogram refactoring.
 * 
 * @author Abhishek Sharma
 */
public class SafeDeleteInternalSubprogramRefactoringTestSuite
    extends PhotranRefactoringTestSuiteFromMarkers<SafeDeleteInternalSubprogramRefactoring>
{
    private static final String DIR = "refactoring-test-code/Safe-Delete-Internal-Subprogram" ;

    public static Test suite()throws Exception
    {
        return new SafeDeleteInternalSubprogramRefactoringTestSuite() ;
    }

    protected SafeDeleteInternalSubprogramRefactoringTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Safe-Delete Internal Subprogram test in",
              DIR,
              SafeDeleteInternalSubprogramRefactoring.class);
    }
   
}
