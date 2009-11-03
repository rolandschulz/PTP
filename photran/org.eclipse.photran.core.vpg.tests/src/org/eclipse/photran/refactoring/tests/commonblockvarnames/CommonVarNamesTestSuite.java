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
package org.eclipse.photran.refactoring.tests.commonblockvarnames;

import org.eclipse.photran.internal.core.util.LineCol;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Make Common Variable Names Consistent refactoring
 *
 * @author Kurt Hendle
 * @author Jeff Overbey
 */
public class CommonVarNamesTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();

        // In the following, the line/column refers to a location in the first file listed

        // 1. Basic check across multiple files
        String[] test1Files = new String[] { "common1.f90", "common2.f90", "common3.f90", "common4.f90" };
        suite.addTest(successTest(test1Files, new LineCol(7,9), 5));

        //0. basic test with one file
        //      -used to test the static data cleanup and implicit typing
        suite.addTest(successTest(new String[] {"justCommons1.f90"}, new LineCol(7,13), 5));
        suite.addTest(successTest(new String[] {"justCommons2.f90"}, new LineCol(2,13), 5));

        // 2. Basic check across multiple files with names provided
        String[] test2Files = new String[] { "namesProv1.f90", "namesProv2.f90" };
        suite.addTest(successTest(test2Files, new LineCol(3,13), 5, "a_hello", "b_hello", "c_hello"));

        //suite.addTest(successTest(new String[] {"justCommons1.f90"}, new LineCol(7,13), 5, "i_hello", "j_hello"));

        // 3. Two of the common block variable names should stay the same in one declaration of the common block
        String[] test3Files = new String[] { "sameName1.f90", "sameName2.f90" };
        // FIXME JO suite.addTest(successTest(test3Files, new LineCol(3,13), 5, "a", "b", "this_is_renamed"));

        // 4. One declaration of the common block does not contain all the variables in the block
        String[] test4Files = new String[] { "short1.f90" };
        suite.addTest(successTest(test4Files, new LineCol(4,11), 5, "aaa", "bbb", "ccc"));
        String[] test4Files2 = new String[] { "short2.f90" };
        suite.addTest(successTest(test4Files2, new LineCol(20,11), 5, "aaa", "bbb"));

        // 5. Precondition test -- make sure naming conflict is avoided
        String[] test5Files = new String[] { "common1.f90", "common2.f90", "common3.f90", "common4.f90" };
        //                                                                          vvvvvv conflicts with a subroutine
        suite.addTest(preconditionTest(test5Files, new LineCol(7,9), 5, "a_hello", "helper", "c_hello"));
        //                                                               vvvvvvvvvvvv conflicts with a subroutine
        suite.addTest(preconditionTest(test5Files, new LineCol(7,9), 5, "help_common2", "b_hello", "c_hello"));

        // 6. Precondition tests -- the variables must have the same types in every declaration of the common block
        String[] test6Files = new String[] { "typesDiffer1.f90" };
        suite.addTest(preconditionTest(test6Files, new LineCol(4,11), 5));
        suite.addTest(preconditionTest(test6Files, new LineCol(20,11), 5));
        suite.addTest(preconditionTest(new String[]{"typesDiffer2.f90"}, new LineCol(2,12), 5));

        return suite;
    }

    private static TestSuite successTest(String[] filenames, LineCol position, int length)
    {
        return successTest(filenames, position, length, (String[])null);
    }

    private static TestSuite successTest(String[] filenames, LineCol position, int length, String... newVarNames)
    {
        TestSuite subSuite = new TestSuite("Making COMMON variable names consistent - " + filenames[0]);
        subSuite.addTest(new CommonVarNamesTestCase(filenames, position, length, false, newVarNames));
        return subSuite;
    }

    private static TestSuite preconditionTest(String[] filenames, LineCol position, int length, String... newVarNames)
    {
        TestSuite subSuite = new TestSuite("Testing preconditions for making COMMON variable names consistent - " +
            filenames[0]);
        subSuite.addTest(new CommonVarNamesTestCase(filenames, position, length, true, newVarNames));
        return subSuite;
    }
}
