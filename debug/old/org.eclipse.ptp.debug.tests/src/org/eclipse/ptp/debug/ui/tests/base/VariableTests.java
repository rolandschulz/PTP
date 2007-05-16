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
	static final int total_process = 2;
	
	public VariableTests(String name) {
		super(name, total_process, 0, total_process);
	}
	@Test public void testVariables() throws CoreException, IOException, PCDIException, InterruptedException {
		BitList t;
		int bpid = 0;
		int length;
		//Create a break point on main
		/*
		t = createBitList();
		debugProxy.debugSetFuncBreakpoint(t, ++bpid, false, false, testAppName, "main", "", 0, 0);
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
		*/
		/** Line breakpoint **/
		//Create a break point in a generic function
		t = createBitList();
		debugProxy.debugSetLineBreakpoint(t, ++bpid, false, false, testAppName, 37, "", 0, 0);
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());

		for (int index=0; index<5; index++) {
			/** GO **/
			t = createBitList();
			debugProxy.debugGo(t);
			//wait suspend event
			waitEvent(t);
			assertTrue("All processes are suspended: " + t.isEmpty(), t.isEmpty());
			
			/** Variable **/
			t = createBitList();
			debugProxy.debugEvaluateExpression(t, "countX");
			waitEvent(t);
			assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
			length = completedReturns.size();
			for (int i = 0; i<length; i++) {
				Object result = completedReturns.get(i);
				System.err.println("("+i+")"+" result: " + BitList.showBitList(completedTasks.get(i)) + ", " + result);
			}
			t = createBitList();
			debugProxy.debugEvaluateExpression(t, "countY");
			waitEvent(t);
			assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
			length = completedReturns.size();
			for (int i = 0; i<length; i++) {
				Object result = completedReturns.get(i);
				System.err.println("("+i+")"+" result: " + BitList.showBitList(completedTasks.get(i)) + ", " + result);
			}
		}

		t = createBitList();
		debugProxy.debugTerminate(t);
		//wait terminated event
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
	}
}