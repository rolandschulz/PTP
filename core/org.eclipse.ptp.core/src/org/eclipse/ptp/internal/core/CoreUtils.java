/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;

/**
 *
 */
public class CoreUtils {
	public static final String ParallelProcessesView_ID = "org.eclipse.ptp.ui.views.parallelProcessesView";

	public static final String PPerspectiveFactory_ID = "org.eclipse.ptp.ui.PTPRunPerspective";

	public static final String ParallelProcessViewer_ID = "org.eclipse.ptp.ui.views.parallelProcessViewer";

	public static final String ParallelNodeStatusView_ID = "org.eclipse.ptp.ui.views.parallelNodeStatusView";

	public static final String ParallelJobsView_ID = "org.eclipse.ptp.ui.views.parallelJobsView";

	public static final String ParallelProcessStatusView_ID = "org.ecliipse.ptd.ui.views.parallelProcessStatusView";

	public static final String PTP_ACTION_SET = "org.eclipse.ptp.actionSets";

	public static final String PTP_SEARCHPAGE_ID = "org.eclipse.ptp.ui.PSearchPage";

	public static final int NORMAL = 0;

	public static final int ASYNC = 1;

	protected static void showDialog(final Shell shell, final String title,
			final String message, final int style) {
		MessageBox dialog = new MessageBox(shell, style);
		dialog.setMessage(message);
		dialog.setText(title);
		dialog.open();
	}

	protected static void showDialogAsync(final Shell shell,
			final String title, final String message, final int style) {
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				showDialog(shell, title, message, style);
			}
		});
	}

	public static void showWarningDialog(final Shell shell, final String title,
			final String message, int flag) {
		if (flag == NORMAL)
			showDialog(shell, title, message, SWT.ICON_WARNING | SWT.OK);
		else
			showDialogAsync(shell, title, message, SWT.ICON_WARNING | SWT.OK);
	}

	public static void showInformationDialog(final Shell shell,
			final String title, final String message, int flag) {
		if (flag == NORMAL)
			showDialog(shell, title, message, SWT.ICON_INFORMATION | SWT.OK);
		else
			showDialogAsync(shell, title, message, SWT.ICON_INFORMATION
					| SWT.OK);
	}

	public static void showErrorDialog(final Shell shell, final String title,
			final String message, int flag) {
		if (flag == NORMAL)
			showDialog(shell, title, message, SWT.ICON_ERROR | SWT.OK);
		else
			showDialogAsync(shell, title, message, SWT.ICON_ERROR | SWT.OK);
	}

	// IStatus can be retrieved from CoreException e.getStatus()
	public static void showDetailErrorDialog(final Shell shell,
			final String title, final String message, final IStatus status) {
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				ErrorDialog.openError(shell, title, message, status);
			}
		});
	}

	public static void showErrorDialog(final String title,
			final String message, final IStatus status) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final Shell shell = Display.getDefault().getActiveShell();
				if (status == null)
					MessageDialog.openError(shell, title, message);
				else
					ErrorDialog.openError(shell, title, message, status);
			}
		});
	}

	public static boolean showQuestionDialog(final String title,
			final String message) {
		final Shell shell = Display.getDefault().getActiveShell();
		return MessageDialog.openQuestion(shell, title, message);
	}

	public static void showView(final String viewID) {
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						try {
							displayView(viewID);
						} catch (NullPointerException e) {
							System.out.println("Show View err: "
									+ e.getMessage());
						}
					}
				});
			}
		});
	}

	public static void switchPerspectiveTo(final String perspectiveID) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = getActivePage();
				if (page.getPerspective().getId().equals(perspectiveID))
					return;
				IWorkbench bench = PTPCorePlugin.getDefault().getWorkbench();
				try {
					bench.showPerspective(perspectiveID, PTPCorePlugin
							.getActiveWorkbenchWindow());
				} catch (WorkbenchException e) {
					showErrorDialog("Display Error",
							"Cannot switch Perspective to: " + perspectiveID, e
									.getStatus());
				}
			}
		});
	}

	public static IWorkbenchPage getActivePage() {
		return PTPCorePlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
	}

	public static void displayView(String viewID) throws NullPointerException {
		IWorkbenchPage page = getActivePage();
		try {
			page.showView(viewID);
		} catch (PartInitException e) {
			showErrorDialog("Display Error", "Cannot show View ID: " + viewID,
					e.getStatus());
		}
	}

	public static IViewPart findView(String viewID) {
		IWorkbenchPage page = getActivePage();
		return page.findView(viewID);
	}

	public static void closeView(String viewID) {
		IViewPart viewPart = findView(viewID);
		if (viewPart == null)
			return;
		viewPart.dispose();
	}
}