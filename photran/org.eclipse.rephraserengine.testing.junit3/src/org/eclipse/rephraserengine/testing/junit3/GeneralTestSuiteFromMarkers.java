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
package org.eclipse.rephraserengine.testing.junit3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.rephraserengine.core.util.StringUtil;

/**
 * A {@link TestSuite} containing a test for each <i>marker</i> found in the files in a particular
 * directory.  A <i>marker</i> is an arbitrary string (Photran uses <pre>!&lt;&lt;&lt;&lt;&lt;</pre>),
 * since <pre>!</pre> is the comment character in Fortran) followed by arbitrary text which continues
 * to the end of the line or the next marker, whichever appears first.
 * <p>
 * Most people will want to subclass from {@link TestSuiteFromMarkers} instead.
 * <p>
 * Subclasses must override {@link #createTestFor(List, File, int, String))}.
 * 
 * @author Jeff Overbey
 * 
 * @see TestSuiteFromMarkers
 */
public abstract class GeneralTestSuiteFromMarkers extends TestSuite
{
    protected final String description;
    protected final String marker;
    
    public GeneralTestSuiteFromMarkers(String description, String marker, String fileOrDirectory, FilenameFilter filenameFilter) throws Exception
    {
        this.description = description;
        this.marker = marker;
        setName(getDescription(fileOrDirectory));
        addTestsFromFiles(collectFiles(fileOrDirectory, filenameFilter));
    }

    public GeneralTestSuiteFromMarkers(String description, String marker, String directorySuffix, final String filenameExtension)
        throws Exception
    {
        this(description, marker, directorySuffix, new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(filenameExtension);
            }
        });
    }

    private List<File> collectFiles(String fileOrDirName, FilenameFilter filenameFilter) throws FileNotFoundException
    {
        File fileOrDirectory = new File(fileOrDirName);
        if (!fileOrDirectory.exists())
            throw new FileNotFoundException(fileOrDirectory.getAbsolutePath() + " not found");
        
        if (fileOrDirectory.isDirectory())
        {
            List<File> files = Arrays.<File>asList(fileOrDirectory.listFiles(filenameFilter));
            if (files.isEmpty())
                throw new FileNotFoundException("No matching test files found in " + fileOrDirectory.getAbsolutePath());
            return files;
        }
        else
        {
            return Collections.<File>singletonList(fileOrDirectory);
        }
    }

    protected String getDescription(String fileOrDirectory)
    {
        StringBuffer sb = new StringBuffer(256);
        sb.append(description);
        sb.append(' ');
        sb.append(fileOrDirectory);
        String message = sb.toString();

        if (!new File(fileOrDirectory).exists())
        {
            message = "NOTE: Some optional test files are not present: directory " + fileOrDirectory
                + " does not exist";
        }

        return message;
    }

    protected void addTestsFromFiles(List<File> files) throws Exception
    {
         for (File file : files)
        {
            String fileContents = StringUtil.read(file);
            for (int index = fileContents.indexOf(marker);
                 index >= 0;
                 index = fileContents.indexOf(marker, index+1))
            {
                int endOfLine = fileContents.indexOf('\n', index);
                if (endOfLine < 0) endOfLine = fileContents.length();
                
                int nextMarker = fileContents.indexOf(marker, index+1);
                if (nextMarker < 0) nextMarker = fileContents.length();
                
                int markerEnd = Math.min(endOfLine, nextMarker);

                this.addTest(createTestFor(files, file, index,
                    fileContents.substring(index+marker.length(), markerEnd).trim()));
            }
        }

        if (countTestCases() == 0)
            throw new Exception("No markers of the form " + marker + " found in " + files);
    }
 
    public void test() {} // to keep JUnit quiet

    protected abstract Test createTestFor(List<File> allFiles, File fileContainingMarker, int markerOffset, String markerText);
}