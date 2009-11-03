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
package org.eclipse.photran.internal.core.tests.minonlylist;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.photran.internal.core.util.LineCol;
/**
 *
 * @author Kurt Hendle
 */
public class MinOnlyListTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();

        //1. functionality test - all module entities used, remove list
        String[] test1Files = {"test1.f90", "module.f90"};
        suite.addTest(successTest(test1Files, new LineCol(2,9), 6));

        //2. functionality test - remove the unused module entity from the list
        String[] test2Files = {"test2.f90", "module.f90"};
        suite.addTest(successTest(test2Files, new LineCol(2,9), 6));

        //3. functionality test - no module entities used, remove the use statement
        String[] test3Files = {"test3.f90", "module.f90"};
        suite.addTest(successTest(test3Files, new LineCol(2,9), 6));

        //4. precondition test - select empty module name
        String[] test4and5Files = {"test4_5.f90", "module.f90"};
        suite.addTest(preconditionTest(test4and5Files, new LineCol(2,9), 0));

        //5. precondition test - empty module selected
        suite.addTest(preconditionTest(test4and5Files, new LineCol(3,9), 11));

        //6. precondition test - nonexistant module selected
        String[] test6Files = {"test6.f90", "module.f90"};
        suite.addTest(preconditionTest(test6Files, new LineCol(3,9), 11));

        //7. precondition test - select entity which is not module
        String[] test7Files = {"test7.f90", "module.f90"};
        suite.addTest(preconditionTest(test7Files, new LineCol(4,16), 1));
        
        //8. functionality test - renamed module var, module same file
        String[] test8Files = {"program3.f90"};
        suite.addTest(successTest(test8Files, new LineCol(6,9), 1));
        
        //9. functionality test - remove an unused entity, module same file
        String[] test9Files = {"program2.f90"};
        suite.addTest(successTest(test9Files, new LineCol(6,9), 1));
        
        //10. functionality test - no only list, 1 unused, module same file
        String[] test10Files = {"program1.f90"};
        suite.addTest(successTest(test10Files, new LineCol(6,9), 1));

        return suite;
    }

    private static TestSuite successTest(String[] filenames, LineCol position, int length)
    {
        TestSuite subSuite = new TestSuite("Minimizing only List - " + filenames[0]);
        subSuite.addTest(new MinOnlyListTestCase(filenames, position, length, false));
        return subSuite;
    }

    private static TestSuite preconditionTest(String[] filenames, LineCol position, int length)
    {
        TestSuite subSuite = new TestSuite("Testing preconditions for minimizing only list - " +
            filenames[0]);
        subSuite.addTest(new MinOnlyListTestCase(filenames, position, length, true));
        return subSuite;
    }
}