/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/

/**
 * @author Esfar Huq
 * @author Rui Wang - Added additional test cases for reindent by line
 */
package org.eclipse.photran.internal.tests.refactoring.infrastructure;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

public class ReindenterTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        suite.addTest(getSuiteFor("01-simple.f90", Strategy.REINDENT_EACH_LINE));
        suite.addTest(getSuiteFor("02-end-after.f90", Strategy.SHIFT_ENTIRE_BLOCK));
        suite.addTest(getSuiteFor("03-blank-after.f90", Strategy.SHIFT_ENTIRE_BLOCK));
        suite.addTest(getSuiteFor("04-guess-indent.f90", Strategy.SHIFT_ENTIRE_BLOCK));
        suite.addTest(getSuiteFor("05-eachline-test.f90", Strategy.REINDENT_EACH_LINE));
        suite.addTest(getSuiteFor("06-continuation-test.f90",Strategy.REINDENT_EACH_LINE));
        suite.addTest(getSuiteFor("07-singleLineIf-test.f90",Strategy.REINDENT_EACH_LINE));
        suite.addTest(getSuiteFor("08-labelAndComments-test.f90",Strategy.REINDENT_EACH_LINE));

        return suite;
    }
    
    private static TestSuite getSuiteFor(String baseFilename, Strategy strategy)
    {
        TestSuite subSuite = new TestSuite("Reindenting " + baseFilename);
        subSuite.addTest(new ReindenterTestCase(baseFilename, strategy));
        return subSuite;
    }
}
