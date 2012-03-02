/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ServicesCorePlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class SyncManager  {
	// Static class - do not allow creating of instances
	private SyncManager() {
	}

	public static enum SYNC_MODE {
		ACTIVE, ALL, NONE
	};

	private static final IServiceModelManager serviceModel = ServiceModelManager.getInstance();
	private static final String DEFAULT_SAVE_FILE_NAME = "SyncManagerData.xml"; //$NON-NLS-1$
	private static final String SYNC_MANAGER_ELEMENT_NAME = "sync-manager-data"; //$NON-NLS-1$
	private static final String SYNC_MODE_ELEMENT_NAME = "project-to-sync-mode"; //$NON-NLS-1$
	private static final String SHOW_ERROR_ELEMENT_NAME = "project-to-show-error"; //$NON-NLS-1$
	private static final String DEFAULT_FILE_FILTER_ELEMENT_NAME = "default-file-filter"; //$NON-NLS-1$
	private static final String FILE_FILTER_ELEMENT_NAME = "project-to-file-filter"; //$NON-NLS-1$
	private static final String FILE_FILTER_INTERNAL_ELEMENT_NAME = "project-to-file-filter-internal"; //$NON-NLS-1$
	private static final String ATTR_PROJECT_NAME = "project"; //$NON-NLS-1$
	private static final String ATTR_SYNC_MODE = "sync-mode"; //$NON-NLS-1$
	private static final String ATTR_SHOW_ERROR = "show-error"; //$NON-NLS-1$
	private static final String ATTR_AUTO_SYNC = "auto-sync"; //$NON-NLS-1$

	private static boolean fSyncAuto = true;
	private static final Map<IProject, SYNC_MODE> fProjectToSyncModeMap = Collections
			.synchronizedMap(new HashMap<IProject, SYNC_MODE>());
	private static final Map<IProject, Boolean> fProjectToShowErrorMap = Collections
			.synchronizedMap(new HashMap<IProject, Boolean>());
	private static final Map<IProject, SyncFileFilter> fProjectToFileFilterMap = Collections
			.synchronizedMap(new HashMap<IProject, SyncFileFilter>());
	private static final Map<IProject, Set<IPath>> fProjectToResolvedFilesMap = Collections
			.synchronizedMap(new HashMap<IProject, Set<IPath>>());
	private static SyncFileFilter defaultFilter = SyncFileFilter.createBuiltInDefaultFilter();

	static {
		try {
			loadConfigurationData();
		} catch (WorkbenchException e) {
			handleInitError(e);
		} catch (IOException e) {
			handleInitError(e);
		}
	}
	
	private static void handleInitError(Throwable e) {
		RDTSyncCorePlugin.log(Messages.SyncManager_1, e);
	}

	private static class SynchronizeJob extends Job {
		private final IResourceDelta fDelta;
		private final IProject fProject;
		private final ISyncServiceProvider fSyncProvider;
		private final EnumSet<SyncFlag> fSyncFlags;
		private final boolean fResolveAsLocal;
		private final ISyncExceptionHandler fSyncExceptionHandler;

		public SynchronizeJob(IResourceDelta delta, IProject project, ISyncServiceProvider provider, EnumSet<SyncFlag> syncFlags,
				boolean resolveAsLocal, ISyncExceptionHandler seHandler) {
			super(Messages.SyncManager_4);
			fDelta = delta;
			fProject = project;
			fSyncProvider = provider;
			fSyncFlags = syncFlags;
			fResolveAsLocal = resolveAsLocal;
			fSyncExceptionHandler = seHandler;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SubMonitor progress = SubMonitor.convert(monitor, 100);
			try {
				if (!fResolveAsLocal) {
					fSyncProvider.synchronize(fDelta, getFileFilter(fProject), progress.newChild(100), fSyncFlags);
				} else {
					fSyncProvider.synchronizeResolveAsLocal(fDelta, getFileFilter(fProject), progress.newChild(100), fSyncFlags);
				}
			} catch (CoreException e) {
				if (fSyncExceptionHandler == null) {
					System.out.println(Messages.SyncManager_8 + e.getLocalizedMessage());
				} else {
					fSyncExceptionHandler.handle(fProject, e);
				}
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	};
	
	/**
	 * Return a copy of the project's file filter.
	 * Since only a copy is returned, users must execute "saveFileFilter(IProject, SyncFileFilter)" after making changes to have
	 * those changes actually applied.
	 *
	 * @param project cannot be null
	 * @return the file filter. This is never null.
	 */
	public static SyncFileFilter getFileFilter(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		
		if (!(fProjectToFileFilterMap.containsKey(project))) {
			fProjectToFileFilterMap.put(project, new SyncFileFilter(defaultFilter));
			try {
				saveConfigurationData();
			} catch (IOException e) {
				RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
			}
		}
		return new SyncFileFilter(fProjectToFileFilterMap.get(project));
	}
	
	/**
	 * Return the default file filter
	 * @return filter
	 */
	public static SyncFileFilter getDefaultFileFilter() {
		return new SyncFileFilter(defaultFilter);
	}

	/**
	 * Return project's current sync mode
	 * On first access, set sync mode to ACTIVE.
	 * 
	 * @param project cannot be null
	 * @return sync mode. This is never null.
	 */
	public static SYNC_MODE getSyncMode(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToSyncModeMap.containsKey(project))) {
			fProjectToSyncModeMap.put(project, SYNC_MODE.ACTIVE);
			try {
				saveConfigurationData();
			} catch (IOException e) {
				RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
			}
		}
		return fProjectToSyncModeMap.get(project);
	}

	/**
	 * Should sync'ing be done automatically?
	 * 
	 * @return if sync'ing should be done automatically
	 */
	public static boolean getSyncAuto() {
		return fSyncAuto;
	}
	
	/**
	 * Should error messages be displayed for the given project?
	 * 
	 * @param project
	 * @return whether error messages should be displayed.
	 */
	public static boolean getShowErrors(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToShowErrorMap.containsKey(project))) {
			fProjectToShowErrorMap.put(project, true);
			try {
				saveConfigurationData();
			} catch (IOException e) {
				RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
			}
		}
		return fProjectToShowErrorMap.get(project);
	}
	
	/**
	 * Is the given path for the given project resolved?
	 *
	 * @param project
	 * @param path
	 * @return whether path is resolved
	 */
	public static boolean getResolved(IProject project, IPath path) {
		if (project == null || path == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToResolvedFilesMap.containsKey(project))) {
			fProjectToResolvedFilesMap.put(project, new HashSet<IPath>());
			try {
				saveConfigurationData();
			} catch (IOException e) {
				RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
			}
		}
		return fProjectToResolvedFilesMap.get(project).contains(path);
	}

	/**
	 * Are the given paths for the given project resolved?
	 *
	 * @param project
	 * @param set of paths
	 * @return whether paths are resolved
	 */
	public static boolean getResolved(IProject project, Set<IPath> paths) {
		if (project == null || paths == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToResolvedFilesMap.containsKey(project))) {
			fProjectToResolvedFilesMap.put(project, new HashSet<IPath>());
			try {
				saveConfigurationData();
			} catch (IOException e) {
				RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
			}
		}
		return fProjectToResolvedFilesMap.get(project).containsAll(paths);
	}

	/**
	 * Set sync mode for project
	 * 
	 * @param project
	 * @param mode
	 */
	public static void setSyncMode(IProject project, SYNC_MODE mode) {
		fProjectToSyncModeMap.put(project, mode);
		try {
			saveConfigurationData();
		} catch (IOException e) {
			RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
		}
	}

	/**
	 * Turn automatic sync'ing on or off
	 * 
	 * @param isSyncAutomatic
	 */
	public static void setSyncAuto(boolean isSyncAutomatic) {
		fSyncAuto = isSyncAutomatic;
		try {
			saveConfigurationData();
		} catch (IOException e) {
			RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
		}
	}
	
	/**
	 * Set whether error messages should be displayed
	 *
	 * @param project
	 * @param shouldBeDisplayed
	 */
	public static void setShowErrors(IProject project, boolean shouldBeDisplayed) {
		fProjectToShowErrorMap.put(project, shouldBeDisplayed);
		try {
			saveConfigurationData();
		} catch (IOException e) {
			RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
		}
	}

	/**
	 * Save a new file filter for a project.
	 * Use this in conjunction with "getFileFilter(IProject)" to modify the current file filtering for a project.
	 *
	 * @param project cannot be null
	 * @param filter cannot be null
	 */
	public static void saveFileFilter(IProject project, SyncFileFilter filter) {
		if (project == null || filter == null) {
			throw new NullPointerException();
		}
		fProjectToFileFilterMap.put(project, filter);
		try {
			saveConfigurationData();
		} catch (IOException e) {
			RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
		}
	}
	
	/**
	 * Set the given path as resolved or unresolved for the given project
	 *
	 * @param project
	 * @param path
	 */
	public static void setResolved(IProject project, IPath path, boolean isResolved) {
		if (project == null || path == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToResolvedFilesMap.containsKey(project))) {
			fProjectToResolvedFilesMap.put(project, new HashSet<IPath>());
		}
		if (isResolved) {
			fProjectToResolvedFilesMap.get(project).add(path);
		} else {
			fProjectToResolvedFilesMap.get(project).remove(path);
		}
		try {
			saveConfigurationData();
		} catch (IOException e) {
			RDTSyncCorePlugin.log(Messages.SyncManager_2, e);
		}
	}
	
	/**
	 * Clear the list of resolved files for the given project.
	 *
	 * @param project
	 */
	public static void clearResolved(IProject project) {
		fProjectToResolvedFilesMap.put(project, new HashSet<IPath>());
	}
	
	/**
	 * Save a new default file filter.
	 * Use this in conjunction with "getDefaultFileFilter()" to modify the default filter.
	 * @param filter cannot be null
	 */
	public static void saveDefaultFileFilter(SyncFileFilter filter) {
		if (filter == null) {
			throw new NullPointerException();
		}
		defaultFilter = filter;
	}

	/**
	 * Invoke sync for active (default) configuration on a project
	 * 
	 * @param delta
	 *            project delta
	 * @param project
	 *            project to sync
	 * @param syncFlags
	 *            sync flags
	 * @param seHandler
	 * 			  logic to handle exceptions
	 * @return the scheduled sync job
	 * @throws CoreException 
	 */
	public static Job sync(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags, ISyncExceptionHandler seHandler)
			throws CoreException {
		return sync(delta, project, syncFlags, false, false, seHandler, null);
	}
	
	/**
	 * Invoke a "resolve as local" sync for active (default) configuration on a project
	 * Warning: This is a dangerous operation as it will destroy all remote differences. The UI should provide precautions
	 * to prevent users from invoking this type of sync without first understanding the implications.
	 *
	 * @param delta
	 *            project delta
	 * @param project
	 *            project to sync
	 * @param syncFlags
	 *            sync flags
	 * @param seHandler
	 * 			  logic to handle exceptions
	 * @return the scheduled sync job
	 * @throws CoreException 
	 */
	public static Job syncResolveAsLocal(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags,
			ISyncExceptionHandler seHandler) throws CoreException {
		return sync(delta, project, syncFlags, false, true, seHandler, null);
	}
	
	/**
	 * Invoke sync and block until sync finishes. This does not spawn another thread and no locking of resources is done.
	 * 
	 * @param delta
	 *            project delta
	 * @param project
	 *            project to sync
	 * @param syncFlags
	 *            sync flags
	 * @return the scheduled sync job
	 * @throws CoreException
	 * 			  on problems sync'ing
	 */
	public static Job syncBlocking(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags, IProgressMonitor monitor)
			throws CoreException {
		return sync(delta, project, syncFlags, true, false, null, monitor);
	}
	
	private static Job sync(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags, boolean isBlocking,
			boolean resolveAsLocal, ISyncExceptionHandler seHandler, IProgressMonitor monitor) throws CoreException {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		if (!(bcm.isInitialized(project))) {
			return null;
		}

		IConfiguration[] buildConfigurations = new IConfiguration[1];
		buildConfigurations[0] = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
		Job[] syncJobs = scheduleSyncJobs(delta, project, syncFlags, buildConfigurations, isBlocking, resolveAsLocal, seHandler,
				monitor);
		return syncJobs[0];
	}

	/**
	 * Invoke sync for all configurations on a project.
	 * Note that there is no syncAllBlocking, because it was not needed but would be easy to add.
	 * 
	 * @param delta
	 *            project delta
	 * @param project
	 *            project to sync
	 * @param syncFlags
	 *            sync flags
	 * @param seHandler
	 *			  logic to handle exceptions
	 * @return array of sync jobs scheduled
	 * @throws CoreException
	 * 			  on problems sync'ing
	 */
	public static Job[] syncAll(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags, ISyncExceptionHandler seHandler)
			throws CoreException {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		if (!(bcm.isInitialized(project))) {
			return new Job[0];
		}

		return scheduleSyncJobs(delta, project, syncFlags, ManagedBuildManager.getBuildInfo(project).getManagedProject()
				.getConfigurations(), false, false, seHandler, null);
	}

	// Note that the monitor is ignored for non-blocking jobs since SynchronizeJob creates its own monitor
	private static Job[] scheduleSyncJobs(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags,
			IConfiguration[] buildConfigurations, boolean isBlocking, boolean resolveAsLocal, ISyncExceptionHandler seHandler,
			IProgressMonitor monitor)
			throws CoreException {
		int jobNum = 0;
		Job[] syncJobs = new Job[buildConfigurations.length];
		for (IConfiguration buildConfig : buildConfigurations) {
			SynchronizeJob job = null;
			ISyncServiceProvider provider = (ISyncServiceProvider) SyncManager.getSyncProvider(buildConfig);
			if (provider != null) {
				if (isBlocking) {
					if (!resolveAsLocal) {
						provider.synchronize(delta, getFileFilter(project), monitor, syncFlags);
					} else {
						provider.synchronizeResolveAsLocal(delta, getFileFilter(project), monitor, syncFlags);
					}
				} else {
						job = new SynchronizeJob(delta, project, provider, syncFlags, resolveAsLocal, seHandler);
					job.schedule();
				}
			}

			// Each build configuration is matched with a job, which may be null if a job could not be created.
			syncJobs[jobNum] = job;
			jobNum++;
		}

		return syncJobs;
	}

	/**
	 * Save configuration data to plugin metadata area
	 * 
	 * @throws IOException
	 *             on problems writing configuration data to file
	 */
	public static synchronized void saveConfigurationData() throws IOException {
		XMLMemento rootMemento = XMLMemento.createWriteRoot(SYNC_MANAGER_ELEMENT_NAME);

		// Save project to sync mode map
		synchronized (fProjectToSyncModeMap) {
			for (IProject project : fProjectToSyncModeMap.keySet()) {
				IMemento modeMemento = rootMemento.createChild(SYNC_MODE_ELEMENT_NAME);
				modeMemento.putString(ATTR_PROJECT_NAME, project.getName());
				modeMemento.putString(ATTR_SYNC_MODE, fProjectToSyncModeMap.get(project).name());
			}
		}
		
		// Save project to "show error" map
		synchronized (fProjectToShowErrorMap) {
			for (IProject project : fProjectToShowErrorMap.keySet()) {
				IMemento showErrorMemento = rootMemento.createChild(SHOW_ERROR_ELEMENT_NAME);
				showErrorMemento.putString(ATTR_PROJECT_NAME, project.getName());
				showErrorMemento.putBoolean(ATTR_SHOW_ERROR, fProjectToShowErrorMap.get(project));
			}
		}
		
		// Save default filter
		IMemento defaultFileFilterMemento = rootMemento.createChild(DEFAULT_FILE_FILTER_ELEMENT_NAME);
		defaultFilter.saveFilter(defaultFileFilterMemento);
		
		// Save project to "file filter" map
		synchronized (fProjectToFileFilterMap) {
			for (IProject project : fProjectToFileFilterMap.keySet()) {
				SyncFileFilter filter = fProjectToFileFilterMap.get(project);
				IMemento fileFilterMemento = rootMemento.createChild(FILE_FILTER_ELEMENT_NAME);
				IMemento fileFilterInternalMemento = fileFilterMemento.createChild(FILE_FILTER_INTERNAL_ELEMENT_NAME);
				filter.saveFilter(fileFilterInternalMemento);
				fileFilterMemento.putString(ATTR_PROJECT_NAME, project.getName());
			}
		}
		
		// Save auto-sync setting
		rootMemento.putBoolean(ATTR_AUTO_SYNC, fSyncAuto);

		IPath savePath = ServicesCorePlugin.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
		File saveFile = savePath.toFile();
		rootMemento.save(new FileWriter(saveFile));
	}

	/**
	 * Load configuration data. All previously stored data is erased.
	 * 
	 * @throws IOException
	 */
	private static void loadConfigurationData() throws IOException, WorkbenchException {
		// Setup root memento
		IPath loadPath = ServicesCorePlugin.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
		File loadFile = loadPath.toFile();
		if (!(loadFile.exists())) {
			return;
		}

		BufferedReader reader = new BufferedReader(new FileReader(loadFile));
		XMLMemento rootMemento;
		try {
			rootMemento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			throw e;
		}

		// Load project sync modes
		fProjectToSyncModeMap.clear();
		for (IMemento modeMemento : rootMemento.getChildren(SYNC_MODE_ELEMENT_NAME)) {
			String projectName = modeMemento.getString(ATTR_PROJECT_NAME);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null) {
				throw new RuntimeException(Messages.SyncManager_0 + project);
			}
			String syncModeString = modeMemento.getString(ATTR_SYNC_MODE);
			SYNC_MODE syncMode = SYNC_MODE.valueOf(syncModeString);
			fProjectToSyncModeMap.put(project, syncMode);
		}
		
		// Load project "show error" settings
		fProjectToShowErrorMap.clear();
		for (IMemento showErrorMemento : rootMemento.getChildren(SHOW_ERROR_ELEMENT_NAME)) {
			String projectName = showErrorMemento.getString(ATTR_PROJECT_NAME);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null) {
				throw new RuntimeException(Messages.SyncManager_0 + project);
			}
			fProjectToShowErrorMap.put(project, showErrorMemento.getBoolean(ATTR_SHOW_ERROR));
		}
		
		// Load default file filter
		IMemento defaultFileFilterMemento = rootMemento.getChild(DEFAULT_FILE_FILTER_ELEMENT_NAME);
		if (defaultFileFilterMemento != null) {
			defaultFilter = SyncFileFilter.loadFilter(defaultFileFilterMemento);
		}
		
		// Load project "file filter" settings
		fProjectToFileFilterMap.clear();
		for (IMemento fileFilterMemento : rootMemento.getChildren(FILE_FILTER_ELEMENT_NAME)) {
			String projectName = fileFilterMemento.getString(ATTR_PROJECT_NAME);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null) {
				throw new RuntimeException(Messages.SyncManager_0 + project);
			}
			IMemento fileFilterInternalMemento = fileFilterMemento.getChild(FILE_FILTER_INTERNAL_ELEMENT_NAME);
			SyncFileFilter filter = SyncFileFilter.loadFilter(fileFilterInternalMemento);
			fProjectToFileFilterMap.put(project, filter);
		}
		
		// Load auto-sync setting
		fSyncAuto = rootMemento.getBoolean(ATTR_AUTO_SYNC);
	}
	
	/**
	 * Get the sync service provider for the given project's current active configuration. 
	 *
	 * @param project
	 * @return sync service provider or null if provider cannot be found. (Logs error message in that case.)
	 */
	public static ISyncServiceProvider getSyncProvider(IProject project) {
		IConfiguration config = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
		return SyncManager.getSyncProvider(config);
	}
	
	/**
	 * Get the sync service provider for the given build configuration.
	 *
	 * @param config
	 * @return sync service provider or null if provider cannot be found. (Logs error message in that case.)
	 */
	public static ISyncServiceProvider getSyncProvider(IConfiguration config) {
		ISyncServiceProvider provider = null;
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		IServiceConfiguration serviceConfig = bcm.getConfigurationForBuildConfiguration(config);
		if (serviceConfig != null) {
			IServiceModelManager serviceModel = ServiceModelManager.getInstance();
			IService syncService = serviceModel.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
			provider = (ISyncServiceProvider) serviceConfig.getServiceProvider(syncService);
		}
		
		if (provider == null) {
			RDTSyncCorePlugin.log(Messages.SyncManager_7 + config.getName());
		}
		return provider;
	}
}
