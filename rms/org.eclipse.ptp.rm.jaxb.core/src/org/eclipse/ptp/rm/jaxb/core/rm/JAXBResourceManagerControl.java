/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.rm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.core.ICommandJob;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFileType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFilesType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.runnable.JobStatusMap;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJobInput;
import org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.JobIdPinTable;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerControl;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * The part of the JAXB resource manager responsible for handling job
 * submission, termination, suspension and resumption. Also provides on-demand
 * job status checking. <br>
 * <br>
 * The state maintained by the control is volatile (in-memory only). The control
 * is responsible for handing off to the caller status objects containing job
 * state, as well as means of acessing the process (if interactive) and the
 * standard out and error streams. When the job completes, these are eliminated
 * from its internal map.<br>
 * <br>
 * The logic of this manager is generic; the specific commands used, files
 * staged, and script constructed (if any) are all configured via the resource
 * manager XML.
 * 
 * @author arossi
 * 
 */
public final class JAXBResourceManagerControl extends AbstractResourceManagerControl implements IJAXBResourceManagerControl,
		IJAXBNonNLSConstants {

	private final IJAXBResourceManagerConfiguration config;

	private Map<String, String> launchEnv;
	private Map<String, IRemoteProcess> processTable;
	private JobStatusMap jobStatusMap;
	private JobIdPinTable pinTable;
	private RMVariableMap rmVarMap;
	private ControlType controlData;
	private boolean appendLaunchEnv;

	/**
	 * @param jaxbServiceProvider
	 *            the configuration object containing resource manager specifics
	 */
	public JAXBResourceManagerControl(AbstractResourceManagerConfiguration jaxbServiceProvider) {
		super(jaxbServiceProvider);
		config = (IJAXBResourceManagerConfiguration) jaxbServiceProvider;
	}

	/**
	 * @return whether to append (true) the env passed in through the
	 *         LaunchConfiguration, or replace the current env with it.
	 */
	public boolean getAppendEnv() {
		return appendLaunchEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl#getEnvironment()
	 */
	public RMVariableMap getEnvironment() {
		return rmVarMap;
	}

	/**
	 * @return any environment variables passed in through the
	 *         LaunchConfiguration
	 */
	public Map<String, String> getLaunchEnv() {
		return launchEnv;
	}

	/**
	 * @return table of open remote processes
	 */
	public Map<String, IRemoteProcess> getProcessTable() {
		return processTable;
	}

	/**
	 * @return wrapper object for remote services, connections and file managers
	 */
	public RemoteServicesDelegate getRemoteServicesDelegate() {
		return new RemoteServicesDelegate(config.getRemoteServicesId(), config.getConnectionName());
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
	 * For termination, pause, hold, suspension and resume requests. Resets the
	 * environment, generates a uuid property; if the control request is
	 * termination, calls remove on the state map. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doControlJob(
	 * java.lang.String, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		try {
			if (!resourceManagerIsActive()) {
				return;
			}
			pinTable.pin(jobId);
			PropertyType p = new PropertyType();
			p.setVisible(false);
			p.setName(jobId);
			rmVarMap.put(jobId, p);
			doControlCommand(jobId, operation);
			rmVarMap.remove(jobId);
			if (TERMINATE_OPERATION.equals(operation)) {
				jobStatusMap.removeJobStatus(jobId);
			}
		} catch (CoreException ce) {
			getResourceManager().setState(IResourceManager.ERROR_STATE);
			throw ce;
		} finally {
			pinTable.release(jobId);
		}
	}

	@Override
	protected void doDispose() {
		// NOP for the moment
	}

	/*
	 * Used by the client to refresh status on demand. (non-Javadoc) Generates a
	 * jobId property; if the returned state is RUNNING, starts the proxy (a
	 * rentrant call to a started proxy does nothing); if COMPLETED, the status
	 * is removed from the map.
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doGetJobStatus
	 * (java.lang.String)
	 */
	@Override
	protected IJobStatus doGetJobStatus(String jobId) throws CoreException {
		try {
			if (!resourceManagerIsActive()) {
				return new CommandJobStatus(getResourceManager().getUniqueName(), jobId, IJobStatus.UNDETERMINED, this);
			}
			pinTable.pin(jobId);

			/*
			 * first check to see when the last call was made; throttle requests
			 * coming in intervals less than
			 * ICommandJobStatus.UPDATE_REQUEST_INTERVAL
			 */
			ICommandJobStatus status = jobStatusMap.getStatus(jobId);
			if (status != null) {
				if (IJobStatus.COMPLETED.equals(status.getState())) {
					/*
					 * leave the status in the map in case there are further
					 * calls; it will be pruned by the daemon
					 */
					status = jobStatusMap.terminated(jobId);
					if (status.stateChanged()) {
						getBaseResourceManager().fireJobChanged(jobId);
					}
					return status;
				}

				long now = System.currentTimeMillis();
				long lapse = now - status.getLastUpdateRequest();
				if (lapse < ICommandJobStatus.UPDATE_REQUEST_INTERVAL) {
					return status;
				}
				status.setUpdateRequestTime(now);
			}

			PropertyType p = new PropertyType();
			p.setVisible(false);
			p.setName(jobId);
			rmVarMap.put(jobId, p);

			CommandType job = controlData.getGetJobStatus();
			if (job == null) {
				throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + JOBSTATUS, null);
			}

			runCommand(jobId, job, false, true);

			p = (PropertyType) rmVarMap.remove(jobId);

			String state = IJobStatus.UNDETERMINED;
			if (p != null) {
				state = (String) p.getValue();
			}

			if (status == null) {
				status = new CommandJobStatus(getResourceManager().getUniqueName(), jobId, state, this);
				jobStatusMap.addJobStatus(jobId, status);
			} else {
				status.setState(state);
			}

			if (IJobStatus.COMPLETED.equals(state)) {
				/*
				 * leave the status in the map in case there are further calls;
				 * it will be pruned by the daemon
				 */
				status = jobStatusMap.terminated(jobId);
			}

			if (status.stateChanged()) {
				getBaseResourceManager().fireJobChanged(jobId);
			}

			// XXX eliminate when monitoring is in place
			System.out.println(Messages.RefreshedJobStatusMessage + jobId + CM + SP + status.getState());
			return status;
		} catch (CoreException ce) {
			getResourceManager().setState(IResourceManager.ERROR_STATE);
			throw ce;
		} finally {
			pinTable.release(jobId);
		}
	}

	/*
	 * Resets the env and executes any shutdown commands, then disconnects.
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doShutdown()
	 */
	@Override
	protected void doShutdown() throws CoreException {
		try {
			doOnShutdown();
			doDisconnect();
			((IJAXBResourceManagerConfiguration) getResourceManager().getConfiguration()).clearReferences();
			jobStatusMap.halt();
		} catch (CoreException ce) {
			getResourceManager().setState(IResourceManager.ERROR_STATE);
			throw ce;
		}
		getResourceManager().setState(IResourceManager.STOPPED_STATE);
	}

	/*
	 * Connects, resets the env and executes any startup commands. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doStartup(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		initialize();
		getResourceManager().setState(IResourceManager.STARTING_STATE);
		try {
			try {
				doConnect(monitor);
			} catch (RemoteConnectionException t) {
				throw CoreExceptionUtils.newException(t.getMessage(), t);
			}
			doOnStartUp(monitor);
		} catch (CoreException ce) {
			getResourceManager().setState(IResourceManager.ERROR_STATE);
			throw ce;
		}
		getResourceManager().setState(IResourceManager.STARTED_STATE);
	}

	/*
	 * The main command for job submission. (non-Javadoc) The environment is
	 * reset on each call; a uuid tag is generated for the submission until a
	 * resource-specific identifier is returned (there should be a stream
	 * tokenizer associated with the job command in this case which sets the
	 * uuid property).
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doSubmitJob(org
	 * .eclipse.debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		if (!resourceManagerIsActive()) {
			return new CommandJobStatus(getResourceManager().getUniqueName(), UUID.randomUUID().toString(),
					IJobStatus.UNDETERMINED, this);
		}

		/*
		 * give submission a unique id which will in most cases be replaced by
		 * the resource-generated id for the job/process
		 */
		String uuid = UUID.randomUUID().toString();
		String jobId = null;
		try {
			pinTable.pin(uuid);
			PropertyType p = new PropertyType();
			p.setVisible(false);
			rmVarMap.put(uuid, p);

			/*
			 * overwrite property/attribute values based on user choices
			 */
			updatePropertyValuesFromTab(configuration);

			/*
			 * create the script if necessary; adds the contents to env as
			 * "${rm:script}". If a custom script has been selected for use, the
			 * SCRIPT_PATH property will have been passed in with the launch
			 * configuration; if so, the following returns immediately.
			 */
			maybeHandleScript(uuid, controlData.getScript());

			ManagedFilesType files = controlData.getManagedFiles();

			/*
			 * if the script is to be staged, a managed file pointing to either
			 * its as its content (${rm:script#value}), or to its path
			 * (SCRIPT_PATH) must exist.
			 */
			files = maybeAddManagedFileForScript(files);

			if (!maybeHandleManagedFiles(uuid, files)) {
				throw CoreExceptionUtils.newException(Messages.CannotCompleteSubmitFailedStaging, null);
			}

			ICommandJob job = doJobSubmitCommand(uuid, mode);

			/*
			 * If the submit job lacks a jobId on the standard streams, then we
			 * assign it the UUID (it is most probably interactive); else we
			 * wait for the id to be set by the tokenizer.
			 */
			CommandJobStatus status = null;
			if (job.waitForId()) {
				status = new CommandJobStatus(getResourceManager().getUniqueName(), this);
				status.waitForJobId(uuid);
			} else {
				String state = job.isActive() ? IJobStatus.RUNNING : IJobStatus.FAILED;
				status = new CommandJobStatus(getResourceManager().getUniqueName(), uuid, state, this);
			}

			/*
			 * property containing actual jobId as name was set in the wait
			 * call; we may need the new jobId mapping momentarily to resolve
			 * proxy-specific info
			 */
			rmVarMap.remove(uuid);
			jobId = p.getName();

			pinTable.release(jobId);
			rmVarMap.put(p.getName(), p);

			ICommandJobStreamsProxy proxy = job.getProxy();
			status.setProxy(proxy);
			jobStatusMap.addJobStatus(status.getJobId(), status);
			status.setLaunchConfig(configuration);
			if (!job.isBatch()) {
				status.setProcess(job.getProcess());
			}

			/*
			 * to ensure the most recent script is used at the next call
			 */
			rmVarMap.remove(p.getName());
			rmVarMap.remove(SCRIPT_PATH);
			rmVarMap.remove(SCRIPT);

			return status;
		} finally {
			pinTable.release(uuid);
			pinTable.release(jobId);
		}
	}

	/**
	 * If there are special server connections to open, those need to be taken
	 * care of by a command to be run on start-up; here we just check for open
	 * connections.
	 * 
	 * @param monitor
	 * @throws RemoteConnectionException
	 */
	private void doConnect(IProgressMonitor monitor) throws RemoteConnectionException {
		IRemoteConnection conn = getRemoteServicesDelegate().getLocalConnection();
		if (!conn.isOpen()) {
			conn.open(monitor);
			if (!conn.isOpen()) {
				throw new RemoteConnectionException(Messages.LocalConnectionError);
			}
		}
		conn = getRemoteServicesDelegate().getRemoteConnection();
		if (!conn.isOpen()) {
			conn.open(monitor);
			if (!conn.isOpen()) {
				throw new RemoteConnectionException(Messages.RemoteConnectionError + conn.getAddress());
			}
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
			if (maybeKillInteractive(jobId)) {
				return;
			}

			job = controlData.getTerminateJob();
			if (job == null) {
				throw ce;
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

		runCommand(jobId, job, false, true);
	}

	/**
	 * Close the connections.
	 */
	private void doDisconnect() {
		IRemoteConnection conn = getRemoteServicesDelegate().getLocalConnection();
		if (conn.isOpen()) {
			conn.close();
		}
		conn = getRemoteServicesDelegate().getRemoteConnection();
		if (conn.isOpen()) {
			conn.close();
		}
	}

	/**
	 * Run either interactive or batch job for run or debug modes.
	 * ILaunchManager.RUN_MODE and ILaunchManager.DEBUG_MODE are the
	 * corresponding LaunchConfiguration modes; batch/interactive are currently
	 * determined by the configuration (the configuration cannot implement
	 * both). This may need to be modified.
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param mode
	 *            either ILaunchManager.RUN_MODE and ILaunchManager.DEBUG_MODE
	 * @return job wrapper object
	 * @throws CoreException
	 */
	private ICommandJob doJobSubmitCommand(String uuid, String mode) throws CoreException {
		CommandType command = null;
		boolean batch = false;

		if (ILaunchManager.RUN_MODE.equals(mode)) {
			command = controlData.getSubmitBatch();
			if (command != null) {
				batch = true;
			} else {
				command = controlData.getSubmitInteractive();
			}
		} else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			command = controlData.getSubmitBatchDebug();
			if (command != null) {
				batch = true;
			} else {
				command = controlData.getSubmitInteractiveDebug();
			}
		}

		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError + SP + uuid + SP + mode, null);
		}

		return runCommand(uuid, command, batch, false);
	}

	/**
	 * Run the shut down commands, if any
	 * 
	 * @throws CoreException
	 */
	private void doOnShutdown() throws CoreException {
		List<CommandType> onShutDown = controlData.getShutDownCommand();
		runCommands(onShutDown);
		for (IRemoteProcess process : processTable.values()) {
			if (!process.isCompleted()) {
				process.destroy();
			}
		}
	}

	/**
	 * Run the start up commands, if any
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	private void doOnStartUp(IProgressMonitor monitor) throws CoreException {
		List<CommandType> onStartUp = controlData.getStartUpCommand();
		runCommands(onStartUp);
	}

	/**
	 * @return specific implementation; used to call fireJobChanged internally.
	 */
	private JAXBResourceManager getBaseResourceManager() {
		return (JAXBResourceManager) getResourceManager();
	}

	/**
	 * Sets the maps and data tree.
	 */
	private void initialize() {
		launchEnv = new TreeMap<String, String>();
		processTable = new HashMap<String, IRemoteProcess>();
		pinTable = new JobIdPinTable();

		/*
		 * Use the base configuration which contains the config file information
		 */
		IJAXBResourceManagerConfiguration base = (IJAXBResourceManagerConfiguration) getResourceManager().getConfiguration();
		try {
			rmVarMap = base.getRMVariableMap();
			controlData = base.getResourceManagerData().getControlData();
		} catch (Throwable t) {
			JAXBCorePlugin.log(t);
		}
		setFixedConfigurationProperties();
		launchEnv.clear();
		appendLaunchEnv = true;

		/*
		 * start daemon
		 */
		jobStatusMap = new JobStatusMap(this);
		jobStatusMap.start();
	}

	/**
	 * Checks for existence of either internally generated script or custom
	 * script path. In either case, either replaces contents of the
	 * corresponding managed file object or creates one.
	 * 
	 * @param files
	 *            the set of managed files for this submission
	 * @return the set of managed files, possibly with the script file added
	 */
	private ManagedFilesType maybeAddManagedFileForScript(ManagedFilesType files) {
		PropertyType scriptVar = (PropertyType) rmVarMap.get(SCRIPT);
		PropertyType scriptPathVar = (PropertyType) rmVarMap.get(SCRIPT_PATH);
		if (scriptVar != null || scriptPathVar != null) {
			if (files == null) {
				files = new ManagedFilesType();
				files.setFileStagingLocation(ECLIPSESETTINGS);
			}
			List<ManagedFileType> fileList = files.getFile();
			ManagedFileType scriptFile = null;
			if (!fileList.isEmpty()) {
				for (ManagedFileType f : fileList) {
					if (f.getName().equals(SCRIPT_FILE)) {
						scriptFile = f;
						break;
					}
				}
			}
			if (scriptFile == null) {
				scriptFile = new ManagedFileType();
				scriptFile.setName(SCRIPT_FILE);
				fileList.add(scriptFile);
			}
			scriptFile.setResolveContents(false);
			scriptFile.setUniqueIdPrefix(true);
			if (scriptPathVar != null) {
				scriptFile.setPath(String.valueOf(scriptPathVar.getValue()));
				scriptFile.setDeleteAfterUse(false);
			} else {
				scriptFile.setContents(OPENVRM + SCRIPT + PD + VALUE + CLOSV);
				scriptFile.setDeleteAfterUse(true);
			}
		}
		return files;
	}

	/**
	 * Write content to file if indicated, and stage to host.
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param files
	 *            the set of managed files for this submission
	 * @return whether the necessary staging completed without error
	 * @throws CoreException
	 */
	private boolean maybeHandleManagedFiles(String uuid, ManagedFilesType files) throws CoreException {
		if (files == null || files.getFile().isEmpty()) {
			return true;
		}
		ManagedFilesJob job = new ManagedFilesJob(uuid, files, getRemoteServicesDelegate(), rmVarMap);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
		return job.getSuccess();
	}

	/**
	 * Serialize script content if necessary. We first check to see if there is
	 * a custom script (path).
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param script
	 *            configuration object describing how to construct the script
	 *            from the environment
	 */
	private void maybeHandleScript(String uuid, ScriptType script) {
		PropertyType p = (PropertyType) rmVarMap.get(SCRIPT_PATH);
		if (p != null && p.getValue() != null) {
			return;
		}
		if (script == null) {
			return;
		}
		ScriptHandler job = new ScriptHandler(uuid, script, rmVarMap, launchEnv);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException ignored) {
		}
	}

	/**
	 * If job is interactive, kill the process directly rather than issuing a
	 * remote command.
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
		if (killed) {
			/*
			 * automatically unpins the id
			 */
			jobStatusMap.removeJobStatus(jobId);
		}
		return killed;
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
	 * Create command job, and schedule. Used for job-specific commands
	 * directly.
	 * 
	 * @param uuid
	 *            temporary internal id for as yet unsubmitted job
	 * @param command
	 *            configuration object containing the command arguments and
	 *            tokenizers
	 * @param batch
	 *            whether batch or interactive
	 * @param join
	 *            whether to launch serially or not
	 * @return the runnable job object
	 * @throws CoreException
	 */
	private ICommandJob runCommand(String uuid, CommandType command, boolean batch, boolean join) throws CoreException {
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError, null);
		}

		ICommandJob job = null;

		if (command.getAsInputToProcess() != null) {
			job = new CommandJobInput(uuid, command, this);
		} else {
			job = new CommandJob(uuid, command, batch, this);
		}

		job.schedule();

		if (join) {
			try {
				job.join();
			} catch (InterruptedException ignored) {
			}
		}

		return job;
	}

	/**
	 * Run command sequence. Invoked by startup or shutdown commands. Delegates
	 * to {@link #runCommand(String, CommandType, boolean, boolean)}. Here we
	 * join to ensure seriality. If a job in the sequence fails, the subsequent
	 * commands will not run.
	 * 
	 * @param cmds
	 *            configuration objects containing the command arguments and
	 *            tokenizers
	 * @throws CoreException
	 */
	private void runCommands(List<CommandType> cmds) throws CoreException {
		for (CommandType cmd : cmds) {
			ICommandJob job = runCommand(null, cmd, false, false);

			if (!job.isActive()) {
				return;
			}

			try {
				job.join();
			} catch (InterruptedException ignored) {
			}
		}
	}

	/**
	 * User name and service address. Set in case the script needs these
	 * variables.
	 */
	private void setFixedConfigurationProperties() {
		IRemoteConnection rc = getRemoteServicesDelegate().getRemoteConnection();
		rmVarMap.maybeAddProperty(CONTROL_USER_VAR, rc.getUsername(), false);
		rmVarMap.maybeAddProperty(CONTROL_ADDRESS_VAR, rc.getAddress(), false);
	}

	/**
	 * Transfers the values from the configuration to the live map.
	 * 
	 * @param configuration
	 *            passed in from Launch Tab when the "run" command is chosen.
	 * @throws CoreException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updatePropertyValuesFromTab(ILaunchConfiguration configuration) throws CoreException {
		Map lcattr = configuration.getAttributes();
		for (Object key : lcattr.keySet()) {
			Object value = lcattr.get(key);
			Object target = rmVarMap.get(key.toString());
			if (target instanceof PropertyType) {
				PropertyType p = (PropertyType) target;
				p.setValue(value.toString());
			} else if (target instanceof AttributeType) {
				AttributeType ja = (AttributeType) target;
				ja.setValue(value);
			}
		}

		/*
		 * The non-selected variables have been excluded from the launch
		 * configuration; but we need to null out the superset values here that
		 * are undefined. We also need to take care of the tailF redirect
		 * variables (which are not visible but are set in the launch tab by an
		 * option checkbox).
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

		/*
		 * pull these out of the configuration; they are needed for the script
		 */
		rmVarMap.maybeOverwrite(SCRIPT_PATH, SCRIPT_PATH, configuration);
		rmVarMap.maybeOverwrite(DIRECTORY, IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, configuration);
		rmVarMap.maybeOverwrite(EXEC_PATH, IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, configuration);
		rmVarMap.maybeOverwrite(PROG_ARGS, IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, configuration);
		setFixedConfigurationProperties();

		launchEnv.clear();
		launchEnv.putAll(configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, launchEnv));
		appendLaunchEnv = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, appendLaunchEnv);
	}
}
