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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Based on Eclipse JFace TableView Tutorial; thanks to Lars Vogel
 * for posting the tutorial
 * (http://www.vogella.de/articles/EclipseJFaceTable/aritcle.html)
 *
 * @author Timofey Yuvashev
 */
public class TableSorter extends ViewerSorter
{
    private int propertyIndex;
    private boolean DESCENDING = true;
    private static final int LESS       = -1;
    private static final int GREATER    = 1;
    private static final int EQUAL      = 0;

    public TableSorter()
    {
        this.propertyIndex = 0;
        this.DESCENDING = true;
    }

    public TableSorter(int columnIndex, boolean descending)
    {
        this.propertyIndex = columnIndex;
        this.DESCENDING = descending;
    }

    public void setColumn(int column)
    {
        if(column == this.propertyIndex)
        {
            this.DESCENDING = !this.DESCENDING;
        }
        else
        {
            this.propertyIndex = column;
            this.DESCENDING = true;
        }
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2)
    {
        IMarker m1 = (IMarker)e1;
        IMarker m2 = (IMarker)e2;
        int result = EQUAL;
        //Based on which column we want to sort, we compare different data.
        //For the list of columns, look in VGPProblemView.java
        switch(this.propertyIndex)
        {
            case 0:
            {
                long id1 = m1.getId();
                long id2 = m2.getId();

                if(id1 < id2)
                    result = LESS;
                else if(id1 > id2)
                    result = GREATER;
                else
                    result = EQUAL;
                break;
            }
            case 1:
            {
                String msg1 = MarkerUtilities.getMessage(m1);
                String msg2 = MarkerUtilities.getMessage(m2);
                result = msg1.compareTo(msg2);
                break;
            }
            case 2:
            {
                String resource1 = m1.getResource().getName().toString();
                String resource2 = m2.getResource().getName().toString();
                result = resource1.compareTo(resource2);
                break;
            }
            case 3:
            {
                String path1 = m1.getResource().getProjectRelativePath().toString();
                String path2 = m2.getResource().getProjectRelativePath().toString();
                result = path1.compareTo(path2);
                break;
            }
            case 4:
            {
                int line1 = m1.getAttribute(IMarker.LINE_NUMBER, -1);
                int line2 = m2.getAttribute(IMarker.LINE_NUMBER, -1);
                if(line1 < line2)
                    result = LESS;
                else if(line1 > line2)
                    result = GREATER;
                else
                    result = EQUAL;
                break;
            }
            case 5:
            {
                String type1 = MarkerUtilities.getMarkerType(m1);
                String type2 = MarkerUtilities.getMarkerType(m2);
                result = type1.compareTo(type2);
                break;
            }
        }
        //If it needs to be ascending sort, simply flip the resulting value
        if(!this.DESCENDING)
            result = -result;
        return result;
    }
}
