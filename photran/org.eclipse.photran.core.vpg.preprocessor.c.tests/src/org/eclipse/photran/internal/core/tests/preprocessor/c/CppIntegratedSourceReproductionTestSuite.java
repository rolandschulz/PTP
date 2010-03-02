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
package org.eclipse.photran.internal.core.tests.preprocessor.c;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.photran.internal.tests.AbstractParserTestCase;
import org.eclipse.photran.internal.tests.AbstractParserTestSuite;

public abstract class CppIntegratedSourceReproductionTestSuite extends AbstractParserTestSuite
{
    public CppIntegratedSourceReproductionTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
    	// Super constructor will append directorySuffix to
    	// ../org.eclipse.photran.core.vpg.tests/parser-test-code/
        super("Reproduce preprocessed source code for",
              "../../org.eclipse.photran.core.vpg.preprocessor.c.tests/cpp-test-code/" + directorySuffix,
              isFixedForm,
              mustExist);
    }

    @Override
    protected AbstractParserTestCase createTestFor(File file, boolean isFixedForm, String fileDescription)
    {
        return new CppIntegratedSourceReproductionTestCase(file, isFixedForm, fileDescription);
    }
}
