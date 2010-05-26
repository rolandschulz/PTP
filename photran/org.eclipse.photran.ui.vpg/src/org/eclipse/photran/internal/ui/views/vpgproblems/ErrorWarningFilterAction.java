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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * A filter action for the VPG Problems view which only displays markers that have a particular
 * severity.
 * 
 * @author Timofey Yuvashev
 * @author Esfar Huq
 * @author Rui Wang
 * @author Jeff Overbey
 */

public class ErrorWarningFilterAction extends Action
{
    private StructuredViewer viewer;
    
    public ErrorWarningFilterAction(StructuredViewer viewer, int severity)
    {
        super(getLabel(severity), AS_CHECK_BOX);
        this.viewer = viewer;
        setChecked(true);
        viewer.addFilter(new MarkerSeverityFilter(severity));
    }
    
    private static String getLabel(int severity)
    {
        switch (severity)
        {
            case IMarker.SEVERITY_ERROR:   return "Errors";
            case IMarker.SEVERITY_WARNING: return "Warnings";
            default: throw new IllegalStateException();
        }
    }

    @Override
    public void run()
    {
        viewer.refresh();
    }

    private class MarkerSeverityFilter extends ViewerFilter
    {
        private int severityToPermit;
        
        private MarkerSeverityFilter(int severity)
        {
            this.severityToPermit = severity;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElem, Object elem)
        {
            IMarker marker = (IMarker)elem;
            
            if (MarkerUtilities.getSeverity(marker) == severityToPermit)
                return ErrorWarningFilterAction.this.isChecked(); // Allow only if button pushed
            else
                return true; // We're not interested in this marker; let another filter handle it
        }
    }
}
