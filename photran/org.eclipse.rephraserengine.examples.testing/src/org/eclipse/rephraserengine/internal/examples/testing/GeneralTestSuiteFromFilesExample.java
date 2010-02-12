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
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.rephraserengine.core.util.StringUtil;
import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromFiles;

/**
 * Simple example illustrating how to use {@link GeneralTestSuiteFromFiles}.
 * 
 * @author Jeff Overbey
 */
public class GeneralTestSuiteFromFilesExample extends GeneralTestSuiteFromFiles
{
    public static Test suite() throws Exception
    {
        return new GeneralTestSuiteFromFilesExample();
    }

    public GeneralTestSuiteFromFilesExample() throws FileNotFoundException, IOException
    {
        super("Running GeneralTestSuiteFromFilesExample on", "test-files-1", ".txt");
    }

    // To prevent the JUnit runner from trying to instantiate and run ExampleTestCase,
    // we make it abstract and instantiate it as an anonymous inner class

    public static abstract class ExampleTestCase extends TestCase
    {
        private final File file;
        
        public ExampleTestCase(File file)
        {
            super("testExample"); // Argument is the name of the test method to run
            this.file = file;
        }

        public void testExample() throws IOException
        {
            assertEquals("This is " + file.getName().toLowerCase(), StringUtil.read(file).trim());
        }
    }
    
    @Override
    protected Test createTestFor(File file)
    {
        return new ExampleTestCase(file) {};
    }
}
