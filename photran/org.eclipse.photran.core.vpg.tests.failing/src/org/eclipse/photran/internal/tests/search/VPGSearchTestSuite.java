package org.eclipse.photran.internal.tests.search;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.photran.internal.tests.search.VPGSearchTestCase;
import org.eclipse.photran.internal.ui.search.VPGSearchQuery;
import org.eclipse.search.ui.text.Match;

/**
 * The suite of tests for testing search functionality
 * 
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 */
public class VPGSearchTestSuite extends TestSuite
{   
    /**
     * Adds all the tests to the suite for searching tests
     */
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        ArrayList<Match> matches = new ArrayList<Match>();
        
        //test 4
        //Tests FIND_REFERENCES
        matches = new ArrayList<Match>();
        matches.add(new Match("foo.f90",30,1));
        matches.add(new Match("implicitTest.f90",22,1));
        suite.addTest(getSuiteFor("testFour", new VPGSearchTestCase("a",
            VPGSearchQuery.FIND_REFERENCES | VPGSearchQuery.FIND_ALL_TYPES, matches, false)));
               
        return suite;
    }
    
    private static TestSuite getSuiteFor(String baseFilename, VPGSearchTestCase testCase)
    {
        TestSuite subSuite = new TestSuite(baseFilename);
        subSuite.addTest(testCase);
        return subSuite;
    }
}
