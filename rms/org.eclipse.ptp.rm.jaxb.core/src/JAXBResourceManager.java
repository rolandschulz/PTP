

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
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFile;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFileJob;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public final class JAXBResourceManager extends AbstractResourceManager {

	private final JAXBServiceProvider config;

	public JAXBResourceManager(IPUniverse universe, IResourceManagerConfiguration jaxbServiceProvider) {
		super(universe, jaxbServiceProvider);
		config = (JAXBServiceProvider) jaxbServiceProvider;
		setRuntimeProperties();
	}

	@Override
	protected void doCleanUp() {
		/*
		 * Do we need to break down any structures here? Might be a good idea to
		 * empty the env.
		 */
	}

	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		/*
		 * Close connections?
		 */
	}

	@Override
	protected void doShutdown() throws CoreException {
		doShutdown();
		doDisconnect();
	}

	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		doConnect();
		doOnStartUp();
		maybeDiscoverAttributes();
		getAvailableQueues();
	}

	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	private void doConnect() {
		// TODO Auto-generated method stub

	}

	private void doDisconnect() {
		// TODO Auto-generated method stub

	}

	private void doOnStartUp() {
		// TODO Auto-generated method stub

	}

	private void getAvailableQueues() {
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
	private void runCommand(Command command, Map<String, Object> env) {

	}

	/*
	 * Should get the values, run through them in map: if Attribute, setValue()
	 * if Property, setValue();
	 */
	private void setPropertyValuesFromTab(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub

	}

	/*
	 * 
	 * 
	 * then set managed-file source/target from the above
	 */
	private void setRuntimeProperties() {
		Map<String, Object> env = RMVariableMap.getInstance().getVariables();
		env.put("control.user.name", config.getControlUserName());
	}
}
