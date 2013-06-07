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
package org.eclipse.ptp.internal.debug.sdm.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory;
import org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory;
import org.eclipse.ptp.internal.debug.core.pdi.Session;
import org.eclipse.ptp.internal.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.PDIDebugger;
import org.eclipse.ptp.internal.debug.sdm.core.utils.DebugUtil;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.osgi.framework.Bundle;

/**
 * Main SDM debugger specified using the parallelDebugger extension point.
 * 
 * A new instance of this class is created for each debug session.
 * 
 * @author clement
 * 
 */
public class SDMDebugger implements IPDebugger {
	private static final String WORKING_DIR = ".eclipsesettings"; //$NON-NLS-1$
	private static final String SDM = "sdm"; //$NON-NLS-1$
	private static final String BUNDLE_PREFIX = "org.eclipse.ptp."; //$NON-NLS-1$
	private static final String OS = "os"; //$NON-NLS-1$

	private final IPDIDebugger fPdiDebugger = new PDIDebugger();
	private final IPDIModelFactory fModelFactory = new SDMModelFactory();
	private final IPDIManagerFactory fManagerFactory = new SDMManagerFactory();
	private final IPDIEventFactory fEventFactory = new SDMEventFactory();
	private final IPDIRequestFactory fRequestFactory = new SDMRequestFactory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugger#cleanup(org.eclipse.ptp.debug.core .launch.IPLaunch)
	 */
	public synchronized void cleanup(IPLaunch launch) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugger#createDebugSession(long, org.eclipse.ptp.debug.core.launch.IPLaunch,
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

		return session;
	}

	private String deploySDM(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 40);
		boolean useBuiltin = configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_USE_BUILTIN_SDM, false);
		if (useBuiltin) {
			String remoteId = LaunchUtils.getRemoteServicesId(configuration);
			String connectionName = LaunchUtils.getConnectionName(configuration);
			if (remoteId != null && connectionName != null) {
				IRemoteServices remoteServices = RemoteServices.getRemoteServices(remoteId, progress.newChild(10));
				if (remoteServices != null) {
					IRemoteConnection remoteConnection = remoteServices.getConnectionManager().getConnection(connectionName);
					if (remoteConnection != null) {
						if (!remoteConnection.isOpen()) {
							try {
								progress.subTask(Messages.SDMDebugger_Opening_connection);
								remoteConnection.open(progress.newChild(10));
							} catch (RemoteConnectionException e) {
								throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(),
										Messages.SDMDebugger_Could_not_open_connection, e));
							}
						}
						if (remoteConnection.isOpen()) {
							String osName = normalize(remoteConnection.getProperty(IRemoteConnection.OS_NAME_PROPERTY));
							String osArch = remoteConnection.getProperty(IRemoteConnection.OS_ARCH_PROPERTY);
							if (osName != null && osArch != null) {
								Bundle bundle = Platform.getBundle(BUNDLE_PREFIX + osName);
								if (bundle != null) {
									IRemoteFileManager fileManager = remoteServices.getFileManager(remoteConnection);
									if (fileManager != null) {
										/*
										 * Create the .eclipssettings directory if it doesn't exist on the remote machine. The mkdir
										 * does nothing if the directory already exists.
										 */
										IPath destDirPath = new Path(remoteConnection.getWorkingDirectory()).append(WORKING_DIR);
										IFileStore destDir = fileManager.getResource(destDirPath.toString());
										destDir.mkdir(EFS.NONE, progress.newChild(1));

										/*
										 * Find sdm in correct bundle
										 */
										IFileStore local = null;
										IPath srcPath = new Path(OS).append(osName).append(osArch).append(SDM);
										URL jarURL = FileLocator.find(bundle, srcPath, null);
										if (jarURL != null) {
											try {
												jarURL = FileLocator.toFileURL(jarURL);
												local = EFS.getStore(URIUtil.toURI(jarURL));
											} catch (Exception e) {
												throw new CoreException(new Status(IStatus.ERROR,
														SDMDebugCorePlugin.getUniqueIdentifier(),
														Messages.SDMDebugger_Could_not_locate_SDM_exectuable, e));
											}
										}

										/*
										 * Now copy the sdm if necessary
										 */
										if (local != null) {
											IFileStore destPath = destDir.getChild(SDM);
											IFileInfo destInfo = destPath.fetchInfo(EFS.NONE, progress.newChild(10));
											IFileInfo localInfo = local.fetchInfo(EFS.NONE, progress.newChild(10));

											/*
											 * Only copy if the sdm does not exist, or the size is different from the local version
											 * TODO: checking the size is not very accurate, it would be better to use a crypto hash
											 */
											if (!destInfo.exists() || destInfo.getLength() != localInfo.getLength()) {
												progress.subTask(Messages.SDMDebugger_Copying_SDM_to_target_system);
												local.copy(destPath, EFS.OVERWRITE, progress.newChild(70));
											}
											return destPath.toURI().getPath();
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_EXECUTABLE, ""); //$NON-NLS-1$
	}

	/**
	 * Work out the expected number of processes in the job. If it hasn't been specified, assume one.
	 * 
	 * @param launch
	 *            job that was launched
	 * @return number of processes
	 */
	private int getJobSize(IPLaunch launch) {
		int nprocs = 1;
		IJobStatus job = JobManager.getInstance().getJob(launch.getJobControl().getControlId(), launch.getJobId());
		if (job != null) {
			IPJobStatus pJob = (IPJobStatus) job.getAdapter(IPJobStatus.class);
			if (pJob != null) {
				nprocs = pJob.getNumberOfProcesses();
				if (nprocs == 0) {
					nprocs = 1;
				}
			}
		}
		return nprocs;
	}

	/**
	 * Helper method to locate the remote connection used by the launch configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return remote connection or null if none specified
	 * @throws CoreException
	 */
	private IRemoteConnection getRemoteConnection(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		String remId = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_REMOTE_SERVICES_ID, (String) null);
		if (remId != null) {
			IRemoteServices services = RemoteServices.getRemoteServices(remId, monitor);
			if (services != null) {
				String name = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONNECTION_NAME, (String) null);
				if (name != null) {
					return services.getConnectionManager().getConnection(name);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugger#initialize(org.eclipse.debug.core .ILaunchConfiguration,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 5.0
	 */
	public synchronized void initialize(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 30);
		if (Preferences.getBoolean(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUG_CLIENT_ENABLED)) {
			int level = Preferences.getInt(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUG_CLIENT_LEVEL);
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

		ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();

		List<String> dbgArgs = new ArrayList<String>();

		try {
			fPdiDebugger.initialize(configuration, dbgArgs, progress.newChild(10));
		} catch (PDIException e) {
			throw newCoreException(e.getLocalizedMessage());
		}

		String localAddress = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, "localhost"); //$NON-NLS-1$

		dbgArgs.add("--host=" + localAddress); //$NON-NLS-1$
		String debuggerBackend = configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
				(String) null);
		if (debuggerBackend != null) {
			dbgArgs.add("--debugger=" + debuggerBackend); //$NON-NLS-1$
		}
		String dbgPath = configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND_PATH, (String) null);
		if (dbgPath != null) {
			dbgArgs.add("--debugger_path=" + dbgPath); //$NON-NLS-1$
		}

		if (Preferences.getBoolean(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUG_ENABLED)) {
			dbgArgs.add("--debug=" + Preferences.getInt(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUG_LEVEL)); //$NON-NLS-1$
		}

		String dbgExePath = deploySDM(configuration, progress.newChild(10));
		verifyResource(dbgExePath, configuration, progress.newChild(10));

		workingCopy.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ARGS, stringify(dbgArgs));
		workingCopy.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, dbgExePath);
		workingCopy.doSave();

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

	private String normalize(String osName) {
		return osName.toLowerCase().replaceAll("\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Create a string containing each element of the list separated by a space
	 * 
	 * @param list
	 * @return
	 */
	private String stringify(List<String> list) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				result += " "; //$NON-NLS-1$
			}
			result += list.get(i);
		}
		return result;
	}

	/**
	 * Verify that the resource "path" actually exists. This just checks that the path references something real.
	 * 
	 * @param path
	 *            path to verify
	 * @param configuration
	 *            launch configuration
	 * @return IPath representing the path
	 * @throws CoreException
	 *             is thrown if the verification fails or the user cancels the progress monitor
	 * @since 5.0
	 */
	private IPath verifyResource(String path, ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		IRemoteConnection conn = getRemoteConnection(configuration, monitor);
		if (monitor.isCanceled()) {
			throw newCoreException(Messages.SDMDebugger_Operation_canceled_by_user);
		}
		if (conn == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, Messages.SDMDebugger_2));
		}
		IRemoteFileManager fileManager = conn.getRemoteServices().getFileManager(conn);
		if (fileManager == null) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, Messages.SDMDebugger_3));
		}
		if (!fileManager.getResource(path).fetchInfo().exists()) {
			throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.PLUGIN_ID, NLS.bind(Messages.SDMDebugger_5,
					new Object[] { path })));
		}
		return new Path(path);
	}
}
