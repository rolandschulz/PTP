/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.examples.testing;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.rephraserengine.core.util.StringUtil;
import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromMarkers;

/**
 * Simple example illustrating how to use {@link GeneralTestSuiteFromMarkers}.
 * 
 * @author Jeff Overbey
 */
public class GeneralTestSuiteFromMarkersExample extends GeneralTestSuiteFromMarkers
{
    public static Test suite() throws Exception
    {
        return new GeneralTestSuiteFromMarkersExample();
    }

    public GeneralTestSuiteFromMarkersExample() throws Exception
    {
        super("Running GeneralTestSuiteFromMarkersExample in", "<<<", new File("test-files-2"), ".txt");
    }

    // To prevent the JUnit runner from trying to instantiate and run ExampleTestCase,
    // we make it abstract and instantiate it as an anonymous inner class

    public static abstract class ExampleTestCase extends TestCase
    {
        private final File fileContainingMarker;
        private final String markerText;

        public ExampleTestCase(File fileContainingMarker, String markerText)
        {
            super("testExample"); // Argument is the name of the test method to run
            this.fileContainingMarker = fileContainingMarker;
            this.markerText = markerText;
        }

        public void testExample() throws IOException
        {
            String[] lineColStrings = markerText.split(",");
            for (int i = 0; i < lineColStrings.length; i++)
                lineColStrings[i] = lineColStrings[i].trim();
            
            assertTrue(lineColStrings.length >= 2);
            int line = Integer.parseInt(lineColStrings[0]);
            int col = Integer.parseInt(lineColStrings[1]);
            
            String fileContents = StringUtil.read(fileContainingMarker);
            int offset = StringUtil.offsetOf(line, col, fileContents);
            assertTrue(offset >= 0);
            assertEquals("is", fileContents.substring(offset, offset+2));
        }
    }

    @Override
    protected Test createTestFor(File fileContainingMarker,
                                 int markerOffset,
                                 String markerText)
    {
        TestSuite suite = new TestSuite(fileContainingMarker.getName() + " - " + markerText);
        suite.addTest(new ExampleTestCase(fileContainingMarker, markerText) {});
        return suite;
    }
}
