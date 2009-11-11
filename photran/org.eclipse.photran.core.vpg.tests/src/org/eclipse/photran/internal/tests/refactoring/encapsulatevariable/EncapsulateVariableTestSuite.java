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
package org.eclipse.photran.internal.tests.refactoring.encapsulatevariable;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author Tim
 */
public class EncapsulateVariableTestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        ArrayList<String> lst1 = new ArrayList<String>();
        lst1.add("module1.f90");
        
        ArrayList<String> emptyList = new ArrayList<String>();

        //Pass pass
        suite.addTest(getSuiteFor(lst1, "test-simple01.f90"));
        suite.addTest(getSuiteFor(emptyList, "test-encap-01a-basic-integer.f90"));
        suite.addTest(getSuiteFor(emptyList, "test-encap-01b-basic-real.f90"));
        suite.addTest(getSuiteFor(emptyList, "test-encap-01c-basic-dt.f90"));
        suite.addTest(getSuiteFor(emptyList, "test-encap-02-contains.f90"));
        suite.addTest(getSuiteFor(emptyList, "test-encap-03-expr.f90"));
        
        //Fail pass
        suite.addTest(getFatalFailSuite(emptyList, "test-encap-04-nosetter-parameter.f90"));
        suite.addTest(getFatalFailSuite(emptyList, "test-encap-06a-fail-array.f90"));
        suite.addTest(getFatalFailSuite(emptyList, "test-encap-06c-fail-allocatable1.f90"));
        suite.addTest(getFatalFailSuite(emptyList, "test-encap-06d-fail-allocatable2.f90"));
        suite.addTest(getFatalFailSuite(emptyList, "test-encap-06b-fail-pointer.f90"));
        suite.addTest(getFatalFailSuite(emptyList, "test-encap-06e-fail-target.f90"));
        suite.addTest(getNonFatalFailSuite(emptyList, "test-encap-05a-fail-conflict.f90"));
        
        
        return suite;
    }
    
    public static TestSuite getSuiteFor(ArrayList<String> fNames, String selectionFile)
    {
        TestSuite subSuite = new TestSuite("Encapsulate variable in " + selectionFile);
        subSuite.addTest(new EncapsulateVariableTestCase(fNames, selectionFile, null));
        return subSuite;
    }
    
    public static TestSuite getFatalFailSuite(ArrayList<String> fNames, String selectionFile)
    {
        TestSuite subSuite = new TestSuite("Encapsulate variable in " + selectionFile);
        subSuite.addTest(new EncapsulateVariableFailTestCase(fNames, selectionFile, true, null));
        return subSuite;
    }
    
    public static TestSuite getNonFatalFailSuite(ArrayList<String> fNames, String selectionFile)
    {
        TestSuite subSuite = new TestSuite("Encapsulate variable in " + selectionFile);
        subSuite.addTest(new EncapsulateVariableFailTestCase(fNames, selectionFile, false, null));
        return subSuite;
    }
}
