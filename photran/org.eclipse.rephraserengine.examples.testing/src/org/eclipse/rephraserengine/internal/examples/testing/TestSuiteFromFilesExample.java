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

import junit.framework.Assert;
import junit.framework.Test;

import org.eclipse.rephraserengine.core.util.StringUtil;
import org.eclipse.rephraserengine.testing.junit3.TestSuiteFromFiles;

/**
 * Simple example illustrating how to use {@link TestSuiteFromFiles}.
 * 
 * @author Jeff Overbey
 */
public class TestSuiteFromFilesExample extends TestSuiteFromFiles
{
    public static Test suite() throws Exception
    {
        return new TestSuiteFromFilesExample();
    }

    public TestSuiteFromFilesExample() throws FileNotFoundException, IOException
    {
        super("Running TestSuiteFromFilesExample on", "test-files-1", ".txt");
    }

    @Override protected void test(File file) throws Exception
    {
        Assert.assertEquals("This is " + file.getName().toLowerCase(), StringUtil.read(file).trim());
    }
}
