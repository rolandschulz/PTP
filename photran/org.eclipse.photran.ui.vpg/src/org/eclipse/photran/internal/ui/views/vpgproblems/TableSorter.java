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
        //For the list of columns, look in VPGProblemView.java
        switch(this.propertyIndex)
        {
            //FIRST COLUMN: Description
            case 0:
            {
                String msg1 = MarkerUtilities.getMessage(m1);
                String msg2 = MarkerUtilities.getMessage(m2);
                result = msg1.compareTo(msg2);
                break;
            }
            //SECOND COLUMN: Resource
            case 1:
            {
                String resource1 = m1.getResource().getName().toString();
                String resource2 = m2.getResource().getName().toString();
                result = resource1.compareTo(resource2);
                break;
            }
            //THIRD COLUMN: Path
            case 2:
            {
                String path1 = m1.getResource().getProjectRelativePath().toString();
                String path2 = m2.getResource().getProjectRelativePath().toString();
                result = path1.compareTo(path2);
                break;
            }
        }//end switch
        
        //If it needs to be ascending sort, simply flip the resulting value
        if(!this.DESCENDING)
            result = -result;
        return result;
    }
}
