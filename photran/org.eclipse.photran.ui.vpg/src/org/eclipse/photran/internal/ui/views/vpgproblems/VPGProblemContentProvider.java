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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the VPG Problems View.
 * <p>
 * Based on samples provided in Java Developer’s Guide to Eclipse,
 * Chapter 18 (http://www.jdg2e.com/ch18.views/doc/index.htm);
 * © Copyright International Business Machines Corporation, 2003, 2004, 2006.
 * All Rights Reserved.
 * Code or samples provided therein are provided without warranty of any kind.
 *
 * @author Timofey Yuvashev
 */
public class VPGProblemContentProvider implements IStructuredContentProvider,
                                                  IResourceChangeListener
{
    private StructuredViewer viewer = null;
    private List<IMarker> input     = null;

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object[] getElements(Object element)
    {
        List<IMarker> markers = (List<IMarker>)(element);
        return markers.toArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
        if(this.input != null)
        {
            //input.removeResourceChangeListener(this);
            input = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void inputChanged(Viewer v, Object oldInput, Object newInput)
    {
        if(this.viewer == null)
            this.viewer = (StructuredViewer)v;

        if(this.input == null && newInput != null)
        {
            this.input = (List<IMarker>)newInput;
            //ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
            //this.input.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        }

        if(newInput == null && input != null)
        {
            //ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
            input = null;
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent arg0)
    {
        // TODO Auto-generated method stub

    }

}
