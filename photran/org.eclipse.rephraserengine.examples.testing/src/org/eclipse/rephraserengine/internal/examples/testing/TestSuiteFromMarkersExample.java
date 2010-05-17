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

import junit.framework.Assert;
import junit.framework.Test;

import org.eclipse.rephraserengine.core.util.StringUtil;
import org.eclipse.rephraserengine.testing.junit3.TestSuiteFromMarkers;

/**
 * Simple example illustrating how to use {@link TestSuiteFromMarkers}.
 * 
 * @author Jeff Overbey
 */
public class TestSuiteFromMarkersExample extends TestSuiteFromMarkers
{
    public static Test suite() throws Exception
    {
        return new TestSuiteFromMarkersExample();
    }

    public TestSuiteFromMarkersExample() throws Exception
    {
        super("Running TestSuiteFromMarkersExample in", "<<<", new File("test-files-2"), ".txt");
    }

    @Override
    protected void test(File fileContainingMarker,
                        int markerOffset,
                        String markerText) throws Exception
    {
        String[] lineColStrings = markerText.split(",");
        Assert.assertTrue(lineColStrings.length >= 2);
        int line = Integer.parseInt(lineColStrings[0].trim());
        int col = Integer.parseInt(lineColStrings[1].trim());
        
        String fileContents = StringUtil.read(fileContainingMarker);
        int offset = StringUtil.offsetOf(line, col, fileContents);
        Assert.assertTrue(offset >= 0);
        Assert.assertEquals("is", fileContents.substring(offset, offset+2));
    }
}
