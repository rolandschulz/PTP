/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.ui.testplugin;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPQueue;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDescription;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDescription;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IAttribute.IllegalValue;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.debugger.ParallelDebugger;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;


/**
 * Helper methods to set up a Debug session.
 */
public class PTPDebugHelper {
	public static IPJob createJob() {
		IPJob job = new IPJob() {
			public Object getAdapter(Class adapter) {
				return null;
			}
			public int getID(){
				return 1;
			}
			public String getElementName() {
				return "Job1";
			}
			public IPProcess findProcess(String processNumber) {
				return null;
			}
			public IPProcess findProcessByName(String pname) {
				return null;
			}
			public IPProcess findProcessByTaskId(int taskId) {
				return null;
			}
			public Object getAttribute(String key) {
				return null;
			}
			public String[] getAttributeKeys() {
				return new String[0];
			}
			public String getIDString() {
				return "1";
			}
			public String getJobNumber() {
				return "1";
			}
			public int getJobNumberInt() {
				return 1;
			}
			public IPMachine[] getMachines() {
				return new IPMachine[0];
			}
			public String getName() {
				return "Job1";
			}
			public IPNode[] getNodes() {
				return new IPNode[0];
			}
			public IPProcess[] getProcesses() {
				return new IPProcess[0];
			}
			public IPNode[] getSortedNodes() {
				return new IPNode[0];
			}
			public IPProcess[] getSortedProcesses() {
				return new IPProcess[0];
			}
			public IPUniverse getUniverse() {
				return null;
			}
			public boolean isAllStop() {
				return false;
			}
			public boolean isDebug() {
				return true;
			}
			public void removeAllProcesses(){}
			public void setAttribute(String key, Object o){}
			public void setDebug(){}
			public int totalNodes() {
				return 1;
			}
			public int totalProcesses() {
				return 10;
			}
			public IPQueue getQueue() {
				return null;
			}
		};
		return job;
	}
	public static IPLaunch createDebugLaunch(ILaunchConfiguration config) {
		return new PLaunch(config, "debug", null);
	}
	public static IAbstractDebugger createDebugger() {
		return new ParallelDebugger();
	}
	/**
	 * Creates a IPCDISession.
	 */	
	public static IPCDISession createSession(IAbstractDebugger debugger, IPJob job, IPLaunch launch, ICProject project) throws CoreException {
		IBinary bins[] = project.getBinaryContainer().getBinaries();
		if (bins.length != 1) {
			//SHOULD NOT HAPPEN
			return null;        
		}
		IBinaryObject binObj = (IBinaryObject)bins[0].getAdapter(IBinaryObject.class);
		if (binObj == null)
			return null;
		return new Session(debugger, job, launch, binObj);
	}
	
	public static IAttribute[] getAttributes(int nProcs, int firstNode, int NProcsPerNode) {
		final IAttributeDescription firstNodeAttrDesc = new AttributeDescription("FNN_ID0", "FirstNodeNumber", "First Node Number");
		final IAttributeDescription nProcsAttrDesc = new AttributeDescription("NP_ID0", "NumProcs", "Number of Processes");
		final IAttributeDescription nProcsPerNodeAttrDesc = new AttributeDescription("NPPN_ID0", "NumProcsPerNode", "Number of Procs Per Node");

		IAttribute[] attrs = new IAttribute[3]; 
		try {
			IntegerAttribute nProcsPerNodeAttr = new IntegerAttribute(nProcsPerNodeAttrDesc, 1);
			nProcsPerNodeAttr.setValidRange(1, Integer.MAX_VALUE);
			nProcsPerNodeAttr.setValue(String.valueOf(nProcs));
			IntegerAttribute firstNodeAttr = new IntegerAttribute(firstNodeAttrDesc, 0);
			firstNodeAttr.setValidRange(0, Integer.MAX_VALUE);
			firstNodeAttr.setValue(String.valueOf(firstNode));
			IntegerAttribute nProcsAttr = new IntegerAttribute(nProcsAttrDesc, 1);
			nProcsAttr.setValidRange(1, Integer.MAX_VALUE);
			nProcsAttr.setValue(String.valueOf(NProcsPerNode));
			attrs[0] = nProcsAttr;
			attrs[1] = firstNodeAttr;
			attrs[2] = nProcsPerNodeAttr;
		} catch (IllegalValue e) {
		}
		return attrs;
	}
	
	public static JobRunConfiguration getJobRunConfiguration() {
		String appName = "TestVar";
		String appPath = "/Users/clement/Documents/runtime-EclipseApplication/TestVar/Debug";
		String resourceMgr = "ORTE";
		String machine = "Machine0";
		String queue = "localQueue";
		IAttribute[] attrs = getAttributes(3,0,3);
		String[] args = new String[0];
		String[] envs = null;
		String dir = "/Users/clement/Documents/runtime-EclipseApplication/TestVar";
		String debugArgs = "--host=localhost --debugger=gdb-mi --port=51281";
		String debugFilePath = "/Users/clement/Documents/workspace_head/org.eclipse.ptp.macosx.ppc/bin/sdm";
		JobRunConfiguration jobConfig = new JobRunConfiguration(appName, appPath, resourceMgr, machine, queue, attrs, args, envs, dir);
		jobConfig.setDebug();
		jobConfig.setDebuggerPath(debugFilePath);
		jobConfig.setDebuggerArgs(debugArgs);
		return jobConfig;
	}
	private static IResourceManager getLaunchManager(String rmName) {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		for (int i = 0; i < rms.length; ++i) {
			if (rms[i].getElementName().equals(rmName)) {
				return rms[i];
			}
		}
		return null;
	}
	public static IPJob launchDebugger(JobRunConfiguration jobConfig) throws CoreException {
		final String resourceManagerName = jobConfig.getResourceManagerName();
		final IResourceManager launchManager = getLaunchManager(resourceManagerName);
		return launchManager.run(null, jobConfig, new SubProgressMonitor(new NullProgressMonitor(), 150));
	}
	
	public static void adasdasd() {
		
	}
}
