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
package org.eclipse.photran.internal.tests.refactoring.datatoparameter;

import junit.framework.Test;

import org.eclipse.photran.internal.core.refactoring.DataToParameterRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Data to Parameter refactoring.
 *
 * @author Tim Yuvashev
 * @author Jeff Overbey
 */
public class DataToParameterTestSuite extends PhotranRefactoringTestSuiteFromMarkers<DataToParameterRefactoring>
{
    private static final String DIR = "refactoring-test-code/data-to-parameter";

    public static Test suite() throws Exception
    {
        return new DataToParameterTestSuite();
    }

    public DataToParameterTestSuite() throws Exception
    {
        super(Activator.getDefault(),
              "Running Data to Parameter refactoring in",
              DIR,
              DataToParameterRefactoring.class);
    }
}
