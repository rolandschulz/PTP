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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        if (dir.exists()) processDirectory(dir, getFilesToSkip(dir));
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

    protected Set<String> getFilesToSkip(File dir)
    {
        File list = new File(dir, "PHOTRAN-PARSER-ERRORS.txt");
        if (list.exists() && list.canRead())
            return filesListedIn(list);
        else
            return Collections.<String>emptySet();
    }

    protected Set<String> filesListedIn(File list)
    {
        try
        {
            Set<String> result = new HashSet<String>();
            
            BufferedReader r = new BufferedReader(new FileReader(list));
            for (String line = r.readLine(); line != null; line = r.readLine())
            {
                line = line.trim().toLowerCase();
                if (!line.equals(""))
                    result.add(line.trim().toLowerCase());
            }
            
            return result;
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
    }

    private void processDirectory(File dir, Set<String> filenamesToSkip)
    {
        addTestsFor(
            dir.listFiles(isFixedForm ? FIXED_FORM_FILENAME_FILTER : FREE_FORM_FILENAME_FILTER),
            filenamesToSkip);
        
        for (File subdir : dir.listFiles(DIRECTORY_FILTER))
            processDirectory(subdir, filenamesToSkip);
    }
    
    private void addTestsFor(File[] filesInDirectory, Set<String> filenamesToSkip)
    {
        for (File file : filesInDirectory)
            if (!shouldSkip(file, filenamesToSkip))
                addTest(createTestFor(file, isFixedForm, describe(file)));
    }

    protected boolean shouldSkip(File file, Set<String> filenamesToSkip)
    {
        return filenamesToSkip.contains(file.getName().toLowerCase());
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

    private static FileFilter DIRECTORY_FILTER = new FileFilter()
    {
        public boolean accept(File f)
        {
            return f.isDirectory() && !f.getName().startsWith("XXX");
        }
    };
}