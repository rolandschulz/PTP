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
package org.eclipse.ptp.debug.sdm.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.Session;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory;
import org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory;
import org.eclipse.ptp.debug.sdm.core.SDMRunner.SDMMasterState;
import org.eclipse.ptp.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.debug.sdm.core.pdi.PDIDebugger;
import org.eclipse.ptp.debug.sdm.core.utils.DebugUtil;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * @author clement
 *
 */
public class SDMDebugger implements IPDebugger {
	private IPDIDebugger fPdiDebugger = null;
	private IPDIModelFactory fModelFactory = null;
	private IPDIManagerFactory fManagerFactory = null;
	private IPDIEventFactory fEventFactory = null;
	private IPDIRequestFactory fRequestFactory = null;

	private IFileStore fRoutingFileStore = null;
	private SDMRunner fSdmRunner = null;
	private boolean fPreLaunchHelpNeeded = false;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#cleanup(org.eclipse.ptp.debug.core.launch.IPLaunch)
	 */
	public void cleanup(IPLaunch launch) {
		if (fSdmRunner != null) {
			if (fSdmRunner.getSdmState() == SDMMasterState.RUNNING) {
				DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_8); 
				new Thread(Messages.SDMDebugger_7) {
					@Override
					public void run() {
						DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMDebugger_9); 
						synchronized (this) {
							try {
								wait(5000);
							} catch (InterruptedException e) {
								// Ignore
							}
						}
						if (fSdmRunner.getSdmState() == SDMMasterState.RUNNING) {
							DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_11); 
							fSdmRunner.cancel();
						} else {
							DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_13); 
						}
						DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMDebugger_14); 
					}
				}.start();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#createDebugSession(long, org.eclipse.ptp.debug.core.launch.IPLaunch, org.eclipse.core.runtime.IPath)
	 */
	public IPDISession createDebugSession(long timeout, final IPLaunch launch, IPath corefile) throws CoreException {
		if (fModelFactory == null) {
			fModelFactory = new SDMModelFactory();
		}
		if (fManagerFactory == null) {
			fManagerFactory = new SDMManagerFactory();
		}
		if (fEventFactory == null) {
			fEventFactory = new SDMEventFactory();
		}
		if (fRequestFactory == null) {
			fRequestFactory = new SDMRequestFactory();
		}

		if (fPreLaunchHelpNeeded) {
			/*
			 * Writing the routing file actually starts the SDM servers.
			 */
			writeRoutingFile(launch);
	
			/*
			 * Delay starting the master SDM (aka SDM client), to wait until SDM servers have started and until the sessions
			 * are listening on the debugger socket.
			 */
			fSdmRunner.setJob(launch.getPJob());
			fSdmRunner.schedule();
		}
		
		IPDISession session = createSession(timeout, launch, corefile);

		return session;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#getLaunchAttributes(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void getLaunchAttributes(ILaunchConfiguration configuration, AttributeManager attrMgr) throws CoreException {
		ArrayAttribute<String> dbgArgsAttr = attrMgr.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition());

		if (dbgArgsAttr == null) {
			dbgArgsAttr = JobAttributes.getDebuggerArgumentsAttributeDefinition().create();
			attrMgr.addAttribute(dbgArgsAttr);
		}

		List<String> dbgArgs = dbgArgsAttr.getValue();

		Preferences store = SDMDebugCorePlugin.getDefault().getPluginPreferences();

		String localAddress = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, "localhost"); //$NON-NLS-1$

		dbgArgs.add("--host=" + localAddress); //$NON-NLS-1$
		dbgArgs.add("--debugger=" + store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)); //$NON-NLS-1$

		String dbgPath = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH);
		if (dbgPath.length() > 0) {
			dbgArgs.add("--debugger_path=" + dbgPath); //$NON-NLS-1$
		}

		String dbgExtraArgs = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS);
		if (dbgExtraArgs.length() > 0) {
			dbgArgs.addAll(Arrays.asList(dbgExtraArgs.split(" "))); //$NON-NLS-1$
		}
		
		if (store.getBoolean(SDMPreferenceConstants.SDM_DEBUG_ENABLED)) {
			dbgArgs.add("--debug=" + store.getInt(SDMPreferenceConstants.SDM_DEBUG_LEVEL)); //$NON-NLS-1$
		} 

		// remote setting
		String dbgExePath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, ""); //$NON-NLS-1$
		IPath path = verifyResource(dbgExePath, configuration);
		attrMgr.addAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
		attrMgr.addAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition().create(path.removeLastSegments(1).toString()));

		String dbgWD = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String)null);
		if (dbgWD != null) {
			StringAttribute wdAttr = attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition());
			if (wdAttr != null) {
				wdAttr.setValueAsString(dbgWD);
			} else {
				attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(dbgWD));
			}
			attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(dbgWD + "/Debug")); //$NON-NLS-1$
		}

		/*
		 * Save SDM command line for future use.
		 */
		List<String> sdmCommand = new ArrayList<String>();
		sdmCommand.add(attrMgr.getAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition()).getValue()+"/"+attrMgr.getAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition()).getValue()); //$NON-NLS-1$
		sdmCommand.add("--master"); //$NON-NLS-1$
		sdmCommand.addAll(dbgArgs);
		fSdmRunner.setCommand(sdmCommand);
		fSdmRunner.setWorkDir(attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#initialize(org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void initialize(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		Preferences store = SDMDebugCorePlugin.getDefault().getPluginPreferences();

		if (store.getBoolean(SDMPreferenceConstants.SDM_DEBUG_MASTER_ENABLED)) {
			int level = store.getInt(SDMPreferenceConstants.SDM_DEBUG_MASTER_LEVEL);
			if ((level & SDMPreferenceConstants.DEBUG_MASTER_TRACING) == SDMPreferenceConstants.DEBUG_MASTER_TRACING) {
				DebugUtil.SDM_MASTER_TRACING = true;
			}
			if ((level & SDMPreferenceConstants.DEBUG_MASTER_TRACING_MORE) == SDMPreferenceConstants.DEBUG_MASTER_TRACING_MORE) {
				DebugUtil.SDM_MASTER_TRACING_MORE = true;
			}
			if ((level & SDMPreferenceConstants.DEBUG_MASTER_OUTPUT) == SDMPreferenceConstants.DEBUG_MASTER_OUTPUT) {
				DebugUtil.SDM_MASTER_OUTPUT_TRACING = true;
			}
		}
		
		ArrayAttribute<String> dbgArgsAttr = attrMgr.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition());

		if (dbgArgsAttr == null) {
			dbgArgsAttr = JobAttributes.getDebuggerArgumentsAttributeDefinition().create();
			attrMgr.addAttribute(dbgArgsAttr);
		}

		List<String> dbgArgs = dbgArgsAttr.getValue();

		try {
			getPDIDebugger().initialize(configuration, dbgArgs, monitor);
		} catch (PDIException e) {
			throw newCoreException(e);
		}

		if (fPreLaunchHelpNeeded) {
			/*
			 * Store information to create routing file later.
			 */
			prepareRoutingFile(configuration, attrMgr, monitor);
	
			/*
			 * Prepare the Master SDM controller thread.
			 */
			IResourceManagerControl rm = null;
			rm = (IResourceManagerControl) getResourceManager(configuration);
			fSdmRunner = new SDMRunner(rm);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#setPreLaunchHelp(boolean)
	 */
	public void setPreLaunchHelp(boolean flag) {
		fPreLaunchHelpNeeded = flag;
	}

	/**
	 * Verify that the resource "path" actually exists. This just checks
	 * that the path references something real.
	 * 
	 * @param path
	 * @param configuration
	 * @return IPath
	 * @throws CoreException
	 */
	public IPath verifyResource(String path, ILaunchConfiguration configuration) throws CoreException {
		IResourceManagerControl rm = (IResourceManagerControl)getResourceManager(configuration);
		if (rm == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, 
					Messages.SDMDebugger_4));
		}
		IResourceManagerConfiguration conf = rm.getConfiguration();
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(conf.getRemoteServicesId());
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID,
					Messages.SDMDebugger_0));
		}
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		if (connMgr == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID,
					Messages.SDMDebugger_1));
		}
		IRemoteConnection conn = connMgr.getConnection(conf.getConnectionName());
		if (conn == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, 
					Messages.SDMDebugger_2));
		}
		IRemoteFileManager fileManager = remoteServices.getFileManager(conn);
		if (fileManager == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, 
					Messages.SDMDebugger_3));
		}
		IPath resPath = new Path(path);
		try {
			IFileStore res = fileManager.getResource(resPath, new NullProgressMonitor());
			if (!res.fetchInfo().exists()) {
				throw new CoreException(new Status(IStatus.INFO, SDMDebugCorePlugin.PLUGIN_ID,
						NLS.bind(Messages.SDMDebugger_5, new Object[] {path})));
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID,
					Messages.SDMDebugger_6, e.getCause()));
		}
		return resPath;
	}

	/**
	 * Work out the expected number of processes in the job. If it hasn't been
	 * specified, assume one.
	 *
	 * @param job job that was launched
	 * @return number of processes
	 */
	private int getJobSize(IPJob job) {
		int nprocs = job.getProcesses().length;
		if (nprocs == 0) {
			nprocs = 1;
		}
		return nprocs;
	}

	/**
	 * Get the PDI debugger implementation. Creates the class if necessary.
	 *
	 * @return IPDIDebugger
	 */
	private IPDIDebugger getPDIDebugger() {
		if (fPdiDebugger == null) {
			fPdiDebugger = new PDIDebugger();
		}
		return fPdiDebugger;
	}

	private IResourceManager getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		String rmUniqueName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String)null);
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED &&
					rm.getUniqueName().equals(rmUniqueName)) {
				return rm;
			}
		}
		return null;
	}

	/**
	 * Create a CoreException that can be thrown
	 *
	 * @param exception
	 * @return CoreException
	 */
	private CoreException newCoreException(Throwable exception) {
		MultiStatus status = new MultiStatus(SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, Messages.SDMDebugger_10, exception);
		status.add(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, exception == null ? new String() : exception.getLocalizedMessage(), exception));
		return new CoreException(status);
	}

	private void prepareRoutingFile(ILaunchConfiguration configuration,
			AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		IPath routingFilePath = new Path(attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue());
		routingFilePath = routingFilePath.append("routing_file"); //$NON-NLS-1$

		IResourceManagerControl rm = (IResourceManagerControl) getResourceManager(configuration);
		IResourceManagerConfiguration conf = rm.getConfiguration();
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(conf.getRemoteServicesId());
		IRemoteConnectionManager rconnMgr = remoteServices.getConnectionManager();
		IRemoteConnection rconn = rconnMgr.getConnection(conf.getConnectionName());
		IRemoteFileManager remoteFileManager = remoteServices.getFileManager(rconn);

		try {
			this.fRoutingFileStore = remoteFileManager.getResource(routingFilePath, monitor);
		} catch (IOException e) {
			throw newCoreException(e);
		}

		IFileInfo info = fRoutingFileStore.fetchInfo();
		if (info.exists()) {
			try {
				fRoutingFileStore.delete(0, monitor);
			} catch (CoreException e) {
				throw newCoreException(e);
			}
			fRoutingFileStore.fetchInfo();
		}
	}

	private void writeRoutingFile(IPLaunch launch) throws CoreException {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_12);
		IProgressMonitor monitor = new NullProgressMonitor();
		OutputStream os = null;
		try {
			os = fRoutingFileStore.openOutputStream(0, monitor);
		} catch (CoreException e) {
			throw newCoreException(e);
		}
		PrintWriter pw = new PrintWriter(os);
		IPProcess processes[] = launch.getPJob().getProcesses();
		pw.format("%d\n", processes.length); //$NON-NLS-1$
		int base = 50000;
		int range = 10000;
		Random random = new Random();
		for (IPProcess process : processes) {
			String index = process.getProcessIndex();
			IPNode node = process.getNode();
			String nodeName = node.getName();
			int portNumber = base + random.nextInt(range);
			pw.format("%s %s %d\n", index, nodeName, portNumber); //$NON-NLS-1$
		}
		pw.close();
		try {
			os.close();
		} catch (IOException e) {
			throw newCoreException(e);
		}
	}
	
	/**
	 * Create a PDI session
	 *
	 * @param timeout
	 * @param launch
	 * @param corefile
	 * @param monitor
	 * @return Session
	 * @throws CoreException
	 */
	protected Session createSession(long timeout, IPLaunch launch, IPath corefile) throws CoreException {
		IPJob job = launch.getPJob();
		int job_size = getJobSize(job);
		try {
			return new Session(fManagerFactory, fRequestFactory, fEventFactory, fModelFactory,
					launch.getLaunchConfiguration(), timeout, getPDIDebugger(), job.getID(), job_size);
		}
		catch (PDIException e) {
			throw newCoreException(e);
		}
	}
}
