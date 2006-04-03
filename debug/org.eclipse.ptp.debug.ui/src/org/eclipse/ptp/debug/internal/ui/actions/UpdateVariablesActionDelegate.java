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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author clement chu
 *
 */
public class UpdateVariablesActionDelegate extends AbstractPVariableAction {
	public void run(IAction action) {
		doAction(view.getViewSite().getShell());
	}
	public static void doAction(Shell shell) {
		if (getCurrentRunningJob() == null)
			return;
		
		if (shell == null) {
			shell = PTPDebugUIPlugin.getActiveWorkbenchShell();
		}
		final Job job = new Job("Updating variables info.") {
			public IStatus run(final IProgressMonitor monitor) {
				if (!monitor.isCanceled()) {
					try {
						UIDebugManager uiManager = PTPDebugUIPlugin.getDefault().getUIDebugManager();
						PTPDebugCorePlugin.getPVariableManager().updateVariableResults(uiManager.getCurrentJob(), uiManager.getCurrentSetId(), monitor);
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
	}
	
	public static void doAction(Shell shell, final String name) {
		if (getCurrentRunningJob() == null)
			return;

		if (shell == null) {
			shell = PTPDebugUIPlugin.getActiveWorkbenchShell();
		}
		final UIDebugManager uiManager = PTPDebugUIPlugin.getDefault().getUIDebugManager();
		final Job job = new Job("Updating variables info.") {
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
	}
}
