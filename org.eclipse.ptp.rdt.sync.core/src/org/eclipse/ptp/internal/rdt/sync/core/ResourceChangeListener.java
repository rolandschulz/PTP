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
package org.eclipse.ptp.internal.rdt.sync.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
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
				if (project != null && RemoteSyncNature.hasNature(project)) {
					SyncCommand.sync(delta, project, SyncFlag.NO_FORCE);
				}
			}
		}
	};
}
