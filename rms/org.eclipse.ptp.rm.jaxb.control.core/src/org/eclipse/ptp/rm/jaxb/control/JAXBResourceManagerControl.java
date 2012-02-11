/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJob;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatusMap;
import org.eclipse.ptp.rm.jaxb.control.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.internal.runnable.JobStatusMap;
import org.eclipse.ptp.rm.jaxb.control.internal.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.control.internal.runnable.ManagedFilesJob.Operation;
import org.eclipse.ptp.rm.jaxb.control.internal.runnable.command.CommandJob;
import org.eclipse.ptp.rm.jaxb.control.internal.runnable.command.CommandJobStatus;
import org.eclipse.ptp.rm.jaxb.control.internal.utils.JobIdPinTable;
import org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.control.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFileType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFilesType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerControl;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * The part of the JAXB resource manager responsible for handling job submission, termination, suspension and resumption. Also
 * provides on-demand job status checking. <br>
 * <br>
 * The state maintained by the control is volatile (in-memory only). The control is responsible for handing off to the caller status
 * objects containing job state, as well as means of acessing the process (if interactive) and the standard out and error streams.
 * When the job completes, these are eliminated from its internal map.<br>
 * <br>
 * The logic of this manager is generic; the specific commands used, files staged, and script constructed (if any) are all
 * configured via the resource manager XML. <br>
 * <br>
 * Currently, it is the control which handles updating the monitor component.
 * 
 * @author arossi
 * 
 */
public final class JAXBResourceManagerControl extends AbstractResourceManagerControl implements IJAXBResourceManagerControl {

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
					getResourceManager().stop();
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
	public static void checkConnection(IRemoteConnection connection, SubMonitor progress) throws RemoteConnectionException {
		if (connection != null) {
			if (!connection.isOpen()) {
				connection.open(progress.newChild(25));
				if (!connection.isOpen()) {
					throw new RemoteConnectionException(Messages.RemoteConnectionError + connection.getAddress());
				}
			}
		} else {
			new RemoteConnectionException(Messages.RemoteConnectionError + connection);
		}
	}

	/**
	 * Checks to see if there was an exception thrown by the run method.
	 * 
	 * @param job
	 * @throws CoreException
	 *             if the job execution raised and exception
	 */
	private static void checkJobForError(ICommandJob job) throws CoreException {
		IStatus status = job.getRunStatus();
		if (status != null && status.getSeverity() == IStatus.ERROR) {
			Throwable t = status.getException();
			if (t instanceof CoreException) {
				throw (CoreException) t;
			} else {
				throw CoreExceptionUtils.newException(status.getMessage(), t);
			}
		}
	}

	private final IJAXBResourceManagerConfiguration config;

	private final ConnectionChangeListener connectionListener;
	private Map<String, String> launchEnv;
	private ICommandJob interactiveJob;
	private ICommandJobStatusMap jobStatusMap;
	private JobIdPinTable pinTable;
	private RMVariableMap rmVarMap;
	private ControlType controlData;
	private String servicesId;

	private String connectionName;

	private RemoteServicesDelegate remoteServicesDelegate;

	private boolean appendLaunchEnv;

	/**
	 * @param jaxbServiceProvider
	 *            the configuration object containing resource manager specifics
	 */
	public JAXBResourceManagerControl(AbstractResourceManagerConfiguration jaxbServiceProvider) {
		super(jaxbServiceProvider);
		config = (IJAXBResourceManagerConfiguration) jaxbServiceProvider;
		connectionListener = new ConnectionChangeListener();
	}

	/**
	 * @return whether to append (true) the env passed in through the LaunchConfiguration, or replace the current env with it.
	 */
	public boolean getAppendEnv() {
		return appendLaunchEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl#getEnvironment()
	 */
	public IVariableMap getEnvironment() {
		return rmVarMap;
	}

	/**
	 * @return open remote process
	 */
	public synchronized ICommandJob getInteractiveJob() {
		return interactiveJob;
	}

	/**
	 * @return any environment variables passed in through the LaunchConfiguration
	 */
	public Map<String, String> getLaunchEnv() {
		return launchEnv;
	}

	/**
	 * Reinitializes when the connection info has been changed on a cached resource manager.
	 * 
	 * @param monitor
	 * @return wrapper object for remote services, connections and file managers
	 * @throws CoreException
	 */
	public RemoteServicesDelegate getRemoteServicesDelegate(IProgressMonitor monitor) throws CoreException {
		String cname = config.getConnectionName();
		String sid = config.getRemoteServicesId();
		if (remoteServicesDelegate == null || !cname.equals(connectionName) || !sid.equals(servicesId)) {
			connectionName = cname;
			servicesId = sid;
			remoteServicesDelegate = new RemoteServicesDelegate(servicesId, connectionName);
			remoteServicesDelegate.initialize(monitor);
		}
		/*
		 * Bug 370775 - Attempt to open the connection before using the delegate as the connection can be closed independently of
		 * the resource manager.
		 */
		IRemoteConnection conn = remoteServicesDelegate.getRemoteConnection();
		if (!conn.isOpen()) {
			try {
				conn.open(monitor);
			} catch (RemoteConnectionException e) {
				// Just use the closed connection
			}
		}
		return remoteServicesDelegate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl#getState()
	 */
	public String getState() {
		return getResourceManager().getState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl#getStatusMap()
	 */
	public ICommandJobStatusMap getStatusMap() {
		return jobStatusMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl#jobStateChanged (java.lang.String)
	 */
	public void jobStateChanged(String jobId, IJobStatus status) {
		/*
		 * Update any debug models associated with this job ID
		 */
		IPResourceManager prm = (IPResourceManager) getResourceManager().getAdapter(IPResourceManager.class);
		if (prm != null) {
			IPJob job = prm.getJobById(jobId);
			if (job != null) {
				if (status.getState().equals(IJobStatus.COMPLETED)) {
					job.getAttribute(JobAttributes.getStateAttributeDefinition()).setValue(JobAttributes.State.COMPLETED);
				}
			}
		}
		((IJAXBResourceManager) getResourceManager()).fireJobChanged(jobId);
		getResourceManager().updateJob(jobId, status);
	}

	/**
	 * First clears the value, if indicated, then checks for the command definition, and calls
	 * {@link #runCommand(String, CommandType, org.eclipse.ptp.rm.jaxb.control.internal.runnable.command.CommandJob.JobMode, boolean)}
	 * .
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl#runActionCommand(java.lang.String)
	 */
	public Object runActionCommand(String action, String resetValue, ILaunchConfiguration configuration) throws CoreException {
		if (!IResourceManager.STARTED_STATE.equals(getResourceManager().getState())) {
			return null;
		}

		updatePropertyValues(configuration, null);

		Object changedValue = null;

		if (resetValue != null) {
			changedValue = rmVarMap.get(resetValue);
			if (changedValue instanceof PropertyType) {
				((PropertyType) changedValue).setValue(null);
			} else if (changedValue instanceof AttributeType) {
				((AttributeType) changedValue).setValue(null);
			}
		}

		CommandType command = null;

		for (CommandType cmd : controlData.getButtonAction()) {
			if (cmd.getName().equals(action)) {
				command = cmd;
				break;
			}
		}

		if (command == null) {
			for (CommandType cmd : controlData.getStartUpCommand()) {
				if (cmd.getName().equals(action)) {
					command = cmd;
					break;
				}
			}
		}

		if (command == null) {
			for (CommandType cmd : controlData.getShutDownCommand()) {
				if (cmd.getName().equals(action)) {
					command = cmd;
					break;
				}
			}
		}

		if (command != null) {
			runCommand(null, command, CommandJob.JobMode.INTERACTIVE, null, ILaunchManager.RUN_MODE, true);
		}

		return changedValue;
	}

	/**
	 * @param interactiveJob
	 *            open remote process
	 */
	public synchronized void setInteractiveJob(ICommandJob interactiveJob) {
		this.interactiveJob = interactiveJob;
	}

	/*
	 * For termination, pause, hold, suspension and resume requests. Resets the environment, generates a uuid property; if the
	 * control request is termination, calls remove on the state map. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doControlJob( java.lang.String, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		if (!resourceManagerIsActive()) {
			return;
		}

		if (jobId == null) {
			synchronized (this) {
				if (interactiveJob != null) {
					interactiveJob.terminate();
					interactiveJob = null;
				}
			}
			return;
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			pinTable.pin(jobId);
			PropertyType p = new PropertyType();
			p.setVisible(false);
			p.setName(jobId);
			rmVarMap.put(jobId, p);
			worked(progress, 30);
			doControlCommand(jobId, operation);
			rmVarMap.remove(jobId);
			worked(progress, 40);
			if (TERMINATE_OPERATION.equals(operation)) {
				IJobStatus canceledStatus = jobStatusMap.cancel(jobId);
				synchronized (this) {
					if (interactiveJob != null) {
						if (canceledStatus == interactiveJob.getJobStatus()) {
							interactiveJob = null;
						}
					}
				}
			}
			worked(progress, 30);
		} finally {
			pinTable.release(jobId);
		}
	}

	@Override
	protected void doDispose() {
		// NOP for the moment
	}

	/*
	 * Used by the client to refresh status on demand. (non-Javadoc) Generates a jobId property; if the returned state is RUNNING,
	 * starts the proxy (a rentrant call to a started proxy does nothing); if COMPLETED, the status is removed from the map.
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doGetJobStatus (java.lang.String)
	 */
	@Override
	protected IJobStatus doGetJobStatus(String jobId, boolean force, IProgressMonitor monitor) throws CoreException {
		try {
			ICommandJobStatus status = jobStatusMap.getStatus(jobId);

			/*
			 * First check to see when the last call was made; throttle requests coming in intervals less than
			 * ICommandJobStatus.UPDATE_REQUEST_INTERVAL
			 */
			SubMonitor progress = SubMonitor.convert(monitor, 100);

			if (status != null) {
				if (IJobStatus.COMPLETED.equals(status.getState())) {

					/*
					 * leave the status in the map in case there are further calls (regarding remote file state); it will be pruned
					 * by the daemon; note that a COMPLETED state can correspond to a COMPLETED, CANCELED, FAILED or
					 * JOB_OUTERR_READY detail
					 */
					status = jobStatusMap.terminated(jobId, progress.newChild(50));
					if (status != null && status.stateChanged()) {
						jobStateChanged(jobId, status);
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

			try {
				PropertyType p = (PropertyType) rmVarMap.get(jobId);

				CommandType job = controlData.getGetJobStatus();
				if (job != null && resourceManagerIsActive() && !progress.isCanceled()) {
					pinTable.pin(jobId);
					p = new PropertyType();
					p.setVisible(false);
					p.setName(jobId);
					rmVarMap.put(jobId, p);
					runCommand(jobId, job, CommandJob.JobMode.STATUS, null, ILaunchManager.RUN_MODE, true);
					p = (PropertyType) rmVarMap.remove(jobId);
				}

				if (p != null) {
					state = String.valueOf(p.getValue());
				}
			} finally {
				pinTable.release(jobId);
			}

			if (status == null) {
				status = new CommandJobStatus(getResourceManager().getUniqueName(), jobId, state, null, this);
				status.setOwner(rmVarMap.getString(JAXBControlConstants.CONTROL_USER_NAME));
				jobStatusMap.addJobStatus(jobId, status);
			} else {
				status.setState(state);
			}

			/*
			 * as specified by the contract
			 */
			if (progress.isCanceled()) {
				status.setState(IJobStatus.UNDETERMINED);
				jobStateChanged(jobId, status);
				return status;
			}

			if (IJobStatus.COMPLETED.equals(state)) {
				/*
				 * leave the status in the map in case there are further calls (regarding remote file state); it will be pruned by
				 * the daemon
				 */
				status = jobStatusMap.terminated(jobId, progress.newChild(50));
			}

			if (status.stateChanged()) {
				jobStateChanged(jobId, status);
			}

			return status;
		} catch (CoreException ce) {
			getResourceManager().setState(IResourceManager.ERROR_STATE);
			throw ce;
		}
	}

	/*
	 * Executes any shutdown commands, then calls halt on the status map thread. NOTE: closing the RM does not terminate the remote
	 * connection it may be using, but merely removes the listeners. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doShutdown()
	 */
	@Override
	protected void doShutdown() throws CoreException {
		doOnShutdown();
		((IJAXBResourceManagerConfiguration) getResourceManager().getConfiguration()).clearReferences(true);
		jobStatusMap.halt();
		RemoteServicesDelegate d = getRemoteServicesDelegate(null);
		IRemoteConnection conn = d.getRemoteConnection();
		if (conn != null) {
			conn.removeConnectionChangeListener(connectionListener);
		}
	}

	/*
	 * Connects and executes any startup commands. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doStartup(org .eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 60);
		try {
			doConnect(progress.newChild(20));
		} catch (CoreException ce) {
			progress.done();
			throw ce;
		} catch (Throwable t) {
			progress.done();
			throw CoreExceptionUtils.newException(t.getMessage(), t);
		}

		try {
			initialize(progress.newChild(30));
			doOnStartUp();
		} catch (CoreException ce) {
			throw ce;
		} catch (Throwable t) {
			throw CoreExceptionUtils.newException(t.getMessage(), t);
		}
	}

	/*
	 * The main command for job submission. (non-Javadoc) A uuid tag is generated for the submission until a resource-specific
	 * identifier is returned (there should be a stream tokenizer associated with the job command in this case which sets the uuid
	 * property).
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doSubmitJob(org .eclipse.debug.core.ILaunchConfiguration,
	 * java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		/*
		 * give submission a unique id which will in most cases be replaced by the resource-generated id for the job/process
		 */
		String uuid = UUID.randomUUID().toString();

		if (!resourceManagerIsActive()) {
			ICommandJobStatus status = new CommandJobStatus(getResourceManager().getUniqueName(), uuid, IJobStatus.UNDETERMINED,
					null, this);
			status.setOwner(rmVarMap.getString(JAXBControlConstants.CONTROL_USER_NAME));
			return status;
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);

		String jobId = null;

		PropertyType p = new PropertyType();
		p.setVisible(false);
		rmVarMap.put(uuid, p);

		/*
		 * Overwrite property/attribute values based on user choices. Note that the launch can also modify attributes.
		 */
		updatePropertyValues(configuration, progress.newChild(5));

		/*
		 * process script
		 */
		ScriptType script = controlData.getScript();
		boolean delScript = maybeHandleScript(uuid, script);
		worked(progress, 20);

		List<ManagedFilesType> files = controlData.getManagedFiles();

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

		ICommandJob job = null;

		try {
			job = doJobSubmitCommand(uuid, configuration, mode);

			IStatus status = job.getRunStatus();
			if (status != null && status.getSeverity() == IStatus.CANCEL) {
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
		if (interactiveJob != null && interactiveJob.getJobStatus() == status) {
			if (interactiveJob != job) {
				return status;
			}
		}

		/*
		 * property containing actual jobId as name was set in the wait call; we may need the new jobId mapping momentarily to
		 * resolve proxy-specific info
		 */
		rmVarMap.remove(uuid);
		jobId = p.getName();

		/*
		 * job was cancelled during waitForId
		 */
		if (jobId == null) {
			status = new CommandJobStatus(getResourceManager().getUniqueName(), uuid, IJobStatus.CANCELED, null, this);
			status.setOwner(rmVarMap.getString(JAXBControlConstants.CONTROL_USER_NAME));
			return status;
		}

		/*
		 * initialize the job status while the id property is live
		 */
		jobStatusMap.addJobStatus(status.getJobId(), status);
		status.setLaunchConfig(configuration);
		worked(progress, 5);

		/*
		 * to ensure the most recent script is used at the next call
		 */
		rmVarMap.remove(JAXBControlConstants.SCRIPT_PATH);
		rmVarMap.remove(JAXBControlConstants.SCRIPT);
		return status;
	}

	/**
	 * If there are special server connections to open, those need to be taken care of by a command to be run on start-up; here we
	 * just check for an open connection and add a change listener to it.
	 * 
	 * @param monitor
	 * @throws RemoteConnectionException
	 * @throws CoreException
	 */
	private void doConnect(IProgressMonitor monitor) throws RemoteConnectionException, CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		RemoteServicesDelegate d = getRemoteServicesDelegate(progress.newChild(50));
		IRemoteConnection conn = d.getRemoteConnection();
		if (conn != null) {
			checkConnection(conn, progress);
			conn.addConnectionChangeListener(connectionListener);
		}
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
			job = controlData.getTerminateJob();
			if (job == null) { // there may not be an external cancel
				return;
			}
		} else if (SUSPEND_OPERATION.equals(operation)) {
			job = controlData.getSuspendJob();
			if (job == null) {
				throw ce;
			}
		} else if (RESUME_OPERATION.equals(operation)) {
			job = controlData.getResumeJob();
			if (job == null) {
				throw ce;
			}
		} else if (RELEASE_OPERATION.equals(operation)) {
			job = controlData.getReleaseJob();
			if (job == null) {
				throw ce;
			}
		} else if (HOLD_OPERATION.equals(operation)) {
			job = controlData.getHoldJob();
			if (job == null) {
				throw ce;
			}
		}

		runCommand(jobId, job, CommandJob.JobMode.INTERACTIVE, null, ILaunchManager.RUN_MODE, true);
	}

	/**
	 * Run either interactive or batch job for run or debug modes. ILaunchManager.RUN_MODE and ILaunchManager.DEBUG_MODE are the
	 * corresponding LaunchConfiguration modes; batch/interactive are currently determined by the configuration (the configuration
	 * cannot implement both). This may need to be modified.
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param mode
	 *            either ILaunchManager.RUN_MODE and ILaunchManager.DEBUG_MODE
	 * @return job wrapper object
	 * @throws CoreException
	 */
	private ICommandJob doJobSubmitCommand(String uuid, ILaunchConfiguration configuration, String mode) throws CoreException {
		CommandType command = null;
		CommandJob.JobMode jobMode = CommandJob.JobMode.INTERACTIVE;

		if (ILaunchManager.RUN_MODE.equals(mode)) {
			command = controlData.getSubmitBatch();
			if (command != null) {
				jobMode = CommandJob.JobMode.BATCH;
			} else {
				command = controlData.getSubmitInteractive();
			}
		} else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			command = controlData.getSubmitBatchDebug();
			if (command != null) {
				jobMode = CommandJob.JobMode.BATCH;
			} else {
				command = controlData.getSubmitInteractiveDebug();
			}
		}

		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError + JAXBControlConstants.SP + uuid
					+ JAXBControlConstants.SP + mode, null);
		}

		/*
		 * NOTE: changed this to join, because the waitForId is now part of the run() method of the command itself (05.01.2011)
		 */
		return runCommand(uuid, command, jobMode, configuration, mode, true);
	}

	/**
	 * Run the shut down commands, if any. Cancel any running interactive process.
	 * 
	 * @throws CoreException
	 */
	private void doOnShutdown() throws CoreException {
		String iJobId = null;
		synchronized (this) {
			if (interactiveJob != null) {
				ICommandJobStatus status = interactiveJob.getJobStatus();
				if (status != null) {
					iJobId = status.getJobId();
				}
			}
		}
		doControlJob(iJobId, TERMINATE_OPERATION, null);

		List<CommandType> onShutDown = controlData.getShutDownCommand();
		runCommands(onShutDown);
	}

	/**
	 * Run the start up commands, if any
	 * 
	 * @throws CoreException
	 */
	private void doOnStartUp() throws CoreException {
		List<CommandType> onStartUp = controlData.getStartUpCommand();
		runCommands(onStartUp);
	}

	/**
	 * Sets the maps and data tree.
	 */
	private void initialize(IProgressMonitor monitor) throws Throwable {
		launchEnv = new TreeMap<String, String>();
		pinTable = new JobIdPinTable();

		/*
		 * Use the base configuration which contains the config file information
		 */
		IJAXBResourceManagerConfiguration base = (IJAXBResourceManagerConfiguration) getResourceManager().getConfiguration();
		base.clearReferences(false);
		rmVarMap = (RMVariableMap) base.getRMVariableMap();
		ResourceManagerData data = base.getResourceManagerData();
		if (data != null) {
			controlData = data.getControlData();
		}
		setFixedConfigurationProperties(monitor);
		launchEnv.clear();
		appendLaunchEnv = true;

		/*
		 * start daemon
		 */
		jobStatusMap = new JobStatusMap(this, getResourceManager());
		((Thread) jobStatusMap).start();
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

		PropertyType scriptVar = (PropertyType) rmVarMap.get(JAXBControlConstants.SCRIPT);
		PropertyType scriptPathVar = (PropertyType) rmVarMap.get(JAXBControlConstants.SCRIPT_PATH);
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
	 * @return whether the script target should be deleted
	 */
	private boolean maybeHandleScript(String uuid, ScriptType script) {
		PropertyType p = (PropertyType) rmVarMap.get(JAXBControlConstants.SCRIPT_PATH);
		if (p != null && p.getValue() != null) {
			return false;
		}
		if (script == null) {
			return false;
		}
		ScriptHandler job = new ScriptHandler(uuid, script, rmVarMap, launchEnv, false);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		return script.isDeleteAfterSubmit();
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
			}
			if (!job.getSuccess()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return whether the state of the resource manager is stopped or not.
	 */
	private boolean resourceManagerIsActive() {
		IResourceManager rm = getResourceManager();
		if (rm != null) {
			String rmState = rm.getState();
			return !rmState.equals(IResourceManager.STOPPED_STATE);
		}
		return false;
	}

	/**
	 * Create command job, and schedule. Used for job-specific commands directly.
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param command
	 *            configuration object containing the command arguments and tokenizers
	 * @param mode
	 *            whether batch, interactive, or a status job
	 * @param join
	 *            whether to launch serially or not
	 * @return the runnable job object
	 * @throws CoreException
	 */
	private ICommandJob runCommand(String uuid, CommandType command, CommandJob.JobMode jobMode,
			ILaunchConfiguration configuration, String launchMode, boolean join) throws CoreException {
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError, null);
		}

		ICommandJob job = new CommandJob(uuid, command, jobMode, (IJAXBResourceManager) getResourceManager(), launchMode);
		((Job) job).setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
		job.schedule();
		if (join) {
			try {
				job.join();
			} catch (InterruptedException ignored) {
			}
		}
		if (!command.isIgnoreExitStatus()) {
			checkJobForError(job);
		}
		return job;
	}

	/**
	 * Run command sequence. Invoked by startup or shutdown commands. Delegates to
	 * {@link #runCommand(String, CommandType, boolean, boolean)}. If a job in the sequence fails, the subsequent commands will not
	 * run.
	 * 
	 * @param cmds
	 *            configuration objects containing the command arguments and tokenizers
	 * @throws CoreException
	 */
	private void runCommands(List<CommandType> cmds) throws CoreException {
		for (CommandType cmd : cmds) {
			runCommand(null, cmd, CommandJob.JobMode.INTERACTIVE, null, ILaunchManager.RUN_MODE, true);
		}
	}

	/**
	 * User name and service address. Set in case the script needs these variables.
	 * 
	 * @throws CoreException
	 */
	private void setFixedConfigurationProperties(IProgressMonitor monitor) throws CoreException {
		IRemoteConnection rc = getRemoteServicesDelegate(monitor).getRemoteConnection();
		if (rc != null) {
			rmVarMap.maybeAddProperty(JAXBControlConstants.CONTROL_USER_VAR, rc.getUsername(), false);
			rmVarMap.maybeAddProperty(JAXBControlConstants.CONTROL_ADDRESS_VAR, rc.getAddress(), false);
			rmVarMap.maybeAddProperty(JAXBControlConstants.CONTROL_WORKING_DIR_VAR, rc.getWorkingDirectory(), false);
		}
	}

	/**
	 * Transfers the values from the configuration to the live map.
	 * 
	 * @param configuration
	 *            passed in from Launch Tab when the "run" command is chosen.
	 * @throws CoreException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updatePropertyValues(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 40);

		setFixedConfigurationProperties(progress.newChild(10));

		Map lcattr = RMVariableMap.getValidAttributes(configuration);
		for (Object key : lcattr.keySet()) {
			Object value = lcattr.get(key);
			Object target = rmVarMap.get(key.toString());
			if (target instanceof PropertyType) {
				PropertyType p = (PropertyType) target;
				p.setValue(value);
			} else if (target instanceof AttributeType) {
				AttributeType ja = (AttributeType) target;
				ja.setValue(value);
			}
		}

		progress.worked(10);

		/*
		 * The non-selected variables have been excluded from the valid attributes of the configuration; but we need to null out the
		 * superset values here that are undefined.
		 */
		for (String key : rmVarMap.getVariables().keySet()) {
			if (!lcattr.containsKey(key)) {
				Object target = rmVarMap.get(key.toString());
				if (target instanceof PropertyType) {
					PropertyType p = (PropertyType) target;
					if (p.isVisible()) {
						p.setValue(null);
					}
				} else if (target instanceof AttributeType) {
					AttributeType ja = (AttributeType) target;
					if (ja.isVisible()) {
						ja.setValue(null);
					}
				}
			}
		}

		progress.worked(10);

		/*
		 * make sure these fixed properties are included
		 */
		rmVarMap.overwrite(JAXBControlConstants.SCRIPT_PATH, JAXBControlConstants.SCRIPT_PATH, lcattr);
		rmVarMap.overwrite(JAXBControlConstants.DIRECTORY, JAXBControlConstants.DIRECTORY, lcattr);
		rmVarMap.overwrite(JAXBControlConstants.EXEC_PATH, JAXBControlConstants.EXEC_PATH, lcattr);
		rmVarMap.overwrite(JAXBControlConstants.EXEC_DIR, JAXBControlConstants.EXEC_DIR, lcattr);
		rmVarMap.overwrite(JAXBControlConstants.PROG_ARGS, JAXBControlConstants.PROG_ARGS, lcattr);
		rmVarMap.overwrite(JAXBControlConstants.DEBUGGER_EXEC_PATH, JAXBControlConstants.DEBUGGER_EXEC_PATH, lcattr);
		rmVarMap.overwrite(JAXBControlConstants.DEBUGGER_ARGS, JAXBControlConstants.DEBUGGER_ARGS, lcattr);
		rmVarMap.overwrite(JAXBControlConstants.PTP_DIRECTORY, JAXBControlConstants.PTP_DIRECTORY, lcattr);

		launchEnv.clear();
		launchEnv.putAll(configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, launchEnv));
		appendLaunchEnv = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, appendLaunchEnv);
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
