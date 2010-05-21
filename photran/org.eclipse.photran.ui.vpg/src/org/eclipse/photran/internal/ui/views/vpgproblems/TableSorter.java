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
import org.eclipse.photran.internal.ui.views.vpgproblems.VPGProblemView.VPGViewColumn;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Sorter for the VPG Problems view.
 * <p>
 * Based on Eclipse JFace TableView Tutorial; thanks to Lars Vogel
 * for posting the tutorial
 * (http://www.vogella.de/articles/EclipseJFaceTable/aritcle.html)
 *
 * @author Timofey Yuvashev
 * 
 * @author Esfar Huq
 * @author Rui Wang
 * 
 * Fixed the method compare() by removing some outdated cases in the switch statement
 */
public class TableSorter extends ViewerSorter
{
    private int columnIndex;
    private boolean ascending = true;

    public TableSorter()
    {
        this.columnIndex = 0;
        this.ascending = true;
    }

    /**
     * Invoked when the user clicks on a column header.  Responds by changing the sort order.
     */
    public void setColumn(int column)
    {
        if (column == this.columnIndex)
            toggleSortOrder();
        else
            changeSortColumnTo(column);
    }

    private void toggleSortOrder()
    {
        this.ascending = !this.ascending;
    }

    private void changeSortColumnTo(int column)
    {
        this.columnIndex = column;
        this.ascending = true;
    }

    /*
     * Callback invoked to compare table items for sorting.
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2)
    {
        IMarker m1 = (IMarker)e1;
        IMarker m2 = (IMarker)e2;
        return compare(m1, m2);
    }

    private int compare(IMarker m1, IMarker m2)
    {
        if (this.ascending)
            return compareAscending(m1, m2);
        else
            return compareDescending(m1, m2);
    }

    private int compareAscending(IMarker m1, IMarker m2)
    {
        switch (VPGViewColumn.values()[this.columnIndex])
        {
            case DESCRIPTION:
            {
                String msg1 = MarkerUtilities.getMessage(m1);
                String msg2 = MarkerUtilities.getMessage(m2);
                return msg1.compareTo(msg2);
            }

            case RESOURCE:
            {
                String resource1 = m1.getResource().getName().toString();
                String resource2 = m2.getResource().getName().toString();
                return resource1.compareTo(resource2);
            }

            case PATH:
            {
                String path1 = m1.getResource().getProjectRelativePath().toString();
                String path2 = m2.getResource().getProjectRelativePath().toString();
                return path1.compareTo(path2);
            }
            
            default: throw new IllegalStateException();
        }
    }

    private int compareDescending(IMarker m1, IMarker m2)
    {
        return -compareAscending(m1, m2);
    }
}
