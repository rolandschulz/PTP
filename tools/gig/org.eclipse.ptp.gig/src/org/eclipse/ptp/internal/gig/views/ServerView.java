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
package org.eclipse.ptp.internal.gig.views;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.internal.gig.GIGPlugin;
import org.eclipse.ptp.internal.gig.messages.Messages;
import org.eclipse.ptp.internal.gig.util.GIGUtilities;
import org.eclipse.ptp.internal.gig.util.GIGUtilities.JobState;
import org.eclipse.ptp.internal.gig.util.IllegalCommandException;
import org.eclipse.ptp.internal.gig.util.IncorrectPasswordException;
import org.eclipse.ptp.internal.gig.util.ProjectNotFoundException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;

/*
 * This view is for interactions with the server.
 */
public class ServerView extends ViewPart {

	public static final String ID = "org.eclipse.ptp.gig.views.ServerView"; //$NON-NLS-1$

	public static ServerView getDefault() {
		return serverView;
	}

	private TreeViewer treeViewer;
	private IAction importAction, resetAction, deleteAction, verifyAction;

	private static ServerView serverView;

	public ServerView() {
		super();
		serverView = this;
	}

	@Override
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent);

		importAction = new Action() {
			@Override
			public void run() {
				try {
					startImport();
				} catch (final ProjectNotFoundException e) {
					GIGUtilities.showErrorDialog(Messages.PROJECT_NOT_FOUND, Messages.PROJECT_NOT_FOUND_MESSAGE);
				}
			}
		};
		importAction.setText(Messages.IMPORT);
		importAction.setToolTipText(Messages.IMPORT);
		importAction.setImageDescriptor(GIGPlugin.getImageDescriptor("icons/import.gif")); //$NON-NLS-1$

		resetAction = new Action() {
			@Override
			public void run() {
				reset();
			}
		};
		resetAction.setText(Messages.REFRESH);
		resetAction.setToolTipText(Messages.REFRESH);
		resetAction.setImageDescriptor(GIGPlugin.getImageDescriptor("icons/refresh.gif")); //$NON-NLS-1$

		deleteAction = new Action() {
			@Override
			public void run() {
				deleteRemoteFiles();
			}
		};
		deleteAction.setText(Messages.DELETE_REMOTE_FILE);
		deleteAction.setToolTipText(Messages.DELETE_REMOTE_FILE);
		deleteAction.setImageDescriptor(GIGPlugin.getImageDescriptor("icons/delete_obj.gif")); //$NON-NLS-1$

		verifyAction = new Action() {
			@Override
			public void run() {
				try {
					prepareVerifySelection();
				} catch (final ProjectNotFoundException e) {
					GIGUtilities.showErrorDialog(Messages.PROJECT_NOT_FOUND, Messages.PROJECT_NOT_FOUND_MESSAGE);
				}
			}
		};
		verifyAction.setText(Messages.RUN_GKLEE);
		verifyAction.setToolTipText(Messages.RUN_GKLEE);
		verifyAction.setImageDescriptor(GIGPlugin.getImageDescriptor("icons/trident.png")); //$NON-NLS-1$

		final MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				populateManager(manager);
			}

		});
		final Menu menu = menuManager.createContextMenu(this.treeViewer.getControl());
		this.treeViewer.getControl().setMenu(menu);
		this.getSite().registerContextMenu(menuManager, this.treeViewer);

		final IActionBars actionBars = this.getViewSite().getActionBars();
		final IToolBarManager toolBarManager = actionBars.getToolBarManager();
		menuManager.add(this.verifyAction);
		toolBarManager.add(this.verifyAction);
		menuManager.add(this.importAction);
		toolBarManager.add(this.importAction);
		menuManager.add(this.deleteAction);
		toolBarManager.add(this.deleteAction);
		menuManager.add(this.resetAction);
		toolBarManager.add(this.resetAction);

		reset();
	}

	/*
	 * Entry point for deleting remote files.
	 */
	protected void deleteRemoteFiles() {
		final IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		final Object[] objects = selection.toArray();
		try {
			GIGUtilities.deleteRemoteFiles(objects);
			this.reset();
		} catch (final IOException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
		} catch (final IncorrectPasswordException e) {
			GIGUtilities.showErrorDialog(Messages.INCORRECT_PASSWORD, Messages.INCORRECT_PASSWORD_MESSAGE);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INCORRECT_PASSWORD, e));
		} catch (final IllegalCommandException e) {
			GIGUtilities.showErrorDialog(Messages.ILLEGAL_COMMAND, Messages.ILLEGAL_COMMAND_MESSAGE);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.ILLEGAL_COMMAND, e));
		}
	}

	/*
	 * close to entry point for importing
	 */
	private void doImport(final IProject project) {
		final IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		final Object[] objects = selection.toArray();
		final Job job = new Job(Messages.IMPORT) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					GIGUtilities.importFoldersAndFiles(project, objects);
					return Status.OK_STATUS;
				} catch (final IOException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
				} catch (final IncorrectPasswordException e) {
					GIGUtilities.showErrorDialog(Messages.INCORRECT_PASSWORD, Messages.INCORRECT_PASSWORD_MESSAGE);
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INCORRECT_PASSWORD, e));
				} catch (final IllegalCommandException e) {
					GIGUtilities.showErrorDialog(Messages.ILLEGAL_COMMAND, Messages.ILLEGAL_COMMAND_MESSAGE);
					StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.ILLEGAL_COMMAND, e));
				} catch (final CoreException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.CORE_EXCEPTION, e));
				}
				finally {
					GIGUtilities.setJobState(JobState.None);
				}
				return Status.CANCEL_STATUS;
			}

		};
		GIGUtilities.startJob(job);

	}

	/*
	 * Populates the pop-up menu manager with these commands.
	 */
	protected void populateManager(IMenuManager manager) {
		manager.add(this.verifyAction);
		manager.add(this.importAction);
		manager.add(this.deleteAction);
		manager.add(this.resetAction);
	}

	/*
	 * Entry point for verification
	 */
	protected void prepareVerifySelection() throws ProjectNotFoundException {
		final IProject project = GIGUtilities.getTargetProject();
		verifySelection(project);
	}

	/*
	 * To be called from anywhere to reset this view. Does require the current thread to be the UI thread though.
	 */
	public void reset() {
		final Object[] expandedElements = treeViewer.getExpandedElements();
		final IContentProvider contentProvider = new ServerContentProvider();
		treeViewer.setContentProvider(contentProvider);
		final IBaseLabelProvider labelProvider = new ServerLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
		final ViewerSorter sorter = new ServerTreeItemSorter();
		treeViewer.setSorter(sorter);
		try {
			final ServerTreeItem treeRoot = GIGUtilities.getServerFoldersAndFilesRoot();
			treeViewer.setInput(treeRoot);
			for (final Object o : expandedElements) {
				treeViewer.expandToLevel(o, 1);
			}
		} catch (final IOException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
		} catch (final IncorrectPasswordException e) {
			GIGUtilities.showErrorDialog(Messages.INCORRECT_PASSWORD, Messages.INCORRECT_PASSWORD_MESSAGE);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INCORRECT_PASSWORD, e));
		} catch (final IllegalCommandException e) {
			GIGUtilities.showErrorDialog(Messages.ILLEGAL_COMMAND, Messages.ILLEGAL_COMMAND_MESSAGE);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.ILLEGAL_COMMAND, e));
		}
	}

	@Override
	public void setFocus() {
	}

	/*
	 * Entry point for import
	 */
	public void startImport() throws ProjectNotFoundException {
		final IProject project = GIGUtilities.getTargetProject();
		doImport(project);
	}

	/*
	 * Close to entry point for verification.
	 */
	protected void verifySelection(final IProject project) {
		final IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		final Object[] objects = selection.toArray();
		if (objects.length != 1 || ((ServerTreeItem) objects[0]).isFolder()) {
			GIGUtilities.showErrorDialog(Messages.SELECTION_ERROR, Messages.SELECT_ONE_FILE);
			return;
		}
		final ServerTreeItem item = (ServerTreeItem) objects[0];

		// we need to ensure the GIGView has been lazily loaded, it is also good to bring it to the front
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(GIGView.ID);
			GIGView.getDefault().cleanTrees();
		} catch (final PartInitException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.PART_INIT_EXCEPTION, e));
		}
		final Job job = new Job(Messages.RUN_GKLEE) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					GIGUtilities.remoteVerifyFile(project, item);
					return Status.OK_STATUS;
				} catch (final IOException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.IO_EXCEPTION, e));
				} catch (final IncorrectPasswordException e) {
					GIGUtilities.showErrorDialog(Messages.INCORRECT_PASSWORD, Messages.INCORRECT_PASSWORD_MESSAGE);
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.INCORRECT_PASSWORD, e));
				} catch (final IllegalCommandException e) {
					GIGUtilities.showErrorDialog(Messages.ILLEGAL_COMMAND, Messages.ILLEGAL_COMMAND_MESSAGE);
					StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.ILLEGAL_COMMAND, e));
				} catch (final CoreException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.CORE_EXCEPTION, e));
				} finally {
					GIGUtilities.setJobState(JobState.None);
				}
				return Status.CANCEL_STATUS;
			}

		};
		GIGUtilities.startJob(job);
	}
}
