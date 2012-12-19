package org.eclipse.ptp.core.util;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.utils.core.ArgumentParser;

/**
 * @since 6.0
 * 
 */
public class LaunchUtils {

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
	 * @since 6.0
	 */
	public static String getConnectionName(ILaunchConfiguration configuration) {
		final String type;
		try {
			type = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONNECTION_NAME, (String) null);
		} catch (CoreException e) {
			return null;
		}
		return type;
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
	 * @since 6.0
	 */
	public static String getRemoteServicesId(ILaunchConfiguration configuration) {
		final String type;
		try {
			type = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_REMOTE_SERVICES_ID, (String) null);
		} catch (CoreException e) {
			return null;
		}
		return type;
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
	 * Given a launch configuration, find the system type that was been selected.
	 * 
	 * @param configuration
	 * @return resource manager type
	 * @throws CoreException
	 * @since 6.0
	 */
	public static String getSystemType(ILaunchConfiguration configuration) {
		final String type;
		try {
			type = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYSTEM_TYPE, (String) null);
		} catch (CoreException e) {
			return null;
		}
		return type;
	}

	/**
	 * Given a launch configuration, find the template name that was been selected.
	 * 
	 * @param configuration
	 * @return resource manager type
	 * @throws CoreException
	 * @since 6.0
	 */
	public static String getTemplateName(ILaunchConfiguration configuration) {
		final String type;
		try {
			type = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONFIGURATION_NAME, (String) null);
		} catch (CoreException e) {
			return null;
		}
		return type;
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
	 * @since 6.0
	 */
	public static void setConnectionName(ILaunchConfigurationWorkingCopy configuration, String name) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_CONNECTION_NAME, name);
	}

	/**
	 * @since 6.0
	 */
	public static void setRemoteServicesId(ILaunchConfigurationWorkingCopy configuration, String id) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_REMOTE_SERVICES_ID, id);
	}

	/**
	 * @since 6.0
	 */
	public static void setResourceManagerUniqueName(ILaunchConfigurationWorkingCopy configuration, String name) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, name);
	}

	/**
	 * @since 6.0
	 */
	public static void setSystemType(ILaunchConfigurationWorkingCopy configuration, String type) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_SYSTEM_TYPE, type);
	}

	/**
	 * @since 6.0
	 */
	public static void setConfigurationName(ILaunchConfigurationWorkingCopy configuration, String name) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_CONFIGURATION_NAME, name);
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
