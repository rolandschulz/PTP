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
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.debug.core.IPDebugger;
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
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.utils.core.BitSetIterable;

/**
 * Main SDM debugger specified using the parallelDebugger extension point.
 * 
 * A new instance of this class is created for each debug session.
 * 
 * @author clement
 * 
 */
public class SDMDebugger implements IPDebugger {
	private final IPDIDebugger fPdiDebugger = new PDIDebugger();
	private final IPDIModelFactory fModelFactory = new SDMModelFactory();
	private final IPDIManagerFactory fManagerFactory = new SDMManagerFactory();
	private final IPDIEventFactory fEventFactory = new SDMEventFactory();
	private final IPDIRequestFactory fRequestFactory = new SDMRequestFactory();
	private IFileStore fRoutingFileStore = null;
	private SDMRunner fSdmRunner = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.IPDebugger#cleanup(org.eclipse.ptp.debug.core
	 * .launch.IPLaunch)
	 */
	public synchronized void cleanup(IPLaunch launch) {
		if (fSdmRunner != null) {
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_8);
			new Thread(Messages.SDMDebugger_7) {
				@Override
				public void run() {
					DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMDebugger_9);
					synchronized (this) {
						// Give the runner a chance to finish itself
						try {
							wait(5000);
						} catch (InterruptedException e) {
							// Ignore
						}
						if (fSdmRunner.getSdmState() == SDMMasterState.RUNNING) {
							DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_11);
							fSdmRunner.cancel();
							try {
								fSdmRunner.join();
							} catch (InterruptedException e) {
								// Not much we can do at this point
							}
						} else {
							DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_13);
						}
						DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMDebugger_14);
						fSdmRunner = null;
					}
				}
			}.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugger#createDebugSession(long,
	 * org.eclipse.ptp.debug.core.launch.IPLaunch,
	 * org.eclipse.core.runtime.IPath)
	 */
	/**
	 * @since 5.0
	 */
	public synchronized IPDISession createDebugSession(long timeout, final IPLaunch launch, IProgressMonitor monitor)
			throws CoreException {
		int jobSize = getJobSize(launch);

		IPDISession session;
		try {
			session = new Session(fManagerFactory, fRequestFactory, fEventFactory, fModelFactory, launch.getLaunchConfiguration(),
					timeout, fPdiDebugger, launch.getJobId(), jobSize);
		} catch (PDIException e) {
			throw newCoreException(e.getLocalizedMessage());
		}

		if (fRoutingFileStore != null) {
			/*
			 * Writing the routing file actually starts the SDM servers.
			 */
			writeRoutingFile(launch, monitor);

			/*
			 * Delay starting the master SDM (aka SDM client), to wait until SDM
			 * servers have started and until the sessions are listening on the
			 * debugger socket.
			 */
			fSdmRunner.setJob(launch.getJobId());
			fSdmRunner.schedule();
		}

		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.IPDebugger#initialize(org.eclipse.ptp.core
	 * .attributes.AttributeManager)
	 */
	public synchronized void initialize(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 30);
		try {
			if (Preferences.getBoolean(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUG_CLIENT_ENABLED)) {
				int level = Preferences.getInt(SDMDebugCorePlugin.getUniqueIdentifier(),
						SDMPreferenceConstants.SDM_DEBUG_CLIENT_LEVEL);
				if ((level & SDMPreferenceConstants.DEBUG_CLIENT_TRACING) == SDMPreferenceConstants.DEBUG_CLIENT_TRACING) {
					DebugUtil.SDM_MASTER_TRACING = true;
				}
				if ((level & SDMPreferenceConstants.DEBUG_CLIENT_TRACING_MORE) == SDMPreferenceConstants.DEBUG_CLIENT_TRACING_MORE) {
					DebugUtil.SDM_MASTER_TRACING_MORE = true;
				}
				if ((level & SDMPreferenceConstants.DEBUG_CLIENT_OUTPUT) == SDMPreferenceConstants.DEBUG_CLIENT_OUTPUT) {
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
				fPdiDebugger.initialize(configuration, dbgArgs, progress.newChild(10));
			} catch (PDIException e) {
				throw newCoreException(e.getLocalizedMessage());
			}

			String localAddress = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, "localhost"); //$NON-NLS-1$

			dbgArgs.add("--host=" + localAddress); //$NON-NLS-1$
			String debuggerBackend = Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE);
			debuggerBackend = configuration
					.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND, debuggerBackend);
			dbgArgs.add("--debugger=" + debuggerBackend); //$NON-NLS-1$

			String dbgPath = Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH);
			if (dbgPath.length() > 0) {
				dbgArgs.add("--debugger_path=" + dbgPath); //$NON-NLS-1$
			}

			String dbgExtraArgs = Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.SDM_DEBUGGER_ARGS);
			if (dbgExtraArgs.length() > 0) {
				dbgArgs.addAll(Arrays.asList(dbgExtraArgs.split(" "))); //$NON-NLS-1$
			}

			if (Preferences.getBoolean(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUG_ENABLED)) {
				dbgArgs.add("--debug=" + Preferences.getInt(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUG_LEVEL)); //$NON-NLS-1$
			}

			// remote setting
			String dbgExePath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, ""); //$NON-NLS-1$
			IPath path = verifyResource(dbgExePath, configuration, progress.newChild(10));
			attrMgr.addAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
			attrMgr.addAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition().create(
					path.removeLastSegments(1).toString()));

			StringAttribute wdAttr = attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition());
			String dbgWD = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String) null);
			if (dbgWD != null) {
				if (wdAttr != null) {
					wdAttr.setValueAsString(dbgWD);
				} else {
					wdAttr = JobAttributes.getWorkingDirectoryAttributeDefinition().create(dbgWD);
					attrMgr.addAttribute(wdAttr);
				}
			}

			/*
			 * Prepare the Master SDM controller thread if required by the RM.
			 */
			IResourceManagerControl rm = getResourceManager(configuration);

			if (rm.getConfiguration().needsDebuggerLaunchHelp()) {
				/*
				 * Store information to create routing file later.
				 */
				prepareRoutingFile(configuration, attrMgr, progress.newChild(10));

				/*
				 * Create SDM master thread
				 */
				fSdmRunner = new SDMRunner(rm);

				/*
				 * Set SDM command line.
				 */
				List<String> sdmCommand = new ArrayList<String>();
				sdmCommand.add(dbgExePath);
				sdmCommand.add("--master"); //$NON-NLS-1$
				sdmCommand.addAll(dbgArgs);
				fSdmRunner.setCommand(sdmCommand);
				if (wdAttr != null) {
					fSdmRunner.setWorkDir(wdAttr.getValue());
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Verify that the resource "path" actually exists. This just checks that
	 * the path references something real.
	 * 
	 * @param path
	 *            path to verify
	 * @param configuration
	 *            launch configuration
	 * @return IPath representing the path
	 * @throws CoreException
	 *             is thrown if the verification fails or the user cancels the
	 *             progress monitor
	 * @since 5.0
	 */
	public IPath verifyResource(String path, ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		IResourceManagerControl rm = getResourceManager(configuration);
		if (rm == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, Messages.SDMDebugger_4));
		}
		IResourceManagerConfiguration conf = rm.getConfiguration();
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(conf.getRemoteServicesId(), monitor);
		if (monitor.isCanceled()) {
			throw newCoreException(Messages.SDMDebugger_Operation_canceled_by_user);
		}
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, Messages.SDMDebugger_0));
		}
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		if (connMgr == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, Messages.SDMDebugger_1));
		}
		IRemoteConnection conn = connMgr.getConnection(conf.getConnectionName());
		if (conn == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, Messages.SDMDebugger_2));
		}
		IRemoteFileManager fileManager = remoteServices.getFileManager(conn);
		if (fileManager == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, Messages.SDMDebugger_3));
		}
		if (!fileManager.getResource(path).fetchInfo().exists()) {
			throw new CoreException(new Status(IStatus.INFO, SDMDebugCorePlugin.PLUGIN_ID, NLS.bind(Messages.SDMDebugger_5,
					new Object[] { path })));
		}
		return new Path(path);
	}

	/**
	 * Work out the expected number of processes in the job. If it hasn't been
	 * specified, assume one.
	 * 
	 * @param launch
	 *            job that was launched
	 * @return number of processes
	 */
	private int getJobSize(IPLaunch launch) {
		int nprocs = 1;
		IResourceManagerControl rmc = launch.getResourceManager();
		if (rmc != null) {
			IPResourceManager rm = (IPResourceManager) rmc.getAdapter(IPResourceManager.class);
			if (rm != null) {
				IPJob job = rm.getJobById(launch.getJobId());
				if (job != null) {
					nprocs = job.getProcessJobRanks().cardinality();
					if (nprocs == 0) {
						nprocs = 1;
					}
				}
			}
		}
		return nprocs;
	}

	/**
	 * Helper method to locate the resource manager used by the launch
	 * configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return resource manager or null if none specified
	 * @throws CoreException
	 */
	private IResourceManagerControl getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		String rmUniqueName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
				(String) null);
		IResourceManagerControl rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmUniqueName);
		if (rm.getState() == ResourceManagerAttributes.State.STARTED) {
			return rm;
		}
		return null;
	}

	/**
	 * Create a CoreException that can be thrown
	 * 
	 * @param exception
	 * @return CoreException
	 */
	private CoreException newCoreException(String message) {
		Status status = new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), message, null);
		return new CoreException(status);
	}

	/**
	 * Initialize the routing file for this debug session.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param attrMgr
	 *            attribute manager used to construct launch attributes
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	private void prepareRoutingFile(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);

		try {
			IPath routingFilePath = new Path(attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition())
					.getValue());
			routingFilePath = routingFilePath.append("routing_file"); //$NON-NLS-1$

			IResourceManagerControl rm = getResourceManager(configuration);
			IResourceManagerConfiguration conf = rm.getConfiguration();
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(conf.getRemoteServicesId(),
					progress.newChild(5));
			if (progress.isCanceled()) {
				throw newCoreException(Messages.SDMDebugger_Operation_canceled_by_user);
			}
			IRemoteConnectionManager rconnMgr = remoteServices.getConnectionManager();
			IRemoteConnection rconn = rconnMgr.getConnection(conf.getConnectionName());
			IRemoteFileManager remoteFileManager = remoteServices.getFileManager(rconn);

			fRoutingFileStore = remoteFileManager.getResource(routingFilePath.toString());

			if (fRoutingFileStore.fetchInfo(EFS.NONE, progress.newChild(3)).exists()) {
				try {
					fRoutingFileStore.delete(0, progress.newChild(2));
				} catch (CoreException e) {
					throw newCoreException(e.getLocalizedMessage());
				}
				fRoutingFileStore.fetchInfo();
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Generate the routing file once the debugger has launched.
	 * 
	 * NOTE: This currently assumes a shared filesystem that all debugger
	 * processes have access to. The plan is to replace this with a routing
	 * distribution operation in the debugger.
	 * 
	 * @param launch
	 *            launch configuration
	 * @throws CoreException
	 */
	private void writeRoutingFile(IPLaunch launch, IProgressMonitor monitor) throws CoreException {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMDebugger_12);
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			OutputStream os = null;
			try {
				os = fRoutingFileStore.openOutputStream(0, progress.newChild(10));
			} catch (CoreException e) {
				throw newCoreException(e.getLocalizedMessage());
			}
			progress.subTask(Messages.SDMDebugger_6);
			PrintWriter pw = new PrintWriter(os);
			final String jobId = launch.getJobId();
			final IPResourceManager rm = (IPResourceManager) launch.getResourceManager().getAdapter(IPResourceManager.class);
			if (rm != null) {
				final IPJob pJob = rm.getJobById(jobId);
				BitSet processJobRanks = pJob.getProcessJobRanks();
				pw.format("%d\n", processJobRanks.cardinality()); //$NON-NLS-1$
				int base = 50000;
				int range = 10000;
				Random random = new Random();
				for (Integer processIndex : new BitSetIterable(processJobRanks)) {
					String nodeId = pJob.getProcessNodeId(processIndex);
					if (nodeId == null) {
						progress.subTask(Messages.SDMDebugger_10);
						while (nodeId == null && !progress.isCanceled()) {
							try {
								wait(1000);
							} catch (InterruptedException e) {
								// ignore
							}
							nodeId = pJob.getProcessNodeId(processIndex);
							progress.worked(1);
						}
					}
					if (progress.isCanceled()) {
						throw newCoreException(Messages.SDMDebugger_Operation_canceled_by_user);
					}
					IPNode node = rm.getNodeById(nodeId);
					if (node == null) {
						throw newCoreException(Messages.SDMDebugger_15);
					}
					String nodeName = node.getName();
					int portNumber = base + random.nextInt(range);
					pw.format("%s %s %d\n", processIndex, nodeName, portNumber); //$NON-NLS-1$
					progress.setWorkRemaining(60);
					progress.worked(10);
				}
				pw.close();
			}
			try {
				os.close();
			} catch (IOException e) {
				throw newCoreException(e.getLocalizedMessage());
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}
