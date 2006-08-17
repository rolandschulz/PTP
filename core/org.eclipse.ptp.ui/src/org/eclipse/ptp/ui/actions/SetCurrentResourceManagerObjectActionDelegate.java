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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class SetCurrentResourceManagerObjectActionDelegate implements
		IObjectActionDelegate, IActionDelegate2 {

	private Shell targetShell;
	private IResourceManager rmManager;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		targetShell = targetPart.getSite().getShell();
	}

	public void run(IAction action) {
		// no-op
	}

	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection ss = (IStructuredSelection) selection;
		if (ss.size() != 1) {
			action.setEnabled(false);
			return;
		}
		action.setEnabled(true);
		IResourceManagerMenuContribution menuCont = (IResourceManagerMenuContribution) ss.getFirstElement();
		rmManager = (IResourceManager) menuCont.getAdapter(IResourceManager.class);
		if (rmManager == PTPCorePlugin.getDefault().getCurrentResourceManager()) {
			action.setEnabled(false);
		}
	}

	public void dispose() {
		// no-op
	}

	public void init(IAction action) {
	}

	public void runWithEvent(IAction action, Event event) {
		PTPCorePlugin.getDefault().setCurrentResourceManager(rmManager);
	}

}
