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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFile;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
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

	/**
	 * Internal class for handling status of submitted jobs.
	 * 
	 * @author arossi
	 * 
	 */
	private class JobStatusMap extends Thread {
		private final Map<String, ICommandJobStatus> map = new HashMap<String, ICommandJobStatus>();
		private boolean running = false;

		/**
		 * Thread daemon for cleanup on the map. Eliminates stray completed
		 * state information, and also starts the stream proxies on jobs which
		 * have been submitted to a scheduler and have become active.
		 */
		@Override
		public void run() {
			Map<String, String> toPrune = new HashMap<String, String>();

			synchronized (map) {
				running = true;
			}

			while (isRunning()) {
				synchronized (map) {
					try {
						map.wait(2 * MINUTE_IN_MS);
					} catch (InterruptedException ignored) {
					}

					for (String jobId : map.keySet()) {
						IJobStatus status = getJobStatus(jobId);
						String state = status.getState();
						if (IJobStatus.COMPLETED.equals(state)) {
							toPrune.put(jobId, jobId);
						} else if (IJobStatus.RUNNING.equals(state)) {
							ICommandJobStatus commandJobStatus = map.get(jobId);
							if (commandJobStatus != null) {
								commandJobStatus.startProxy();
							}
						}
					}
					for (Iterator<Map.Entry<String, ICommandJobStatus>> i = map.entrySet().iterator(); i.hasNext();) {
						Map.Entry<String, ICommandJobStatus> e = i.next();
						if (null != toPrune.remove(e.getKey())) {
							ICommandJobStatus status = e.getValue();
							if (status != null) {
								status.cancel();
							}
							i.remove();
						}
					}
				}
			}

			synchronized (map) {
				for (String jobId : map.keySet()) {
					ICommandJobStatus status = map.get(jobId);
					if (status != null) {
						status.cancel();
					}
				}
				map.clear();
			}
		}

		/**
		 * @param jobId
		 *            either internal UUID or scheduler id for the job.
		 * @param status
		 *            object containing status info and stream proxy
		 */
		private void addJobStatus(String jobId, ICommandJobStatus status) {
			synchronized (map) {
				map.put(jobId, status);
			}
		}

		/**
		 * 
		 * @param jobId
		 *            either internal UUID or scheduler id for the job.
		 * @return object containing status info and stream proxy
		 */
		private ICommandJobStatus getStatus(String jobId) {
			ICommandJobStatus status = null;
			synchronized (map) {
				status = map.get(jobId);
			}
			return status;
		}

		/**
		 * shuts down the daemon
		 */
		private void halt() {
			synchronized (map) {
				running = false;
				map.notifyAll();
			}
		}

		/**
		 * @return whether the daemon is running
		 */
		private boolean isRunning() {
			boolean b = false;
			synchronized (map) {
				b = running;
			}
			return b;
		}

		/**
		 * @param jobId
		 *            either internal UUID or scheduler id for the job.
		 */
		private void removeJobStatus(String jobId) {
			synchronized (map) {
				map.remove(jobId);
			}
		}
	}

	private final IJAXBResourceManagerConfiguration config;
	private RMVariableMap rmVarMap;
	private Control controlData;
	private final Map<String, String> launchEnv;
	private final JobStatusMap jobStatusMap;
	private final RemoteServicesDelegate delegate;
	private boolean appendLaunchEnv;

	/**
	 * @param jaxbServiceProvider
	 *            the configuration object containing resource manager specifics
	 */
	public JAXBResourceManagerControl(AbstractResourceManagerConfiguration jaxbServiceProvider) {
		super(jaxbServiceProvider);
		config = (IJAXBResourceManagerConfiguration) jaxbServiceProvider;
		delegate = new RemoteServicesDelegate(config.getRemoteServicesId(), config.getConnectionName());
		launchEnv = new TreeMap<String, String>();
		jobStatusMap = new JobStatusMap();
		jobStatusMap.start();
	}

	/**
	 * @return whether to append (true) the env passed in through the
	 *         LaunchConfiguration, or replace the current env with it.
	 */
	public boolean getAppendEnv() {
		return appendLaunchEnv;
	}

	/**
	 * @return any environment variables passed in through the
	 *         LaunchConfiguration
	 */
	public Map<String, String> getLaunchEnv() {
		return launchEnv;
	}

	/**
	 * @return wrapper object for remote services, connections and file managers
	 */
	public RemoteServicesDelegate getRemoteServicesDelegate() {
		return delegate;
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
			resetEnv();
			Property p = new Property();
			p.setVisible(false);
			p.setName(jobId);
			p.setValue(jobId);
			rmVarMap.put(jobId, p);
			doControlCommand(jobId, operation);
			rmVarMap.remove(jobId);
			if (TERMINATE_OPERATION.equals(operation)) {
				jobStatusMap.removeJobStatus(jobId);
			}
		} catch (CoreException ce) {
			getResourceManager().setState(IResourceManager.ERROR_STATE);
			throw ce;
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
				return new CommandJobStatus(jobId, IJobStatus.UNDETERMINED);
			}

			/*
			 * first check to see when the last call was made; throttle requests
			 * coming in intervals less than
			 * ICommandJobStatus.UPDATE_REQUEST_INTERVAL
			 */
			ICommandJobStatus status = jobStatusMap.getStatus(jobId);
			if (status != null) {
				if (IJobStatus.COMPLETED.equals(status.getState())) {
					status.cancel();
					/*
					 * leave the status in the map in case there are further
					 * calls; it will be pruned by the daemon
					 */
					return status;
				}

				long now = System.currentTimeMillis();
				long lapse = now - status.getLastUpdateRequest();
				if (lapse < ICommandJobStatus.UPDATE_REQUEST_INTERVAL) {
					return status;
				}
				status.setUpdateRequestTime(now);
			}

			Property p = new Property();
			p.setVisible(false);
			p.setName(jobId);
			p.setValue(jobId);
			rmVarMap.put(jobId, p);

			Command job = controlData.getGetJobStatus();
			if (job == null) {
				throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + JOBSTATUS, null);
			}

			runCommand(jobId, job, false, true);

			p = (Property) rmVarMap.remove(jobId);
			String state = IJobStatus.UNDETERMINED;
			if (p != null) {
				state = (String) p.getValue();
			}

			if (status == null) {
				status = new CommandJobStatus(jobId, state);
				jobStatusMap.addJobStatus(jobId, status);
			} else {
				status.setState(state);
			}

			if (IJobStatus.COMPLETED.equals(state)) {
				/*
				 * leave the status in the map in case there are further calls;
				 * it will be pruned by the daemon
				 */
				status.cancel();
			} else if (IJobStatus.RUNNING.equals(state)) {
				status.startProxy();
			}

			// XXX eliminate when monitoring is in place
			System.out.println(Messages.RefreshedJobStatusMessage + jobId + CM + SP + status.getState());
			return status;
		} catch (CoreException ce) {
			getResourceManager().setState(IResourceManager.ERROR_STATE);
			throw ce;
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
			resetEnv();
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
		getResourceManager().setState(IResourceManager.STARTING_STATE);
		try {
			try {
				doConnect(monitor);
			} catch (RemoteConnectionException t) {
				throw CoreExceptionUtils.newException(t.getMessage(), t);
			}
			resetEnv();
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
	protected synchronized IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		if (!resourceManagerIsActive()) {
			return new CommandJobStatus(UUID.randomUUID().toString(), IJobStatus.UNDETERMINED);
		}

		resetEnv();

		/*
		 * give submission a unique id which will in most cases be replaced by
		 * the resource-generated id for the job/process
		 */
		String uuid = UUID.randomUUID().toString();
		Property p = new Property();
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

		ManagedFiles files = controlData.getManagedFiles();

		/*
		 * if the script is to be staged, a managed file pointing to either its
		 * as its content (${rm:script#value}), or to its path (SCRIPT_PATH)
		 * must exist.
		 */
		files = maybeAddManagedFileForScript(files);

		if (!maybeHandleManagedFiles(uuid, files)) {
			throw CoreExceptionUtils.newException(Messages.CannotCompleteSubmitFailedStaging, null);
		}

		CommandJob job = doJobSubmitCommand(uuid, mode);

		/*
		 * If the submit job lacks a jobId on the standard streams, then we
		 * assign it the UUID (it is most probably interactive); else we wait
		 * for the id to be set by the tokenizer.
		 */
		CommandJobStatus status = null;
		if (job.waitForId()) {
			status = new CommandJobStatus();
			status.waitForJobId(uuid);
		} else {
			String state = job.isActive() ? IJobStatus.RUNNING : IJobStatus.FAILED;
			status = new CommandJobStatus(uuid, state);
		}

		/*
		 * property containing actual jobId as name was accessed in the wait
		 * call
		 */
		rmVarMap.remove(uuid);
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
		rmVarMap.remove(SCRIPT_PATH);
		rmVarMap.remove(SCRIPT);

		return status;
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
		IRemoteConnection conn = delegate.getLocalConnection();
		if (!conn.isOpen()) {
			conn.open(monitor);
			if (!conn.isOpen()) {
				throw new RemoteConnectionException(Messages.LocalConnectionError);
			}
		}
		conn = delegate.getRemoteConnection();
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
		Command job = null;
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
		IRemoteConnection conn = delegate.getLocalConnection();
		if (conn.isOpen()) {
			conn.close();
		}
		conn = delegate.getRemoteConnection();
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
	private CommandJob doJobSubmitCommand(String uuid, String mode) throws CoreException {
		List<JAXBElement<Command>> commands = controlData.getSubmitInteractiveOrSubmitBatchOrSubmitDebugInteractive();
		if (commands.isEmpty()) {
			throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError, null);
		}

		Command command = null;
		boolean batch = false;
		for (JAXBElement<Command> job : commands) {
			command = job.getValue();
			if (command == null) {
				throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError + mode, null);
			}

			if (command.getName().equals(SUBMIT_INTERACTIVE)) {
				if (ILaunchManager.RUN_MODE.equals(mode)) {
					break;
				}
			} else if (command.getName().equals(SUBMIT_BATCH)) {
				System.out.println("submit-batch, mode: " + mode);
				if (ILaunchManager.RUN_MODE.equals(mode)) {
					batch = true;
					break;
				}
			} else if (command.getName().equals(SUBMIT_DEBUG_INTERACTIVE)) {
				if (ILaunchManager.DEBUG_MODE.equals(mode)) {
					break;
				}
			} else if (command.getName().equals(SUBMIT_DEBUG_BATCH)) {
				if (ILaunchManager.DEBUG_MODE.equals(mode)) {
					batch = true;
					break;
				}
			}
		}

		return runCommand(uuid, command, batch, false);
	}

	/**
	 * Run the shut down commands, if any
	 * 
	 * @throws CoreException
	 */
	private void doOnShutdown() throws CoreException {
		List<Command> onShutDown = controlData.getShutDownCommand();
		runCommands(onShutDown);
	}

	/**
	 * Run the start up commands, if any
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	private void doOnStartUp(IProgressMonitor monitor) throws CoreException {
		List<Command> onStartUp = controlData.getStartUpCommand();
		runCommands(onStartUp);
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
	private ManagedFiles maybeAddManagedFileForScript(ManagedFiles files) {
		Property scriptVar = (Property) RMVariableMap.getActiveInstance().get(SCRIPT);
		Property scriptPathVar = (Property) RMVariableMap.getActiveInstance().get(SCRIPT_PATH);
		if (scriptVar != null || scriptPathVar != null) {
			if (files == null) {
				files = new ManagedFiles();
				files.setFileStagingLocation(ECLIPSESETTINGS);
			}
			List<ManagedFile> fileList = files.getFile();
			ManagedFile scriptFile = null;
			if (!fileList.isEmpty()) {
				for (ManagedFile f : fileList) {
					if (f.getName().equals(SCRIPT_FILE)) {
						scriptFile = f;
						break;
					}
				}
			}
			if (scriptFile == null) {
				scriptFile = new ManagedFile();
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
	private boolean maybeHandleManagedFiles(String uuid, ManagedFiles files) throws CoreException {
		if (files == null || files.getFile().isEmpty()) {
			return true;
		}
		ManagedFilesJob job = new ManagedFilesJob(uuid, files, delegate);
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
	private void maybeHandleScript(String uuid, Script script) {
		Property p = (Property) rmVarMap.get(SCRIPT_PATH);
		if (p != null && p.getValue() != null) {
			return;
		}
		if (script == null) {
			return;
		}
		ScriptHandler job = new ScriptHandler(uuid, script, rmVarMap, launchEnv, appendLaunchEnv);
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
		if (status != null && status.isInteractive()) {
			status.cancel();
			jobStatusMap.removeJobStatus(jobId);
			return true;
		}
		return false;
	}

	/**
	 * Ensure that this manager has its own environement. Add the fixed
	 * properties again, clear environment from the tab.
	 */
	private void resetEnv() {
		try {
			/*
			 * Use the base configuration which contains the config file
			 * information
			 */
			IJAXBResourceManagerConfiguration config = (IJAXBResourceManagerConfiguration) getResourceManager().getConfiguration();
			config.setActive();
			rmVarMap = RMVariableMap.getActiveInstance();
			controlData = config.getResourceManagerData().getControlData();
		} catch (Throwable t) {
			JAXBCorePlugin.log(t);
			return;
		}
		setFixedConfigurationProperties();
		launchEnv.clear();
		appendLaunchEnv = true;
	}

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
	private CommandJob runCommand(String uuid, Command command, boolean batch, boolean join) throws CoreException {
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError, null);
		}

		CommandJob job = new CommandJob(uuid, command, batch, this);
		if (batch) {
			Property p = (Property) rmVarMap.get(STDOUT);
			if (p != null) {
				job.setRemoteOutPath((String) p.getValue());
			}
			p = (Property) rmVarMap.get(STDERR);
			if (p != null) {
				job.setRemoteErrPath((String) p.getValue());
			}
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
	 * to {@link #runCommand(String, Command, boolean, boolean)}. Here we join
	 * to ensure seriality. If a job in the sequence fails, the subsequent
	 * commands will not run.
	 * 
	 * @param cmds
	 *            configuration objects containing the command arguments and
	 *            tokenizers
	 * @throws CoreException
	 */
	private void runCommands(List<Command> cmds) throws CoreException {
		for (Command cmd : cmds) {
			CommandJob job = runCommand(null, cmd, false, false);

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
		IRemoteConnection rc = delegate.getRemoteConnection();
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
			if (target instanceof Property) {
				Property p = (Property) target;
				p.setValue(value.toString());
			} else if (target instanceof Attribute) {
				Attribute ja = (Attribute) target;
				ja.setValue(value);
			}
		}

		/*
		 * The non-selected variables have been excluded from the launch
		 * configuration; but we need to null out the superset values here that
		 * are undefined.
		 */
		for (String key : rmVarMap.getVariables().keySet()) {
			if (!lcattr.containsKey(key)) {
				Object target = rmVarMap.get(key.toString());
				if (target instanceof Property) {
					Property p = (Property) target;
					p.setValue(null);
				} else if (target instanceof Attribute) {
					Attribute ja = (Attribute) target;
					ja.setValue(null);
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
