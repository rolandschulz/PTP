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

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.AttributeDescription;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDescription;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IAttribute.IllegalValue;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.external.core.debugger.ParallelDebugger;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerConfiguration;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 * @author Clement chu
 */
public class PTPDebugHelper {
	public static IPLaunch createDebugLaunch(ILaunchConfiguration config) {
		return new PLaunch(config, "debug", null);
	}
	public static IAbstractDebugger createDebugger() {
		return new ParallelDebugger();
	}
	private static IAttribute[] getAttributes(int nProcs, int firstNode, int NProcsPerNode) {
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
	public static JobRunConfiguration getJobRunConfiguration(String path, String appName, String resourceMgrName, String machineName, String queueName, int nProcs, int firstNode, int NProcsPerNode) {
		IAttribute[] attrs = getAttributes(nProcs, firstNode, NProcsPerNode);
		String[] args = new String[0];
		String[] envs = null;
		return new JobRunConfiguration(appName, path, resourceMgrName, machineName, queueName, attrs, args, envs, path);
	}
	public static JobRunConfiguration getJobRunConfiguration(ICProject project, String appName, String resourceMgrName, String machineName, String queueName, int nProcs, int firstNode, int NProcsPerNode) {
		return getJobRunConfiguration(project.getProject().getLocation().toOSString(), appName, resourceMgrName, machineName, queueName, nProcs, firstNode, NProcsPerNode);
	}
	public static JobRunConfiguration getJobDebugConfiguration(String path, String appName, String resourceMgrName, String machineName, String queueName, int nProcs, int firstNode, int NProcsPerNode, String debuggerType, String debugHost, int debugPort, String sdmPath) {
		String debugArgs = "--host=" + debugHost + " --debugger=" + debuggerType + " --port=" + debugPort;
		System.err.println("*** If you start JUnit test with manually launch sdm, please type the following on command line ***");
		System.err.println(">>> mpirun -np " + (nProcs+1) + " ./sdm " + debugArgs);
		JobRunConfiguration jobConfig = getJobRunConfiguration(path, appName, resourceMgrName, machineName, queueName, nProcs, firstNode, NProcsPerNode);
		jobConfig.setDebug();
		jobConfig.setDebuggerPath(sdmPath);
		jobConfig.setDebuggerArgs(debugArgs);
		return jobConfig;
	}
	public static JobRunConfiguration getJobDebugConfiguration(ICProject project, String appName, String resourceMgrName, String machineName, String queueName, int nProcs, int firstNode, int NProcsPerNode, String debuggerType, String debugHost, int debugPort, String sdmPath) {
		return getJobDebugConfiguration(project.getProject().getLocation().toOSString(), appName, resourceMgrName, machineName, queueName, nProcs, firstNode, NProcsPerNode, debuggerType, debugHost, debugPort, sdmPath);
	}
	public static IResourceManager createOrteManager(String ptp_orte_proxyPath) {
		ORTEResourceManagerFactory factory = new ORTEResourceManagerFactory();
		return factory.create(new ORTEResourceManagerConfiguration(factory, "ORTE", "Orte Resource", ptp_orte_proxyPath, false));		
	}
	/*
	public static IResourceManager createIResourceManager(String xmlFile, String mgrID) throws CoreException {
		File xml = PTPProjectHelper.getFileInPlugin(new Path(xmlFile));
		FileReader reader;
		try {
			reader = new FileReader(xml);
		} catch (IOException e) {
			System.err.println("No XML is found.  Err: " + e.getMessage());
			return null;
		}
		XMLMemento memento = XMLMemento.createReadRoot(reader);
		IMemento[] children = memento.getChildren("ResourceManager");
		if (children.length == 0) {
			System.err.println("IMemento: No ResourceManager found");
			return null;
		}

		IModelManager modelMgr = PTPCorePlugin.getDefault().getModelManager();
		if (modelMgr == null)
			return null;
		IResourceManagerFactory factory = modelMgr.getResourceManagerFactory(mgrID);
		if (factory == null) {
			System.err.println("IResourceManagerFactory is NULL");
			return null;
		}
		
		IMemento configMemento = children[0].getChild("Configuration");
		if (configMemento == null) {
			System.err.println("IMemento Configuration is NULL");
			return null;
		}
			
		if (!new File(configMemento.getString("proxyPath")).exists()) {
			System.err.println("Proxy path is not found: " + configMemento.getString("proxyPath"));
			System.err.println("Please check the resources/resourceManagers.xml file");
			return null;
		}
	
		IResourceManagerConfiguration config = factory.loadConfiguration(configMemento);
		if (config == null) {
			System.err.println("IResourceManagerConfiguration is not found");
			return null;
		}
		
		IResourceManager mgr = factory.create(config);
		//Boolean.valueOf(children[0].getString("IsRunning")).booleanValue();
		if (mgr != null) {
			//in any case start resource manager
			mgr.startUp(new SubProgressMonitor(new NullProgressMonitor(), 1));
		}
		return mgr;
	}
	*/
}
