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

public abstract class LoopReplacerSRTestSuite extends AbstractParserTestSuite
{
    public LoopReplacerSRTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        super("Replace loops and reproduce source code for", directorySuffix, isFixedForm, mustExist);
    }

    @Override
    protected AbstractParserTestCase createTestFor(File file, boolean isFixedForm, String fileDescription)
    {
        return new LoopReplacerSRTestCase(file, isFixedForm, fileDescription);
    }
}
