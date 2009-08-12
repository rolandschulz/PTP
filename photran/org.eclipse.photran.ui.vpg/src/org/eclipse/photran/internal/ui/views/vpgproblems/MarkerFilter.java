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
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * This class is responsible for filtering out the Markers that
 * we want to display in our view
 * <p>
 * Based on Eclipse JFace TableView Tutorial; thanks to Lars Vogel
 * for posting the tutorial
 * (http://www.vogella.de/articles/EclipseJFaceTable/aritcle.html)
 *
 * @author Timofey Yuvashev
 */
public class MarkerFilter extends ViewerFilter
{
    private int mySeverityID = -1;
    private boolean myIsVisible = true;

    public MarkerFilter(StructuredViewer viewer, int severityID)
    {
        super();

        assert(severityID == 0 ||
               severityID == 1 ||
               severityID == 2);

        mySeverityID = severityID;
        viewer.addFilter(this);
    }

    public boolean isVisible()
    {
        return myIsVisible;
    }

    /*
     * Sets the marker with severity @sever to value @isVisible
     * If the value of @sever is not one of three SWT.SEVERITY_*
     * values, the method does nothing.
     *
     * params:
     *  @sever      - severity of the marker we are setting
     *  @isVisible  - whether or not that type of marker will be visible
     */
    public void setVisible(boolean isVisible)
    {
        myIsVisible = isVisible;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElem, Object elem)
    {
        IMarker mark = (IMarker)elem;
        int sever = MarkerUtilities.getSeverity(mark);
        if(sever == mySeverityID)
            return myIsVisible;
        return true;
    }

}
