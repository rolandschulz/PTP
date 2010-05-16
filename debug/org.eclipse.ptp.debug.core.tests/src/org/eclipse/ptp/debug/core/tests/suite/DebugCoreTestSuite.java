/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.tests.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ptp.debug.core.tests.AIFTests;
import org.eclipse.ptp.debug.core.tests.TaskSetTests;

public class DebugCoreTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(DebugCoreTestSuite.class.getName());

		suite.addTestSuite(TaskSetTests.class);
		suite.addTestSuite(AIFTests.class);
		return suite;
	}

}
