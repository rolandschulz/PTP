/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.rmsystem.ResourceManagerStatus;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class StartResourceManagersObjectActionDelegate implements
		IObjectActionDelegate, IActionDelegate2 {

	private Shell targetShell;

	private IResourceManagerMenuContribution[] menuContribs;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		targetShell = targetPart.getSite().getShell();
	}

	public void run(IAction action) {
		// no-op
		System.out.println("StartResourceManagerObjectActionDelegate.run");
	}

	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection ss = (IStructuredSelection) selection;
		Object[] selections = ss.toArray();
		menuContribs = new IResourceManagerMenuContribution[selections.length];
		System.arraycopy(selections, 0, menuContribs, 0, menuContribs.length);
	}

	public void dispose() {
	}

	public void init(IAction action) {
	}

	public void runWithEvent(IAction action, Event event) {
		for (int i = 0; i < menuContribs.length; ++i) {
			IResourceManagerMenuContribution menuContrib = menuContribs[i];
			IResourceManager rmManager = (IResourceManager) menuContrib.getAdapter(IResourceManager.class);

			if (rmManager.getStatus().equals(ResourceManagerStatus.STARTED)) {
				MessageDialog.openInformation(targetShell,
						"Resource Manager Already Started", "Resource Manager \""
						+ rmManager.getConfiguration().getName()
						+ "\" Already Started");
				return;
			}

			try {
				rmManager.start();
			} catch (CoreException e) {
				final String message = "Unable to start ResourceManager \""
					+ rmManager.getConfiguration().getName() + "\"";
				Status status = new Status(Status.ERROR, PTPUIPlugin.PLUGIN_ID, 1,
						message, e);
				ErrorDialog dlg = new ErrorDialog(targetShell,
						"Error Starting Resource Manager", message, status,
						IStatus.ERROR);
				dlg.open();
				PTPUIPlugin.log(status);
			}
		}
	}
}
