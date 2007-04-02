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
public class BreakpointTests extends AbstractBaseTest {
	//total process
	static final int total_process = 8;
	
	public BreakpointTests(String name) {
		super(name, total_process, 0, total_process);
	}
	@Test public void testBreakpoints() throws CoreException, IOException, PCDIException, InterruptedException {
		BitList t;
		int bpid = 0;
		/** Function breakpoint **/
		//Create a break point on a generic function
		t = createBitList();
		debugProxy.debugSetFuncBreakpoint(t, ++bpid, false, false, testApp, "func1", "", 0, 0);
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
		
		//Create a break point on main
		t = createBitList();
		debugProxy.debugSetFuncBreakpoint(t, ++bpid, false, false, testApp, "main", "", 0, 0);
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());

		/** Line breakpoint **/
		//Create a break point in a generic function
		t = createBitList();
		debugProxy.debugSetLineBreakpoint(t, ++bpid, false, false, testApp, 7, "", 0, 0);
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());

		/*
		t = createBitList();
		proxy.debugGo(t);
		//wait suspend event
		waitEvent(t);
		assertTrue("All processes are suspended: " + t.isEmpty(), t.isEmpty());
		
		//delete all breakpoints
		for (int i=bpid; i>0; i--) {
			t = createBitList();
			proxy.debugDeleteBreakpoint(t, i);
			waitEvent(t);
			assertTrue("Bpt ID: " + (i) + ", Command completed: " + t.isEmpty(), t.isEmpty());
		}
		
		t = createBitList();
		proxy.debugGo(t);
		//wait terminated event
		waitEvent(t);
		assertTrue("All processes are terminated: " + t.isEmpty(), t.isEmpty());
		*/
		
		t = createBitList();
		debugProxy.debugTerminate(t);
		//wait terminated event
		waitEvent(t);
		assertTrue("Command completed: " + t.isEmpty(), t.isEmpty());
	}
}