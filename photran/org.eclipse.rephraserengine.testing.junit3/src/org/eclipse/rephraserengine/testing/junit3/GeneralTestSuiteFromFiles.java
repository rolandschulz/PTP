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
package org.eclipse.rephraserengine.testing.junit3;

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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A {@link TestSuite} containing a test for each file found in the subdirectories of a particular
 * directory.
 * <p>
 * Most people will want to subclass from {@link TestSuiteFromFiles} instead.
 * <p>
 * Subclasses must override {@link #createTestFor(File)}.
 * 
 * @author Jeff Overbey
 * 
 * @see TestSuiteFromFiles
 */
public abstract class GeneralTestSuiteFromFiles extends TestSuite
{
    protected String testDirectory;

    protected FilenameFilter filenameFilter;

    public GeneralTestSuiteFromFiles(String directorySuffix, FilenameFilter filenameFilter,
        boolean mustExist) throws FileNotFoundException, IOException
    {
        this.testDirectory = getFullPath(directorySuffix);
        this.filenameFilter = filenameFilter;

        setName(getDescription(directorySuffix, mustExist));

        File dir = new File(testDirectory);
        if (!dir.exists())
            throw new FileNotFoundException("Directory " + dir.getAbsolutePath() + " not found");

        processDirectory(dir, getFilesToSkip(dir));

        if (this.countTestCases() == 0)
            throw new FileNotFoundException("No test files found in " + dir.getAbsolutePath());
    }

    public GeneralTestSuiteFromFiles(String directorySuffix, FilenameFilter filenameFilter)
        throws FileNotFoundException, IOException
    {
        this(directorySuffix, filenameFilter, true);
    }

    public GeneralTestSuiteFromFiles(String directorySuffix, final String filenameExtension,
        boolean mustExist) throws FileNotFoundException, IOException
    {
        this(directorySuffix, new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(filenameExtension);
            }
        }, mustExist);
    }

    public GeneralTestSuiteFromFiles(String directorySuffix, final String filenameExtension)
        throws FileNotFoundException, IOException
    {
        this(directorySuffix, filenameExtension, true);
    }

    protected String getDescription(String directorySuffix, boolean mustExist)
    {
        String dir = getFullPath(directorySuffix);

        StringBuffer sb = new StringBuffer(256);
        sb.append(describeTestAction());
        sb.append(' ');
        sb.append(directorySuffix);
        String message = sb.toString();

        if (!new File(dir).exists())
        {
            if (mustExist)
                throw new Error("Unable to find directory " + dir + " (working directory is "
                    + getWorkingDirectory() + ")");
            else
                message = "NOTE: Some optional test files are not present: directory " + dir
                    + " does not exist";
        }

        return message;
    }

    protected String describeTestAction()
    {
        return "";
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

    private String getFullPath(String directorySuffix)
    {
        StringBuilder sb = new StringBuilder();

        String rootDirectory = getRootDirectory();
        sb.append(rootDirectory);
        if (!rootDirectory.equals("") && !rootDirectory.endsWith("/")) sb.append('/');

        sb.append(directorySuffix);
        if (!directorySuffix.endsWith("/")) sb.append('/');

        return sb.toString();
    }

    protected String getRootDirectory()
    {
        return "";
    }

    protected Set<String> getFilesToSkip(File subdirectory)
    {
        File list = new File(subdirectory, nameOfTextFileContainingFilesToSkip());
        if (!list.exists())
            list = new File(testDirectory, nameOfTextFileContainingFilesToSkip());
        if (!list.exists())
            list = new File(getRootDirectory(), nameOfTextFileContainingFilesToSkip());

        if (list.exists() && list.canRead())
            return filesListedIn(list);
        else
            return Collections.<String> emptySet();
    }

    protected String nameOfTextFileContainingFilesToSkip()
    {
        return "FILES-TO-SKIP.txt";
    }

    protected Set<String> filesListedIn(File list)
    {
        try
        {
            Set<String> result = new HashSet<String>();

            BufferedReader r = new BufferedReader(new FileReader(list));
            for (String line = r.readLine(); line != null; line = r.readLine())
            {
                line = line.trim();
                if (!line.equals("")) result.add(line);
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
        addTestsFor(dir.listFiles(filenameFilter), filenamesToSkip);

        for (File subdir : dir.listFiles(DIRECTORY_FILTER))
            processDirectory(subdir, filenamesToSkip);
    }

    private void addTestsFor(File[] filesInDirectory, Set<String> filenamesToSkip)
    {
        for (File file : filesInDirectory)
        {
            if (!shouldSkip(file, filenamesToSkip))
            {
                TestSuite subSuite = new TestSuite(describeTestAction() + " " + describe(file));
                subSuite.addTest(createTestFor(file));
                addTest(subSuite);
            }
        }
    }

    protected boolean shouldSkip(File file, Set<String> filenamesToSkip)
    {
        for (String filename : filenamesToSkip)
            if (file.getAbsolutePath().endsWith(filename)) return true;

        return false;
    }

    protected String describe(File file)
    {
        return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('/') + 1);
    }

    private static FileFilter DIRECTORY_FILTER = new FileFilter()
    {
        public boolean accept(File f)
        {
            return f.isDirectory();
        }
    };

    public void test() {} // to keep JUnit quiet

    protected abstract Test createTestFor(File file);
}