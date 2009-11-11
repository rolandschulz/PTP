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
import java.util.Set;

/**
 * This is a special instance of {@link ParserTestSuite} which attempts to parse only files listed
 * in a PHOTRAN-PARSER-ERRORS.txt file in the project's directory.
 * 
 * @author joverbey
 */
public abstract class FailingParserTestSuite extends ParserTestSuite
{
    public FailingParserTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        super(directorySuffix, isFixedForm, mustExist);
    }

    @Override protected boolean shouldSkip(File file, Set<String> filenamesToSkip)
    {
        return !super.shouldSkip(file, filenamesToSkip);
    }

}
