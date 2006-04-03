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
package org.eclipse.ptp.debug.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Clement chu
 * 
 */
public class RemovePVariableActionDelegate extends AbstractPVariableAction {
	private String variable = null;
	public void dispose() {}
	public void init(IAction action) {
		this.action = action;
	}
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	public void run(IAction action) {
		if (variable != null) {
			doAction(view.getViewSite().getShell(), variable);
		}
	}
	public static void doAction(Shell shell, final String name) {
		if (shell == null) {
			shell = PTPDebugUIPlugin.getActiveWorkbenchShell();
		}
		final UIDebugManager uiManager = PTPDebugUIPlugin.getDefault().getUIDebugManager();
		/*
		final Job job = new Job("Adding variables info.") {
			public IStatus run(final IProgressMonitor monitor) {
				if (!monitor.isCanceled()) {
					try {
						PTPDebugCorePlugin.getPVariableManager().removeVariable(uiManager.getCurrentJob(), name, monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		PlatformUI.getWorkbench().getProgressService().showInDialog(shell, job);
		job.schedule();
		*/
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						PTPDebugCorePlugin.getPVariableManager().removeVariable(uiManager.getCurrentJob(), name, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
		} catch (InvocationTargetException e) {
			ErrorDialog.openError(shell, "Error", e.getMessage(), new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, e.getMessage(), e.getTargetException()));
		}
	}
	public void selectionChanged(IAction action, ISelection selection) {
		variable = null;
		if (selection instanceof IStructuredSelection) {
			IPVariable var = (IPVariable)((IStructuredSelection)selection).getFirstElement();
			if (var != null) {
				try {
					variable = var.getName();
				} catch (DebugException e) {
					variable = null;
				}
			}
		}
		action.setEnabled(variable != null);
	}	
	public void changeJobEvent(String cur_job_id, String pre_job_id) {
		if (cur_job_id == null || cur_job_id == "") {
			if (action != null)
				action.setEnabled(false);
		}
	}
	public void update(IPJob job) {}
}
