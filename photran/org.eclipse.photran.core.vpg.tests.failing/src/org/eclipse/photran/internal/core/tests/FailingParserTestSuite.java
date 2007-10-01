package org.eclipse.photran.internal.core.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FailingParserTestSuite extends TestSuite
{
    public FailingParserTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist)
    {
        super("TODO: Some " + (mustExist ? "" : "confidential ") + (isFixedForm ? "fixed" : "free") + " form parser tests from " + ParserTestSuite.TEST_ROOT + directorySuffix + " are known to fail; revisit later");
        
        addTest(new TestCase() { @SuppressWarnings("unused") public void test() {} }); // Prevent JUnit warning about empty test suite
    }
}
