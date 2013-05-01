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
package org.eclipse.ptp.internal.gig.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.gig.GIGPlugin;
import org.eclipse.ptp.internal.gig.messages.Messages;
import org.eclipse.ptp.internal.gig.util.GIGUtilities;
import org.eclipse.ptp.internal.gig.util.GIGUtilities.JobState;
import org.eclipse.ptp.internal.gig.util.IllegalCommandException;
import org.eclipse.ptp.internal.gig.util.IncorrectPasswordException;
import org.eclipse.ptp.internal.gig.views.GIGView;
import org.eclipse.ptp.internal.gig.views.ServerView;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

@SuppressWarnings("restriction")
public class PopupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// this handles two events, so find which one
		final String eventName = event.getCommand().getId();
		if (eventName.equals("org.eclipse.ptp.gig.commands.sourcePopup")) { //$NON-NLS-1$
			// This means verify the one element that is selected
			final ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (structuredSelection.size() != 1) {
					// short circuit if they selected more than one, or less than one
					return null;
				}
				final Object object = structuredSelection.getFirstElement();
				IFile file;
				if (object instanceof IFile) {
					file = (IFile) object;
				}
				else if (object instanceof TranslationUnit) {
					final TranslationUnit unit = (TranslationUnit) object;
					file = (IFile) unit.getUnderlyingResource();
				}
				else {
					return null;
				}
				final IPath filePath = file.getFullPath();

				final IWorkbench workbench = PlatformUI.getWorkbench();
				final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				final IWorkbenchPage page = window.getActivePage();
				// we need to ensure the GIGView has been lazily loaded, it is also good to bring it to the front
				// same with the ServerView
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
							GIGUtilities.setJobState(JobState.None);
							// finally clause activates before returning
							return ret;
						} catch (final IOException e) {
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
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
		}
		else if (eventName.equals("org.eclipse.ptp.gig.commands.sendSourceToServer")) { //$NON-NLS-1$
			// This command is for sending folders and files to the server
			final ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				final Object[] objectArray = structuredSelection.toArray();

				// we need to ensure that the ServerView is loaded
				final IWorkbench workbench = PlatformUI.getWorkbench();
				final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				final IWorkbenchPage page = window.getActivePage();
				try {
					page.showView(ServerView.ID);
				} catch (final PartInitException e) {
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.PART_INIT_EXCEPTION, e));
				}

				final Job job = new Job(Messages.SEND_TO_SERVER) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// Transform the objects into lists of folders and files
						final List<IFolder> folders = new ArrayList<IFolder>();
						final List<IFile> files = new ArrayList<IFile>();
						for (final Object o : objectArray) {
							if (o instanceof IFile) {
								final IFile file = (IFile) o;
								files.add(file);
							}
							else if (o instanceof ICContainer) {
								final ICContainer container = (ICContainer) o;
								final IResource resource = container.getResource();
								if (resource instanceof IFolder) {
									final IFolder folder = (IFolder) resource;
									folders.add(folder);
								}
							}
							else if (o instanceof TranslationUnit) {
								final TranslationUnit unit = (TranslationUnit) o;
								final IResource resource = unit.getResource();
								if (resource instanceof IFile) {
									final IFile file = (IFile) resource;
									files.add(file);
								}
							}
							else if (o instanceof IFolder) {
								final IFolder folder = (IFolder) o;
								folders.add(folder);
							}
						}
						try {
							GIGUtilities.sendFoldersAndFiles(folders, files);
							final UIJob job = new UIJob(Messages.IMPORT) {

								@Override
								public IStatus runInUIThread(IProgressMonitor monitor) {
									ServerView.getDefault().reset();
									return Status.OK_STATUS;
								}

							};
							GIGUtilities.startJob(job);
							// finally activates here
							return Status.OK_STATUS;
						} catch (final CoreException e) {
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.CORE_EXCEPTION, e));
						} catch (final IOException e) {
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
						} catch (final IncorrectPasswordException e) {
							GIGUtilities.showErrorDialog(Messages.INCORRECT_PASSWORD, Messages.INCORRECT_PASSWORD_MESSAGE);
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INCORRECT_PASSWORD, e));
						} catch (final IllegalCommandException e) {
							GIGUtilities.showErrorDialog(Messages.ILLEGAL_COMMAND, Messages.ILLEGAL_COMMAND_MESSAGE);
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.ILLEGAL_COMMAND, e));
						}
						finally {
							GIGUtilities.setJobState(JobState.None);
						}
						return Status.CANCEL_STATUS;
					}

				};

				GIGUtilities.startJob(job);
			}
		}

		return null;
	}

}
