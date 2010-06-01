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
 * This class handles events where the user right-clicks and "Copy"-ies from table
 * 
 * @author Tim Yuvashev
 * @author Esfar Huq, Rui Wang - Modified the asText() method, removing id and marker detail fields
 */
public class CopyMarkedFileAction extends Action
{
    private static final String SEPARATOR = " "; //$NON-NLS-1$
    
    private VPGProblemView problemsView = null;

    public CopyMarkedFileAction(VPGProblemView view)
    {
        super(Messages.CopyMarkedFileAction_Copy);
        problemsView = view;
    }
    
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY);
        return ImageDescriptor.createFromImage(img);
    }
    
    @Override
    public void run() 
    {
        ISelection sel = problemsView.getSite().getSelectionProvider().getSelection();
        problemsView.getClipboard().setContents(
            new Object[]   { asText(sel)                },
            new Transfer[] { TextTransfer.getInstance() });
        
    }
    
    protected String asText(ISelection sel)
    {
        StringBuilder sb = new StringBuilder();
        for (Object element : ((IStructuredSelection)sel).toList())
        {
            if (element instanceof IMarker)
            {
                IMarker marker = (IMarker)element;
                sb.append(asText(marker));
                sb.append("\n"); //$NON-NLS-1$
            }
        }
        return sb.toString();
    }

    protected String asText(IMarker marker)
    {
        String markerMsg    = Messages.CopyMarkedFileAction_DescriptionLabel + MarkerUtilities.getMessage(marker);
        String markerRes    = Messages.CopyMarkedFileAction_ResourceLabel + marker.getResource().getName().toString();
        String markerPath   = Messages.CopyMarkedFileAction_PathLabel + marker.getResource().getProjectRelativePath().toString();
        String markerLoc    = Messages.CopyMarkedFileAction_LocationLineLabel + String.valueOf(MarkerUtilities.getLineNumber(marker));
        
        String result = markerMsg  + SEPARATOR +
                        markerRes  + SEPARATOR +
                        markerPath + SEPARATOR +
                        markerLoc  + SEPARATOR;
        return result;
    }
}
