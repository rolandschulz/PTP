/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.smoa.core.util.NotifyShell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.smoa.comp.sdk.SMOARsync;
import com.smoa.comp.sdk.jsdl.JSDL;

/**
 * Action for synchronizing files with rsync mechanism
 */
public class SMOAToRemoteSyncAction extends AbstractHandler {

	public Object execute(ExecutionEvent execEvent) throws ExecutionException {
		try {
			final IProject project = SelectConnetionAndDestDir
					.getSelectedProject(execEvent);
			final SelectConnetionAndDestDir dialog = new SelectConnetionAndDestDir(
					project);
			dialog.setWindowTitle(Messages.SMOAToRemoteSyncAction_WindowTitle);
			dialog.setShowRules(true);
			dialog.open();

			if (!dialog.hasSucceeded()) {
				return null;
			}

			final SMOARsync rsync = dialog.getConnection().getRsync();

			final JSDL jsdl = new JSDL("rsync to " + dialog.getRemoteDir()); //$NON-NLS-1$
			jsdl.setWorkingDirectory(dialog.getRemoteDir());

			final Shell shell = new Shell(Display.getCurrent().getActiveShell());
			shell.setLayout(new GridLayout(1, false));
			shell.setText(Messages.SMOASyncAction_RsyncInProgress);
			final ProgressBar progress = new ProgressBar(shell, SWT.INDETERMINATE
					| SWT.SMOOTH | SWT.BORDER);
			final GridData data = new GridData(GridData.FILL_BOTH);
			data.minimumWidth = 200;
			progress.setLayoutData(data);

			shell.pack();

			shell.open();

			new Thread() {
				@Override
				public void run() {
					try {
						final String localDir = project.getLocation().toString();
						rsync.localToRemote(jsdl, localDir, dialog.getRules());
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (!shell.isDisposed()) {
									shell.close();
								}
							}
						});
					} catch (final Exception e) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (!shell.isDisposed()) {
									shell.close();
								}
								e.printStackTrace();
								final Shell s = Display.getDefault().getActiveShell();
								final MessageBox mb = new MessageBox(s != null ? s
										: new Shell(), SWT.APPLICATION_MODAL
										| SWT.ICON_ERROR | SWT.OK);
								mb.setText(Messages.SMOASyncAction_ErrorByRsync_title);
								mb.setMessage(Messages.SMOASyncAction_ErrorByRsync_desc
										+ e.getClass().getName()
										+ (e.getLocalizedMessage() == null ? "" //$NON-NLS-1$
												: ("\n" + e //$NON-NLS-1$
														.getLocalizedMessage())));
								mb.open();
							}
						});
					}
				}
			}.start();

			return null;
		} catch (final CoreException e) {
			NotifyShell.open(Messages.SMOASyncAction_ExceptionBySynchro, e.getLocalizedMessage());
			throw new ExecutionException(e.getLocalizedMessage(), e);
		}
	}

}
