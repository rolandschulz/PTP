/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.ui.tests.base;

import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.junit.Test;

/**
 * @author Clement chu
 */
public class VariableTests extends AbstractBaseTest {
	//total process
	static final int total_process = 4;
	
	public VariableTests(String name) {
		super(name, total_process, 0, total_process);
	}
	@Test public void testVariables() throws CoreException, IOException, PCDIException, InterruptedException {
		BitList t;
		int length;
		/** Variable **/
		//Create a break point on a generic function
		t = createBitList();
		debugProxy.debugEvaluateExpression(t, "long_string");
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
		length = completedReturns.size();
		for (int i = 0; i<length; i++) {
			Object result = completedReturns.get(i);
			System.err.println("("+i+")"+" result: " + BitList.showBitList(completedTasks.get(i)) + ", " + result);
		}
		t = createBitList();
		debugProxy.debugEvaluateExpression(t, "long_long_string");
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
		length = completedReturns.size();
		for (int i = 0; i<length; i++) {
			Object result = completedReturns.get(i);
			System.err.println("("+i+")"+" result: " + BitList.showBitList(completedTasks.get(i)) + ", " + result);
		}

		t = createBitList();
		debugProxy.debugTerminate(t);
		//wait terminated event
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
	}
}