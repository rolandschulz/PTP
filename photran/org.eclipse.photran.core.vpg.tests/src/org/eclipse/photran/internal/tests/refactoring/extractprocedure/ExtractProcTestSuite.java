/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring.extractprocedure;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ExtractProcTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        suite.addTest(getSuiteFor("test01-simple.f90"));
        suite.addTest(getSuiteFor("test02-locals.f90"));
        suite.addTest(getSuiteFor("test03-locals2.f90"));
        suite.addTest(getSuiteFor("test04-in-loop.f90"));
        suite.addTest(getSuiteFor("test05-parameter.f90"));
        suite.addTest(getSuiteFor("test06-parameter.f90"));
        suite.addTest(getSuiteFor("test07-attribs.f90"));
        return suite;
    }
    
    private static TestSuite getSuiteFor(String baseFilename)
    {
        TestSuite subSuite = new TestSuite("Extracting Procedures from " + baseFilename);
        subSuite.addTest(new ExtractProcTestCase(baseFilename, null));
        return subSuite;
    }
}
