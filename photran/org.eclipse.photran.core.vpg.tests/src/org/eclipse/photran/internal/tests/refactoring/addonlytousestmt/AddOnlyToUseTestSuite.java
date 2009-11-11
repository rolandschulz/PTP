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
package org.eclipse.photran.internal.tests.refactoring.addonlytousestmt;

import org.eclipse.photran.internal.core.util.LineCol;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author Kurt Hendle
 */
public class AddOnlyToUseTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        
        //1. basic test, add no list, leave the file the same
        String[] test1Files = new String[] { "test1.f90", "module2.f90", "module3.f90", "module4.f90" };
        suite.addTest(successTest(test1Files, new LineCol(3,5), 7));
        
        //2. basic test, add list of given names
        String[] test2Files = new String[] { "test2.f90", "module2.f90", "module3.f90", "module4.f90" };
        suite.addTest(successTest(test2Files, new LineCol(3,5), 7, "help_common3", "onlyTest"));
        
        //3. precondition test - give an empty selection for module name
        String[] test3Files = new String[] { "test3.f90", "module2.f90", "module3.f90", "module4.f90", "module5.f90" };
        suite.addTest(finalPreconditionTest(test3Files, new LineCol(3,5), 0));
        
        //4. precondition test - give a non-existent module name
        String[] test4Files = new String[] { "test3.f90", "module2.f90", "module3.f90", "module4.f90", "module5.f90" };
        suite.addTest(finalPreconditionTest(test4Files, new LineCol(8,9), 5));
        
        //5. precondition test - give a module containing no declarations
        String[] test5Files = new String[] { "test3.f90", "module2.f90", "module3.f90", "module4.f90", "module5.f90" };
        suite.addTest(preconditionTest(test5Files, new LineCol(5,5), 7));
        
        //6. precondition test - have entity conflict with local variable
        String[] test6Files = new String[] { "test6.f90", "module4.f90" };  //v name conflict
        suite.addTest(finalPreconditionTest(test6Files, new LineCol(2,9), 7, "f"));
        
        //7. precondition test - have 2 modules in project with same name
        String[] test7Files = new String[] { "test7.f90", "module4.f90" };
        suite.addTest(preconditionTest(test7Files, new LineCol(2,9), 7));
        
        //8. precondition test - have 2 declarations of subroutine in same project
        String[] test8Files = new String[] { "test8.f90", "module4.f90" };
        suite.addTest(finalPreconditionTest(test8Files, new LineCol(2,9), 7, "help_common4"));
        
        return suite;
    }
    
    private static TestSuite successTest(String[] filenames, LineCol position, int length)
    {
        return successTest(filenames, position, length, (String[])null);
    }
    
    private static TestSuite successTest(String[] filenames, LineCol position, int length, String... entityNames)
    {
        TestSuite subSuite = new TestSuite("Adding ONLY clause to USE statement - " + filenames[0]);
        subSuite.addTest(new AddOnlyToUseTestCase(filenames, position, length, false, false, entityNames));
        return subSuite;
    }
    
    private static TestSuite preconditionTest(String[] filenames, LineCol position, int length, String... entityNames)
    {
        TestSuite subSuite = new TestSuite("Testing preconditions for adding ONLY to USE statement - " +
            filenames[0]);
        subSuite.addTest(new AddOnlyToUseTestCase(filenames, position, length, true, false, entityNames));
        return subSuite;
    }
    
    private static TestSuite finalPreconditionTest(String[] filenames, LineCol position, int length, String... entityNames)
    {
        TestSuite subSuite = new TestSuite("Testing final preconditions for adding ONLY to USE statement - " +
            filenames[0]);
        subSuite.addTest(new AddOnlyToUseTestCase(filenames, position, length, false, true, entityNames));
        return subSuite;   
    }
}
