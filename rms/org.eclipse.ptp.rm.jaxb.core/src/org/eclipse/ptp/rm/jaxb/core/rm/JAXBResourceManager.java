package org.eclipse.ptp.rm.jaxb.core.rm;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
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
		setFixedConfigurationProperties();
	}

	/*
	 * The "do" methods should be Jobs.
	 */

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

	public IRemoteServices getLocalServices() {
		return localServices;
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

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

	@Override
	protected void doCleanUp() {

	}

	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		/*
		 * Do we need to break down any structures here? Might be a good idea to
		 * empty the env
		 */
	}

	@Override
	protected void doShutdown() throws CoreException {
		doOnShutdown();
		doDisconnect();
	}

	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		initializeConnections();
		doConnect();
		doOnStartUp();
		maybeDiscoverAttributes();
	}

	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		updatePropertyValuesFromTab(configuration);
		/*
		 * create the script if necessary; adds the contents to env as
		 * "${rm:script}"
		 */
		maybeHandleScript(controlData.getScript());
		maybeHandleManagedFiles(controlData.getManagedFiles());
		// according to mode, select the job type
		// run job commands
		return null;
	}

	private void doConnect() {

		// TODO Auto-generated method stub

	}

	private void doDisconnect() {
		// TODO Auto-generated method stub

	}

	/*
	 * run the shut down commands, if any
	 */
	private void doOnShutdown() {
		// TODO Auto-generated method stub

	}

	/*
	 * run the start up commands, if any
	 */
	private void doOnStartUp() {

		// also run maybe discover attributes
	}

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

	private void maybeDiscoverAttributes() {
		// TODO Auto-generated method stub

	}

	private void maybeHandleManagedFiles(ManagedFiles files) throws CoreException {
		ManagedFilesJob job = new ManagedFilesJob(files, this);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
	}

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

	private void runCommand(Command command) {

	}

	private void setFixedConfigurationProperties() {
		Map<String, Object> env = RMVariableMap.getInstance().getVariables();
		env.put(CONTROL_USER_VAR, config.getControlUserName());
		env.put(MONITOR_USER_VAR, config.getMonitorUserName());
		env.put(CONTROL_ADDRESS_VAR, config.getControlAddress());
		env.put(MONITOR_ADDRESS_VAR, config.getMonitorAddress());
	}

	/*
	 * Transfers the values from the configuration to the live map.
	 */
	private void updatePropertyValuesFromTab(ILaunchConfiguration configuration) throws CoreException {
		@SuppressWarnings("unchecked")
		Map<String, String> lcattr = configuration.getAttributes();
		Map<String, Object> env = RMVariableMap.getInstance().getVariables();
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
