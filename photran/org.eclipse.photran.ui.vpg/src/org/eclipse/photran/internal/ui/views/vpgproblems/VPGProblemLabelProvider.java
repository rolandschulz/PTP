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

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Label provider for the VPG Problems view.
 * <p>
 * Based on samples provided in Java DeveloperÕs Guide to Eclipse,
 * Chapter 18 (http://www.jdg2e.com/ch18.views/doc/index.htm);
 * © Copyright International Business Machines Corporation, 2003, 2004, 2006.
 * All Rights Reserved.
 * Code or samples provided therein are provided without warranty of any kind.
 *
 * @author Timofey Yuvashev
 */
@SuppressWarnings("restriction")
public class VPGProblemLabelProvider implements ITableLabelProvider
{
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object obj, int colIndex)
    {
        // Only put images in the first column
        if (colIndex != 0) return null;

        switch (MarkerUtilities.getSeverity((IMarker)obj))
        {
            case IMarker.SEVERITY_INFO:
                return CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_INFO);
            case IMarker.SEVERITY_WARNING:
                return CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_WARNING);
            case IMarker.SEVERITY_ERROR:
                return CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_ERROR);
            default:
                return null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object item, int index)
    {
        if(item instanceof IMarker)
        {
            IMarker m = (IMarker)item;
            //Depending on which column we are populating, we return different values
            // To see the columns in order, look in VPGProblemView.java
            switch(index)
            {
                //case 0: return String.valueOf(m.getId());                           //Marker ID
                case 0: return getFirstMessageSentense(m);                          //Marker message
                case 1: return m.getResource().getName().toString();                //Name of file/resource
                case 2: return m.getResource().getProjectRelativePath().toString(); //Project-relative path
                case 3: return getLineWithNumber(m);                                //Line number
                case 4: return MarkerUtilities.getMarkerType(m);                    //Marker type
            }
        }
        return null;
    }

    private static String getLineWithNumber(IMarker marker)
    {
        int lineNum = MarkerUtilities.getLineNumber(marker);
        if(lineNum >= 0)
            return "line " + String.valueOf(lineNum);
        return "";
    }

    private static String getFirstMessageSentense(IMarker marker)
    {
        String fullMessage = MarkerUtilities.getMessage(marker);
        /*if(fullMessage.length() > MAX_NUM_CHARS)
            return fullMessage.substring(0, MAX_NUM_CHARS-1)+"...";*/
        return fullMessage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener arg0)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose()
    {}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(Object arg0, String arg1)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener arg0)
    {}

}
