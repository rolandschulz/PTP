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
package org.eclipse.photran.internal.core.tests;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestSuite;

/**
 * An aggregate test suite that tests the parser, source reproducer, and loop replacer.
 * 
 * @author Jeff Overbey
 */
public class MultiTestSuite extends TestSuite
{
    public MultiTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        setName("Running multiple tests on " + directorySuffix);
        
        addTest(new ParserTestSuite(directorySuffix, isFixedForm, mustExist) {});
        
        if (!isFixedForm)
        {
            addTest(new SourceReproductionTestSuite(directorySuffix, isFixedForm, mustExist) {});
            addTest(new LoopReplacerSRTestSuite(directorySuffix, isFixedForm, mustExist) {});
        }
    }
    
    public MultiTestSuite() {;} // to keep JUnit quiet
    public void test() {} // to keep JUnit quiet
}
