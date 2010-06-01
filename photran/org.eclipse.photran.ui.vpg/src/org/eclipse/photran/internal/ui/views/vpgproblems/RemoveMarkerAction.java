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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.photran.internal.ui.vpg.Activator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

/**
 * Action to remove a marker.
 * 
 * @author Timofey Yuvashev
 */
public class RemoveMarkerAction extends MarkerDispatchAction
{
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
    }
    
    @Override public ImageDescriptor getImageDescriptor()
    {
        Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
        return ImageDescriptor.createFromImage(img);
    }
    
    @Override protected void run(final IMarker marker)
    {
        //FIXME: We should figure out a way to delete marker(s)
        // from the log. Should we clear them out for the whole 
        // resource, or just delete one(s) selected? Should user
        // be able to delete markers that are tied to a particular
        // resource, or only non-resource specific ones?
        
        //PhotranVPG.getInstance().log.clearEntriesFor(marker.getResource().getFullPath());
        try
        {
            marker.delete();
        }
        catch (CoreException e)
        {
            Activator.log(e);
            e.printStackTrace();
        }
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
}
