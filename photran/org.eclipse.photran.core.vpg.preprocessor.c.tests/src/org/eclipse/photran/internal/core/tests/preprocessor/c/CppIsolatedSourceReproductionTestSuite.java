/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.tests.preprocessor.c;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Test suite for CppSourceReproductionTestCase's. Based on the
 * code from SourceReproductionTestSuite.
 * @author Matthew Michelotti
 */
public abstract class CppIsolatedSourceReproductionTestSuite extends AbstractCppTestSuite
{
    public CppIsolatedSourceReproductionTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist, boolean print) throws FileNotFoundException, IOException
    {
        super(directorySuffix, isFixedForm, mustExist, print);
    }

    public CppIsolatedSourceReproductionTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        this(directorySuffix, isFixedForm, mustExist, true);
    }

    @Override
    protected String describeTestAction()
    {
        return "Reproduce unpreprocessed source code for";
    }

    @Override
    protected AbstractCppTestCase createTestFor(File file, boolean isFixedForm, String fileDescription)
    {
        return new CppIsolatedSourceReproductionTestCase(file, isFixedForm, fileDescription, print);
    }
}
