/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.views.vpgproblems;

import java.util.Iterator;

import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

/**
 * Action to remove a marker.
 * 
 * @author Timofey Yuvashev
 */
public class RemoveMarkerAction extends SelectionDispatchAction
{

    /**
     * @param site
     */
    public RemoveMarkerAction(IWorkbenchSite site)
    {
        super(site);
        setText(Messages.RemoveMarkerAction_Delete);
        setToolTipText(Messages.RemoveMarkerAction_DeleteTooltip);
    }
    
    public RemoveMarkerAction(IWorkbenchSite site, String text, String toolTipText)
    {
        super(site);
        setText(text);
        setToolTipText(toolTipText);
        // TODO Auto-generated constructor stub
    }
    
    @Override public ImageDescriptor getImageDescriptor()
    {
        Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
        return ImageDescriptor.createFromImage(img);
    }
    
    /*protected void deleteMarkers(IMarker[] markers)
    {
        try
        {
            ResourcesPlugin.getWorkspace().deleteMarkers(markers);
        }
        catch (CoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
    
    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(checkEnabled(selection));
    }
    
    //Only enables if the selected type is an IMarker
    private boolean checkEnabled(IStructuredSelection selection) {
        if (selection.isEmpty())
            return false;
        for (Iterator<?> iter= selection.iterator(); iter.hasNext();) {
            Object element= iter.next();
            if (element instanceof IMarker)
                continue;
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    @Override
    public void run(IStructuredSelection selection) 
    {
        if (!checkEnabled(selection))
            return;
        
        //ArrayList<IMarker> markers = new ArrayList<IMarker>();
        for (Iterator<?> iter= selection.iterator(); iter.hasNext();) 
        {
            Object element= iter.next();
            if (element instanceof IMarker)
            {
                IMarker marker = (IMarker)element;
                try
                {
                    //FIXME: We should figure out a way to delete marker(s)
                    // from the log. Should we clear them out for the whole 
                    // resource, or just delete one(s) selected? Should user
                    // be able to delete markers that are tied to a particular
                    // resource, or only non-resource specific ones?
                    
                    //PhotranVPG.getInstance().log.clearEntriesFor(marker.getResource().getFullPath());
                    marker.delete();
                }
                catch (CoreException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //markers.add(marker);
            }
        }
       // IMarker[] marks = new IMarker[markers.size()];
       // deleteMarkers(markers.toArray(marks));
    }

}
