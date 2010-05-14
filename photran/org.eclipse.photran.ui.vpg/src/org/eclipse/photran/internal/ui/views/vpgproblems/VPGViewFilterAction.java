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
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * Filter action for the VPG Problems view.
 * 
 * @author Timofey Yuvashev
 */
public class VPGViewFilterAction extends Action
{
    private MarkerFilter markerFilter = null;
    private StructuredViewer myViewer = null;
    
    public VPGViewFilterAction(StructuredViewer viewer, String text, int severityID)
    {
        super(text, AS_CHECK_BOX);
        super.setChecked(true);
        super.setToolTipText(text);
        
        assert(severityID == IMarker.SEVERITY_INFO      ||
               severityID == IMarker.SEVERITY_WARNING   ||
               severityID == IMarker.SEVERITY_ERROR);
        
        markerFilter = new MarkerFilter(viewer, severityID);
        myViewer = viewer;
    }
    
    
    
    public void run()
    {
        assert(markerFilter != null && myViewer != null);
        
        boolean oldValue = markerFilter.isVisible();
        markerFilter.setVisible(!oldValue);
        myViewer.refresh();        
    }
}
