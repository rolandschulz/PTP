/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests.suite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ptp.internal.rdt.core.AdapterTests;
import org.eclipse.ptp.internal.rdt.core.SerializationTests;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallHierarchySerializationTests;
import org.eclipse.ptp.internal.rdt.core.model.CModelBuilder2Tests;

/**
 * Tests that do not require a connection to a remote server.
 */
public class CoreBasicTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(CoreBasicTestSuite.class.getName());
		
		suite.addTestSuite(AdapterTests.class);
		suite.addTestSuite(SerializationTests.class);
		suite.addTestSuite(CallHierarchySerializationTests.class);
		suite.addTestSuite(CModelBuilder2Tests.class);
		
		return suite;
	}
}