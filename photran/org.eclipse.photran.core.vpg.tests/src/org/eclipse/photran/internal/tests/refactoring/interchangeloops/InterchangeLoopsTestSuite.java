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
package org.eclipse.photran.internal.tests.refactoring.interchangeloops;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author Tim
 */
public class InterchangeLoopsTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        suite.addTest(getSuiteFor("test01-simple.f90"));
        suite.addTest(getSuiteFor("test02-simple.f90"));
        suite.addTest(getFailSuiteFor("test-single-loop.f90"));
        suite.addTest(getSuiteFor("test-triple-loop.f90"));
        suite.addTest(getSuiteFor("test-triple-loop2.f90"));
        return suite;
    }
    
    private static TestSuite getSuiteFor(String baseFilename)
    {
        TestSuite subSuite = new TestSuite("Interchanging loops in " + baseFilename);
        subSuite.addTest(new InterchangeLoopsTestCase(baseFilename, null));
        return subSuite;
    }
    
    private static TestSuite getFailSuiteFor(String baseFilename)
    {
        TestSuite subSuite = new TestSuite("Interchanging loops in " + baseFilename);
        subSuite.addTest(new InterchangeLoopsTestCase.InterchangeLoopsFailureTest(baseFilename, null));
        return subSuite;
    }
}
