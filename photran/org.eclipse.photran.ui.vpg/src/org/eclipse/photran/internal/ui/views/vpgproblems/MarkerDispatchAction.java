/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.views.vpgproblems;

import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Abstract superclass for actions that operate on markers in the VPG Problems view.
 * 
 * @author Jeff Overbey
 */
public abstract class MarkerDispatchAction extends SelectionDispatchAction
{
    protected MarkerDispatchAction(IWorkbenchSite site)
    {
        super(site);
    }

    @Override
    public final void selectionChanged(IStructuredSelection selection)
    {
        setEnabled(checkEnabled(selection));
    }

    /** @return true iff the workbench selection is 1 or more markers */
    private boolean checkEnabled(IStructuredSelection selection)
    {
        if (selection.isEmpty())
            return false;
        
        for (Object element : selection.toList())
            if (!(element instanceof IMarker))
                return false;

        return true;
    }

    @Override
    public void run(IStructuredSelection selection)
    {
        if (!checkEnabled(selection))
            return;

        for (Object element : selection.toList())
        {
            if (element instanceof IMarker)
            {
                IMarker marker = (IMarker)element;
                run(marker);
            }
        }
    }

    protected abstract void run(final IMarker marker);
}
