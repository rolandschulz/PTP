/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.ui.tests.advance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.ui.testplugin.PTPDebugHelper;
import org.eclipse.ptp.debug.ui.testplugin.PTPProjectHelper;
import org.eclipse.ptp.debug.ui.tests.AbstractDebugTest;

/**
 * @author clement
 */
public abstract class AbstractAdvanceTest extends AbstractDebugTest {
	//project
	protected final String testAppName = "main";
	protected final String testApp = "/home/clement/Desktop/runtime-PTP/testMPI/" + testAppName + ".c";
	protected final String testAppPath = "resources/debugTest.zip";
	protected final String projectName = "filetest";

	protected IWorkspace workspace;
	protected IWorkspaceRoot root;
	protected NullProgressMonitor monitor;
	protected IPCDISession cdiSession = null;
	protected ICProject testProject = null;
	protected IResourceManager resourceMgr = null;
	protected PCDIDebugModel debugModel = null;

	protected final String debugHost = "localhost";
	protected final String debuggerType = "gdb-mi"; //gdb-mi or test
	protected final String localPath = "/home/clement/Desktop/workspace_head_3.3/org.eclipse.ptp.linux.x86/bin";

	protected final String sdmPath = localPath + "/sdm";
	protected final String ptp_orte_proxyPath = localPath + "/ptp_orte_proxy";

	/**
	 * Constructor
	 * @param name
	 * @param nProcs
	 * @param firstNode
	 * @param NProcsPerNode
	 */
	public AbstractAdvanceTest(String name, int nProcs, int firstNode, int NProcsPerNode) {
		super(name, nProcs, firstNode, NProcsPerNode);
		/***********************************************************************
		 * The tests assume that they have a working workspace and workspace
		 * root object to use to create projects/files in, so we need to get
		 * them setup first.
		 */
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null)
			fail("Workspace was not setup");
		if (root == null)
			fail("Workspace root was not setup");
	}
	protected void setUp() throws CoreException, InvocationTargetException, IOException, InterruptedException {
		ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
		 //Create a new project and import the test source.
		testProject = PTPProjectHelper.createCProjectWithImport(projectName, new Path(testAppPath));
		if (testProject == null)
			fail("Unable to create project");
		//Build the test project..
		testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		debugModel = PTPDebugCorePlugin.getDebugModel();
		super.setUp();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.tests.AbstractDebugTest#tearDown()
	 */
	protected void tearDown() throws CoreException, IOException, PCDIException {
		if (cdiSession != null)
			cdiSession.shutdown();
		if (resourceMgr != null)
			resourceMgr.shutdown();
		if (debugModel != null)
			debugModel.shutdown();
		if (testProject != null)
			PTPProjectHelper.delete(testProject);
		cdiSession = null;
		resourceMgr = null;
		debugModel = null;
		testProject = null;
		super.tearDown();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.tests.AbstractDebugTest#startDebugServer()
	 */
	public void startDebugServer() throws CoreException, IOException, InterruptedException {
		IAbstractDebugger debugger;
		IPLaunch launch;
		AttributeManager attrMgr;
		IBinaryObject exec;
		int port = 0;
		
		assertTrue(new File(sdmPath).exists());
		assertTrue(new File(ptp_orte_proxyPath).exists());
		assertNotNull(testProject);

		exec = PTPProjectHelper.findBinaryObject(testProject, testAppName);
		assertNotNull(exec);
		resourceMgr = PTPDebugHelper.createOrteManager(ptp_orte_proxyPath);
		assertNotNull(resourceMgr);

		debugger = PTPDebugHelper.createDebugger();
		assertNotNull(debugger);
		port = debugger.getDebuggerPort();
		assertTrue(port > 1000);
		launch = PTPDebugHelper.createDebugLaunch(null);
		assertNotNull(launch);

		IPath location = PTPDebugHelper.getProjectPath(testProject);
		attrMgr = PTPDebugHelper.createDebugAttrManager(resourceMgr, location.toOSString(), location.toOSString()+"/"+testApp, new String[0], new String[0], nProcs, debugHost, debuggerType, port, new String[0], sdmPath);
		assertNotNull(attrMgr);
		job = resourceMgr.submitJob(attrMgr, new SubProgressMonitor(monitor, 150));
		assertNotNull(job);
		assertTrue(job.size() > 0);
		
		launch.setAttribute(ElementAttributes.getIdAttributeDefinition().getId(), job.getID());
		launch.setPJob(job);
		cdiSession = debugger.createDebuggerSession(launch, exec, new SubProgressMonitor(new NullProgressMonitor(), 40));
		assertNotNull(cdiSession);
		debugModel.newJob(job, cdiSession.createBitList());
		debugModel.fireSessionEvent(job, cdiSession);
	}
}
