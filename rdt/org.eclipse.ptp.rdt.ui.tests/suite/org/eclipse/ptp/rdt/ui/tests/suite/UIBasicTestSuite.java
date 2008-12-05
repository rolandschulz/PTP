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
package org.eclipse.ptp.rdt.ui.tests.suite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests that do not require a connection to a remote server.
 */
public class UIBasicTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(UIBasicTestSuite.class.getName());
		// TODO add some tests
		return suite;
	}
}