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

/**
 * Tests that require a connection to be run,
 * this suite must be launched from a ConnectionSuite.
 */
public class CoreConnectionTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(CoreConnectionTestSuite.class.getName());
		return suite;
	}
}