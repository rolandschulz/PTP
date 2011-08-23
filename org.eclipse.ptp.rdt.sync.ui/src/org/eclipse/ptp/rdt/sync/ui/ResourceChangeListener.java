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
import org.eclipse.swt.widgets.Display;

public class ResourceChangeListener {
	private ResourceChangeListener() {
	}

	public static void startListening() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
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
							SyncManager.sync(delta, project, SyncFlag.NO_SYNC, null);
						} else if (syncMode == SYNC_MODE.ALL) {
							SyncManager.syncAll(delta, project, SyncFlag.NO_FORCE, new SyncRCLExceptionHandler(project));
						} else if (syncMode == SYNC_MODE.ACTIVE) {
							SyncManager.sync(delta, project, SyncFlag.NO_FORCE, new SyncRCLExceptionHandler(project));
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