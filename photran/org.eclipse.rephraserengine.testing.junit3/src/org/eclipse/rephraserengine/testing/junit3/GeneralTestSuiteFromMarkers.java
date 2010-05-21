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
import java.io.IOException;

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
 * @see TestSuiteFromMarkers
 * 
 * @since 2.0
 */
public abstract class GeneralTestSuiteFromMarkers extends TestSuite
{
    protected final String description;
    protected final String marker;
    
    /**
     * Constructor.  Creates this {@link TestSuite} and populates it with test cases.
     * 
     * @param description
     * @param marker
     * @param fileOrDirectory
     * @param filenameFilter
     * @param initializationData these arguments (if any) will be passed directly to {@link #initialize(Object...)} before adding tests to the test suite
     * @throws Exception
     */
    public GeneralTestSuiteFromMarkers(String description, String marker, File fileOrDirectory, FilenameFilter filenameFilter, Object... initializationData) throws Exception
    {
        this.description = description;
        this.marker = marker;
        setName(getDescription(fileOrDirectory));
        
        initialize(initializationData);
        
        addTestsForFileOrDirectory(fileOrDirectory, filenameFilter);

        if (countTestCases() == 0)
            throw new Exception("No markers of the form " + marker + " found in " + fileOrDirectory.getName());
    }

    /**
     * Callback method which is invoked before adding tests to this test suite.
     * <p>
     * This may be overridden to configure the object (e.g., set field values) before tests are added.
     * 
     * @param initializationData the <code>initializationData</code> argument(s) passed to the constructor
     */
    protected void initialize(Object... initializationData)
    {
    }

    public GeneralTestSuiteFromMarkers(String description, String marker, File fileOrDirectory, final String filenameExtension)
        throws Exception
    {
        this(description, marker, fileOrDirectory, new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(filenameExtension);
            }
        });
    }

    private void addTestsForFileOrDirectory(File fileOrDirectory, FilenameFilter filenameFilter) throws Exception
    {
        if (!fileOrDirectory.exists())
            throw new FileNotFoundException(fileOrDirectory.getAbsolutePath() + " not found");
        if (!fileOrDirectory.canRead())
            throw new IOException(fileOrDirectory.getAbsolutePath() + " cannot be read");

        if (fileOrDirectory.isDirectory())
            for (File file : fileOrDirectory.listFiles(filenameFilter))
                addTestsForFileOrDirectory(file, filenameFilter);
        else
            addTestForFile(fileOrDirectory);
    }

    private void addTestForFile(File file) throws IOException, Exception
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

            this.addTest(createTestFor(file, index,
                fileContents.substring(index+marker.length(), markerEnd).trim()));
        }
    }

    protected String getDescription(File fileOrDirectory)
    {
        StringBuffer sb = new StringBuffer(256);
        sb.append(description);
        sb.append(' ');
        sb.append(fileOrDirectory.getName());
        String message = sb.toString();

        if (!fileOrDirectory.exists())
        {
            message = "NOTE: Some optional test files are not present: directory " + fileOrDirectory
                + " does not exist";
        }

        return message;
    }
 
    public void test() {} // to keep JUnit quiet

    protected abstract Test createTestFor(File fileContainingMarker, int markerOffset, String markerText) throws Exception;
}