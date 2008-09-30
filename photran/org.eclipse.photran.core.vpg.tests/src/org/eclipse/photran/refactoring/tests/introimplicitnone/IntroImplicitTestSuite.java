package org.eclipse.photran.refactoring.tests.introimplicitnone;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.photran.core.vpg.util.LineCol;

public class IntroImplicitTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        suite.addTest(getSuiteFor("bigexpression.f90"));
        suite.addTest(getSuiteFor("fortran.f90"));
        suite.addTest(getSuiteFor("crazyimplicits.f90"));
        return suite;
    }
    
    private static TestSuite getSuiteFor(String baseFilename)
    {
        TestSuite subSuite = new TestSuite("Introducing Implicit None in " + baseFilename);
        subSuite.addTest(new IntroImplicitTestCase(baseFilename, new LineCol(1, 9)));
        return subSuite;
    }
}
