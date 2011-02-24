package org.eclipse.ptp.rm.jaxb.core.rm;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFile;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFileJob;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public final class JAXBResourceManager extends AbstractResourceManager implements IJAXBNonNLSConstants {

	private final IJAXBResourceManagerConfiguration config;

	public JAXBResourceManager(IPUniverse universe, IResourceManagerConfiguration jaxbServiceProvider) {
		super(universe, jaxbServiceProvider);
		config = (IJAXBResourceManagerConfiguration) jaxbServiceProvider;
		setFixedConfigurationProperties();
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
		doConnect();
		doOnStartUp();
		maybeDiscoverAttributes();
	}

	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		updatePropertyValuesFromTab(configuration);
		// handle files here
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
		// TODO Auto-generated method stub

	}

	private IRemoteConnection getControlConnection() {
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(config.getRemoteServicesId(),
				new NullProgressMonitor());
		if (remoteServices != null) {
			IRemoteConnectionManager rconnMgr = remoteServices.getConnectionManager();
			if (rconnMgr != null) {
				return rconnMgr.getConnection(config.getConnectionName());
			}
		}
		return null;
	}

	private ManagedFileJob handleManagedFile(ManagedFiles files, ManagedFile file, Map<String, Object> env) {
		ManagedFileJob job = new ManagedFileJob(file, files, getControlConnection(), env);
		job.schedule();
		return job;
	}

	/*
	 * Assemble the script: dereference the values: directives; environment;
	 * commands, add each to string buffer. Assign the string to the "script"
	 * variable.
	 */
	private void handleScript(Control control, Map<String, Object> env) {

	}

	private void maybeDiscoverAttributes() {
		// TODO Auto-generated method stub

	}

	/*
	 * Do this as Job. Assemble the args. Dereference the entire command string.
	 * Issue across the connection; capture the stream(s); parse the stream(s),
	 * adding to environment
	 */
	private void runCommand(Command command) {

	}

	/*
	 * 
	 * 
	 * then set managed-file source/target from the above
	 */
	private void setFixedConfigurationProperties() {
		Map<String, Object> env = RMVariableMap.getInstance().getVariables();
		env.put(CONTROL_USER_VAR, config.getControlUserName());
		env.put(MONITOR_USER_VAR, config.getMonitorUserName());
		env.put(CONTROL_ADDRESS_VAR, config.getControlAddress());
		env.put(MONITOR_ADDRESS_VAR, config.getMonitorAddress());
	}

	/*
	 * Transfers the values from the configuration to the environment.
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
			} else {
				env.put(key, value);
			}
		}
	}
}
