/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.internal.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SYNC_MODE;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;

public class ResourceChangeListener {
	private ResourceChangeListener() {
	}

	public static void startListening() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
	}

	public static void stopListening() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}

	private static IResourceChangeListener resourceListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
				IProject project = delta.getResource().getProject();
				if (project == null) {
					return;
				}
				if (RemoteSyncNature.hasNature(project)) {
					SYNC_MODE syncMode = SyncManager.getSyncMode(project);
					// Note that sync'ing is necessary even if user has turned sync'ing off. The actual synchronization call does
					// more than just sync files to remote.
					try {
						if (!(SyncManager.getSyncAuto()) || syncMode == SYNC_MODE.NONE) {
							SyncManager.sync(delta, project, SyncFlag.NO_SYNC);
						} else if (syncMode == SYNC_MODE.ALL) {
							SyncManager.syncAll(delta, project, SyncFlag.NO_FORCE);
						} else if (syncMode == SYNC_MODE.ACTIVE) {
							SyncManager.sync(delta, project, SyncFlag.NO_FORCE);
						}
					} catch (CoreException e){
						// This should never happen because only a blocking sync can throw a core exception, and all syncs here are non-blocking.
						RDTSyncCorePlugin.log(Messages.SyncManager_3);
					}
				}
			}
		}
	};
}