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
import org.eclipse.photran.internal.ui.views.vpgproblems.VPGProblemView.VPGViewColumn;
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
    public Image getColumnImage(Object obj, int colIndex)
    {
        // Only put images in the first column
        if (colIndex != VPGViewColumn.DESCRIPTION.ordinal()) return null;

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

    public String getColumnText(Object item, int index)
    {
        if(item instanceof IMarker)
        {
            IMarker marker = (IMarker)item;
            switch (VPGViewColumn.values()[index])
            {
                case DESCRIPTION: return MarkerUtilities.getMessage(marker);
                case RESOURCE:    return marker.getResource().getName().toString();
                case PATH:        return marker.getResource().getProjectRelativePath().toString();
                //case 3: return getLineWithNumber(m);
                //case 4: return MarkerUtilities.getMarkerType(m);
            }
        }
        return null;
    }

//    private static String getLineWithNumber(IMarker marker)
//    {
//        int lineNum = MarkerUtilities.getLineNumber(marker);
//        if(lineNum >= 0)
//            return Messages.bind(Messages.VPGProblemLabelProvider_LineN, lineNum);
//        return ""; //$NON-NLS-1$
//    }

    public boolean isLabelProperty(Object arg0, String arg1) { return false; }
    public void addListener(ILabelProviderListener arg0) {}
    public void removeListener(ILabelProviderListener arg0) {}
    public void dispose() {}
}
