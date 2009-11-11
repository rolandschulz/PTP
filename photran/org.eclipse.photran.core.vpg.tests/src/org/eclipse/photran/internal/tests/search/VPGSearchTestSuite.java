package org.eclipse.photran.internal.tests.search;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

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
        
        
        //test 1
        //Does a basic test across two different files
        matches.add(new Match("foo.f90",23,1));
        matches.add(new Match("foo.f90",30,1));
        matches.add(new Match("implicitTest.f90",22,1));
        suite.addTest(getSuiteFor("testOne", new VPGSearchTestCase("a",
            VPGSearchQuery.FIND_ALL_OCCURANCES | VPGSearchQuery.FIND_ALL_TYPES, matches, false)));
        
        //test 2
        //Same as 2 except different search string
        matches = new ArrayList<Match>();
        matches.add(new Match("foo.f90",26,1));
        matches.add(new Match("implicitTest.f90",67,1));
        suite.addTest(getSuiteFor("testTwo", new VPGSearchTestCase("b",
            VPGSearchQuery.FIND_ALL_OCCURANCES | VPGSearchQuery.FIND_ALL_TYPES, matches, false)));

        //test 3
        //Tests FIND_DECLARATIONS to make sure it is different from FIND_ALL_OCCURANCES
        matches = new ArrayList<Match>();
        matches.add(new Match("foo.f90",23,1));
        suite.addTest(getSuiteFor("testThree", new VPGSearchTestCase("a",
            VPGSearchQuery.FIND_DECLARATIONS | VPGSearchQuery.FIND_ALL_TYPES, matches, false)));
        
//        //test 4
//        //Tests FIND_REFERENCES
//        matches = new ArrayList<Match>();
//        matches.add(new Match("foo.f90",30,1));
//        matches.add(new Match("implicitTest.f90",22,1));
//        suite.addTest(getSuiteFor("testFour", new VPGSearchTestCase("a",
//            VPGSearchQuery.FIND_REFERENCES | VPGSearchQuery.FIND_ALL_TYPES, matches, false)));
        
        //test 5
        //Tests FIND_FUNCTION to make sure it is different from test 6
        matches = new ArrayList<Match>();
        suite.addTest(getSuiteFor("testFive", new VPGSearchTestCase("main",
            VPGSearchQuery.FIND_ALL_OCCURANCES | VPGSearchQuery.FIND_FUNCTION, matches, false)));
        
        
        //test 6
        //Tests FIND_PROGRAM so it's not the same as FIND_FUNCTION
        matches = new ArrayList<Match>();
        matches.add(new Match("foo.f90",8,4));
        suite.addTest(getSuiteFor("testSix", new VPGSearchTestCase("main",
            VPGSearchQuery.FIND_ALL_OCCURANCES | VPGSearchQuery.FIND_PROGRAM, matches, false)));
        
        //test 7
        //Tests the glob '?'
        suite.addTest(getSuiteFor("testSeven", new VPGSearchTestCase("ma?n",
            VPGSearchQuery.FIND_ALL_OCCURANCES | VPGSearchQuery.FIND_PROGRAM, matches, false)));
        
        //test 8
        //Tests the glob '*'
        suite.addTest(getSuiteFor("testEight", new VPGSearchTestCase("m*n",
            VPGSearchQuery.FIND_ALL_OCCURANCES | VPGSearchQuery.FIND_PROGRAM, matches, false)));

        //test 9
        //Tests regex
        suite.addTest(getSuiteFor("testNine", new VPGSearchTestCase("m.*",
            VPGSearchQuery.FIND_ALL_OCCURANCES | VPGSearchQuery.FIND_PROGRAM, matches, true)));
        
               
        return suite;
    }
    
    
    private static TestSuite getSuiteFor(String baseFilename, VPGSearchTestCase testCase)
    {
        TestSuite subSuite = new TestSuite(baseFilename);
        subSuite.addTest(testCase);
        return subSuite;
    }
}
