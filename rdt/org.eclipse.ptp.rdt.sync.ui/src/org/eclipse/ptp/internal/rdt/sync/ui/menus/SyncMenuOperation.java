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
package org.eclipse.ptp.internal.rdt.sync.ui.menus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.SyncMergeFileTableViewer;
import org.eclipse.ptp.internal.rdt.sync.ui.handlers.CommonSyncExceptionHandler;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.preferences.SyncFileFilterDialog;
import org.eclipse.ptp.internal.rdt.sync.ui.properties.ManageConfigurationDialog;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SyncMode;
import org.eclipse.ptp.rdt.sync.core.handlers.ISyncExceptionHandler;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

public class SyncMenuOperation extends AbstractHandler implements IElementUpdater {
	private static final String SYNC_COMMAND_PARAMETER_ID = "org.eclipse.ptp.rdt.sync.ui.syncCommand.syncModeParameter"; //$NON-NLS-1$
	private static final String syncActiveCommand = "sync_active"; //$NON-NLS-1$
	private static final String syncAllCommand = "sync_all"; //$NON-NLS-1$
	private static final String setNoneCommand = "set_none"; //$NON-NLS-1$
	private static final String setActiveCommand = "set_active"; //$NON-NLS-1$
	private static final String setAllCommand = "set_all"; //$NON-NLS-1$
	private static final String syncAutoCommand = "sync_auto"; //$NON-NLS-1$
	private static final String syncFileList = "sync_file_list"; //$NON-NLS-1$
	private static final String syncDefaultFileList = "sync_default_file_list"; //$NON-NLS-1$
	private static final String syncExcludeCommand = "sync_exclude"; //$NON-NLS-1$
	private static final String syncIncludeCommand = "sync_include"; //$NON-NLS-1$
	private static final String checkoutCommand = "checkout"; //$NON-NLS-1$
	private static final String resolveAsRemoteCommand = "checkout_remote_copy"; //$NON-NLS-1$
	private static final String resolveMergeCommand = "resolve"; //$NON-NLS-1$
	private static final String manageCommand = "manage"; //$NON-NLS-1$
	private static final ISyncExceptionHandler syncExceptionHandler = new CommonSyncExceptionHandler(false, true);

	@Override
	public Object execute(ExecutionEvent event) {
		String command = event.getParameter(SYNC_COMMAND_PARAMETER_ID);
		IProject project = getProject();
		if (project == null) {
			RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMenuOperation_0);
			return null;
		}

		// On sync request, sync regardless of the flags
		try {
			if (command.equals(syncActiveCommand)) {
				SyncManager.sync(null, project, SyncFlag.FORCE, syncExceptionHandler);
			} else if (command.equals(syncAllCommand)) {
				SyncManager.syncAll(null, project, SyncFlag.FORCE, syncExceptionHandler);
				// If user switches to active or all, assume the user wants to sync right away
			} else if (command.equals(setActiveCommand)) {
				SyncManager.setSyncMode(project, SyncMode.ACTIVE);
				SyncManager.sync(null, project, SyncFlag.FORCE, syncExceptionHandler);
			} else if (command.equals(setAllCommand)) {
				SyncManager.setSyncMode(project, SyncMode.ALL);
				SyncManager.syncAll(null, project, SyncFlag.FORCE, syncExceptionHandler);
			} else if (command.equals(setNoneCommand)) {
				SyncManager.setSyncMode(project, SyncMode.NONE);
			} else if (command.equals(syncAutoCommand)) {
				SyncManager.setSyncAuto(!(SyncManager.getSyncAuto()));
				// If user switches to automatic sync'ing, go ahead and sync based on current setting for project
				if (SyncManager.getSyncAuto()) {
					SyncMode syncMode = SyncManager.getSyncMode(project);
					if (syncMode == SyncMode.ACTIVE) {
						SyncManager.sync(null, project, SyncFlag.FORCE, syncExceptionHandler);
					} else if (syncMode == SyncMode.ALL) {
						SyncManager.syncAll(null, project, SyncFlag.FORCE, syncExceptionHandler);
					}
				}
			} else if (command.equals(syncExcludeCommand) || command.equals(syncIncludeCommand)) {
				AbstractSyncFileFilter sff = SyncManager.getFileFilter(project);
				IStructuredSelection sel = this.getSelectedElements();
				boolean exclude = command.equals(syncExcludeCommand);

				for (Object element : sel.toArray()) {
					IResource selection;
					if (element instanceof IResource) {
						selection = (IResource) element;
					} else if (element instanceof IAdaptable) {
						selection = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
					} else {
						RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMenuOperation_6);
						continue;
					}

					sff.addPattern(selection, exclude);
				}
				try {
					SyncManager.saveFileFilter(project, sff);
				} catch (IOException e) {
					RDTSyncUIPlugin.log(e);
				}
			} else if (command.equals(syncFileList)) {
				SyncFileFilterDialog.open(HandlerUtil.getActiveShell(event), project);
			} else if (command.equals(syncDefaultFileList)) {
				SyncFileFilterDialog.open(HandlerUtil.getActiveShell(event), null);
			} else if (command.equals(checkoutCommand) || command.equals(resolveMergeCommand)
					|| (command.equals(resolveAsRemoteCommand))) {
				ISynchronizeService syncService = SyncConfigManager.getActive(project).getSyncService();
				SyncConfig syncConfig = SyncConfigManager.getActive(project);
				IStructuredSelection sel = this.getSelectedElements();

				ArrayList<IPath> paths = new ArrayList<IPath>();
				for (Object element : sel.toArray()) {
					IResource selection;
					if (element instanceof IResource) {
						selection = (IResource) element;
					} else if (element instanceof IAdaptable) {
						selection = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
					} else {
						RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMenuOperation_6);
						continue;
					}

					paths.add(selection.getProjectRelativePath());
				}

				if (command.equals(checkoutCommand)) {
					syncService.checkout(project, syncConfig, paths.toArray(new IPath[paths.size()]));
				}
				if (command.equals(resolveAsRemoteCommand)) {

					syncService.checkoutRemoteCopy(project, syncConfig, paths.toArray(new IPath[paths.size()]));
				}
				if (command.equals(resolveMergeCommand) || command.equals(resolveAsRemoteCommand)) {
					syncService.setMergeAsResolved(project, syncConfig, paths.toArray(new IPath[paths.size()]));
					SyncMergeFileTableViewer viewer = SyncMergeFileTableViewer.getActiveInstance();
					if (viewer != null) {
						viewer.update(null);
					}
				}
			} else if (command.equals(manageCommand)) {
				new ManageConfigurationDialog(HandlerUtil.getActiveShell(event), project).open();
			}
		} catch (CoreException e) {
			// This should never happen because only a blocking sync can throw a core exception, and all syncs here are
			// non-blocking.
			RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMenuOperation_1);
		}

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window != null) {
			ICommandService service = (ICommandService) window.getService(ICommandService.class);
			service.refreshElements(event.getCommand().getId(), null);
		}

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		String command = (String) parameters.get(SYNC_COMMAND_PARAMETER_ID);
		if (command == null) {
			RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMenuOperation_2);
			return;
		}

		IProject project = this.getProject();
		if (project == null) {
			// Disable error message - this happens routinely for non-IResource selections
			// RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMenuOperation_0);
			return;
		}

		SyncMode syncMode = SyncManager.getSyncMode(project);
		if ((command.equals(setActiveCommand) && syncMode == SyncMode.ACTIVE)
				|| (command.equals(setAllCommand) && syncMode == SyncMode.ALL)
				|| (command.equals(setNoneCommand) && (syncMode == SyncMode.NONE || syncMode == SyncMode.UNAVAILABLE))
				|| (command.equals(syncAutoCommand) && SyncManager.getSyncAuto())) {
			element.setChecked(true);
		} else {
			element.setChecked(false);
		}
	}

	/*
	 * Portions copied from org.eclipse.ptp.services.ui.wizards.setDefaultFromSelection
	 */
	private IProject getProject() {
		IStructuredSelection selection = this.getSelectedElements();
		if (selection == null) {
			return null;
		}

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

	private IStructuredSelection getSelectedElements() {
		IWorkbenchWindow wnd = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage pg = wnd.getActivePage();
		ISelection sel = pg.getSelection();

		if (!(sel instanceof IStructuredSelection)) {
			return null;
		} else {
			return (IStructuredSelection) sel;
		}
	}
}
