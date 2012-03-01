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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ptp.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.rdt.sync.core.ISyncExceptionHandler;
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

	private static IResourceChangeListener resourceListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			// Turn off sync'ing for a project before deleting it and close repository - see bug 360170
			if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
				IProject project = (IProject) event.getResource();
				SyncManager.setSyncMode(project, SYNC_MODE.NONE);
				ISyncServiceProvider provider = SyncManager.getSyncProvider(project);
				provider.close();
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
						// Ignore auto builds, which are triggered for every resource change
						if (event.getType() == IResourceChangeEvent.POST_BUILD && 
								event.getBuildKind() != IncrementalProjectBuilder.AUTO_BUILD) {
							if (!syncEnabled) {
								continue;
							} else if (syncMode == SYNC_MODE.ALL) {
								SyncManager.syncAll(null, project, SyncFlag.FORCE,
										new CommonSyncExceptionHandler(project, true, true));
							} else if (syncMode == SYNC_MODE.ACTIVE) {
								SyncManager.sync(null, project, SyncFlag.FORCE,
										new CommonSyncExceptionHandler(project, true, true));
							}
						}
						// Post-change event
						// Do a non-forced sync to update any changes reported in delta. Sync'ing is necessary even if user has
						// disabled it. This allows for some bookkeeping but no files are transferred.
						else {
							if (!syncEnabled) {
								SyncManager.sync(delta, project, SyncFlag.NO_SYNC, null);
							} else if (syncMode == SYNC_MODE.ALL) {
								SyncManager.syncAll(delta, project, SyncFlag.NO_FORCE,
										new CommonSyncExceptionHandler(project, true, false));
							} else if (syncMode == SYNC_MODE.ACTIVE) {
								SyncManager.sync(delta, project, SyncFlag.NO_FORCE,
										new CommonSyncExceptionHandler(project, true, false));
							}
						}
					} catch (CoreException e){
						// This should never happen because only a blocking sync can throw a core exception, and all syncs here are non-blocking.
						RDTSyncCorePlugin.log(Messages.ResourceChangeListener_0);
					}
				}
			}
		}
	};
}