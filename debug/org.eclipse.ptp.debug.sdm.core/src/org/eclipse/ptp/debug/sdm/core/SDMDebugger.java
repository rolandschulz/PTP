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
import org.eclipse.ptp.debug.sdm.core.pdi.PDIDebugger;
import org.eclipse.ptp.debug.sdm.core.utils.DebugUtil;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
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
	private IPDIDebugger pdiDebugger = null;
	private IPDIModelFactory modelFactory = null;
	private IPDIManagerFactory managerFactory = null;
	private IPDIEventFactory eventFactory = null;
	private IPDIRequestFactory requestFactory = null;

	IFileStore routingFileStore = null;
	SDMRunner sdmRunner = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#createDebugSession(long, org.eclipse.ptp.debug.core.launch.IPLaunch, org.eclipse.core.runtime.IPath)
	 */
	public IPDISession createDebugSession(long timeout, final IPLaunch launch, IPath corefile) throws CoreException {
		if (modelFactory == null) {
			modelFactory = new SDMModelFactory();
		}
		if (managerFactory == null) {
			managerFactory = new SDMManagerFactory();
		}
		if (eventFactory == null) {
			eventFactory = new SDMEventFactory();
		}
		if (requestFactory == null) {
			requestFactory = new SDMRequestFactory();
		}

		/*
		 * Writing the rounting file actually starts the SDM servers.
		 */
		writeRoutingFile(launch);

		/*
		 * Delay starting the master SDM (aka SDM client), to wait until SDM servers have started and until the sessions
		 * are listening on the debugger socket.
		 */
		sdmRunner.setJob(launch.getPJob());
		sdmRunner.schedule();

		IPDISession session = createSession(timeout, launch, corefile);

		return session;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#initialize(org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void initialize(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		ArrayAttribute<String> dbgArgsAttr = attrMgr.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition());

		if (dbgArgsAttr == null) {
			dbgArgsAttr = JobAttributes.getDebuggerArgumentsAttributeDefinition().create();
			attrMgr.addAttribute(dbgArgsAttr);
		}

		List<String> dbgArgs = dbgArgsAttr.getValue();

		try {
			getDebugger().initialize(configuration, dbgArgs, monitor);
		} catch (PDIException e) {
			throw newCoreException(e);
		}

		/*
		 * Store information to create routing file later.
		 */
		prepareRoutingFile(configuration, attrMgr, monitor);

		/*
		 * Prepare the Master SDM controller thread.
		 */
		IResourceManagerControl rm = null;
		rm = (IResourceManagerControl) getResourceManager(configuration);
		sdmRunner = new SDMRunner(rm);
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

		// remote setting
		String dbgExePath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, "");
		//if (dbgExePath == null) {
			//dbgExePath = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_FILE);
		//}
		IPath path = PTPLaunchPlugin.getDefault().verifyResource(dbgExePath, configuration);
		//IPath path = new Path(dbgExePath);
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
		attrMgr.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(true));

		/*
		 * Save SDM command line for future use.
		 */
		List<String> sdmCommand = new ArrayList<String>();
		sdmCommand.add(attrMgr.getAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition()).getValue()+"/"+attrMgr.getAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition()).getValue());
		sdmCommand.add("--master");
		sdmCommand.addAll(dbgArgs);
		sdmRunner.setCommand(sdmCommand);
		sdmRunner.setWorkDir(attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue());
	}

	public void cleanup(ILaunchConfiguration configuration, AttributeManager attrMgr, IPLaunch launch) {
		if (sdmRunner != null) {
			if (sdmRunner.getSdmState() == SDMMasterState.RUNNING) {
				DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master: still running, cancel is to be issued soon"); //$NON-NLS-1$
				new Thread("SDM master killer thread") {
					@Override
					public void run() {
						DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "sdm master killer: thread started"); //$NON-NLS-1$
						synchronized (this) {
							try {
								wait(5000);
							} catch (InterruptedException e) {
								// Ignore
							}
						}
						if (sdmRunner.getSdmState() == SDMMasterState.RUNNING) {
							DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master killer: cancel SDM master now"); //$NON-NLS-1$
							sdmRunner.cancel();
						} else {
							DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "sdm master killer: do not cancel SDM master, since it finished by itself."); //$NON-NLS-1$
						}
						DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, "sdm master killer: thread finished"); //$NON-NLS-1$
					}
				}.start();
			}
		}
	}

	/**
	 * Get the PDI debugger implementation. Creates the class if necessary.
	 *
	 * @return IPDIDebugger
	 */
	private IPDIDebugger getDebugger() {
		if (pdiDebugger == null) {
			pdiDebugger = new PDIDebugger();
		}
		return pdiDebugger;
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
	 * Create a CoreException that can be thrown
	 *
	 * @param exception
	 * @return CoreException
	 */
	private CoreException newCoreException(Throwable exception) {
		MultiStatus status = new MultiStatus(SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, "Cannot start debugging", exception);
		status.add(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, exception == null ? new String() : exception.getLocalizedMessage(), exception));
		return new CoreException(status);
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
			return new Session(managerFactory, requestFactory, eventFactory, modelFactory,
					launch.getLaunchConfiguration(), timeout, getDebugger(), job.getID(), job_size);
		}
		catch (PDIException e) {
			throw newCoreException(e);
		}
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

	private void prepareRoutingFile(ILaunchConfiguration configuration,
			AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		IPath routingFilePath = new Path(attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue());
		routingFilePath = routingFilePath.append("routing_file");

		IResourceManagerControl rm = (IResourceManagerControl) getResourceManager(configuration);
		IResourceManagerConfiguration conf = rm.getConfiguration();
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(conf.getRemoteServicesId());
		IRemoteConnectionManager rconnMgr = remoteServices.getConnectionManager();
		IRemoteConnection rconn = rconnMgr.getConnection(conf.getConnectionName());
		IRemoteFileManager remoteFileManager = remoteServices.getFileManager(rconn);

		try {
			this.routingFileStore = remoteFileManager.getResource(routingFilePath, monitor);
		} catch (IOException e) {
			throw newCoreException(e);
		}

		IFileInfo info = routingFileStore.fetchInfo();
		if (info.exists()) {
			try {
				routingFileStore.delete(0, monitor);
			} catch (CoreException e) {
				throw newCoreException(e);
			}
			routingFileStore.fetchInfo();
		}
	}

	private void writeRoutingFile(IPLaunch launch) throws CoreException {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, "debug: Write routing file");
		IProgressMonitor monitor = new NullProgressMonitor();
		OutputStream os = null;
		try {
			os = routingFileStore.openOutputStream(0, monitor);
		} catch (CoreException e) {
			throw newCoreException(e);
		}
		PrintWriter pw = new PrintWriter(os);
		IPProcess processes[] = launch.getPJob().getProcesses();
		pw.format("%d\n", processes.length);
		int base = 10000;
		int range = 10000;
		Random random = new Random();
		for (IPProcess process : processes) {
			String index = process.getProcessIndex();
			IPNode node = process.getNode();
			String nodeName = node.getName();
			int portNumber = base + random.nextInt(range);
			pw.format("%s %s %d\n", index, nodeName, portNumber);
		}
		pw.close();
		try {
			os.close();
		} catch (IOException e) {
			throw newCoreException(e);
		}
	}
}
