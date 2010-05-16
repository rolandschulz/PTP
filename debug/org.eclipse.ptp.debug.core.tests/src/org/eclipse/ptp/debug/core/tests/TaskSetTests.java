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
package org.eclipse.ptp.debug.core.tests;

import junit.framework.TestCase;

import org.eclipse.ptp.debug.core.TaskSet;

public class TaskSetTests extends TestCase {
	public void testToString() {
		TaskSet b = new TaskSet();
		b.set(3, 13);
		b.set(15, 22);
		String str = b.toHexString().toUpperCase();

		assertEquals(str, "00000000003F9FF8");

		assertEquals(b.toString(), "{3-12,15-21}");
	}

	public void testFromString() {
		String str = "06411eda";
		TaskSet b = new TaskSet(28, str);

		String res = b.toHexString();

		assertEquals(str, res);

		str = "07";
		b = new TaskSet(3, str);
		res = b.toHexString();

		assertEquals(str, res);
	}
}
