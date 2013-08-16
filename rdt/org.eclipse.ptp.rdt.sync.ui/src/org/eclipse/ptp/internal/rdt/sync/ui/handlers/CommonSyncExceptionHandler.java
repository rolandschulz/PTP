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
package org.eclipse.ptp.internal.rdt.sync.ui.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.SyncMergeFileTableViewer;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncMergeConflictException;
import org.eclipse.ptp.rdt.sync.core.handlers.ISyncExceptionHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * A sync exception handler for most common uses. It creates a dialog with a button to open the merge conflict viewer if there is
 * a merge conflict and optionally creates a toggle to disable or enable error messages. The client may also specify whether to
 * respect the SyncManager "show errors" settings or to always show the dialog.
 */
public class CommonSyncExceptionHandler implements ISyncExceptionHandler {
	private static final String SYNC_MERGE_FILE_VIEW = "org.eclipse.ptp.rdt.sync.ui.SyncMergeFileTableViewer"; //$NON-NLS-1$
	private final boolean showErrorMessageToggle;
	private final boolean alwaysShowDialog;
	private static long lastMergeConflictDialogTimeStamp = 0;
	private static final long timeBetweenMergeConflicts = 5000; // 5 seconds

	public CommonSyncExceptionHandler(boolean showToggle, boolean alwaysShow) {
		showErrorMessageToggle = showToggle;
		alwaysShowDialog = alwaysShow;
	}

	@Override
	public void handle(final IProject project, final CoreException e) {
		RDTSyncUIPlugin.log(e);
		if (!alwaysShowDialog && !SyncManager.getShowErrors(project)) {
			return;
		}

		String message;
		String endOfLineChar = System.getProperty("line.separator"); //$NON-NLS-1$

		message = Messages.CommonSyncExceptionHandler_0 + project.getName() + ":" + endOfLineChar + endOfLineChar; //$NON-NLS-1$
		if (e.getMessage() != null) {
			message = message + e.getMessage();
		}

		final String finalMessage = message;
		Display errorDisplay = RDTSyncUIPlugin.getStandardDisplay();
		errorDisplay.syncExec(new Runnable() {
			@Override
			public void run() {
				String[] buttonLabels;
				if (e instanceof RemoteSyncMergeConflictException) {
					// Avoid flooding the display with merge conflict dialogs.
					if (System.currentTimeMillis() - lastMergeConflictDialogTimeStamp <= timeBetweenMergeConflicts) {
						return;
					}
					lastMergeConflictDialogTimeStamp = System.currentTimeMillis();
					buttonLabels = new String[2];
					buttonLabels[0] = IDialogConstants.OK_LABEL;
					buttonLabels[1] = Messages.CommonSyncExceptionHandler_1; // Custom button
				} else {
					buttonLabels = new String[1];
					buttonLabels[0] = IDialogConstants.OK_LABEL;
				}

				MessageDialog dialog;
				if (showErrorMessageToggle) {
					dialog = new MessageDialogWithToggle(null, Messages.CommonSyncExceptionHandler_2, null, finalMessage,
							MessageDialog.ERROR, buttonLabels, 0, Messages.CommonSyncExceptionHandler_3, !SyncManager
									.getShowErrors(project));
				} else {
					dialog = new MessageDialog(null, Messages.CommonSyncExceptionHandler_4, null, finalMessage,
							MessageDialog.ERROR, buttonLabels, 0);
				}

				int buttonPressed = dialog.open();
				if (showErrorMessageToggle) {
					if (((MessageDialogWithToggle) dialog).getToggleState()) {
						SyncManager.setShowErrors(project, false);
					} else {
						SyncManager.setShowErrors(project, true);
					}
				}
				// See bug #236617 concerning custom button ids.
				if ((showErrorMessageToggle && IDialogConstants.INTERNAL_ID - buttonPressed == 0)
						|| (!showErrorMessageToggle && buttonPressed == 1)) {
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.showView(SYNC_MERGE_FILE_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
						SyncMergeFileTableViewer viewer = SyncMergeFileTableViewer.getActiveInstance();
						if (viewer != null) {
							viewer.update(project);
						}
					} catch (CoreException e) {
						throw new RuntimeException(e);
					}
				}
			};
		});
	}
}