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
package org.eclipse.ptp.core.tests.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ptp.core.tests.RangeSetTests;

public class CoreTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(CoreTestSuite.class.getName());

		suite.addTestSuite(RangeSetTests.class);
		return suite;
	}

}
