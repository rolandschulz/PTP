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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rephraserengine.ui.WorkbenchSelectionInfo;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A filter action for the VPG Problems view which only displays markers on files that are inside
 * the resource(s) that are currently selected in the workbench.
 * <p>
 * If no resources are selected but the active editor is editing a file in the workspace, it will
 * show markers on that file.
 * 
 * @author Timofey Yuvashev
 * @author Esfar Huq
 * @author Rui Wang
 * @author Jeff Overbey
 */
public class SelectedResourceFilterAction extends Action implements ISelectionListener
{
    private VPGProblemView vpgView;
    
    public SelectedResourceFilterAction(VPGProblemView vpgView)
    {
        super(Messages.SelectedResourceFilterAction_FilterBySelection, AS_CHECK_BOX);
        
        this.vpgView = vpgView;
        
        setToolTipText(Messages.SelectedResourceFilterAction_FilterBySelectionTooltip);
        setChecked(false);
        
        vpgView.getTableViewer().addFilter(new MarkerResourceFilter());
        
        vpgView.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
    }
    
    @Override public void run()
    {
        vpgView.setErrorWarningFilterButtonText();
        refreshTableViewer();
    }
    
    private void refreshTableViewer()
    {
        vpgView.getTableViewer().refresh();
    }

    private class MarkerResourceFilter extends ViewerFilter
    {
        @Override
        public boolean select(Viewer viewer, Object parentElem, Object elem)
        {
            if (!SelectedResourceFilterAction.this.isChecked()) return true;
            
            IMarker marker = (IMarker)elem;
            IPath markerPath = marker.getResource().getFullPath();
            
            for (IResource res : getSelectedResources())
                if (res.getFullPath().isPrefixOf(markerPath))
                    return true;

            return false;
        }

        private List<? extends IResource> getSelectedResources()
        {
            WorkbenchSelectionInfo selectionInfo =
                new WorkbenchSelectionInfo(vpgView.getSite().getWorkbenchWindow());

            List<? extends IResource> selectedResources = selectionInfo.getSelectedResources();

            if (selectedResources.isEmpty() && selectionInfo.editingAnIFile())
                selectedResources = Collections.singletonList(selectionInfo.getFileInEditor());

            return selectedResources;
        }
    }

    // ISelectionListener Implementation //////////////////////////////////////////////////////////

    /**
     * Updates the view when the workbench selection changes. This will refresh the markers that are
     * displayed if the user has selected different resources.
     */
    public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection)
    {
        if (!(sourcepart instanceof VPGProblemView))
            refreshTableViewer();
    }
}
