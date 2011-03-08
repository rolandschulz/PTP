package org.eclipse.ptp.rm.jaxb.core.rm;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
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
import org.eclipse.ptp.rm.jaxb.core.data.Control.SubmitCommands;
import org.eclipse.ptp.rm.jaxb.core.data.HoldJob;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.OnShutDown;
import org.eclipse.ptp.rm.jaxb.core.data.OnStartUp;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.ReleaseJob;
import org.eclipse.ptp.rm.jaxb.core.data.ResumeJob;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.data.SubmitBatch;
import org.eclipse.ptp.rm.jaxb.core.data.SubmitDebug;
import org.eclipse.ptp.rm.jaxb.core.data.SubmitInteractive;
import org.eclipse.ptp.rm.jaxb.core.data.SuspendJob;
import org.eclipse.ptp.rm.jaxb.core.data.TerminateJob;
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
		controlData = config.resourceManagerData().getControl();
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

	/*
	 * @see updateJobId
	 */
	private String currentJobId() {
		Property p = (Property) RMVariableMap.getActiveInstance().getVariables().get(JOB_ID);
		if (p != null) {
			return (String) p.getValue();
		}
		return null;
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
	private void doControlCommand(String operation) throws CoreException {
		CoreException ce = CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + operation, null);
		List<String> cmds = null;
		if (TERMINATE_OPERATION.equals(operation)) {
			TerminateJob job = controlData.getTerminateJob();
			if (job == null) {
				throw ce;
			}
			cmds = job.getCommandRef();
		} else if (SUSPEND_OPERATION.equals(operation)) {
			SuspendJob job = controlData.getSuspendJob();
			if (job == null) {
				throw ce;
			}
			cmds = job.getCommandRef();
		} else if (RESUME_OPERATION.equals(operation)) {
			ResumeJob job = controlData.getResumeJob();
			if (job == null) {
				throw ce;
			}
			cmds = job.getCommandRef();
		} else if (RELEASE_OPERATION.equals(operation)) {
			ReleaseJob job = controlData.getReleaseJob();
			if (job == null) {
				throw ce;
			}
			cmds = job.getCommandRef();
		} else if (HOLD_OPERATION.equals(operation)) {
			HoldJob job = controlData.getHoldJob();
			if (job == null) {
				throw ce;
			}
			cmds = job.getCommandRef();
		}
		runCommands(cmds, operation);
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
	private void doJobSubmitCommand(String mode) throws CoreException {
		SubmitCommands commands = controlData.getSubmitCommands();
		if (commands == null) {
			throw CoreExceptionUtils.newException(Messages.MissingRunCommandsError, null);
		}
		List<Object> list = commands.getSubmitInteractiveOrSubmitBatchOrSubmitDebug();
		List<String> cmds = null;
		// check mode for type
		for (Object job : list) {
			if (job instanceof SubmitInteractive) {
				SubmitInteractive interactive = (SubmitInteractive) job;
				if (ILaunchManager.RUN_MODE.equals(mode)) {
					cmds = interactive.getCommandRef();
					break;
				}
			} else if (job instanceof SubmitBatch) {
				SubmitBatch batch = (SubmitBatch) job;
				if (ILaunchManager.RUN_MODE.equals(mode)) {
					cmds = batch.getCommandRef();
					break;
				}
			} else if (job instanceof SubmitDebug) {
				SubmitDebug debug = (SubmitDebug) job;
				if (ILaunchManager.DEBUG_MODE.equals(mode)) {
					cmds = debug.getCommandRef();
					break;
				}
			}
		}
		runCommands(cmds, mode);
	}

	/*
	 * Run the shut down commands, if any
	 */
	private void doOnShutdown() throws CoreException {
		OnShutDown onShutDown = controlData.getOnShutDown();
		if (onShutDown == null) {
			return;
		}
		runCommands(onShutDown.getCommandRef(), SHUTDOWN);
	}

	/*
	 * Run the start up commands, if any
	 */
	private void doOnStartUp(IProgressMonitor monitor) throws CoreException {
		OnStartUp onStartUp = controlData.getOnStartUp();
		if (onStartUp == null) {
			return;
		}
		runCommands(onStartUp.getCommandRef(), STARTUP);
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
	private boolean maybeHandleManagedFiles(ManagedFiles files) throws CoreException {
		ManagedFilesJob job = new ManagedFilesJob(files, localFileManager, remoteFileManager);
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
	private void maybeHandleScript(Script script) {
		if (script == null) {
			return;
		}
		ScriptHandler job = new ScriptHandler(script, dynSystemEnv, appendSysEnv);
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
		setFixedConfigurationProperties();
		dynSystemEnv.clear();
		appendSysEnv = true;
	}

	/*
	 * Create command job, schedule and join.
	 */
	private boolean runCommand(String commandRef) throws CoreException {
		Command command = (Command) RMVariableMap.getActiveInstance().getVariables().get(commandRef);
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + commandRef, null);
		}
		CommandJob job = new CommandJob(command, (JAXBResourceManager) getResourceManager());
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
	private void runCommands(List<String> cmds, String operation) throws CoreException {
		if (cmds == null) {
			throw CoreExceptionUtils.newException(Messages.EmptyCommandDef + operation, null);
		}
		for (String ref : cmds) {
			if (!runCommand(ref)) {
				return;
			}
		}
	}

	/*
	 * From the user runtime choices.
	 */
	private void setFixedConfigurationProperties() {
		Map<String, Object> env = RMVariableMap.getActiveInstance().getVariables();
		env.put(CONTROL_USER_VAR, config.getControlUserName());
		env.put(MONITOR_USER_VAR, config.getMonitorUserName());
		env.put(CONTROL_ADDRESS_VAR, config.getControlAddress());
		env.put(MONITOR_ADDRESS_VAR, config.getMonitorAddress());
	}

	/*
	 * @warning: current implementation treats jobs serially; only one jobId can
	 * be in the map at a time.
	 */
	private void updateJobId(String jobId) {
		RMVariableMap.getActiveInstance().getVariables().put(JOB_ID, jobId);
	}

	/*
	 * Transfers the values from the configuration to the live map. Runs
	 * validator? or should this be in UI?
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

	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		resetEnv();
		updateJobId(jobId);
		doControlCommand(operation);
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
		// TODO Auto-generated method stub
		return null;
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
		updatePropertyValuesFromTab(configuration);
		/*
		 * create the script if necessary; adds the contents to env as
		 * "${rm:script}"
		 */
		maybeHandleScript(controlData.getScript());
		if (!maybeHandleManagedFiles(controlData.getManagedFiles())) {
			throw CoreExceptionUtils.newException(Messages.CannotCompleteSubmitFailedStaging, null);
		}
		doJobSubmitCommand(mode);
		/*
		 * parser will have set the jobId in the map
		 */
		return getJobStatus(currentJobId());
	}
}
