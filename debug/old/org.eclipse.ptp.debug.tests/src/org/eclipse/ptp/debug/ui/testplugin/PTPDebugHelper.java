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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.external.core.debugger.ParallelDebugger;
import org.eclipse.ptp.orte.core.ORTEAttributes;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerConfiguration;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerFactory;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration.CommonConfig;

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
	/**
	 * Create ORTE IResourceManager
	 * @param ptp_orte_proxyPath
	 * @return IResourceManager
	 */
	public static IResourceManager createOrteManager(String ptp_orte_proxyPath) {
		ORTEResourceManagerFactory factory = new ORTEResourceManagerFactory();
		String key = "ORTE";
		String desc = "Orte Resource";
		String uniqueID = "RMID:1178771512777"; 
		return factory.create(new ORTEResourceManagerConfiguration(factory, new CommonConfig(key, desc, uniqueID), ptp_orte_proxyPath, false));
	}
	
	public static AttributeManager createRuntimeAttrManager(IResourceManager rm, String workDir, String app, String[] argArr, String[] envArr, int numProcs) {
		AttributeManager attrMgr = new AttributeManager();
		try {
			setOrteAttrManager(attrMgr, rm, workDir, app, argArr, envArr, numProcs);
		} catch (CoreException e) {
			return null;
		}
		return attrMgr;
	}
	public static AttributeManager createDebugAttrManager(IResourceManager rm, String workDir, String app, String[] argArr, String[] envArr, int numProcs, String debugHost, String debuggerType, int debugPort, String[] extArgs, String sdmPath) {
		AttributeManager attrMgr = new AttributeManager();
		try {
			setOrteAttrManager(attrMgr, rm, workDir, app, argArr, envArr, numProcs);
			setDebugOrteAttrManager(attrMgr, debugHost, debuggerType, debugPort, extArgs, sdmPath, numProcs);
		} catch (CoreException e) {
			return null;
		}
		return attrMgr;
	}
	private static void setOrteAttrManager(AttributeManager attrMgr, IResourceManager rm, String workDir, String app, String[] argArr, String[] envArr, int numProcs) throws CoreException {
		try {
			IPQueue[] queues = rm.getQueues();
			if (queues != null) {
				attrMgr.addAttribute(QueueAttributes.getIdAttributeDefinition().create(queues[0].getID()));
			}
			IPath appPath = new Path(app);
			if (!appPath.toFile().exists()) {
				System.err.println("No application found.");
				throw new CoreException(Status.CANCEL_STATUS);
			}
			attrMgr.addAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(appPath.lastSegment()));

			String path = appPath.removeLastSegments(1).toOSString();
			if (path != null) {
				attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
			}
			
			if (workDir != null) {
				attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(workDir));
			}
			
			if (argArr != null) {
				attrMgr.addAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(argArr));
			}
			
			if (envArr != null) {
				attrMgr.addAttribute(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
			}
			//IRMLaunchConfigurationDynamicTab rmDynamicTab = new ORTERMLaunchConfigurationDynamicTab(rm);
			IntegerAttribute iattr = new IntegerAttribute(ORTEAttributes.getNumberOfProcessesAttributeDefinition(), numProcs);
			attrMgr.addAttributes(new IAttribute[] { iattr });
		} catch (IllegalValueException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		//attrMgr.addAttributes(getLaunchAttributes(configuration));
	}
	private static void setDebugOrteAttrManager(AttributeManager attrMgr, String debugHost, String debuggerType, int debugPort, String[] extArgs, String sdmPath, int numProcs) throws CoreException {
		String debugArgs = "--host=" + debugHost + " --debugger=" + debuggerType + " --port=" + debugPort;
		System.err.println("*** If you start JUnit test with manually launch sdm, please type the following on command line ***");
		System.err.println(">>> mpirun -np " + (numProcs+1) + " ./sdm " + debugArgs);
		try {
			String[] dbgArgs = new String[extArgs.length + 4];
			dbgArgs[0] = "--host=" + debugHost;
			dbgArgs[1] = "--debugger=" + debuggerType;
			dbgArgs[2] = "--debugger_path=" + sdmPath;
			dbgArgs[3] = "--port=" + debugPort;
			for (int i=0; i<extArgs.length; i++) {
				dbgArgs[4+i] = extArgs[i];
			}
			IPath path = new Path(sdmPath);
			attrMgr.addAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
			attrMgr.addAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition().create(path.removeLastSegments(1).toOSString()));
			attrMgr.addAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition().create(dbgArgs));
			attrMgr.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(true));
		} catch (IllegalValueException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}
	public static IPath getProjectPath(ICProject project) {
		return project.getProject().getLocation();	
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
