/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.refactoring.tests.keyword;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test Suite for Keyword Case Refactoring
 * @author Kurt Hendle
 */
public class KeywordCaseTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        
        suite.addTest(getSuiteFor("01-simple.f90"));
        suite.addTest(getSuiteFor("01-simple.f90", "02-end-after.f90"));
        
        return suite;
    }
    
    private static TestSuite getSuiteFor(String baseFilename)
    {
        TestSuite subSuite = new TestSuite("Upcasing all keywords in " + baseFilename);
        subSuite.addTest(new KeywordCaseTestCase(baseFilename, false));
        return subSuite;
    }
    
    private static TestSuite getSuiteFor(String... filenames)
    {
        TestSuite subSuite = new TestSuite("Upcasing all keywords in selected files");
        for(String filename : filenames)
        {
            subSuite.addTest(new KeywordCaseTestCase(filename, false));
        }
        return subSuite;
    }
}

