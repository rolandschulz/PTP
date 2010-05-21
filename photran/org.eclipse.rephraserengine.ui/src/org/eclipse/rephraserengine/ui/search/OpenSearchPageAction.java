/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     UIUC - Fortran modifications, Rephraser Engine modifications
 *******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.rephraserengine.ui.search;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.internal.Workbench;

/**
 * Implements the Search/Fortran menu item; an action contribution that opens the Fortran Search
 * dialog.
 * <p>
 * Based on org.eclipse.cdt.internal.ui.search.OpenCSearchPageAction
 * 
 * @author bgheorgh
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 * @author Jeff Overbey - moved into Rephraser Engine
 * 
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class OpenSearchPageAction implements IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow fWindow;

    public void init(IWorkbenchWindow window)
    {
        fWindow = window;
    }

    public void run(IAction action)
    {
        if (fWindow == null || fWindow.getActivePage() == null)
        {
            beep();
            return;
        }

        NewSearchUI.openSearchDialog(fWindow, searchPageID());
    }

    /**
     * @return the ID of a search page, i.e., the ID of a <tt>page</tt> element contributed to the
     *         <tt>org.eclipse.search.searchPages</tt> extension point
     */
    protected abstract String searchPageID();

    public void selectionChanged(IAction action, ISelection selection)
    {
        // do nothing since the action isn't selection dependent.
    }

    public void dispose()
    {
        fWindow = null;
    }

    protected void beep()
    {
        IWorkbenchWindow window = Workbench.getInstance().getActiveWorkbenchWindow();
        Shell shell;

        if (window == null)
        {
            shell = null;
        }
        else
        {
            shell = window.getShell();
        }

        if (shell != null && shell.getDisplay() != null) shell.getDisplay().beep();
    }
}
