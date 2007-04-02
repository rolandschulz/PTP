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
package org.eclipse.ptp.debug.ui.tests.advance;

import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.debug.core.cdi.IPCDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;

/**
 * @author Clement chu
 */
public class BreakpointTests extends AbstractAdvanceTest {
	//total process
	static final int total_process = 8;

	public BreakpointTests(String name) {
		super(name, total_process, 0, total_process);
	}
	public void testBreakpoints() throws CoreException, IOException, PCDIException, InterruptedException {
		// Function breakpoint
		IPCDIFunctionLocation funcLoc;
		IPCDIFunctionBreakpoint funcBpt;
		//Create a break point on a generic function
		funcLoc = cdiSession.getBreakpointManager().createFunctionLocation(testApp, "func1");
		assertNotNull(funcLoc);
		funcBpt = cdiSession.getBreakpointManager().setFunctionBreakpoint(cdiSession.createBitList(), 0, funcLoc, null, true);
		assertNotNull(funcBpt);
		//Create a break point on main
		funcLoc = cdiSession.getBreakpointManager().createFunctionLocation(testApp, "main");
		assertNotNull(funcLoc);
		funcBpt = cdiSession.getBreakpointManager().setFunctionBreakpoint(cdiSession.createBitList(), 0, funcLoc, null, true);
		assertNotNull(funcBpt);
		//Try to create a break point on a function name that does not exist We
		//expect that this will cause the setLocationBreakpoint to throw a PCDIException
		//funcLoc = cdiSession.getBreakpointManager().createFunctionLocation(null, "noThisFunc");
		//assertNotNull(funcLoc);
		//funcBpt = cdiSession.getBreakpointManager().setFunctionBreakpoint(cdiSession.createBitList(), 0, funcLoc, null, true);
		//assertNotNull(funcBpt);
		
		// Line breakpoint
		IPCDILineLocation lineLoc;
		IPCDILineBreakpoint lineBpt;
		//Create a break point in a generic function
		lineLoc = cdiSession.getBreakpointManager().createLineLocation(testApp, 7);
		assertNotNull(lineLoc);
		lineBpt = cdiSession.getBreakpointManager().setLineBreakpoint(cdiSession.createBitList(), 0, lineLoc, null, true);
		assertNotNull(lineBpt);
		//Create a break point in main
		lineLoc = cdiSession.getBreakpointManager().createLineLocation(testApp, 18);
		assertNotNull(lineLoc);
		lineBpt = cdiSession.getBreakpointManager().setLineBreakpoint(cdiSession.createBitList(), 0, lineLoc, null, true);
		assertNotNull(lineBpt);
		//Try to create a break point on a line that does not exist We expect
		//that this will cause the setLocationBreakpoint to throw a PCDIException
		//lineLoc = cdiSession.getBreakpointManager().createLineLocation("main.c", 30);
		//assertNotNull(lineLoc);
		//lineBpt = cdiSession.getBreakpointManager().setLineBreakpoint(cdiSession.createBitList(), 0, lineLoc, null, true);
		//assertNotNull(lineBpt);
		//Try to create a break point on a line that does not have code on it
		lineLoc = cdiSession.getBreakpointManager().createLineLocation(testApp, 11);
		assertNotNull(lineLoc);
		lineBpt = cdiSession.getBreakpointManager().setLineBreakpoint(cdiSession.createBitList(), 0, lineLoc, null, true);
		assertNotNull(lineBpt);

		//Create a break point in a generic function without passing the source
		//file name. At the time of writing this would just silently fail, so
		//to make sure it works, we will do it once with a valid line number
		//and once with an invalid line number, and the first should always
		//succeed and the second should always throw an exception.
		lineLoc = cdiSession.getBreakpointManager().createLineLocation(null, 7);
		assertNotNull(lineLoc);
		lineBpt = cdiSession.getBreakpointManager().setLineBreakpoint(cdiSession.createBitList(), 0, lineLoc, null, true);
		assertNotNull(lineBpt);

		//Give the process up to 10 seconds to become either terminated or
		//suspended. It sould hit the breakponint almost immediatly so we
		//should only sleep for max 1000 ms
		//Resume the target, this should cause it to run till it hits the breakpoint
		cdiSession.resume(cdiSession.createBitList());
		for (int x = 0; x < 100; x++) {
			if (cdiSession.getDebugger().isSuspended(cdiSession.createBitList()) || cdiSession.getDebugger().isTerminated(cdiSession.createBitList()))
				break;
			Thread.sleep(1000);
		}
		assertTrue("Suspended: " + cdiSession.getDebugger().isSuspended(cdiSession.createBitList()) + " Termiunated: " + cdiSession.getDebugger().isTerminated(cdiSession.createBitList()), cdiSession.getDebugger().isSuspended(cdiSession.createBitList()));
		//register process 0
		cdiSession.registerTargets(cdiSession.createBitList(0), false);
		IPCDITarget[] targets = cdiSession.getTargets();
		assertNotNull(targets);
		assertTrue(targets.length==1);
		IPCDILocator locator = targets[0].getCurrentThread().getStackFrames()[0].getLocator();
		assertNotNull(locator);
		assertTrue(locator.getLineNumber() == 17);
		assertTrue(locator.getFunction().equals("main"));
		assertTrue(locator.getFile().equals(testApp));

		// clean up the session
		cdiSession.terminate();
	}
}