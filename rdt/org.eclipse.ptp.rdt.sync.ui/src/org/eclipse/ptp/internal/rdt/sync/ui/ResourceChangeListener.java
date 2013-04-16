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
package org.eclipse.ptp.internal.rdt.sync.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.sync.ui.handlers.CommonSyncExceptionHandler;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SyncMode;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;

public class ResourceChangeListener {
	private ResourceChangeListener() {
	}

	public static void startListening() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceListener,
				IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_BUILD
						| IResourceChangeEvent.POST_BUILD);
	}

	public static void stopListening() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}

	private static IResourceChangeListener resourceListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			// Turn off sync'ing for a project before deleting it and close repository - see bug 360170
			// Note that event.getDelta() returns null, so this event cannot be handled inside the loop below.
			if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
				IProject project = (IProject) event.getResource();
				if (!RemoteSyncNature.hasNature(project)) {
					return;
				}
				SyncManager.setSyncMode(project, SyncMode.UNAVAILABLE);
				SyncConfigManager.getActive(project).getSyncService().close(project);
				return;
			}
			for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
				IProject project = delta.getResource().getProject();
				if (project == null) {
					return;
				}
				if (RemoteSyncNature.hasNature(project)) {
					SyncMode syncMode = SyncManager.getSyncMode(project);
					boolean syncOn = true;
					if (!(SyncManager.getSyncAuto()) || syncMode == SyncMode.NONE) {
						syncOn = false;
					}
					SyncConfig syncConfig = SyncConfigManager.getActive(project);
					/*
					 * syncConfig can be null when sync nature is added to the project as this generates a resource change event
					 */
					if (syncConfig != null) {
						try {
							// Post-build event
							// Force a sync in order to download any new remote files but no need to sync if sync'ing is disabled.
							if (event.getType() == IResourceChangeEvent.POST_BUILD && syncConfig.isSyncOnPostBuild()) {
								// Ignore auto builds, which are triggered for every resource change.
								if (event.getBuildKind() == IncrementalProjectBuilder.AUTO_BUILD) {
									continue;
								}
								if (!syncOn || syncMode == SyncMode.UNAVAILABLE) {
									continue;
								} else if (syncMode == SyncMode.ALL) {
									SyncManager.syncAll(null, project, SyncFlag.FORCE, new CommonSyncExceptionHandler(true, true));
								} else if (syncMode == SyncMode.ACTIVE) {
									SyncManager.sync(null, project, SyncFlag.FORCE, new CommonSyncExceptionHandler(true, true));
								}
							}
							// Pre-build event
							// Force a sync in order to download any new remote files but no need to sync if sync'ing is disabled.
							else if (event.getType() == IResourceChangeEvent.PRE_BUILD && syncConfig.isSyncOnPreBuild()) {
								// Ignore auto builds, which are triggered for every resource change.
								if (event.getBuildKind() == IncrementalProjectBuilder.AUTO_BUILD) {
									continue;
								}
								if (!syncOn || syncMode == SyncMode.UNAVAILABLE) {
									continue;
								} else if (syncMode == SyncMode.ALL) {
									SyncManager.syncAll(null, project, SyncFlag.FORCE, new CommonSyncExceptionHandler(true, true));
								} else if (syncMode == SyncMode.ACTIVE) {
									SyncManager.sync(null, project, SyncFlag.FORCE, new CommonSyncExceptionHandler(true, true));
								}
							}
							// Post-change event
							// Sync on all CHANGED events
							else if (delta.getKind() == IResourceDelta.CHANGED && syncConfig.isSyncOnSave()) {
								// Do a non-forced sync to update any changes reported in delta. Sync'ing is necessary even if user
								// has
								// turned it off. This allows for some bookkeeping but no files are transferred.
								if (syncMode == SyncMode.UNAVAILABLE) {
									continue;
								} else if (!syncOn) {
									SyncManager.sync(delta, project, SyncFlag.NO_SYNC, null);
								} else if (syncMode == SyncMode.ALL) {
									SyncManager.syncAll(delta, project, SyncFlag.NO_FORCE, new CommonSyncExceptionHandler(true,
											false));
								} else if (syncMode == SyncMode.ACTIVE) {
									SyncManager
											.sync(delta, project, SyncFlag.NO_FORCE, new CommonSyncExceptionHandler(true, false));
								}
							}
						} catch (CoreException e) {
							// This should never happen because only a blocking sync can throw a core exception, and all syncs here
							// are
							// non-blocking.
							RDTSyncUIPlugin.log(Messages.ResourceChangeListener_0, e);
						}
					}
				}
			}
		}
	};
}