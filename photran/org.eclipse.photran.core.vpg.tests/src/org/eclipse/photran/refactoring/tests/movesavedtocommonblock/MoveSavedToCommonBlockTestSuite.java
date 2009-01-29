/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.refactoring.tests.movesavedtocommonblock;

/**
 * 
 * @author Stas Negara
 */

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.photran.core.vpg.util.LineCol;

public class MoveSavedToCommonBlockTestSuite extends TestSuite
{
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        suite.addTest(getSuiteFor("test1.f90", new LineCol(29, 10)));
        suite.addTest(getSuiteFor("test2.f90", new LineCol(65, 10)));
        return suite;
    }
    
    private static TestSuite getSuiteFor(String baseFilename, LineCol lineCol)
    {
        TestSuite subSuite = new TestSuite("Move Saved To Common Block in file " + baseFilename);
        subSuite.addTest(new MoveSavedToCommonBlockTestCase(baseFilename, lineCol));
        return subSuite;
    }
}
