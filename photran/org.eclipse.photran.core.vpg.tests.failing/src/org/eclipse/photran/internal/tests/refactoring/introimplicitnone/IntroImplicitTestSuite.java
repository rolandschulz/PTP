package org.eclipse.photran.internal.tests.refactoring.introimplicitnone;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.tests.refactoring.introimplicitnone.IntroImplicitTestCase;

public class IntroImplicitTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        suite.addTest(getSuiteFor("hello.f90"));
        suite.addTest(getSuiteFor("hello2.f90"));
        return suite;
    }

    private static TestSuite getSuiteFor(String baseFilename)
    {
        TestSuite subSuite = new TestSuite("Introducing Implicit None in " + baseFilename);
        subSuite.addTest(new IntroImplicitTestCase(baseFilename, new LineCol(1, 9), true));
        return subSuite;
    }
}
