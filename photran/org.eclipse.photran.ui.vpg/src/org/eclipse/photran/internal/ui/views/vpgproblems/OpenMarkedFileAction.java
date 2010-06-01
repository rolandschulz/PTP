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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Action to open a marker.
 * <p>
 * Based on OpenAction from CDT
 * (org.eclipse.cdt.internal.ui.actions.OpenAction)
 *
 * @author Timofey Yuvashev
 */
@SuppressWarnings("restriction")
public class OpenMarkedFileAction extends MarkerDispatchAction
{
    public OpenMarkedFileAction(IWorkbenchSite site)
    {
        super(site);
        setText(Messages.OpenMarkedFileAction_GoTo);
        setToolTipText(Messages.OpenMarkedFileAction_GoToTooltip);
    }

    @SuppressWarnings("deprecation")
    @Override public ImageDescriptor getImageDescriptor()
    {
        Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD_HOVER);
        return ImageDescriptor.createFromImage(img);
    }

    @Override public void run(IMarker marker)
    {
        IWorkspaceRoot r = ResourcesPlugin.getWorkspace().getRoot();

        //TODO: Currently, trying to get the starting and ending
        // positions of the marker returns -1. Probably need to fix it
        // later, so that we can display the exact location of marker
        // in the file.

        int start = marker.getAttribute(IMarker.CHAR_START, -1);
        int end = marker.getAttribute(IMarker.CHAR_END, -1);

        IPath path = marker.getResource().getFullPath();
        IFile fileToOpen = r.getFile(path);
        try
        {
            OpenActionUtil.open(fileToOpen, true);
            IEditorPart editor =
                Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            AbstractTextEditor textEditor = null;
            if(editor instanceof AbstractTextEditor)
                textEditor = (AbstractTextEditor)editor;
            if(textEditor != null)
                textEditor.selectAndReveal(start, end-start);

        }
        catch (PartInitException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (CModelException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
