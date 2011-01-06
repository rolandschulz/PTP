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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractResourceManagerSelectionActionDelegate implements IObjectActionDelegate, IActionDelegate2 {

	private Shell targetShell;

	private final List<IResourceManagerMenuContribution> menuContribs = new ArrayList<IResourceManagerMenuContribution>();

	public void dispose() {
		menuContribs.clear();
	}

	public void init(IAction action) {
	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection ss = (IStructuredSelection) selection;
		menuContribs.clear();
		menuContribs.addAll(ss.toList());

		boolean isEnabled = isEnabled();
		action.setEnabled(isEnabled);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		targetShell = targetPart.getSite().getShell();
	}

	protected List<IResourceManagerMenuContribution> getMenuContribs() {
		return menuContribs;
	}

	protected Shell getTargetShell() {
		return targetShell;
	}

	/**
	 * @return
	 */
	protected boolean isEnabled() {
		if (menuContribs.size() == 0) {
			return false;
		}
		boolean isEnabled = true;
		for (IResourceManagerMenuContribution menuContrib : menuContribs) {
			final IPResourceManager rmManager = (IPResourceManager) menuContrib.getAdapter(IPResourceManager.class);
			if (rmManager == null) {
				return false;
			}
			if (!isEnabledFor(rmManager)) {
				isEnabled = false;
				break;
			}
		}
		return isEnabled;
	}

	/**
	 * @param rmManager
	 * @return - is this rmManager allowed with this action
	 * @since 5.0
	 */
	protected abstract boolean isEnabledFor(IPResourceManager rmManager);
}
