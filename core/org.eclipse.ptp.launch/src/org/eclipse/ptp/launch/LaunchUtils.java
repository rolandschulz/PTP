package org.eclipse.ptp.launch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.ptp.launch.rulesengine.RuleFactory;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.utils.core.ArgumentParser;

/**
 * @since 6.0
 * 
 */
public class LaunchUtils {

	/**
	 * Check if the copy local file is enabled. If it is, copy the executable file from the local host to the remote host.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @throws CoreException
	 *             if the copy fails or is cancelled
	 */
	public static void copyExecutable(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		boolean copyExecutable = getCopyExecutable(configuration);

		if (copyExecutable) {
			// Get remote and local paths
			String remotePath = getExecutablePath(configuration);
			String localPath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_LOCAL_EXECUTABLE_PATH,
					(String) null);

			// Check if local path is valid
			if (localPath == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_1));
			}

			// Copy data
			copyFileToRemoteHost(localPath, remotePath, configuration, monitor);
		}
	}

	/**
	 * Copy a data from a path (can be a file or directory) from the remote host to the local host.
	 * 
	 * @param remotePath
	 * @param localPath
	 * @param configuration
	 * @throws CoreException
	 */
	public static void copyFileFromRemoteHost(String remotePath, String localPath, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			IRemoteFileManager localFileManager = getLocalFileManager(configuration);
			IRemoteFileManager remoteFileManager = getRemoteFileManager(configuration, progress.newChild(5));
			if (progress.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user, null));
			}
			if (remoteFileManager == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_0));
			}

			IFileStore rres = remoteFileManager.getResource(remotePath);
			if (!rres.fetchInfo(EFS.NONE, progress.newChild(5)).exists()) {
				// Local file not found!
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Remote_resource_does_not_exist));
			}
			IFileStore lres = localFileManager.getResource(localPath);

			// Copy file
			rres.copy(lres, EFS.OVERWRITE, progress.newChild(5));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Copy a data from a path (can be a file or directory) from the local host to the remote host.
	 * 
	 * @param localPath
	 * @param remotePath
	 * @param configuration
	 * @throws CoreException
	 */
	public static void copyFileToRemoteHost(String localPath, String remotePath, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			IRemoteFileManager localFileManager = getLocalFileManager(configuration);
			IRemoteFileManager remoteFileManager = getRemoteFileManager(configuration, progress.newChild(5));
			if (progress.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user, null));
			}
			if (remoteFileManager == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_0));
			}

			IFileStore lres = localFileManager.getResource(localPath);
			if (!lres.fetchInfo(EFS.NONE, progress.newChild(5)).exists()) {
				// Local file not found!
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Local_resource_does_not_exist));
			}
			IFileStore rres = remoteFileManager.getResource(remotePath);

			// Copy file
			lres.copy(rres, EFS.OVERWRITE, progress.newChild(5));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Get the program arguments specified in the Arguments tab
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, (String) null);
	}

	/**
	 * Get if the executable shall be copied to remote target before launch.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static boolean getCopyExecutable(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_COPY_EXECUTABLE, false);
	}

	/**
	 * Get the debugger configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return debugger configuration
	 * @throws CoreException
	 */
	public static IPDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
		return PTPDebugCorePlugin.getDefault().getDebugConfiguration(getDebuggerID(config));
	}

	/**
	 * Get the debugger executable path
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getDebuggerExePath(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String) null);
	}

	/**
	 * Get the ID of the debugger for this launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getDebuggerID(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, (String) null);
	}

	/**
	 * Get the debugger "stop in main" flag
	 * 
	 * @param configuration
	 * @return "stop in main" flag
	 * @throws CoreException
	 */
	public static boolean getDebuggerStopInMainFlag(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
	}

	/**
	 * Get the working directory for this debug session
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getDebuggerWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String) null);
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String[] getEnvironmentToAppend(ILaunchConfiguration configuration) throws CoreException {
		Map<?, ?> defaultEnv = null;
		Map<?, ?> configEnv = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, defaultEnv);
		if (configEnv == null) {
			return null;
		}
		if (!configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true)) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Parallel_launcher_does_not_support));
		}

		List<String> strings = new ArrayList<String>(configEnv.size());
		Iterator<?> iter = configEnv.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			strings.add(key + "=" + value); //$NON-NLS-1$

		}
		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Get the absolute path of the executable to launch. If the executable is on a remote machine, this is the path to the
	 * executable on that machine.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getExecutablePath(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String) null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback#getLocalFileManager
	 * (org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public static IRemoteFileManager getLocalFileManager(ILaunchConfiguration configuration) throws CoreException {
		IRemoteServices localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		assert (localServices != null);
		IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
		assert (lconnMgr != null);
		IRemoteConnection lconn = lconnMgr.getConnection(IRemoteConnectionManager.DEFAULT_CONNECTION_NAME);
		assert (lconn != null);
		IRemoteFileManager localFileManager = localServices.getFileManager(lconn);
		assert (localFileManager != null);
		return localFileManager;
	}

	/**
	 * Convert application arguments to an array of strings.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return array of strings containing the program arguments
	 * @throws CoreException
	 */
	public static String[] getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String temp = getArguments(configuration);
		if (temp != null && temp.length() > 0) {
			ArgumentParser ap = new ArgumentParser(temp);
			List<String> args = ap.getTokenList();
			if (args != null) {
				return args.toArray(new String[args.size()]);
			}
		}
		return new String[0];
	}

	/**
	 * Get the name of the executable to launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
		String exePath = getExecutablePath(configuration);
		if (exePath != null) {
			return new Path(exePath).lastSegment();
		}
		return null;
	}

	/**
	 * Get the path component of the executable to launch.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 * @since 5.0
	 */
	public static String getProgramPath(ILaunchConfiguration configuration) throws CoreException {
		String exePath = getExecutablePath(configuration);
		if (exePath != null) {
			return new Path(exePath).removeLastSegments(1).toString();
		}
		return null;
	}

	/**
	 * Get the IProject object from the project name.
	 * 
	 * @param project
	 *            name of the project
	 * @return IProject resource
	 */
	public static IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}

	/**
	 * Get the name of the project
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
	}

	/**
	 * Get the default queue for the given resource manager
	 * 
	 * @param rm
	 *            resource manager
	 * @return default queue
	 * @since 5.0
	 */
	public static IPQueue getQueueDefault(IPResourceManager rm) {
		final IPQueue[] queues = rm.getQueues();
		if (queues.length == 0) {
			return null;
		}
		return queues[0];
	}

	/**
	 * @since 5.0
	 */
	public static IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		IResourceManager rm = getResourceManager(configuration);
		if (rm != null) {
			IResourceManagerComponentConfiguration conf = rm.getControlConfiguration();
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault()
					.getRemoteServices(conf.getRemoteServicesId(), monitor);
			if (remoteServices != null) {
				IRemoteConnectionManager rconnMgr = remoteServices.getConnectionManager();
				if (rconnMgr != null) {
					IRemoteConnection rconn = rconnMgr.getConnection(conf.getConnectionName());
					if (rconn != null && rconn.isOpen()) {
						return remoteServices.getFileManager(rconn);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Find the resource manager that corresponds to the unique name specified in the configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return resource manager
	 * @throws CoreException
	 * @since 5.0
	 */
	public static IResourceManager getResourceManager(ILaunchConfiguration configuration) {
		String rmUniqueName = getResourceManagerUniqueName(configuration);
		if (rmUniqueName != null) {
			return ModelManager.getInstance().getResourceManagerFromUniqueName(rmUniqueName);
		}
		return null;
	}

	/**
	 * Get the resource manager to use for the launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getResourceManagerUniqueName(ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String) null);
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * Returns the (possible empty) list of synchronization rule objects according to the rules described in the configuration.
	 * 
	 * @since 5.0
	 */
	public static ISynchronizationRule[] getSynchronizeRules(ILaunchConfiguration configuration) throws CoreException {
		List<?> ruleStrings = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES, new ArrayList<String>());
		List<ISynchronizationRule> result = new ArrayList<ISynchronizationRule>();

		for (Object ruleObj : ruleStrings) {
			String element = (String) ruleObj;
			try {
				ISynchronizationRule rule = RuleFactory.createRuleFromString(element);
				result.add(rule);
			} catch (RuntimeException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Error_converting_rules));
			}
		}

		return result.toArray(new ISynchronizationRule[result.size()]);
	}

	/**
	 * Get the working directory for the application launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 * @since 5.0
	 */
	public static String getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, (String) null);
	}

	/**
	 * Get the workspace root.
	 * 
	 * @return workspace root
	 */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Set the working directory
	 * 
	 * @param configuration
	 * @param dir
	 * @throws CoreException
	 * @since 5.0
	 */
	public static void setWorkingDirectory(ILaunchConfigurationWorkingCopy configuration, String dir) throws CoreException {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, dir);
	}

	/**
	 * Constructor
	 */
	public LaunchUtils() {
	}
}
