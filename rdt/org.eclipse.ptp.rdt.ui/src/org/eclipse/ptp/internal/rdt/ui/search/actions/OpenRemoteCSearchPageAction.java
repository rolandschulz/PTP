/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchPage;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 */
public class OpenRemoteCSearchPageAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow fWindow;
	
	public OpenRemoteCSearchPageAction() {
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		if (fWindow == null || fWindow.getActivePage() == null) {
			return;
		}
		NewSearchUI.openSearchDialog(fWindow, RemoteSearchPage.EXTENSION_ID);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	public void dispose() {
		fWindow= null;
	}

}
