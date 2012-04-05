/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     John Eblen - Oak Ridge National Laboratory - change to use sync manager
 *                  and move to ui package
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ptp.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.rdt.sync.core.SyncExceptionHandler;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SYNC_MODE;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.swt.widgets.Display;

public class ResourceChangeListener {
	private ResourceChangeListener() {
	}

	public static void startListening() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE |
				IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_BUILD);
	}

	public static void stopListening() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}

	// For sync errors, print information and provide a toggle box so that user can turn off error displays until the next
	// successful sync, which presumably means the problem has been resolved. Note that errors are re-enabled inside the
	// sync call.
	private static class SyncRCLExceptionHandler implements SyncExceptionHandler {
		private final IProject project;
		public SyncRCLExceptionHandler(IProject p) {
			project = p;
		}

		public void handle(CoreException e) {
			if (!SyncManager.getShowErrors(project)) {
				return;
			}

			String message;
			String endOfLineChar = System.getProperty("line.separator"); //$NON-NLS-1$
			Display errorDisplay = RDTSyncUIPlugin.getStandardDisplay();
			
			message = Messages.SyncMenuOperation_5 + project.getName() + ":" + endOfLineChar + endOfLineChar; //$NON-NLS-1$
			if ((e.getMessage() != null && e.getMessage().length() > 0) || e.getCause() == null) {
				message = message + e.getMessage();
			} else {
				message = message + e.getCause().getMessage();
			}
			
			final String finalMessage = message;
			errorDisplay.syncExec(new Runnable () {
				public void run() {
					MessageDialogWithToggle dialog = MessageDialogWithToggle.openError(null, Messages.SyncMenuOperation_3,
							finalMessage, Messages.SyncMenuOperation_4, !SyncManager.getShowErrors(project), null, null);
					if (dialog.getToggleState()) {
						SyncManager.setShowErrors(project, false);
					} else {
						SyncManager.setShowErrors(project, true);
					}
				}
			});
		}
	}

	private static IResourceChangeListener resourceListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			// Turn off sync'ing for a project before deleting it and close repository - see bug 360170
			// Note that event.getDelta() returns null, so this event cannot be handled inside the loop below.
			if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
				IProject project = (IProject) event.getResource();
				if (!RemoteSyncNature.hasNature(project)) {
					return;
				}
				SyncManager.setSyncMode(project, SYNC_MODE.NONE);
				ISyncServiceProvider provider = SyncManager.getSyncProvider(project);
				if (provider != null) {
					provider.close();
				}
				return;
			}
			for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
				IProject project = delta.getResource().getProject();
				if (project == null) {
					return;
				}
				if (RemoteSyncNature.hasNature(project)) {
					SYNC_MODE syncMode = SyncManager.getSyncMode(project);
					boolean syncEnabled = true;
					if (!(SyncManager.getSyncAuto()) || syncMode == SYNC_MODE.NONE) {
						syncEnabled = false;
					}
					try {
						// Post-build event
						// Force a sync in order to download any new remote files but no need to sync if sync'ing is disabled.
						if (event.getType() == IResourceChangeEvent.POST_BUILD ) {
							if (!syncEnabled) {
								continue;
							} else if (syncMode == SYNC_MODE.ALL) {
								SyncManager.syncAll(null, project, SyncFlag.FORCE, new SyncRCLExceptionHandler(project));
							} else if (syncMode == SYNC_MODE.ACTIVE) {
								SyncManager.sync(null, project, SyncFlag.FORCE, new SyncRCLExceptionHandler(project));
							}
						}
						// Post-change event
						else {
							RDTSyncCorePlugin.log(String.valueOf(delta.getKind()));
							// Only interested in ADDED with MOVED_FROM, which indicates a project was renamed
							if ((delta.getKind() == IResourceDelta.ADDED) && ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0)) {
								ISyncServiceProvider provider = SyncManager.getSyncProvider(project);
								if (provider == null) {
									RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.ResourceChangeListener_1 +
											project.getName());
								} else {
									provider.setProject(project);
								}
							}
							
							// Sync project on all changed events
							else if (delta.getKind() == IResourceDelta.CHANGED) {
								// Do a non-forced sync to update any changes reported in delta. Sync'ing is necessary even if user has
								// disabled it. This allows for some bookkeeping but no files are transferred.
								if (!syncEnabled) {
									SyncManager.sync(delta, project, SyncFlag.NO_SYNC, null);
								} else if (syncMode == SYNC_MODE.ALL) {
									SyncManager.syncAll(delta, project, SyncFlag.NO_FORCE, new SyncRCLExceptionHandler(project));
								} else if (syncMode == SYNC_MODE.ACTIVE) {
									SyncManager.sync(delta, project, SyncFlag.NO_FORCE, new SyncRCLExceptionHandler(project));
								}
							}
							
							// Not interested in remove events
						}
					} catch (CoreException e){
						// This should never happen because only a blocking sync can throw a core exception, and all syncs here are non-blocking.
						RDTSyncUIPlugin.log(Messages.ResourceChangeListener_0, e);
					}
				}
			}
		}
	};
}