/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.photran.internal.ui.search;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.photran.ui.vpg.Activator;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Implements the Search/Fortran menu item; an action contribution that
 * opens the Fortran Search dialog.
 * <p>
 * Based on org.eclipse.cdt.internal.ui.search.OpenCSearchPageAction
 * 
 * @author bgheorgh
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 */
public class OpenFortranSearchPageAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow fWindow;
    
    public OpenFortranSearchPageAction() {
        super();
    }

    public void init(IWorkbenchWindow window) {
        fWindow= window;
    }

    public void run(IAction action) {
        if (fWindow == null || fWindow.getActivePage() == null) {
            beep();
            return;
        }
                
        NewSearchUI.openSearchDialog(fWindow, VPGSearchPage.EXTENSION_ID);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing since the action isn't selection dependent.
    }

    public void dispose() {
        fWindow= null;
    }

    protected void beep() {
        IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
        Shell shell;
        
        if (window == null) {
            shell = null;
        } else {
            shell = window.getShell();
        }
        
        if (shell != null && shell.getDisplay() != null)
            shell.getDisplay().beep();
    }   

}
