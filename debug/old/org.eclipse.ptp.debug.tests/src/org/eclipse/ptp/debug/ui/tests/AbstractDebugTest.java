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
package org.eclipse.ptp.debug.ui.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.ui.testplugin.PTPDebugHelper;
import org.eclipse.ptp.debug.ui.testplugin.PTPProjectHelper;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 * @author clement
 */
public abstract class AbstractDebugTest extends TestCase implements IProxyDebugEventListener {
	final static String testAppName = "main";
	final static String testApp = testAppName + ".c";
	final static String testAppPath = "resources/debugTest.zip";
	final static String projectName = "filetest";
	final static String resourceMgrName = "ORTE";
	final static String machineName = "Machine0";
	final static String queueName = "localQueue";
	final static String debugHost = "localhost";
	//NEED MODIFICATION
	final static String debuggerType = "test"; //gdb-mi or test
	final static String localPath = "/Users/clement/Documents/workspace_head/org.eclipse.ptp.macosx.ppc/bin";

	final static String sdmPath = localPath + "/sdm";
	final static String ptp_orte_proxyPath = localPath + "/ptp_orte_proxy";
	//final static String resourceXML = "resources/resourceManagers.xml";
	//final static String resourceMgrID = "org.eclipse.ptp.orte.core.resourcemanager";

	protected IWorkspace workspace;
	protected IWorkspaceRoot root;
	protected NullProgressMonitor monitor;
	protected IPJob job = null;
	protected IPCDISession cdiSession = null;
	protected ICProject testProject = null;
	protected PCDIDebugModel debugModel = null;
	protected IResourceManager resourceMgr = null;
	protected ProxyDebugClient proxy = null;

	private int nProcs = 1;
	private int firstNode = 0;
	private int NProcsPerNode = 1;
	private Object LOCK = new Object();
	private BitList tasks = null;
	
	public AbstractDebugTest(String name, int nProcs, int firstNode, int NProcsPerNode) {
		super(name);
		this.nProcs = nProcs;
		this.firstNode = firstNode;
		this.NProcsPerNode = NProcsPerNode;
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
	/**
	 * Sets up the test fixture.
	 * 
	 * Called before every test case method.
	 */
	protected void setUp() throws CoreException, InvocationTargetException, IOException, InterruptedException {
		ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
		 //Create a new project and import the test source.
		testProject = PTPProjectHelper.createCProjectWithImport(projectName, new Path(testAppPath));
		if (testProject == null)
			fail("Unable to create project");
		//Build the test project..
		testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		//Creae debug model object..
		debugModel = PTPDebugCorePlugin.getDebugModel();
		//Start debug server
		startDebugServer();
	}
	/**
	 * Tears down the test fixture.
	 * 
	 * Called after every test case method.
	 */
	protected void tearDown() throws CoreException, IOException, PCDIException {
		if (job != null) {
			if(!job.isAllStop()) {
				job.removeAllProcesses();
				if (cdiSession != null)
					cdiSession.shutdown();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (proxy != null) {
			proxy.sessionFinish();
			proxy.removeEventListener(this);
			proxy.closeConnection();
			proxy = null;
		}

		if (resourceMgr != null)
			resourceMgr.shutdown();
		PTPProjectHelper.delete(testProject);
		cdiSession = null;
		tasks = null;
		resourceMgr = null;
		job = null;
		debugModel = null;
	}
	public void startDebugServer2() throws CoreException, IOException, InterruptedException {
		IAbstractDebugger debugger;
		IPLaunch launch;
		JobRunConfiguration jobConfig;
		IBinaryObject exec;
		int port = 0;
		
		assertTrue(new File(sdmPath).exists());
		assertTrue(new File(ptp_orte_proxyPath).exists());

		exec = PTPProjectHelper.findBinaryObject(testProject, testAppName);
		assertNotNull(exec);
		proxy = new ProxyDebugClient();
		assertNotNull(proxy);
		proxy.sessionCreate();
		port = proxy.getSessionPort();
		assertTrue(port > 1000);
		resourceMgr = PTPDebugHelper.createOrteManager(ptp_orte_proxyPath);
		assertNotNull(resourceMgr);
		resourceMgr.startUp(monitor);

		debugger = PTPDebugHelper.createDebugger();
		assertNotNull(debugger);
		port = debugger.getDebuggerPort();
		launch = PTPDebugHelper.createDebugLaunch(null);
		assertNotNull(launch);

		jobConfig = PTPDebugHelper.getJobDebugConfiguration(testProject, testAppName, resourceMgrName, machineName, queueName, nProcs, firstNode, NProcsPerNode, debuggerType, debugHost, port, sdmPath);
		assertNotNull(jobConfig);
		job = resourceMgr.run(null, jobConfig, new SubProgressMonitor(monitor, 150));
		assertNotNull(job);
		launch.setAttribute(IPJob.JOB_ID_TEXT, job.getIDString());
		job.setAttribute(PreferenceConstants.JOB_APP_NAME, jobConfig.getExecName());
		job.setAttribute(PreferenceConstants.JOB_APP_PATH, jobConfig.getPathToExec());
		job.setAttribute(PreferenceConstants.JOB_WORK_DIR, jobConfig.getWorkingDir());
		job.setAttribute(PreferenceConstants.JOB_ARGS, jobConfig.getArguments());
		job.setAttribute(PreferenceConstants.JOB_DEBUG_DIR, jobConfig.getPathToExec());
		launch.setPJob(job);
		cdiSession = debugger.createDebuggerSession(launch, exec, 1000, new SubProgressMonitor(new NullProgressMonitor(), 40));
		assertNotNull(cdiSession);
		debugModel.newJob(job, cdiSession.createBitList());
		debugModel.fireSessionEvent(job, cdiSession);
	}
	/**
	 * Tears down the test fixture.
	 * 
	 * Called after every test case method.
	 */
	public void startDebugServer() throws CoreException, IOException, InterruptedException {
		JobRunConfiguration jobConfig;
		IBinaryObject exec;
		int port = 0;
		
		assertTrue(new File(sdmPath).exists());
		assertTrue(new File(ptp_orte_proxyPath).exists());

		exec = PTPProjectHelper.findBinaryObject(testProject, testAppName);
		assertNotNull(exec);
		proxy = new ProxyDebugClient();
		assertNotNull(proxy);
		proxy.sessionCreate();
		port = proxy.getSessionPort();
		assertTrue(port > 1000);
		//resourceMgr = PTPDebugHelper.createOrteManager(ptp_orte_proxyPath);
		//assertNotNull(resourceMgr);
		//resourceMgr.startUp(monitor);
		
		jobConfig = PTPDebugHelper.getJobDebugConfiguration(testProject, testAppName, resourceMgrName, machineName, queueName, nProcs, firstNode, NProcsPerNode, debuggerType, debugHost, port, sdmPath);
		assertNotNull(jobConfig);
		//job = resourceMgr.run(null, jobConfig, new SubProgressMonitor(monitor, 150));
		//assertNotNull(job);
		//launch.setAttribute(IPJob.JOB_ID_TEXT, job.getIDString());
		//job.setAttribute(PreferenceConstants.JOB_APP_NAME, jobConfig.getExecName());
		//job.setAttribute(PreferenceConstants.JOB_APP_PATH, jobConfig.getPathToExec());
		//job.setAttribute(PreferenceConstants.JOB_WORK_DIR, jobConfig.getWorkingDir());
		//job.setAttribute(PreferenceConstants.JOB_ARGS, jobConfig.getArguments());
		//job.setAttribute(PreferenceConstants.JOB_DEBUG_DIR, jobConfig.getPathToExec());
		//launch.setPJob(job);
		
		assertTrue(proxy.waitForConnect(monitor));
		proxy.addEventListener(this);

		assertTrue(new File(jobConfig.getPathToExec()).exists());
		proxy.debugStartSession(jobConfig.getExecName(), jobConfig.getPathToExec(), jobConfig.getWorkingDir(), jobConfig.getArguments());
		BitList t = createBitList();
		//wait suspend event
		waitEvent(t);
		assertTrue("Debugger is initialized: " + t.isEmpty(), t.isEmpty());
	}
	protected BitList createBitList() {
		BitList tasks = new BitList(nProcs);
		tasks.set(0, nProcs);
		return tasks;
	}
	protected BitList createBitList(int index) {
		BitList tasks = new BitList(nProcs);
		tasks.set(index);
		return tasks;
	}
	protected void waitEvent(BitList tasks) throws InterruptedException {
		synchronized (LOCK) {
			this.tasks = tasks;
			LOCK.wait(5000);
		}
	}
	protected void notifyEvent(BitList evtTasks) {
		synchronized (LOCK) {
			tasks.andNot(evtTasks);
			if (tasks.isEmpty())
				LOCK.notifyAll();
		}
	}
}

