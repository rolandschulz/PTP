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
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ServicesCorePlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

public class SyncManager extends AbstractHandler implements IElementUpdater {
	public static enum SYNC_MODE {
		ACTIVE, ALL, NONE
	};

	private static final IServiceModelManager serviceModel = ServiceModelManager.getInstance();
	private static final IService syncService = serviceModel.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
	private static final String SYNC_COMMAND_PARAMETER_ID = "org.eclipse.ptp.rdt.sync.core.syncCommand.syncModeParameter"; //$NON-NLS-1$
	private static final String DEFAULT_SAVE_FILE_NAME = "SyncManagerData.xml"; //$NON-NLS-1$
	private static final String SYNC_MANAGER_ELEMENT_NAME = "sync-manager-data"; //$NON-NLS-1$
	private static final String SYNC_MODE_ELEMENT_NAME = "project-to-sync-mode"; //$NON-NLS-1$
	private static final String ATTR_PROJECT_NAME = "project"; //$NON-NLS-1$
	private static final String ATTR_SYNC_MODE = "sync-mode"; //$NON-NLS-1$
	private static final String ATTR_AUTO_SYNC = "auto-sync"; //$NON-NLS-1$

	private static boolean fSyncAuto = true;
	private static final Map<IProject, SYNC_MODE> fProjectToSyncModeMap = Collections
			.synchronizedMap(new HashMap<IProject, SYNC_MODE>());

	private static final String syncActiveCommand = "sync_active"; //$NON-NLS-1$
	private static final String syncAllCommand = "sync_all"; //$NON-NLS-1$
	private static final String setNoneCommand = "set_none"; //$NON-NLS-1$
	private static final String setActiveCommand = "set_active"; //$NON-NLS-1$
	private static final String setAllCommand = "set_all"; //$NON-NLS-1$
	private static final String syncAutoCommand = "sync_auto"; //$NON-NLS-1$
	
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
		private final ISyncServiceProvider fSyncProvider;
		private final IResourceDelta fDelta;
		private final EnumSet<SyncFlag> fSyncFlags;

		public SynchronizeJob(IResourceDelta delta, ISyncServiceProvider provider, EnumSet<SyncFlag> syncFlags) {
			super(Messages.SyncManager_4);
			fDelta = delta;
			fSyncProvider = provider;
			fSyncFlags = syncFlags;
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
				fSyncProvider.synchronize(fDelta, progress.newChild(100), fSyncFlags);
			} catch (CoreException e) {
				System.out.println(Messages.SyncManager_8 + e.getLocalizedMessage());
				e.printStackTrace();
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	};

	// Simple rule to prevent sync jobs from running concurrently
	private static class SyncMutex implements ISchedulingRule {
		// Enforce as a singleton
		private static SyncMutex instance = null;

		private SyncMutex() {
		}

		public static SyncMutex getInstance() {
			if (instance == null) {
				instance = new SyncMutex();
			}
			return instance;
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	}

	/**
	 * Return project's current sync mode
	 * On first access, set sync mode to ACTIVE.
	 * 
	 * @param project
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

	public void addHandlerListener(IHandlerListener handlerListener) {
		// Listeners not yet supported
	}

	public void dispose() {
		// Nothing to do
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String command = event.getParameter(SYNC_COMMAND_PARAMETER_ID);
		IProject project = getProject();
		if (project == null) {
			RDTSyncCorePlugin.log(Messages.SyncManager_5);
			return null;
		}

		// On sync request, sync regardless of the flags
		try {
			if (command.equals(syncActiveCommand)) {
				sync(null, project, SyncFlag.FORCE);
			} else if (command.equals(syncAllCommand)) {
				syncAll(null, project, SyncFlag.FORCE);
				// If user switches to active or all, assume the user wants to sync right away
			} else if (command.equals(setActiveCommand)) {
				setSyncMode(project, SYNC_MODE.ACTIVE);
				sync(null, project, SyncFlag.FORCE);
			} else if (command.equals(setAllCommand)) {
				setSyncMode(project, SYNC_MODE.ALL);
				syncAll(null, project, SyncFlag.FORCE);
			} else if (command.equals(setNoneCommand)) {
				setSyncMode(project, SYNC_MODE.NONE);
			} else if (command.equals(syncAutoCommand)) {
				setSyncAuto(!(getSyncAuto()));
				// If user switches to automatic sync'ing, go ahead and sync based on current setting for project
				if (getSyncAuto()) {
					SYNC_MODE syncMode = getSyncMode(project);
					if (syncMode == SYNC_MODE.ACTIVE) {
						sync(null, project, SyncFlag.FORCE);
					} else if (syncMode == SYNC_MODE.ALL) {
						syncAll(null, project, SyncFlag.FORCE);
					}
				}
			}
		} catch (CoreException e) {
			// This should never happen because only a blocking sync can throw a core exception, and all syncs here are non-blocking.
			RDTSyncCorePlugin.log(Messages.SyncManager_3);
		}

		ICommandService service = (ICommandService) HandlerUtil.getActiveWorkbenchWindowChecked(event).getService(
				ICommandService.class);
		service.refreshElements(event.getCommand().getId(), null);

		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// Listeners not yet supported
	}

	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		String command = (String) parameters.get(SYNC_COMMAND_PARAMETER_ID);
		if (command == null) {
			RDTSyncCorePlugin.log(Messages.SyncManager_6);
			return;
		}

		IProject project = this.getProject();
		if (project == null) {
			RDTSyncCorePlugin.log(Messages.SyncManager_5);
			return;
		}

		SYNC_MODE syncMode = getSyncMode(project);
		if ((command.equals(setActiveCommand) && syncMode == SYNC_MODE.ACTIVE) ||
				(command.equals(setAllCommand) && syncMode == SYNC_MODE.ALL) ||
				(command.equals(setNoneCommand) && syncMode == SYNC_MODE.NONE) ||
				(command.equals(syncAutoCommand) && fSyncAuto)) {
			element.setChecked(true);
		} else {
			element.setChecked(false);
		}
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
	 * @return the scheduled sync job
	 * @throws CoreException 
	 */
	public static Job sync(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags) throws CoreException {
		return sync(delta, project, syncFlags, false, null);
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
		return sync(delta, project, syncFlags, true, monitor);
	}
	
	private static Job sync(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags, boolean isBlocking,
			IProgressMonitor monitor) throws CoreException {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		if (!(bcm.isInitialized(project))) {
			return null;
		}

		IConfiguration[] buildConfigurations = new IConfiguration[1];
		buildConfigurations[0] = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
		Job[] syncJobs = scheduleSyncJobs(delta, project, syncFlags, buildConfigurations, isBlocking, monitor);
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
	 * 
	 * @return array of sync jobs scheduled
	 * @throws CoreException
	 * 			  on problems sync'ing
	 */
	public static Job[] syncAll(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags) throws CoreException {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		if (!(bcm.isInitialized(project))) {
			return new Job[0];
		}

		return scheduleSyncJobs(delta, project, syncFlags, ManagedBuildManager.getBuildInfo(project).getManagedProject()
				.getConfigurations(), false, null);
	}

	// Note that the monitor is ignored for non-blocking jobs since SynchronizeJob creates its own monitor
	private static Job[] scheduleSyncJobs(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags,
			IConfiguration[] buildConfigurations, boolean isBlocking, IProgressMonitor monitor) throws CoreException {
		int jobNum = 0;
		Job[] syncJobs = new Job[buildConfigurations.length];
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		ISchedulingRule syncRule = createSyncRule(project);
		for (IConfiguration buildConfig : buildConfigurations) {
			SynchronizeJob job = null;
			IServiceConfiguration serviceConfig = bcm.getConfigurationForBuildConfiguration(buildConfig);
			if (serviceConfig != null) {
				ISyncServiceProvider provider = (ISyncServiceProvider) serviceConfig.getServiceProvider(syncService);
				if (provider != null) {
					if (isBlocking) {
						provider.synchronize(delta, monitor, syncFlags);
					} else {
						job = new SynchronizeJob(delta, provider, syncFlags);
						job.setRule(syncRule);
						job.schedule();
					}
				}
			} else {
				RDTSyncCorePlugin.log(Messages.SyncManager_7 + buildConfig.getName());
			}

			// Each build configuration is matched with a job, which may be null if a job could not be created.
			syncJobs[jobNum] = job;
			jobNum++;
		}

		return syncJobs;
	}

	// Create the scheduling rule for a sync job. This is a multirule that combines two other rules:
	// 1) SyncMutex to ensure that two sync jobs do not run concurrently
	// 2) The project itself since sync'ing can cause changes to any number of project files
	private static ISchedulingRule createSyncRule(IProject project) {
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		ISchedulingRule projectRule = ruleFactory.createRule(project);
		return MultiRule.combine(SyncMutex.getInstance(), projectRule);
	}

	/*
	 * Portions copied from org.eclipse.ptp.services.ui.wizards.setDefaultFromSelection
	 */
	private IProject getProject() {
		IWorkbenchWindow wnd = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage pg = wnd.getActivePage();
		ISelection sel = pg.getSelection();

		if (!(sel instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection selection = (IStructuredSelection) sel;

		Object firstElement = selection.getFirstElement();
		if (!(firstElement instanceof IAdaptable)) {
			return null;
		}
		Object o = ((IAdaptable) firstElement).getAdapter(IResource.class);
		if (o == null) {
			return null;
		}
		IResource resource = (IResource) o;

		return resource.getProject();
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
			SYNC_MODE syncMode = SYNC_MODE.ACTIVE;
			syncMode = SYNC_MODE.valueOf(syncModeString);
			fProjectToSyncModeMap.put(project, syncMode);
		}
		
		// Load auto-sync setting
		fSyncAuto = rootMemento.getBoolean(ATTR_AUTO_SYNC);
	}
}
