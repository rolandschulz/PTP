/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 *  Greg Watson - adapted to new framework
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.ems.core.EnvManagerRegistry;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.JobStatusMap;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.ManagedFilesJob;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.ManagedFilesJob.Operation;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.ScriptHandler;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command.CommandJob;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command.CommandJob.JobMode;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command.CommandJobStatus;
import org.eclipse.ptp.internal.rm.jaxb.control.core.utils.JobIdPinTable;
import org.eclipse.ptp.internal.rm.jaxb.control.core.variables.RMVariableMap;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemotePreferenceConstants;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.server.core.RemoteServerManager;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFileType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFilesType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;
import org.eclipse.ptp.rm.jaxb.core.data.SiteType;
import org.eclipse.ptp.rm.lml.da.server.core.LMLDAServer;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * The part of the JAXB framework responsible for handling job submission, termination, suspension and resumption. Also provides
 * on-demand job status checking. <br>
 * <br>
 * The state maintained by the control is volatile (in-memory only). The control is responsible for handing off to the caller status
 * objects containing job state, as well as means of accessing the process (if interactive) and the standard out and error streams.
 * When the job completes, these are eliminated from its internal map.<br>
 * <br>
 * The logic of this manager is generic; the specific commands used, files staged, and script constructed (if any) are all
 * configured via the resource manager XML. <br>
 * <br>
 * Currently, it is the control which handles updating the monitor component.
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class LaunchController implements ILaunchController {

	/*
	 * copied from AbstractToolRuntimeSystem; the RM should shut down when the remote connection is closed
	 */
	private class ConnectionChangeListener implements IRemoteConnectionChangeListener {
		public ConnectionChangeListener() {
			// Nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener# connectionChanged
		 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent)
		 */
		public void connectionChanged(IRemoteConnectionChangeEvent event) {
			if (event.getType() == IRemoteConnectionChangeEvent.CONNECTION_ABORTED
					|| event.getType() == IRemoteConnectionChangeEvent.CONNECTION_CLOSED) {
				try {
					stop();
				} catch (CoreException e) {
					JAXBControlCorePlugin.log(e);
				}
			}
		}
	}

	/**
	 * tries to open connection if closed
	 * 
	 * @param connection
	 * @param progress
	 * @throws RemoteConnectionException
	 */
	public static void checkConnection(IRemoteConnection connection, SubMonitor progress) throws CoreException {
		try {
			if (connection != null) {
				if (!connection.isOpen()) {
					connection.open(progress.newChild(25));
					if (!connection.isOpen()) {
						throw CoreExceptionUtils.newException(Messages.RemoteConnectionError + connection.getAddress());
					}
				}
			}
		} catch (RemoteConnectionException e) {
			throw CoreExceptionUtils.newException(Messages.RemoteConnectionError + connection.getAddress(), e);
		}
	}

	private final ConnectionChangeListener connectionListener = new ConnectionChangeListener();
	private final Map<String, String> launchEnv = new TreeMap<String, String>();

	private final JobIdPinTable pinTable = new JobIdPinTable();

	private String fControlId;
	private ICommandJobStatusMap jobStatusMap;
	private RMVariableMap rmVarMap;
	private String servicesId;
	private String connectionName;
	private boolean appendLaunchEnv;
	private boolean isActive = false;
	private boolean isInitialized = false;
	private String configURL;
	private RemoteServicesDelegate fRemoteServicesDelegate;

	private ResourceManagerData configData;

	public LaunchController() {
	}

	/**
	 * Helper to add a read-only attribute
	 * 
	 * @param name
	 * @param value
	 */
	private void addAttribute(String name, String value) {
		AttributeType attr = getEnvironment().get(name);
		if (attr == null) {
			attr = new AttributeType();
			attr.setName(name);
			attr.setVisible(true);
			attr.setReadOnly(true);
			getEnvironment().put(name, attr);
		}
		attr.setValue(value);
	}

	/**
	 * Checks to see if there was an exception thrown by the run method.
	 * 
	 * @param job
	 * @throws CoreException
	 *             if the job execution raised and exception
	 */
	private void checkJobForError(ICommandJob job) throws CoreException {
		IStatus status = job.getRunStatus();
		if (status != null && status.getSeverity() == IStatus.ERROR) {
			throw CoreExceptionUtils.newException(status.getMessage(), status.getException());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#control(java.lang.String, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void control(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		if (!isActive()) {
			throw CoreExceptionUtils.newException(Messages.LaunchController_notStarted, null);
		}

		if (RERUN_OPERATION.equals(operation)) {
			ICommandJobStatus status = jobStatusMap.getStatus(jobId);
			if (status != null) {
				status.rerun();
			}
		} else {
			SubMonitor progress = SubMonitor.convert(monitor, 100);
			try {
				pinTable.pin(jobId);
				AttributeType a = getRMVariableMap().get(jobId);
				AttributeType tmp = null;
				if (a == null) {
					tmp = new AttributeType();
					tmp.setVisible(false);
					tmp.setName(jobId);
					getRMVariableMap().put(jobId, tmp);
				}
				worked(progress, 30);
				doControlCommand(jobId, operation);
				if (tmp != null) {
					getRMVariableMap().remove(jobId);
				}
				worked(progress, 40);
				if (TERMINATE_OPERATION.equals(operation)) {
					jobStatusMap.cancel(jobId);
				}
				worked(progress, 30);
			} finally {
				pinTable.release(jobId);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#dispose()
	 */
	public void dispose() {
		// NOP for the moment
	}

	/**
	 * @param jobId
	 *            resource-specific id
	 * @param operation
	 *            terminate, hold, suspend, release, resume.
	 * @throws CoreException
	 *             If the command is not supported
	 */
	private void doControlCommand(String jobId, String operation) throws CoreException {
		CoreException ce = CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + operation, null);

		CommandType job = null;
		if (TERMINATE_OPERATION.equals(operation)) {
			maybeKillInteractive(jobId);
			job = getConfiguration().getControlData().getTerminateJob();
			if (job == null) { // there may not be an external cancel
				return;
			}
		} else if (SUSPEND_OPERATION.equals(operation)) {
			job = getConfiguration().getControlData().getSuspendJob();
			if (job == null) {
				throw ce;
			}
		} else if (RESUME_OPERATION.equals(operation)) {
			job = getConfiguration().getControlData().getResumeJob();
			if (job == null) {
				throw ce;
			}
		} else if (RELEASE_OPERATION.equals(operation)) {
			job = getConfiguration().getControlData().getReleaseJob();
			if (job == null) {
				throw ce;
			}
		} else if (HOLD_OPERATION.equals(operation)) {
			job = getConfiguration().getControlData().getHoldJob();
			if (job == null) {
				throw ce;
			}
		}

		runCommand(jobId, job, CommandJob.JobMode.INTERACTIVE, getEnvironment(), null, ILaunchManager.RUN_MODE);
	}

	/**
	 * Run either interactive or batch job for run or debug modes. ILaunchManager.RUN_MODE and ILaunchManager.DEBUG_MODE are the
	 * corresponding LaunchConfiguration modes; batch/interactive are currently determined by the configuration (the configuration
	 * cannot implement both). This may need to be modified.
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param launch
	 *            launch information for the job
	 * @return job wrapper object
	 * @throws CoreException
	 */
	private ICommandJob doJobSubmitCommand(String uuid, ILaunchConfiguration launchConfig, String launchMode) throws CoreException {
		CommandType command = null;
		CommandJob.JobMode jobMode = CommandJob.JobMode.INTERACTIVE;

		if (ILaunchManager.RUN_MODE.equals(launchMode)) {
			command = getConfiguration().getControlData().getSubmitBatch();
			if (command != null) {
				jobMode = CommandJob.JobMode.BATCH;
			} else {
				command = getConfiguration().getControlData().getSubmitInteractive();
			}
		} else if (ILaunchManager.DEBUG_MODE.equals(launchMode)) {
			command = getConfiguration().getControlData().getSubmitBatchDebug();
			if (command != null) {
				jobMode = CommandJob.JobMode.BATCH;
			} else {
				command = getConfiguration().getControlData().getSubmitInteractiveDebug();
			}
		}

		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError + JAXBControlConstants.SP + uuid
					+ JAXBControlConstants.SP + launchMode, null);
		}

		/*
		 * NOTE: changed this to join, because the waitForId is now part of the run() method of the command itself (05.01.2011)
		 */
		return runCommand(uuid, command, jobMode, getEnvironment(), launchConfig, launchMode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ILaunchController#getAppendEnv()
	 */
	public boolean getAppendEnv() {
		return appendLaunchEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBJobControl#getConfiguration()
	 */
	public ResourceManagerData getConfiguration() {
		return configData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#getConnectionName()
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBJobControl#getControlId()
	 */
	public String getControlId() {
		return fControlId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBJobControl#getEnvironment()
	 */
	public IVariableMap getEnvironment() {
		return getRMVariableMap();
	}

	/**
	 * Get the environment manager configuration that was specified in the launch configuration. If no
	 * launchConfiguration was specified then this CommandJob does not need to use environment management so we can safely return
	 * null.
	 * 
	 * @return environment manager configuration or null if no configuration can be found
	 */
	private IEnvManagerConfig getEnvManagerConfig(ILaunchConfiguration configuration) {
		try {
			String emsConfigAttr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EMS_CONFIG, (String) null);
			if (emsConfigAttr != null) {
				final EnvManagerConfigString config = new EnvManagerConfigString(emsConfigAttr);
				if (config.isEnvMgmtEnabled()) {
					return config;
				}
			}
		} catch (CoreException e) {
			// Ignore
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#getJobStatus(java.lang.String, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IJobStatus getJobStatus(String jobId, boolean force, IProgressMonitor monitor) throws CoreException {
		if (!isActive()) {
			throw CoreExceptionUtils.newException(Messages.LaunchController_notStarted, null);
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);
		ICommandJobStatus status = jobStatusMap.getStatus(jobId);

		/*
		 * First check to see when the last call was made; throttle requests coming in intervals less than
		 * ICommandJobStatus.UPDATE_REQUEST_INTERVAL
		 */

		if (status != null) {
			if (IJobStatus.COMPLETED.equals(status.getState())) {

				/*
				 * leave the status in the map in case there are further calls (regarding remote file state); it will be pruned
				 * by the daemon; note that a COMPLETED state can correspond to a COMPLETED, CANCELED, FAILED or
				 * JOB_OUTERR_READY detail
				 */
				status = jobStatusMap.terminated(jobId, progress.newChild(50));
				if (status != null && status.stateChanged()) {
					JobManager.getInstance().fireJobChanged(status);
				}
				return status;
			}

			if (!force) {
				long now = System.currentTimeMillis();
				long lapse = now - status.getLastUpdateRequest();
				if (lapse < ICommandJobStatus.UPDATE_REQUEST_INTERVAL) {
					return status;
				}
				status.setUpdateRequestTime(now);
			}
		}

		String state = status == null ? IJobStatus.UNDETERMINED : status.getStateDetail();

		ICommandJob job = null;
		try {
			pinTable.pin(jobId);
			AttributeType a = getRMVariableMap().get(jobId);

			CommandType cmd = getConfiguration().getControlData().getGetJobStatus();
			if (cmd != null && isActive() && !progress.isCanceled()) {
				AttributeType tmp = null;
				if (a == null) {
					tmp = new AttributeType();
					tmp.setVisible(false);
					tmp.setName(jobId);
					getRMVariableMap().put(jobId, tmp);
				}
				job = runCommand(jobId, cmd, CommandJob.JobMode.STATUS, getEnvironment(), null, ILaunchManager.RUN_MODE);
				if (tmp != null) {
					a = getRMVariableMap().remove(jobId);
				}
			}

			if (a != null) {
				state = String.valueOf(a.getValue());
			}
		} finally {
			pinTable.release(jobId);
		}

		if (status == null) {
			status = new CommandJobStatus(jobId, state, null, this, getEnvironment(), ILaunchManager.RUN_MODE);
			status.setOwner(getRMVariableMap().getString(JAXBControlConstants.CONTROL_USER_NAME));
			jobStatusMap.addJobStatus(jobId, status);
		} else {
			status.setState(state);
		}

		/*
		 * as specified by the contract
		 */
		if ((job != null && isCanceled(job)) || progress.isCanceled()) {
			status.setState(IJobStatus.UNDETERMINED);
			JobManager.getInstance().fireJobChanged(status);
			return status;
		}

		if (IJobStatus.COMPLETED.equals(state)) {
			/*
			 * leave the status in the map in case there are further calls (regarding remote file state); it will be pruned by
			 * the daemon
			 */
			jobStatusMap.terminated(jobId, progress.newChild(50));
		}

		if (status.stateChanged()) {
			JobManager.getInstance().fireJobChanged(status);
		}

		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#getJobStatus(java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IJobStatus getJobStatus(String jobId, IProgressMonitor monitor) throws CoreException {
		return getJobStatus(jobId, false, monitor);
	}

	/**
	 * @return any environment variables passed in through the LaunchConfiguration
	 */
	public Map<String, String> getLaunchEnv() {
		return launchEnv;
	}

	private IRemoteConnection getRemoteConnection(IProgressMonitor monitor) {
		final IRemoteServices rsrv = getRemoteServices(monitor);
		if (rsrv == null) {
			return null;
		} else {
			IRemoteConnectionManager connMgr = rsrv.getConnectionManager();
			if (connMgr == null) {
				return null;
			} else {
				return connMgr.getConnection(connectionName);
			}
		}
	}

	private IRemoteServices getRemoteServices(IProgressMonitor monitor) {
		return RemoteServices.getRemoteServices(servicesId, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#getRemoteServicesId()
	 */
	public String getRemoteServicesId() {
		return servicesId;
	}

	/**
	 * Get the variable map. Returns an initialized map if one doesn't already exist.
	 * 
	 * @return initialized variable map
	 */
	private RMVariableMap getRMVariableMap() {
		if (rmVarMap == null) {
			rmVarMap = new RMVariableMap();
		}
		if (!rmVarMap.isInitialized()) {
			JAXBInitializationUtils.initializeMap(getConfiguration(), rmVarMap);
		}
		return rmVarMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#getStatusMap()
	 */
	public ICommandJobStatusMap getStatusMap() {
		return jobStatusMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ILaunchController#hasRunningJobs()
	 */
	public boolean hasRunningJobs() {
		return jobStatusMap != null && !jobStatusMap.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#initialize()
	 */
	public void initialize() throws CoreException {
		realizeRMDataFromXML();
		if (servicesId == null && connectionName == null) {
			/*
			 * Set connection information from the site configuration. This may get overidden by the launch configuration
			 * later
			 */
			SiteType site = getConfiguration().getSiteData();
			if (site != null) {
				servicesId = site.getRemoteServices();
				if (servicesId == null) {
					servicesId = IRemotePreferenceConstants.REMOTE_TOOLS_REMOTE_SERVICES_ID;
				}
				connectionName = site.getConnectionName();
			}
		}
		if (servicesId == null || connectionName == null) {
			throw CoreExceptionUtils.newException(Messages.LaunchController_missingServicesOrConnectionName);
		}
		fControlId = LaunchControllerManager.generateControlId(servicesId, connectionName, getConfiguration().getName());
		isInitialized = true;
	}

	/**
	 * @return whether the state of the resource manager is stopped or not.
	 */
	private boolean isActive() {
		return isActive;
	}

	private boolean isCanceled(ICommandJob job) {
		IStatus status = job.getRunStatus();
		return (status != null && status.getSeverity() == IStatus.CANCEL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ILaunchController#isInitialized()
	 */
	public boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * Checks for existence of either internally generated script or custom script path. In either case, either replaces contents of
	 * the corresponding managed file object or creates one.
	 * 
	 * @param lists
	 *            the lists of managed files for this submission
	 * @param stagingLocation
	 *            for the script (may be <code>null</null>
	 * @param delete
	 *            whether the script target should be deleted after submission
	 */
	private void maybeAddManagedFileForScript(List<ManagedFilesType> lists, String stagingLocation, boolean delete) {
		ManagedFilesType files = null;
		if (stagingLocation == null) {
			stagingLocation = JAXBControlConstants.ECLIPSESETTINGS;
		}
		for (ManagedFilesType f : lists) {
			if (stagingLocation.equals(f.getFileStagingLocation())) {
				files = f;
				break;
			}
		}

		AttributeType scriptVar = getRMVariableMap().get(JAXBControlConstants.SCRIPT);
		AttributeType scriptPathVar = getRMVariableMap().get(JAXBControlConstants.SCRIPT_PATH);
		if (scriptVar != null || scriptPathVar != null) {
			if (files == null) {
				files = new ManagedFilesType();
				files.setFileStagingLocation(stagingLocation);
				lists.add(files);
			}
			List<ManagedFileType> fileList = files.getFile();
			ManagedFileType scriptFile = null;
			if (!fileList.isEmpty()) {
				for (ManagedFileType f : fileList) {
					if (f.getName().equals(JAXBControlConstants.SCRIPT_FILE)) {
						scriptFile = f;
						break;
					}
				}
			}
			if (scriptFile == null) {
				scriptFile = new ManagedFileType();
				scriptFile.setName(JAXBControlConstants.SCRIPT_FILE);
				fileList.add(scriptFile);
			}
			scriptFile.setResolveContents(false);
			scriptFile.setUniqueIdPrefix(true);
			if (scriptPathVar != null) {
				scriptFile.setPath(String.valueOf(scriptPathVar.getValue()));
				scriptFile.setDeleteSourceAfterUse(false);
			} else {
				scriptFile.setContents(JAXBControlConstants.OPENVRM + JAXBControlConstants.SCRIPT + JAXBControlConstants.PD
						+ JAXBControlConstants.VALUE + JAXBControlConstants.CLOSV);
				scriptFile.setDeleteSourceAfterUse(true);
			}
			scriptFile.setDeleteTargetAfterUse(delete);
		}
	}

	/**
	 * Looks for cleanup flag and removes the remote file if indicated.
	 * 
	 * @param uuid
	 * @param lists
	 * @throws CoreException
	 */
	private void maybeCleanupManagedFiles(String uuid, List<ManagedFilesType> lists) throws CoreException {
		if (lists == null || lists.isEmpty()) {
			return;
		}
		for (ManagedFilesType files : lists) {
			if (files.getFile().isEmpty()) {
				continue;
			}
			ManagedFilesJob job = new ManagedFilesJob(uuid, files, this);
			job.setOperation(Operation.DELETE);
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException ignored) {
				// Ignore
			}
		}
	}

	/**
	 * Serialize script content if necessary. We first check to see if there is a custom script (path).
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param script
	 *            configuration object describing how to construct the script from the environment
	 * @param monitor
	 *            progress monitor
	 * @return whether the script target should be deleted
	 */
	private boolean maybeHandleScript(String uuid, ScriptType script, IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		AttributeType a = getRMVariableMap().get(JAXBControlConstants.SCRIPT_PATH);
		if (a != null && a.getValue() != null) {
			return false;
		}
		if (script == null) {
			return false;
		}
		IRemoteConnection conn = getRemoteConnection(progress.newChild(5));
		if (conn != null) {
			getRMVariableMap().setEnvManagerFromConnection(conn);
			ScriptHandler job = new ScriptHandler(uuid, script, getRMVariableMap(), launchEnv, false);
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException ignored) {
				// Ignore
			}
			return script.isDeleteAfterSubmit();
		}
		return false;
	}

	/**
	 * If job is interactive, kill the process directly rather than issuing a remote command.
	 * 
	 * @param jobId
	 *            either process id or internal identifier.
	 * @return whether job has been canceled
	 */
	private boolean maybeKillInteractive(String jobId) {
		ICommandJobStatus status = jobStatusMap.getStatus(jobId);
		boolean killed = false;
		if (status != null) {
			killed = status.cancel();
		}
		return killed;
	}

	/**
	 * Write content to file if indicated, and stage to host.
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param lists
	 *            the set of managed files for this submission
	 * @return whether the necessary staging completed without error
	 * @throws CoreException
	 */
	private boolean maybeTransferManagedFiles(String uuid, List<ManagedFilesType> lists) throws CoreException {
		if (lists == null || lists.isEmpty()) {
			return true;
		}
		/*
		 * one job to a staging location
		 */
		for (ManagedFilesType files : lists) {
			if (files.getFile().isEmpty()) {
				continue;
			}
			ManagedFilesJob job = new ManagedFilesJob(uuid, files, this);
			job.setOperation(Operation.COPY);
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException ignored) {
				// Ignore
			}
			if (!job.getSuccess()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Some target configurations require helper scripts for launching, debugging, etc. Make sure these helper scripts are available
	 * on the target system if no monitoring is enabled.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	private void maybeUpdateServer(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		IRemoteConnection conn = fRemoteServicesDelegate.getRemoteConnection();
		LMLDAServer server = (LMLDAServer) RemoteServerManager.getServer(LMLDAServer.SERVER_ID, conn);
		server.setWorkDir(new Path(conn.getWorkingDirectory()).append(JAXBControlConstants.ECLIPSESETTINGS).toString());
		try {
			server.updateServer(progress.newChild(100));
		} catch (IOException e) {
			throw CoreExceptionUtils.newException(e.getMessage(), e);
		}
	}

	/**
	 * Unmarshals the XML into the JAXB data tree.
	 * 
	 * @throws unmarshaling
	 *             or URL exceptions
	 */
	private void realizeRMDataFromXML() throws CoreException {
		if (configURL != null) {
			try {
				String configXML = JAXBInitializationUtils.getRMConfigurationXML(new URL(configURL));
				configData = JAXBInitializationUtils.initializeRMData(configXML);
			} catch (Exception e) {
				throw CoreExceptionUtils.newException(e.getLocalizedMessage(), e.getCause());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ILaunchController#runCommand(org.eclipse.ptp.rm.jaxb.core.data.CommandType,
	 * org.eclipse.ptp.rm.jaxb.core.IVariableMap)
	 */
	public void runCommand(CommandType command, IVariableMap attributes) throws CoreException {
		runCommand(null, command, CommandJob.JobMode.INTERACTIVE, attributes, null, ILaunchManager.RUN_MODE);
	}

	/**
	 * Create command job, and schedule. Used for job-specific commands directly.
	 * 
	 * @param uuid
	 *            Temporary internal id for as yet unsubmitted job. If null, the command will be assumed to be interactive.
	 * @param command
	 *            Configuration object containing the command arguments and tokenizers.
	 * @param jobMode
	 *            Whether batch, interactive, or a status job.
	 * @param map
	 *            Attribute map to use when running the command. Allows an alternate map to be used if required.
	 * @param launchConfig
	 *            Launch configuration. This is only required if the launch environment needs to be passed to the remote command,
	 *            otherwise null can be used.
	 * @param launchMode
	 *            Launch mode (see {@link ILaunchMode}). Will be returned in the job status {@link IJobStatus#getLaunchMode()}.
	 * @return the runnable job object
	 * @throws CoreException
	 */
	private ICommandJob runCommand(String uuid, CommandType command, CommandJob.JobMode jobMode, IVariableMap map,
			ILaunchConfiguration launchConfig, String launchMode) throws CoreException {
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError, null);
		}

		ICommandJob job = new CommandJob(uuid, command, jobMode, this, map, launchConfig, launchMode);
		((Job) job).setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
			// Ignore
		}
		if (!command.isIgnoreExitStatus()) {
			checkJobForError(job);
		}
		return job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ILaunchController#runCommand(java.lang.String, java.lang.String,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void runCommand(String name, String resetValue, ILaunchConfiguration configuration) throws CoreException {
		if (!isActive()) {
			throw CoreExceptionUtils.newException(Messages.LaunchController_notStarted, null);
		}

		if (configuration != null) {
			updateAttributeValues(configuration, ILaunchManager.RUN_MODE, null);
		}

		AttributeType changedValue = null;

		if (resetValue != null) {
			changedValue = getRMVariableMap().get(resetValue);
			changedValue.setValue(null);
		}

		CommandType command = null;

		for (CommandType cmd : getConfiguration().getControlData().getButtonAction()) {
			if (cmd.getName().equals(name)) {
				command = cmd;
				break;
			}
		}

		if (command == null) {
			for (CommandType cmd : getConfiguration().getControlData().getStartUpCommand()) {
				if (cmd.getName().equals(name)) {
					command = cmd;
					break;
				}
			}
		}

		if (command == null) {
			for (CommandType cmd : getConfiguration().getControlData().getShutDownCommand()) {
				if (cmd.getName().equals(name)) {
					command = cmd;
					break;
				}
			}
		}

		if (command != null) {
			runCommand(null, command, CommandJob.JobMode.INTERACTIVE, getEnvironment(), configuration, ILaunchManager.RUN_MODE);
		}
	}

	/**
	 * Run command sequence. Invoked by startup or shutdown commands. Delegates to
	 * {@link #runCommand(String, CommandType, JobMode, ILaunchConfiguration, String, boolean)}. If a job in the sequence fails, the
	 * subsequent commands will not run.
	 * 
	 * @param cmds
	 *            configuration objects containing the command arguments and tokenizers
	 * @throws CoreException
	 */
	private void runCommands(List<CommandType> cmds) throws CoreException {
		for (CommandType cmd : cmds) {
			runCommand(null, cmd, CommandJob.JobMode.INTERACTIVE, getEnvironment(), null, ILaunchManager.RUN_MODE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ILaunchController#setConnectionName(java.lang.String)
	 */
	public void setConnectionName(String connName) {
		connectionName = connName;
	}

	/**
	 * Add connection properties to the attribute map.
	 * 
	 * @param conn
	 */
	private void setConnectionPropertyAttributes(IRemoteConnection conn) {
		String property = conn.getProperty(IRemoteConnection.OS_ARCH_PROPERTY);
		if (property != null) {
			addAttribute(IRemoteConnection.OS_ARCH_PROPERTY, property);
		}
		property = conn.getProperty(IRemoteConnection.OS_NAME_PROPERTY);
		if (property != null) {
			addAttribute(IRemoteConnection.OS_NAME_PROPERTY, property);
		}
		property = conn.getProperty(IRemoteConnection.OS_VERSION_PROPERTY);
		if (property != null) {
			addAttribute(IRemoteConnection.OS_VERSION_PROPERTY, property);
		}
	}

	/**
	 * Create attributes from constants that are fixed while the controller is initialized.
	 * 
	 * @throws CoreException
	 */
	private void setFixedConfigurationProperties(IRemoteConnection rc) throws CoreException {
		if (rc != null) {
			getRMVariableMap().maybeAddAttribute(JAXBControlConstants.CONTROL_USER_VAR, rc.getUsername(), false);
			getRMVariableMap().maybeAddAttribute(JAXBControlConstants.CONTROL_ADDRESS_VAR, rc.getAddress(), false);
			getRMVariableMap().maybeAddAttribute(JAXBControlConstants.CONTROL_WORKING_DIR_VAR, rc.getWorkingDirectory(), false);
			getRMVariableMap().maybeAddAttribute(JAXBControlConstants.DIRECTORY, rc.getWorkingDirectory(), false);
			getRMVariableMap().maybeAddAttribute(JAXBControlConstants.PTP_DIRECTORY,
					new Path(rc.getWorkingDirectory()).append(JAXBControlConstants.ECLIPSESETTINGS).toString(), false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ILaunchController#setRemoteServicesId(java.lang.String)
	 */
	public void setRemoteServicesId(String id) {
		servicesId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#setRMConfigurationURL(java.net.URL)
	 */
	public void setRMConfigurationURL(URL url) {
		if (url != null) {
			configURL = url.toExternalForm();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#start(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void start(IProgressMonitor monitor) throws CoreException {
		if (!isActive) {
			SubMonitor progress = SubMonitor.convert(monitor, 60);
			/*
			 * Support legacy RM API
			 */
			if (!isInitialized) {
				initialize();
			}

			fRemoteServicesDelegate = RemoteServicesDelegate.getDelegate(servicesId, connectionName, progress.newChild(50));
			IRemoteConnection conn = fRemoteServicesDelegate.getRemoteConnection();
			if (conn != null) {
				checkConnection(conn, progress);
				conn.addConnectionChangeListener(connectionListener);
			}

			setFixedConfigurationProperties(conn);
			setConnectionPropertyAttributes(conn);

			appendLaunchEnv = true;

			/*
			 * start daemon
			 */
			jobStatusMap = JobStatusMap.getInstance(this);
			jobStatusMap.initialize();

			/*
			 * Run the start up commands, if any
			 */
			List<CommandType> onStartUp = getConfiguration().getControlData().getStartUpCommand();
			runCommands(onStartUp);

			isActive = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.IJAXBLaunchControl#stop()
	 */
	public void stop() throws CoreException {
		if (isActive) {
			List<CommandType> onShutDown = getConfiguration().getControlData().getShutDownCommand();
			runCommands(onShutDown);

			jobStatusMap.dispose();

			if (rmVarMap != null) {
				rmVarMap.clear();
			}

			IRemoteConnection conn = fRemoteServicesDelegate.getRemoteConnection();
			if (conn != null) {
				conn.removeConnectionChangeListener(connectionListener);
			}

			isActive = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobControl#submitJob(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String submitJob(ILaunchConfiguration launchConfig, String launchMode, IProgressMonitor monitor) throws CoreException {
		/*
		 * give submission a unique id which will in most cases be replaced by the resource-generated id for the job/process
		 */
		String uuid = UUID.randomUUID().toString();

		if (!isActive()) {
			throw CoreExceptionUtils.newException(Messages.LaunchController_notStarted, null);
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);

		/*
		 * Create attribute representing job ID. This will be updated with a name (the job ID returned from the scheduler) and a
		 * value (the job status) by the tokenizer for the job submission command.
		 */
		AttributeType a = new AttributeType();
		a.setVisible(false);
		getRMVariableMap().put(uuid, a);

		/*
		 * Overwrite attribute values based on user choices. Note that the launch can also modify attributes.
		 */
		updateAttributeValues(launchConfig, launchMode, progress.newChild(5));

		/*
		 * process script
		 */
		ScriptType script = getConfiguration().getControlData().getScript();
		boolean delScript = maybeHandleScript(uuid, script, progress.newChild(5));
		worked(progress, 20);

		List<ManagedFilesType> files = getConfiguration().getControlData().getManagedFiles();

		/*
		 * if the script is to be staged, a managed file pointing to either its content (${ptp_rm:script#value}), or to its path
		 * (SCRIPT_PATH) must exist.
		 */
		if (script != null) {
			maybeAddManagedFileForScript(files, script.getFileStagingLocation(), delScript);
		}
		worked(progress, 5);

		if (!maybeTransferManagedFiles(uuid, files)) {
			throw CoreExceptionUtils.newException(Messages.CannotCompleteSubmitFailedStaging, null);
		}
		worked(progress, 20);

		maybeUpdateServer(progress.newChild(10));

		ICommandJob job = null;

		try {
			job = doJobSubmitCommand(uuid, launchConfig, launchMode);

			if (isCanceled(job)) {
				throw CoreExceptionUtils.newException(Messages.OperationWasCancelled, null);
			}
			worked(progress, 40);
		} finally {
			/*
			 * if the staged files can be removed, delete them
			 */
			maybeCleanupManagedFiles(uuid, files);
			worked(progress, 5);
		}

		ICommandJobStatus status = job.getJobStatus();

		/*
		 * property containing actual jobId as name was set in the wait call; we may need the new jobId mapping momentarily to
		 * resolve proxy-specific info
		 */
		getRMVariableMap().remove(uuid);

		/*
		 * initialize the job status while the id property is live
		 */
		jobStatusMap.addJobStatus(status.getJobId(), status);
		worked(progress, 5);

		/*
		 * to ensure the most recent script is used at the next call
		 */
		getRMVariableMap().remove(JAXBControlConstants.SCRIPT_PATH);
		getRMVariableMap().remove(JAXBControlConstants.SCRIPT);
		return status.getJobId();
	}

	/**
	 * Transfers the values from the configuration to the attribute map. This needs to be called whenever a new launch configuration
	 * is being used in order to switch the attributes to use the new values.
	 * 
	 * @param configuration
	 *            passed in from Launch Tab when the "run" command is chosen.
	 * @throws CoreException
	 */
	private void updateAttributeValues(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		/*
		 * Update attributes from launch configuration
		 */
		Map<String, Object> lcattr = RMVariableMap.getValidAttributes(configuration);
		for (String key : lcattr.keySet()) {
			Object value = lcattr.get(key);
			AttributeType target = getRMVariableMap().get(key.toString());
			if (target != null) {
				target.setValue(value);
			}
		}

		/*
		 * The non-selected variables have been excluded from the valid attributes of the configuration; but we need to null out the
		 * superset values here that are undefined.
		 */
		for (String key : getRMVariableMap().getAttributes().keySet()) {
			if (!lcattr.containsKey(key)) {
				AttributeType target = getRMVariableMap().get(key.toString());
				if (target.isVisible()) {
					target.setValue(null);
				}
			}
		}

		/*
		 * Add launch mode attribute or update it if present
		 */
		getRMVariableMap().maybeAddAttribute(JAXBControlConstants.LAUNCH_MODE, mode, false);

		/*
		 * make sure these fixed properties are included
		 */
		getRMVariableMap().overwrite(JAXBControlConstants.SCRIPT_PATH, JAXBControlConstants.SCRIPT_PATH, lcattr);
		getRMVariableMap().overwrite(JAXBControlConstants.EXEC_PATH, JAXBControlConstants.EXEC_PATH, lcattr);
		getRMVariableMap().overwrite(JAXBControlConstants.EXEC_DIR, JAXBControlConstants.EXEC_DIR, lcattr);
		getRMVariableMap().overwrite(JAXBControlConstants.PROG_ARGS, JAXBControlConstants.PROG_ARGS, lcattr);
		getRMVariableMap().overwrite(JAXBControlConstants.DEBUGGER_EXEC_PATH, JAXBControlConstants.DEBUGGER_EXEC_PATH, lcattr);
		getRMVariableMap().overwrite(JAXBControlConstants.DEBUGGER_ID, JAXBControlConstants.DEBUGGER_ID, lcattr);
		getRMVariableMap().overwrite(JAXBControlConstants.DEBUGGER_LAUNCHER, JAXBControlConstants.DEBUGGER_LAUNCHER, lcattr);

		/*
		 * update the dynamic attributes
		 */
		String attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ARGS, (String) null);
		if (attr != null) {
			AttributeType a = getEnvironment().get(JAXBControlConstants.DEBUGGER_ARGS);
			if (a == null) {
				a = new AttributeType();
				a.setName(JAXBControlConstants.DEBUGGER_ARGS);
				getEnvironment().put(JAXBControlConstants.DEBUGGER_ARGS, a);
			}
			a.setValue(attr);
		}

		attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, (String) null);
		if (attr == null) {
			AttributeType a = getEnvironment().get(JAXBControlConstants.EXEC_DIR);
			if (a != null) {
				attr = (String) a.getValue();
			}
		}
		if (attr != null) {
			AttributeType a = getEnvironment().get(JAXBControlConstants.DIRECTORY);
			if (a == null) {
				a = new AttributeType();
				a.setName(JAXBControlConstants.DIRECTORY);
				getEnvironment().put(JAXBControlConstants.DIRECTORY, a);
			}
			a.setValue(attr);
		}

		IEnvManagerConfig envMgrConfig = getEnvManagerConfig(configuration);
		if (envMgrConfig != null) {
			IEnvManager envManager = EnvManagerRegistry.getEnvManager(monitor, fRemoteServicesDelegate.getRemoteConnection());
			if (envManager != null) {
				String emsStr = envManager.getBashConcatenation("\n", false, envMgrConfig, null); //$NON-NLS-1$
				AttributeType a = getEnvironment().get(JAXBControlConstants.EMS_ATTR);
				if (a == null) {
					a = new AttributeType();
					a.setName(JAXBControlConstants.EMS_ATTR);
					getEnvironment().put(JAXBControlConstants.EMS_ATTR, a);
				}
				a.setValue(emsStr);
			}
		}
	}

	/**
	 * Encapsulates null check.
	 * 
	 * @param monitor
	 * @param units
	 */
	private void worked(IProgressMonitor monitor, int units) {
		if (monitor != null) {
			if (units == 0) {
				monitor.done();
			} else {
				monitor.worked(units);
			}
		}
	}
}
