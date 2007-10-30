/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.data.ISynchronizationRule;
import org.eclipse.ptp.remotetools.environment.launcher.data.RuleFactory;
import org.eclipse.ptp.remotetools.environment.launcher.internal.integration.NullLaunchObserver;
import org.eclipse.ptp.remotetools.environment.launcher.internal.macros.LaunchVariableManager;
import org.eclipse.ptp.remotetools.environment.launcher.macros.ILaunchVariableContextInfo;
import org.eclipse.ptp.remotetools.environment.launcher.preferences.LaunchPreferences;

/**
 * Implementation of utility methods to retrieve information from a remote launch configuration.
 * @author Daniel Felix Ferber
 */
public abstract class RemoteLaunchDelegate extends
		AbstractCLaunchDelegate {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#getPluginID()
	 */
	public String getPluginID() {
		return RemoteLauncherPlugin.getUniqueIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#finalLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return super.finalLaunchCheck(configuration, mode, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return super.preLaunchCheck(configuration, mode, monitor);
	}
	
	/**
	 * Returns the <b>local</b> working directory of the launch
	 * configuration. 
	 * 
	 * If the local working directory is not specified in the launch configuration,
	 * the the project directory is assumed as fallback.
	 * 
	 * If the directory is not absolute, then it is interpreted as a directory
	 * relative to the workspace.
	 * 
	 * Returns <code>null</code> if no fall back information is available.
	 * 
	 * @param configuration
	 *            launch configuration.
	 * @return an absolute path to a directory in the remote file system, or
	 *         <code>null</code> if not available.
	 * @throws CoreException
	 *             On failre to retrieve the attributes
	 *             
	 * @deprecated This property was removed from the launcher tab.
	 */
	protected IPath getLocalDirectoryWithFallback(ILaunchConfiguration configuration) throws CoreException {
		// ok
		String location = configuration.getAttribute(IRemoteLaunchAttributes.ATTR_LOCAL_DIRECTORY, (String) null);
		if (location != null || location.length() == 0) {
			// Fallback to user project directory
			ICProject cp = getCProject(configuration);
			if (cp != null) {
				IProject p = cp.getProject();
				return p.getLocation();
			}
			return null;
		}
		
		IPath path = new Path(location);
		if (! path.isAbsolute()) {
			IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
			return folder.getLocation();
		}
		
		return new Path(location);
	}

	/**
	 * Returns the verified <b>local</b> working directory specified by the
	 * given launch configuration.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the <b>local</b> working directory specified by the given
	 *         launch configuration
	 * @exception CoreException
	 *                <ul>
	 *                <li>On failure to retrieve the attribute.
	 *                <li>If not absolute.
	 *                <li>If the directory does not exist on the local file system.
	 *                <li>If not a directory on the local file system.
	 *                </ul>
	 * @deprecated
	 */
	public File getValidatedLocalDirectory(ILaunchConfiguration configuration)
			throws CoreException {
		// ok
		
		IPath path = getLocalDirectoryWithFallback(configuration);
		if (path == null) {
			abort(Messages.RemoteLaunchDelegate_LocalDirectory_Missing, null, 0);
		} 

		if (! path.isAbsolute()) {
			abort(Messages.RemoteLaunchDelegate_LocalDirectory_MustBeAbsolute, null, 0);
		} 
			
		File directory = path.toFile();
		if (! directory.exists()) {
			abort(Messages.RemoteLaunchDelegate_LocalDirectory_DoesNotExist, null, 0);
		}
		if (! directory.isDirectory()) {
			abort(Messages.RemoteLaunchDelegate_LocalDirectory_IsNotADirectory, null, 0);
		}

		return directory;
	}

	/**
	 * Returns the <b>local</b> working directory attribute of the given launch
	 * configuration. Returns <code>null</code> if a working directory is not
	 * specified.
	 * 
	 * @param configuration
	 *            launch configuration.
	 * @return an absolute path to a directory in the remote file system, or
	 *         <code>null</code> if unspecified.
	 * @throws CoreException
	 *             On failre to retrieve the attributes
	 *             
	 * @deprecated This property was removed from the launcher tab.
	 */
	public IPath getLocalDirectory(ILaunchConfiguration config)
			throws CoreException {
		// ok
		return getLocalDirectoryWithFallback(config);
	}
	
	/**
	 * Returns the <b>remote</b> working directory of the launch
	 * configuration.
	 * No macros are expanded. To expand macros, use {@link #getValidatedRemoteDirectory(ILaunchConfiguration)}.
	 * 
	 * @param configuration
	 *            The launch configuration.
	 * @return path to a directory in the remote file system, or
	 *         <code>null</code> if unspecified.
	 * @throws CoreException
	 *             On failure the get the attribute.
	 */
	public IPath getRemoteDirectory(ILaunchConfiguration configuration) throws CoreException {
		// ok
		String location = null;
		if (configuration.getAttribute(IRemoteLaunchAttributes.ATTR_AUTOMATIC_WORKING_DIRECTORY, false)) {
			location = LaunchPreferences.getPreferenceStore().getString(LaunchPreferences.ATTR_WORKING_DIRECTORY);
		} else {
			location = configuration.getAttribute(IRemoteLaunchAttributes.ATTR_REMOTE_DIRECTORY, (String)null);
		}
		
		if (location == null) {
			return null;
		}
		
		try {
			location = LaunchVariableManager.getDefault().resolveValue(location, EMPTY_STRING, EMPTY_STRING, ILaunchVariableContextInfo.CONTEXT_LAUNCH, configuration);
		} catch (CoreException e) {
			return null;
		}
		
		if (location == null) {
			return null;
		}
		return LinuxPath.fromString(location);
	}
	
	/**
	 * Returns the <b>remote</b> working directory of the launch
	 * configuration.
	 *
	 * 
	 * @param configuration
	 *            The launch configuration.
	 * @return path to a directory in the remote file system, or
	 *         <code>null</code> if unspecified.
	 * @throws CoreException 
	 * @throws CoreException
	 * <ul>
	 * <li>On failure the get the attribute.
	 * <li>On failure to resolve macros
	 * <li>If the path is not absolure.
	 * </ul>
	 */
	public IPath getValidatedRemoteDirectory(ILaunchConfiguration configuration) throws CoreException {
		// ok
		String location = null;
		if (configuration.getAttribute(IRemoteLaunchAttributes.ATTR_AUTOMATIC_WORKING_DIRECTORY, false)) {
			location = LaunchPreferences.getPreferenceStore().getString(LaunchPreferences.ATTR_WORKING_DIRECTORY);
			if (location == null || location.length() == 0) {
				abort(Messages.RemoteLaunchDelegate_RemoteDirectory_MissingInPreferences, null, 0);
			}
		} else {
			location = configuration.getAttribute(IRemoteLaunchAttributes.ATTR_REMOTE_DIRECTORY, (String)null);
			if (location == null || location.length() == 0) {
				abort(Messages.RemoteLaunchDelegate_RemoteDirectory_Missing, null, 0);
			}
		}
		try {
			location = LaunchVariableManager.getDefault().resolveValue(location, EMPTY_STRING, EMPTY_STRING, ILaunchVariableContextInfo.CONTEXT_LAUNCH, configuration);
		} catch (CoreException e) {
			abort(NLS.bind(Messages.RemoteLaunchDelegate_RemoteDirectory_MacroFailed, e.getMessage()), e, 0);
		}
		if (location == null) {
			abort(Messages.RemoteLaunchDelegate_RemoteDirectory_MacroFailed_Unknown, null, 0);
		}
		IPath path = LinuxPath.fromString(location);
		if (! path.isAbsolute()) {
			abort(Messages.RemoteLaunchDelegate_RemoteDirectory_MustBeAbsolute, null, 0);
		}
		return path;
	}

	/**
	 * Return whether synchronization before launch is activated in the launch configuration.
	 * 
	 * @param configuration The launch configuration
	 * @return True/False.
	 * 
	 * @throws CoreException On failure the get the attribute.
	 */
	public boolean getSynchronizeBefore(ILaunchConfiguration configuration) throws CoreException {
		// ok
		return configuration.getAttribute(IRemoteLaunchAttributes.ATTR_SYNC_BEFORE, IRemoteLaunchAttributes.DEFAULT_SYNC_BEFORE);
	}

	/**
	 * Return whether synchronization after launch is activated in the launch configuration.
	 * 
	 * @param configuration The launch configuration
	 * @return True/False.
	 * 
	 * @throws CoreException On failure the get the attribute.
	 */
	public boolean getSynchronizeAfter(ILaunchConfiguration configuration) throws CoreException {
		// ok
		return configuration.getAttribute(IRemoteLaunchAttributes.ATTR_SYNC_AFTER, IRemoteLaunchAttributes.DEFAULT_SYNC_AFTER);
	}

	/**
	 * Return whether X11 forwarding is activated in the launch configuration.
	 * 
	 * @param configuration The launch configuration
	 * @return True/False.
	 * 
	 * @throws CoreException On failure the get the attribute.
	 */
	public boolean getUseForwardedX11(ILaunchConfiguration configuration) throws CoreException {
		// ok
		return configuration.getAttribute(IRemoteLaunchAttributes.ATTR_USE_FORWARDED_X11, IRemoteLaunchAttributes.DEFAULT_USE_FORWARDED_X11);
	}	

	/**
	 * Return the observer plug-in that will parse output of the launch configuration.
	 * 
	 * @param configuration
	 *            The launch configuration.
	 * @return The observer plug-in or a void observer if no was selected.
	 * @throws CoreException
	 *             If the given observer does not exist.  On failure the get the attribute.
	 */
	public ILaunchObserver getOutputObserver(
			ILaunchConfiguration configuration) throws CoreException {
		// ok
		String id = configuration.getAttribute(IRemoteLaunchAttributes.ATTR_OUTPUT_OBSERVER, (String)null);
		if (id == null) {
			return new NullLaunchObserver();
		}
		
		ILaunchObserver launchObserver = RemoteLauncherPlugin.getLaunchObserverByID(id);
		if (launchObserver == null) {
			return new NullLaunchObserver();
		}
		return launchObserver;
	}

// not needed anymore.
//	protected static void throwsCoreException(String message, Throwable exception, int code) throws CoreException {
//		MultiStatus status = new MultiStatus(Activator.getUniqueIdentifier(), code, message, exception);
//		status.add(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), code, exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
//				exception));
//		throw new CoreException(status);
//	}
	
	public String getBeforeCommand(ILaunchConfiguration configuration) {
		// ok
		try {
			return configuration.getAttribute(IRemoteLaunchAttributes.ATTR_BEFORE_COMMAND, (String)null);
		} catch (CoreException e) {
			return null;
		}
	}

	public String getAfterCommand(ILaunchConfiguration configuration) {
		// ok
		try {
			return configuration.getAttribute(IRemoteLaunchAttributes.ATTR_AFTER_COMMAND, (String)null);
		} catch (CoreException e) {
			return null;
		}
	}
	
//	not used anymore
//	protected String getRemoteRelativePath(ILaunchConfiguration configuration, String path) throws CoreException {
//		IPath base = getRemoteDirectory(configuration);
//		if (path == null) {
//			return base.toString();
//		} else if (isRemoteAbsolutePath(path)) {
//			return path;
//		} else {
//			if (base == null) {
//				abort(
//						"Synchronization of remote files with relative paths are is not allowed without specifying remote working directory",
//						new FileNotFoundException(base.toOSString()),
//						ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST
//				);
//			}
//			return  concatenateRemotePath(LinuxPath.toString(base), path);
//		}
//	}
	
//	protected String getLocalRelativePath(ILaunchConfiguration configuration, String path) throws CoreException {
//		if (path == null) {
//			return null;
//		} else {
//			return getLocalRelativePath(configuration, new Path(path));
//		}
//	}
		
//	protected String getLocalRelativePath(ILaunchConfiguration configuration, IPath path) throws CoreException {
//		if (path == null) {
//			return null;
//		} else if (path.isAbsolute()) {
//			return path.toOSString();
//		} else {
//			IPath base = getLocalDirectory(configuration);
//			if (base == null) {
//				abort(
//						"Synchronization of local files with relative paths are is not allowed without specifying local working directory or project",
//						new FileNotFoundException(base.toString()),
//						ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST
//				);
//			}
//			IPath basePath = new Path(base.toString());
//			return basePath.append(path).toOSString();
//		}
//	}


	protected boolean getSynchronizeCleanup(ILaunchConfiguration configuration) {
		boolean result;
		try {
			result = configuration.getAttribute(IRemoteLaunchAttributes.ATTR_SYNC_CLEANUP, IRemoteLaunchAttributes.DEFAULT_SYNC_CLEANUP);
			return result;
		} catch (CoreException e) {
			return false;
		}
	}
	
	protected static boolean getAllocateTerminal(ILaunchConfiguration configuration) {
		boolean result;
		try {
			result = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			return result;
		} catch (CoreException e) {
			return false;
		}
	}
	
//	private String concatenateRemotePath(String sp1, String sp2) {
//		IPath p = new Path(sp1);
//		p = p.append(sp2);
//
//		return p.toOSString();
//	}
	
//	private String parentOfRemotePath(String spath) {
//		IPath p = new Path(spath);
//		p = p.removeLastSegments(1);
//		return p.toOSString();
//	}
//	
//	private String suffixOfRemotePath(String path) {
//		if (isRemoteDirectoryPath(path)) {
//			return null;
//		} else {
//			int index = path.lastIndexOf('/');
//			if (index == -1) return null;
//			return removeTrailingSlash(path.substring(index));
//		}		
//	}
//	
//	private String addTrailingSlash(String path) {
//		if (path.endsWith("/")) {
//			return path;
//		} else {
//			return path + "/";
//		}
//	}

//	private String removeTrailingSlash(String path) {
//		if (path.endsWith("/")) {
//			return path.substring(0, path.length() - 1);
//		} else {
//			return path;
//		}
//	}
//
//	private boolean isRemoteDirectoryPath(String path) {
//		return path.endsWith("/");
//	}
//	
//	private boolean isRemoteAbsolutePath(String path) {
//		return path.startsWith("/");
//	}

//	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
//		
//	}
	/**
	 * Returns the (possible empty) list of synchronization rule objects according to the rules described in the configuration.
	 */
	protected ISynchronizationRule[] getSynchronizeRules(ILaunchConfiguration configuration) throws CoreException {
		List ruleStrings = configuration.getAttribute(IRemoteLaunchAttributes.ATTR_SYNC_RULES, new ArrayList());
		List result = new ArrayList();
		
		for (Iterator iter = ruleStrings.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			try {
				ISynchronizationRule rule = RuleFactory.createRuleFromString(element);
				result.add(rule);
			} catch (RuntimeException e) {
				abort(Messages.RemoteLaunchDelegate_SynchronizationRules_InternalError, e, IRemoteLaunchErrors.ERROR_PARSING_RULE);
			}
		}
		
		return (ISynchronizationRule[]) result.toArray(new ISynchronizationRule[result.size()]);
	}
	
}
