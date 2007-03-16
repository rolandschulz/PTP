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
package org.eclipse.ptp.debug.ui.tests;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.debugger.ParallelDebugger;
import org.eclipse.ptp.debug.ui.testplugin.PTPDebugHelper;
import org.junit.Test;


/**
 * This file contains a set of generic tests for the debug stuff. It currenly
 * uses the mi debugger.
 * 
 */
public class DebugTests extends TestCase {
	IWorkspace workspace;
	IWorkspaceRoot root;
	NullProgressMonitor monitor;
	PCDIDebugModel debugModel;
	IAbstractDebugger debugger;

	/**
	 * Constructor for DebugTests
	 * 
	 * @param name
	 */
	public DebugTests(String name) {
		super(name);
		/***********************************************************************
		 * The assume that they have a working workspace and workspace root
		 * object to use to create projects/files in, so we need to get them
		 * setup first.
		 */
		/*
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null)
			fail("Workspace was not setup");
		if (root == null)
			fail("Workspace root was not setup");
		*/
	}

	/**
	 * Sets up the test fixture.
	 * 
	 * Called before every test case method.
	 * 
	 * Example code test the packages in the project
	 * "com.qnx.tools.ide.cdt.core"
	 */
	protected void setUp() throws CoreException, InvocationTargetException, IOException {
		//ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
		debugModel = PTPDebugCorePlugin.getDebugModel();
		debugger = new ParallelDebugger();
		/***********************************************************************
		 * Create a new project and import the test source.
		 */
		//IPath importFile = new Path("resources/debugTest.zip");
		//testProject = CProjectHelper.createCProjectWithImport("filetest", importFile);
		//if (testProject == null)
			//fail("Unable to create project");
		/* Build the test project.. */

		//testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
	}

	/**
	 * Tears down the test fixture.
	 * 
	 * Called after every test case method.
	 */
	protected void tearDown() throws CoreException, PCDIException {
		/*
		if (session != null) {
			session.terminate();
			session = null;
		}
		CProjectHelper.delete(testProject);
		*/
		debugModel = null;
	}

	public static TestSuite suite() {
		return new TestSuite(DebugTests.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/***************************************************************************
	 * Can we setup a debug? This is sort of a catch all sanity tests to make
	 * sure we can create a debug session with a break point and start it
	 * without having any exceptions thrown. It's not ment to be a real proper
	 * test.
	 */
	@Test public void testDebug() throws CoreException, IOException, PCDIException {
		IPLaunch launch = PTPDebugHelper.createDebugLaunch(null);
		IPJob job = PTPDebugHelper.createJob();
		
		IPCDISession session = new Session(debugger, job, launch, null);
		assertNotNull(session);
		assertTrue(session.getTotalProcesses()>0);
		session.registerTargets(session.createBitList(0), false);
		IPCDITarget[] targets = session.getTargets();
		assertNotNull(targets);
		assertTrue(targets.length > 0);
		IPCDITarget cdiTarget = targets[0];
		assertNotNull(cdiTarget);
		IPCDIFunctionLocation funcLocation = cdiTarget.createFunctionLocation(null, "func1");
		assertNotNull(funcLocation);
		IPCDILineLocation lineLocation = cdiTarget.createLineLocation(null, 10);
		assertNotNull(lineLocation);
		IBreakpoint lineBpt = debugModel.createLineBreakpoint(null, null, lineLocation.getLineNumber(), true, 0, null, false, "set1", "job1", "job1");
		assertNotNull(lineBpt);
		/*
		ICDITarget cdiTarget;
		ICDIFunctionLocation location;

		session = CDebugHelper.createSession("main", testProject);
		assertNotNull(session);
		ICDITarget[] targets = session.getTargets();
		assertNotNull(targets);
		assertTrue(targets.length > 0);
		cdiTarget = targets[0];
		assertNotNull(cdiTarget);
		location = cdiTarget.createFunctionLocation(null, "func1");
		assertNotNull(location);
		cdiTarget.setFunctionBreakpoint(0, location, null, false);
		cdiTarget.resume();
		session.terminate();
		session = null;
		*/
	}
}
