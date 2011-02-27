package org.eclipse.ptp.rm.jaxb.core.rm;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.Control.SubmitCommands;
import org.eclipse.ptp.rm.jaxb.core.data.DiscoverAttributes;
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
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public final class JAXBResourceManager extends AbstractResourceManager implements IJAXBNonNLSConstants {

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
	private IRemoteProcessBuilder processBuilder;

	public JAXBResourceManager(IPUniverse universe, IResourceManagerConfiguration jaxbServiceProvider) {
		super(universe, jaxbServiceProvider);
		config = (IJAXBResourceManagerConfiguration) jaxbServiceProvider;
		controlData = config.resourceManagerData().getControl();
		config.setActive();
		setFixedConfigurationProperties();
	}

	public IJAXBResourceManagerConfiguration getConfig() {
		return config;
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

	public IRemoteProcessBuilder getProcessBuilder() {
		return processBuilder;
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

	@Override
	protected void doCleanUp() {
		config.clearReferences();
	}

	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		config.setActive();
		updateJobId(jobId);
		doControlCommand(operation);
	}

	@Override
	protected void doDispose() {
		// NOP for the moment
	}

	@Override
	protected void doShutdown() throws CoreException {
		config.setActive();
		doOnShutdown();
		doDisconnect();
	}

	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		config.setActive();
		initializeConnections();
		try {
			doConnect(monitor);
		} catch (RemoteConnectionException t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}
		doOnStartUp(monitor);
		maybeDiscoverAttributes(monitor);
	}

	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		config.setActive();
		updatePropertyValuesFromTab(configuration);
		/*
		 * create the script if necessary; adds the contents to env as
		 * "${rm:script}"
		 */
		maybeHandleScript(controlData.getScript());
		maybeHandleManagedFiles(controlData.getManagedFiles());
		doJobSubmitCommand(mode);
		/*
		 * parser will have set the jobId in the map
		 */
		return getJobStatus(currentJobId());
	}

	/*
	 * @see updateJobId
	 */
	private String currentJobId() {
		return (String) RMVariableMap.getActiveInstance().getVariables().get(JOB_ID);
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

	/*
	 * Run the discover attributes commands, if any; these can include queues,
	 * for instance.
	 */
	private void maybeDiscoverAttributes(IProgressMonitor monitor) throws CoreException {
		DiscoverAttributes discoverAttributes = controlData.getDiscoverAttributes();
		if (discoverAttributes == null) {
			return;
		}
		runCommands(discoverAttributes.getCommandRef(), DISCATTR);
	}

	/*
	 * Write necessary content and stage to host if necessary.
	 */
	private void maybeHandleManagedFiles(ManagedFiles files) throws CoreException {
		ManagedFilesJob job = new ManagedFilesJob(files, this);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
	}

	/*
	 * Serialize script content if necessary.
	 */
	private void maybeHandleScript(Script script) {
		if (script == null) {
			return;
		}
		ScriptHandler job = new ScriptHandler(script);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
	}

	/*
	 * Create command job, schedule and join.
	 */
	private void runCommand(String commandRef) throws CoreException {
		Command command = (Command) RMVariableMap.getActiveInstance().getVariables().get(commandRef);
		if (command == null) {
			throw CoreExceptionUtils.newException(Messages.RMNoSuchCommandError + commandRef, null);
		}
		CommandJob job = new CommandJob(command);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
	}

	/*
	 * Run command sequence.
	 */
	private void runCommands(List<String> cmds, String operation) throws CoreException {
		if (cmds == null) {
			throw CoreExceptionUtils.newException(Messages.EmptyCommandDef + operation, null);
		}
		for (String ref : cmds) {
			runCommand(ref);
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
	 * Transfers the values from the configuration to the live map.
	 */
	private void updatePropertyValuesFromTab(ILaunchConfiguration configuration) throws CoreException {
		@SuppressWarnings("unchecked")
		Map<String, String> lcattr = configuration.getAttributes();
		Map<String, Object> env = RMVariableMap.getActiveInstance().getVariables();
		for (String key : lcattr.keySet()) {
			String value = lcattr.get(key);
			Object target = env.get(key);
			if (target instanceof Property) {
				((Property) target).setValue(value);
			} else if (target instanceof JobAttribute) {
				((JobAttribute) target).setValue(value);
			}
		}
	}
}
