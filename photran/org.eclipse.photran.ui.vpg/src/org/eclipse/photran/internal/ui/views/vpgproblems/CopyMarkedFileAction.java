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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * 
 * @author tyuvash2
 */
public class CopyMarkedFileAction extends Action
{
    private static final String SEPARATOR = " ";
    
    VGPProblemView myView = null;
    /**
     * @param site
     */
    public CopyMarkedFileAction(VGPProblemView view, String text)
    {
        super(text);
        myView = view;
    }
    
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY);
        return ImageDescriptor.createFromImage(img);
    }
    
    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    @Override
    public void run() 
    {
        ISelection sel = myView.getSite().getSelectionProvider().getSelection();
        myView.getClipboard().setContents(
            new Object[]{asText(sel)},
            new Transfer[] {TextTransfer.getInstance()});            
        
    }
    
    protected String asText(ISelection sel)
    {
        IStructuredSelection selection = (IStructuredSelection)sel;
        String result = "";
        for (Iterator<?> iter= selection.iterator(); iter.hasNext();) 
        {
            Object element= iter.next();
            if (element instanceof IMarker)
            {
                IMarker marker = (IMarker)element;
                result = result.concat(asText(marker));
                result = result.concat("\n");
            }
        }
        return result;
    }
        
    //TODO: We can format the output for our Markers as we want. Currently this will only 
    // get the message associated with the marker
    protected String asText(IMarker marker)
    {
        String markerID     = "ID: " + String.valueOf(marker.getId());
        String markerMsg    = "Description: " + MarkerUtilities.getMessage(marker);
        String markerRes    = "Resource: " + marker.getResource().getName().toString();
        String markerPath   = "Path: " + marker.getResource().getProjectRelativePath().toString();
        String markerLoc    = "Location: line " + String.valueOf(MarkerUtilities.getLineNumber(marker));
        String markerType   = "Error Type: " + MarkerUtilities.getMarkerType(marker);
        
        String result = markerID   + SEPARATOR +
                        markerMsg  + SEPARATOR +
                        markerRes  + SEPARATOR +
                        markerPath + SEPARATOR +
                        markerLoc  + SEPARATOR +
                        markerType + SEPARATOR;
        
        return result;
    }
}
