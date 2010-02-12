/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class SourceReproductionTestSuite extends AbstractParserTestSuite
{
    public SourceReproductionTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        super("Reproduce source code for", directorySuffix, isFixedForm, mustExist);
    }

    @Override
    protected AbstractParserTestCase createTestFor(File file, boolean isFixedForm, String fileDescription)
    {
        return new SourceReproductionTestCase(file, isFixedForm, fileDescription);
    }
}
