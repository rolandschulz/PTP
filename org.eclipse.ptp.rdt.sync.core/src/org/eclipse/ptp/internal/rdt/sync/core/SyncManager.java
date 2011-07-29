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
package org.eclipse.ptp.internal.rdt.sync.core;

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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
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
	private static final String SYNC_COMMAND_PARAMETER_ID = "org.eclipse.ptp.internal.rdt.sync.core.syncCommand.syncModeParameter"; //$NON-NLS-1$
	private boolean fSyncAuto = true;

	private final Map<IProject, SYNC_MODE> fProjectToSyncModeMap = Collections.synchronizedMap(new HashMap<IProject, SYNC_MODE>());

	private final static String syncActiveCommand = "sync_active"; //$NON-NLS-1$
	private final static String syncAllCommand = "sync_all"; //$NON-NLS-1$
	private final static String setNoneCommand = "set_none"; //$NON-NLS-1$
	private final static String setActiveCommand = "set_active"; //$NON-NLS-1$
	private final static String setAllCommand = "set_all"; //$NON-NLS-1$
	private final static String syncAutoCommand = "sync_auto"; //$NON-NLS-1$

	// Enforce as a singleton class
	private static SyncManager fInstance = null;

	public static synchronized SyncManager getInstance() {
		if (fInstance == null) {
			fInstance = new SyncManager();
		}
		return fInstance;
	}

	private static class SynchronizeJob extends Job {
		private final ISyncServiceProvider fSyncProvider;
		private final IResourceDelta fDelta;
		private final EnumSet<SyncFlag> fSyncFlags;

		public SynchronizeJob(IResourceDelta delta, ISyncServiceProvider provider, EnumSet<SyncFlag> syncFlags) {
			super(Messages.SyncConfigurationManager_2);
			fDelta = delta;
			fSyncProvider = provider;
			fSyncFlags = syncFlags;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
		 * IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SubMonitor progress = SubMonitor.convert(monitor, 100);
			try {
				fSyncProvider.synchronize(fDelta, progress.newChild(100), fSyncFlags);
			} catch (CoreException e) {
				System.out.println("sync failed: " + e.getLocalizedMessage()); //$NON-NLS-1$
				e.printStackTrace();
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	};

	/**
	 * Return project's current sync mode
	 * On first access, set sync mode to ACTIVE.
	 * 
	 * @param project
	 * @return sync mode. This is never null.
	 */
	public SYNC_MODE getSyncMode(IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		if (!(fProjectToSyncModeMap.containsKey(project))) {
			fProjectToSyncModeMap.put(project, SYNC_MODE.ACTIVE);
		}
		return fProjectToSyncModeMap.get(project);
	}

	/**
	 * Should sync'ing be done automatically?
	 * 
	 * @return if sync'ing should be done automatically
	 */
	public boolean getSyncAuto() {
		return fSyncAuto;
	}

	/**
	 * Set sync mode for project
	 * 
	 * @param project
	 * @param mode
	 */
	public void setSyncMode(IProject project, SYNC_MODE mode) {
		fProjectToSyncModeMap.put(project, mode);
	}

	/**
	 * Turn automatic sync'ing on or off
	 * 
	 * @param isSyncAutomatic
	 */
	public void setSyncAuto(boolean isSyncAutomatic) {
		fSyncAuto = isSyncAutomatic;
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
			RDTSyncCorePlugin.log(Messages.SyncConfigurationManager_3);
			return null;
		}

		if (command.equals(syncActiveCommand)) {
			this.sync(null, project, SyncFlag.FORCE);
		} else if (command.equals(syncAllCommand)) {
			this.syncAll(null, project, SyncFlag.FORCE);
		} else if (command.equals(setActiveCommand)) {
			this.setSyncMode(project, SYNC_MODE.ACTIVE);
		} else if (command.equals(setAllCommand)) {
			this.setSyncMode(project, SYNC_MODE.ALL);
		} else if (command.equals(setNoneCommand)) {
			this.setSyncMode(project, SYNC_MODE.NONE);
		} else if (command.equals(syncAutoCommand)) {
			this.setSyncAuto(!(this.getSyncAuto()));
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
			RDTSyncCorePlugin.log(Messages.SyncConfigurationManager_0);
			return;
		}

		IProject project = this.getProject();
		if (project == null) {
			RDTSyncCorePlugin.log(Messages.SyncConfigurationManager_3);
			return;
		}

		SYNC_MODE syncMode = this.getSyncMode(project);
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
	 */
	public void sync(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags) {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		if (!(bcm.isInitialized(project))) {
			return;
		}

		IConfiguration[] buildConfigurations = new IConfiguration[1];
		buildConfigurations[0] = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
		this.scheduleSyncJobs(delta, syncFlags, buildConfigurations);
	}

	/**
	 * Invoke sync for all configurations on a project
	 * 
	 * @param delta
	 *            project delta
	 * @param project
	 *            project to sync
	 * @param syncFlags
	 *            sync flags
	 */
	public void syncAll(IResourceDelta delta, IProject project, EnumSet<SyncFlag> syncFlags) {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		if (!(bcm.isInitialized(project))) {
			return;
		}

		this.scheduleSyncJobs(delta, syncFlags, ManagedBuildManager.getBuildInfo(project).getManagedProject().getConfigurations());
	}

	private void scheduleSyncJobs(IResourceDelta delta, EnumSet<SyncFlag> syncFlags, IConfiguration[] buildConfigurations) {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		for (IConfiguration buildConfig : buildConfigurations) {
			IServiceConfiguration serviceConfig = bcm.getConfigurationForBuildConfiguration(buildConfig);
			if (serviceConfig != null) {
				ISyncServiceProvider provider = (ISyncServiceProvider) serviceConfig.getServiceProvider(syncService);
				if (provider != null) {
					SynchronizeJob job = new SynchronizeJob(delta, provider, syncFlags);
					job.schedule();
				}
			} else {
				RDTSyncCorePlugin.log(Messages.SyncConfigurationManager_1 + buildConfig.getName());
			}
		}
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
}
