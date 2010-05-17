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
package org.eclipse.photran.internal.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromFiles;

/**
 * Base class for a test suite that contains one test for each file in a directory.
 * <p>
 * This class takes a given directory (usually in the org.eclipse.photran.core.vpg.tests
 * source tree), finds all of the Fortran source files in that directory or a subdirectory,
 * and then creates a test case for each file.
 * <p>
 * The creation of the test case is delegated to {@link #createTestFor(File, boolean, String)},
 * which subclasses must override.
 * 
 * @author Jeff Overbey
 * 
 * @see PhotranASTTestCase
 */
public abstract class PhotranTestSuiteFromFiles extends GeneralTestSuiteFromFiles
{
    public static final String TEST_ROOT = "../org.eclipse.photran.core.vpg.tests/parser-test-code/";
    
    protected String directory;
    protected boolean isFixedForm;
    
    public PhotranTestSuiteFromFiles(String description, String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        super(description + (isFixedForm ? " fixed form" : " free form"),
              directorySuffix,
              isFixedForm ? FIXED_FORM_FILENAME_FILTER : FREE_FORM_FILENAME_FILTER,
              mustExist,
              isFixedForm);
    }
    
    @Override protected void initialize(Object... initializationData)
    {
        this.isFixedForm = (Boolean)initializationData[0];
    }

    @Override protected String getRootDirectory()
    {
        return TEST_ROOT;
    }

    @Override protected String nameOfTextFileContainingFilesToSkip()
    {
        return "PHOTRAN-PARSER-ERRORS.txt";
    }

    private static FilenameFilter FREE_FORM_FILENAME_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            return (name.endsWith(".f90")
                || name.endsWith(".f03")
                || name.endsWith(".f08")
                || name.endsWith(".F90")
                || name.endsWith(".F03")
                || name.endsWith(".F08")
                || name.endsWith(".FRE")) && !name.startsWith("XXX");
        }
    };

    private static FilenameFilter FIXED_FORM_FILENAME_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            return (name.endsWith(".f")
                || name.endsWith(".FIX")) && !name.startsWith("XXX");
        }
    };
    
    @Override protected final Test createTestFor(File file)
    {
        return createTestFor(file, isFixedForm, describe(file));
    }
    
    protected abstract PhotranASTTestCase createTestFor(File file, boolean isFixedForm, String fileDescription);
}