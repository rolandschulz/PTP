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
package org.eclipse.photran.internal.tests.refactoring.introimplicitnone;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.photran.internal.core.util.LineCol;

public class IntroImplicitTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();

        suite.addTest(getSuiteFor("bigexpression.f90"));
        suite.addTest(getSuiteFor("fortran.f90"));
        suite.addTest(getSuiteFor("crazyimplicits.f90"));
        suite.addTest(getSuiteFor("implicit-subprog.f90", false));

        suite.addTest(getSuiteFor("crazyimplicits.f90", "fortran.f90"));
        suite.addTest(getSuiteFor("crazyimplicits.f90", "fortran.f90", "bigexpression.f90"));

        return suite;
    }

    private static TestSuite getSuiteFor(String baseFilename)
    {
        return getSuiteFor(baseFilename, true);
    }

    private static TestSuite getSuiteFor(String baseFilename, boolean shouldCompileAndRun)
    {
        TestSuite subSuite = new TestSuite("Introducing Implicit None in " + baseFilename);
        subSuite.addTest(new IntroImplicitTestCase(baseFilename, new LineCol(1, 9), shouldCompileAndRun));
        return subSuite;
    }

    private static TestSuite getSuiteFor(String... filenames)
    {
        TestSuite subSuite = new TestSuite("Introducing Implicit None in selected files");
        for(String filename : filenames)
        {
            subSuite.addTest(new IntroImplicitTestCase(filename, new LineCol(1, 9), true));
        }
        return subSuite;
    }
}
