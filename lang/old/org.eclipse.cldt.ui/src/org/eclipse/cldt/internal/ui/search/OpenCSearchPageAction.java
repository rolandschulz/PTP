/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cldt.internal.ui.search;

import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author bgheorgh
 */
public class OpenCSearchPageAction implements IWorkbenchWindowActionDelegate {

	private static final String C_SEARCH_PAGE_ID= "org.eclipse.cldt.ui.CSearchPage";  //$NON-NLS-1$

	private IWorkbenchWindow fWindow;
	
	public OpenCSearchPageAction() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		if (fWindow == null || fWindow.getActivePage() == null) {
			beep();
			return;
		}
		SearchUI.openSearchDialog(fWindow, C_SEARCH_PAGE_ID);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	public void dispose() {
		fWindow= null;
	}

	protected void beep() {
		Shell shell= FortranUIPlugin.getActiveWorkbenchShell();
		if (shell != null && shell.getDisplay() != null)
			shell.getDisplay().beep();
	}	

}
