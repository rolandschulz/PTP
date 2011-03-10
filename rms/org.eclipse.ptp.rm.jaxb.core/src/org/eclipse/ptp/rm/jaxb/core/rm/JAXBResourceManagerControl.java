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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.runnable.CommandJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerControl;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public final class JAXBResourceManagerControl extends AbstractResourceManagerControl implements IJAXBNonNLSConstants {

	private final IJAXBResourceManagerConfiguration config;
	private final Control controlData;

	private IRemoteServices remoteServices;
	private IRemoteServices localServices;
	private IRemoteConnectionManager remoteConnectionManager;
	private IRemoteConnectionManager localConnectionManager;
	private IRemoteConnection remoteConnection;
	private IRemoteConnection localConnection;
	private IRemoteFileManager remoteFileManager;
	private IRemoteFileManager localFileManager;

	private final Map<String, String> dynSystemEnv;
	private boolean appendSysEnv;

	public JAXBResourceManagerControl(IResourceManagerConfiguration jaxbServiceProvider) {
		super(jaxbServiceProvider);
		config = (IJAXBResourceManagerConfiguration) jaxbServiceProvider;
		controlData = config.resourceManagerData().getControlData();
		dynSystemEnv = new TreeMap<String, String>();
	}

	public boolean getAppendSysEnv() {
		return appendSysEnv;
	}

	public IJAXBResourceManagerConfiguration getConfig() {
		return config;
	}

	public Map<String, String> getDynSystemEnv() {
		return dynSystemEnv;
	}

	public IRemoteConnection getLocalConnection() {
		return localConnection;
	}

	public IRemoteConnectionManager getLocalConnectionManager() {
		return localConnectionManager;
	}

	public IRemoteFileManager getLocalFileManager() {
		return localFileManager;
	}

	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	public IRemoteConnectionManager getRemoteConnectionManager() {
		return remoteConnectionManager;
	}

	public IRemoteFileManager getRemoteFileManager() {
		return remoteFileManager;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		resetEnv();
		doControlCommand(jobId, operation);
		/*
		 * TODO: call the update handler?
		 */
		RMVariableMap.getActiveInstance().getVariables().remove(jobId);
	}

	@Override
	protected void doDispose() {
		// NOP for the moment
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doGetJobStatus
	 * (java.lang.String)
	 */
	@Override
	protected IJobStatus doGetJobStatus(String jobId) throws CoreException {
		Command job = controlData.getGetJobStatus();
		if (job == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + JOBSTATUS, null);
		}
		runCommand(jobId, job);
		return statusFromEnv(jobId);
	}

	@Override
	protected void doShutdown() throws CoreException {
		resetEnv();
		doOnShutdown();
		doDisconnect();
		config.clearReferences();
	}

	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		resetEnv();
		initializeConnections();
		try {
			doConnect(monitor);
		} catch (RemoteConnectionException t) {
			throw CoreExceptionUtils.newException(t.getMessage(), t);
		}
		doOnStartUp(monitor);
	}

	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		resetEnv();
		String uuid = UUID.randomUUID().toString();
		Property p = new Property();
		RMVariableMap.getActiveInstance().getVariables().put(uuid, p);
		updatePropertyValuesFromTab(configuration);

		/*
		 * create the script if necessary; adds the contents to env as
		 * "${rm:script}"
		 */
		maybeHandleScript(uuid, controlData.getScript());
		if (!maybeHandleManagedFiles(uuid, controlData.getManagedFiles())) {
			throw CoreExceptionUtils.newException(Messages.CannotCompleteSubmitFailedStaging, null);
		}

		doJobSubmitCommand(uuid, mode);
		IJobStatus status = statusFromEnv(uuid);
		// property should now contain the jobId as name
		RMVariableMap.getActiveInstance().getVariables().remove(uuid);
		RMVariableMap.getActiveInstance().getVariables().put(p.getName(), p);
		return status;
	}

	/*
	 * If there are special server connections to open, those need to be taken
	 * care of by a command to be run on start-up; here we just check for open
	 * connections.
	 */
	private void doConnect(IProgressMonitor monitor) throws RemoteConnectionException {
		if (!localConnection.isOpen()) {
			localConnection.open(monitor);
		}
		if (!remoteConnection.isOpen()) {
			remoteConnection.open(monitor);
		}
	}

	/*
	 * If the command is not supported, throws exception
	 */
	private void doControlCommand(String jobId, String operation) throws CoreException {
		CoreException ce = CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + operation, null);
		Command job = null;
		if (TERMINATE_OPERATION.equals(operation)) {
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
		runCommand(jobId, job);
	}

	/*
	 * Close the connections.
	 */
	private void doDisconnect() {
		if (localConnection.isOpen()) {
			localConnection.close();
		}
		if (!remoteConnection.isOpen()) {
			remoteConnection.close();
		}
	}

	/*
	 * Run either in interactive, batch or debug mode. right now,
	 * ILaunchManager.RUN_MODE and ILaunchManager.DEBUG_MODE are the two
	 * choices, meaning a single configuration cannot support both batch and
	 * interactive.
	 */
	private void doJobSubmitCommand(String uuid, String mode) throws CoreException {

		List<JAXBElement<Command>> commands = controlData.getSubmitInteractiveOrSubmitBatchOrSubmitDebug();
		if (commands.isEmpty()) {
			throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError, null);
		}
		Command command = null;
		for (JAXBElement<Command> job : commands) {
			command = job.getValue();
			if (job.getName().equals(SUBMIT_INTERACTIVE)) {
				if (ILaunchManager.RUN_MODE.equals(mode)) {
					break;
				}
			} else if (job.getName().equals(SUBMIT_BATCH)) {
				if (ILaunchManager.RUN_MODE.equals(mode)) {
					break;
				}
			} else if (job.getName().equals(SUBMIT_DEBUG)) {
				if (ILaunchManager.DEBUG_MODE.equals(mode)) {
					break;
				}
			}
		}
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError + mode, null);
		}
		runCommand(uuid, command);
	}

	/*
	 * Run the shut down commands, if any
	 */
	private void doOnShutdown() throws CoreException {
		List<Command> onShutDown = controlData.getShutDownCommand();
		runCommands(null, onShutDown, SHUTDOWN);
	}

	/*
	 * Run the start up commands, if any
	 */
	private void doOnStartUp(IProgressMonitor monitor) throws CoreException {
		List<Command> onStartUp = controlData.getStartUpCommand();
		runCommands(null, onStartUp, STARTUP);
	}

	/*
	 * For use by the command and file jobs.
	 */
	private void initializeConnections() {
		localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		assert (localServices != null);
		localConnectionManager = localServices.getConnectionManager();
		assert (localConnectionManager != null);
		/*
		 * Since it's a local service, it doesn't matter which parameter is
		 * passed
		 */
		localConnection = localConnectionManager.getConnection(ZEROSTR);
		assert (localConnection != null);
		localFileManager = localServices.getFileManager(localConnection);
		assert (localFileManager != null);
		remoteServices = PTPRemoteCorePlugin.getDefault()
				.getRemoteServices(config.getRemoteServicesId(), new NullProgressMonitor());
		assert (null != remoteServices);
		remoteConnectionManager = remoteServices.getConnectionManager();
		assert (null != remoteConnectionManager);
		remoteConnection = remoteConnectionManager.getConnection(config.getConnectionName());
		assert (null != remoteConnection);
		remoteFileManager = remoteServices.getFileManager(remoteConnection);
		assert (null != remoteFileManager);
	}

	private void maybeAddProperty(String name, Object value, Map<String, Object> env) {
		if (value == null) {
			return;
		}
		Property p = new Property();
		p.setName(name);
		p.setValue(value);
		env.put(name, p);
	}

	/*
	 * Write necessary content and stage to host if necessary.
	 */
	private boolean maybeHandleManagedFiles(String uuid, ManagedFiles files) throws CoreException {
		ManagedFilesJob job = new ManagedFilesJob(uuid, files, localFileManager, remoteFileManager);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
		return job.getSuccess();
	}

	/*
	 * Serialize script content if necessary.
	 */
	private void maybeHandleScript(String uuid, Script script) {
		if (script == null) {
			return;
		}
		ScriptHandler job = new ScriptHandler(uuid, script, dynSystemEnv, appendSysEnv);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private void maybeOverwrite(String key1, String key2, ILaunchConfiguration configuration, Map<String, Object> env)
			throws CoreException {
		Object value = null;
		Property p = (Property) env.get(key1);
		if (p != null) {
			value = p.getValue();
		}

		if (value instanceof Integer) {
			value = configuration.getAttribute(key2, (Integer) value);
		} else if (value instanceof Boolean) {
			value = configuration.getAttribute(key2, (Boolean) value);
		} else if (value instanceof String) {
			value = configuration.getAttribute(key2, (String) value);
		} else if (value instanceof List) {
			value = configuration.getAttribute(key2, (List) value);
		} else if (value instanceof Map) {
			value = configuration.getAttribute(key2, (Map) value);
		}

		maybeAddProperty(key1, value, env);
	}

	private void resetEnv() {
		config.setActive();
		Map<String, Object> env = RMVariableMap.getActiveInstance().getVariables();
		setFixedConfigurationProperties(env);
		dynSystemEnv.clear();
		appendSysEnv = true;
	}

	/*
	 * Create command job, schedule and join.
	 */
	private boolean runCommand(String uuid, Command command) throws CoreException {
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError, null);
		}
		CommandJob job = new CommandJob(uuid, command, (JAXBResourceManager) getResourceManager());
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
		return job.getSuccess();
	}

	/*
	 * Run command sequence.
	 */
	private void runCommands(String uuid, List<Command> cmds, String operation) throws CoreException {
		for (Command cmd : cmds) {
			if (!runCommand(uuid, cmd)) {
				return;
			}
		}
	}

	/*
	 * From the user runtime choices.
	 */
	private void setFixedConfigurationProperties(Map<String, Object> env) {
		env.put(CONTROL_USER_VAR, config.getControlUserName());
		env.put(MONITOR_USER_VAR, config.getMonitorUserName());
		env.put(CONTROL_ADDRESS_VAR, config.getControlAddress());
		env.put(MONITOR_ADDRESS_VAR, config.getMonitorAddress());
	}

	/*
	 * parser will have set the jobId against the UUID; we remove the env entry
	 * here
	 */
	private IJobStatus statusFromEnv(final String id) {
		final Property jobId = (Property) RMVariableMap.getActiveInstance().getVariables().get(id);
		return new IJobStatus() {
			public String getJobId() {
				if (jobId == null) {
					return id;
				}
				return jobId.getName();
			}

			public ILaunchConfiguration getLaunchConfiguration() {
				return null;
			}

			public String getState() {
				if (jobId == null) {
					return IJobStatus.UNDETERMINED;
				}
				return (String) jobId.getValue();
			}

			public String getStateDetail() {
				return getState();
			}

			public IStreamsProxy getStreamsProxy() {
				return null;
			}
		};
	}

	/*
	 * Transfers the values from the configuration to the live map.
	 */
	@SuppressWarnings("unchecked")
	private void updatePropertyValuesFromTab(ILaunchConfiguration configuration) throws CoreException {
		@SuppressWarnings("rawtypes")
		Map lcattr = configuration.getAttributes();
		Map<String, Object> env = RMVariableMap.getActiveInstance().getVariables();
		for (Object key : lcattr.keySet()) {
			Object value = lcattr.get(key);
			Object target = env.get(key.toString());
			if (target instanceof Property) {
				((Property) target).setValue(value.toString());
			} else if (target instanceof JobAttribute) {
				((JobAttribute) target).setValue(value.toString());
			}
		}

		dynSystemEnv.putAll(configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, dynSystemEnv));
		appendSysEnv = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, appendSysEnv);
		maybeOverwrite(DIRECTORY, IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, configuration, env);
		maybeOverwrite(EXEC_PATH, IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, configuration, env);
		maybeOverwrite(PROG_ARGS, IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, configuration, env);
	}
}
