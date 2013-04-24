/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ptp.gig.util.GIGUtilities;
import org.eclipse.ptp.gig.util.GIGUtilities.JobState;
import org.eclipse.ptp.gig.views.GIGView;
import org.eclipse.ptp.gig.views.ServerView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

public class ToolbarHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IEditorInput input;
		try {
			input = HandlerUtil.getActiveEditorInputChecked(event);
		} catch (final ExecutionException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, e.getMessage(), e));
			return null;
		}
		if (input instanceof IFileEditorInput) {
			final IFileEditorInput fileInput = (IFileEditorInput) input;
			final IFile file = fileInput.getFile();
			final IPath filePath = file.getFullPath();

			// We need to show the GIGView in order to ensure it is loaded, same with the ServerView
			final IWorkbench workBench = PlatformUI.getWorkbench();
			final IWorkbenchWindow window = workBench.getActiveWorkbenchWindow();
			final IWorkbenchPage page = window.getActivePage();
			try {
				page.showView(GIGView.ID);
				GIGView.getDefault().cleanTrees();
				page.showView(ServerView.ID);
			} catch (final PartInitException e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.PART_INIT_EXCEPTION, e));
			}

			// start it in a new thread so as to not block UI thread
			final Job job = new Job(Messages.RUN_GKLEE) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						final IStatus ret = GIGUtilities.processSource(filePath);
						return ret;
					} catch (final IOException e) {
						StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
					} catch (final CoreException e) {
						StatusManager.getManager().handle(e, GIGPlugin.PLUGIN_ID);
					} catch (final InterruptedException e) {
						StatusManager.getManager().handle(
								new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INTERRUPTED_EXCEPTION, e));
					}
					finally {
						GIGUtilities.setJobState(JobState.None);
					}
					return Status.CANCEL_STATUS;
				}
			};

			GIGUtilities.startJob(job);
		}

		return null;
	}
}
