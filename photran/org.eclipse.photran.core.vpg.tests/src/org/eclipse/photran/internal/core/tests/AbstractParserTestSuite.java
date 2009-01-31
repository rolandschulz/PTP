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
package org.eclipse.photran.internal.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestSuite;

/**
 * Attempts to parse all of the Fortran source code found in the subdirectories of a particular directory.
 * Created by the various classes in org.eclipse.photran.internal.core.tests.parser.
 * 
 * @author joverbey
 */
public abstract class AbstractParserTestSuite extends TestSuite
{
    public static final String TEST_ROOT = "../org.eclipse.photran.core.vpg.tests/parser-test-code/";
    
    protected String directory;
    protected boolean isFixedForm;
    
    public AbstractParserTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        this.directory = getFullPath(directorySuffix);
        this.isFixedForm = isFixedForm;

        setName(getDescription(directorySuffix, isFixedForm, mustExist));

        File dir = new File(directory);
        if (dir.exists()) processDirectory(dir);
    }

    protected String getDescription(String directorySuffix, boolean isFixedForm, boolean mustExist)
    {
        String dir = getFullPath(directorySuffix);
        
        StringBuffer sb = new StringBuffer(256);
        sb.append(describeTestAction());
        sb.append(" ");
        sb.append(directorySuffix);
        sb.append(isFixedForm ? " (fixed format)" : " (free format)");
        String message = sb.toString();
        
        if (!new File(dir).exists())
        {
            if (mustExist)
                throw new Error("Unable to find directory " + dir + " (working directory is " + getWorkingDirectory() + ")");
            else
                message = "NOTE: Confidential parser tests are not installed on this system: " + dir + " does not exist";
        }
        
        return message;
    }

    private String getWorkingDirectory()
    {
        try
        {
            return new File(".").getCanonicalPath();
        }
        catch (IOException e)
        {
            return "???";
        }
    }

    protected abstract String describeTestAction();

    private static String getFullPath(String directorySuffix)
    {
        return TEST_ROOT + directorySuffix + (directorySuffix.endsWith("/") ? "" : "/");
    }
    
    private void processDirectory(File dir)
    {
        if (isFixedForm)
            for (File file : dir.listFiles(FIXED_FORM_FILENAME_FILTER))
                addTest(createTestFor(file, isFixedForm, describe(file)));
        else
            for (File file : dir.listFiles(FREE_FORM_FILENAME_FILTER))
                addTest(createTestFor(file, isFixedForm, describe(file)));
        
        for (File subdir : dir.listFiles(DIRECTORY_FILTER))
            processDirectory(subdir);
    }

    protected abstract AbstractParserTestCase createTestFor(File file, boolean isFixedForm, String fileDescription);
    
    private String describe(File file)
    {
        return file.getAbsolutePath().substring(directory.length());
    }

    private static FilenameFilter FREE_FORM_FILENAME_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            return (name.endsWith(".f90")
                || name.endsWith(".f03")
                || name.endsWith(".F90")
                || name.endsWith(".F03")
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

    private static FileFilter DIRECTORY_FILTER = new FileFilter()
    {
        public boolean accept(File f)
        {
            return f.isDirectory() && !f.getName().startsWith("XXX");
        }
    };
}