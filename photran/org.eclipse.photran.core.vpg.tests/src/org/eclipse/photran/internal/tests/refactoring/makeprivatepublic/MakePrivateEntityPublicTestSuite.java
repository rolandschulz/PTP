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
package org.eclipse.photran.internal.tests.refactoring.makeprivatepublic;

import org.eclipse.photran.internal.core.util.LineCol;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Kurt Hendle
 */
public class MakePrivateEntityPublicTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();

        //1. functionality test - multiple variables in list
        suite.addTest(successTest("priv1.f90",new LineCol(4,13), 1));

        //2. functionality test - single variable list (simply change private to public)
        suite.addTest(successTest("priv2.f90",new LineCol(5,13), 1));

        //3. functionality test - single variable, private statement in declaration attributes
        suite.addTest(successTest("test3.f90", new LineCol(3,25), 1));

        //4. functionality test - multiple variables, private statement in declaration attributes
        suite.addTest(successTest("test4.f90", new LineCol(3,22), 4));

        //5. functionality test - single var, private stmt and other attributes in declaration
        suite.addTest(successTest("test5.f90", new LineCol(3,35), 5));

        //6. functionality test - mult. vars., private stmt and other attributes in declaration
        suite.addTest(successTest("test6.f90", new LineCol(3,42), 4));

        //7. functionality test - private statement alone, all program entities private
        suite.addTest(successTest("test7.f90", new LineCol(6,23), 4));
        suite.addTest(successTest("test7_1.f90", new LineCol(6,23), 4));

        //8. precondition test - empty selection ""
        suite.addTest(preconditionTest("priv1.f90", new LineCol(3,13), 0));

        //9. precondition test - select variable which is not tagged private
        suite.addTest(preconditionTest("priv1.f90", new LineCol(3,16), 1));
        suite.addTest(preconditionTest("testFile1.f90", new LineCol(4,13), 1));

        //10. precondition test - select a public variable
        suite.addTest(preconditionTest("priv2.f90", new LineCol(6,12), 1));
        suite.addTest(preconditionTest("testFile1.f90", new LineCol(11,15), 1));

        //11. precondition test - test if possible to make definition public
        // NOTE: will fail til ScopingNode#canAddDefinition is added
        // FIXME JO suite.addTest(preconditionTest("main.f90", new LineCol(19,24), 8));

        //12. functionality test - subroutine/function made private by a lone private stmt
        suite.addTest(successTest("test12.f90", new LineCol(6,16), 6));

        //13 precondition test - subroutine/function not made private
        suite.addTest(preconditionTest("test13.f90", new LineCol(16,14), 3));

        return suite;
    }

    private static TestSuite successTest(String filename, LineCol position, int length)
    {
        TestSuite subSuite = new TestSuite("Making private variable public - " + filename);
        subSuite.addTest(new MakePrivateEntityPublicTestCase(filename, position, length, false));
        return subSuite;
    }

    private static TestSuite preconditionTest(String filename, LineCol position, int length)
    {
        TestSuite subSuite = new TestSuite("Testing preconditions for making private entity public - " +
            filename);
        subSuite.addTest(new MakePrivateEntityPublicTestCase(filename, position, length, true));
        return subSuite;
    }
}
