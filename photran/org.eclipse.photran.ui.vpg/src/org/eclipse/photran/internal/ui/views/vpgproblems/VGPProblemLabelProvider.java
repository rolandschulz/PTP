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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Based on samples provided in Java DeveloperÕs Guide to Eclipse,
 * Chapter 18 (http://www.jdg2e.com/ch18.views/doc/index.htm);
 * © Copyright International Business Machines Corporation, 2003, 2004, 2006.
 * All Rights Reserved.
 * Code or samples provided therein are provided without warranty of any kind.
 *
 * @author Timofey Yuvashev
 */
public class VGPProblemLabelProvider implements ITableLabelProvider
{
    private static final int MAX_NUM_CHARS = 60;
    //TODO: Possibly move images to the project's icon folder?
    private final String WARNING_PIC_PATH = "/Users/tyuvash2/workspace/org.eclipse.cdt.ui/icons/obj16/warning_obj.gif";
    private final String ERROR_PIC_PATH = "/Users/tyuvash2/workspace/org.eclipse.cdt.ui/icons/obj16/error_obj.gif";
    private final String INFO_PIC_PATH = "/Users/tyuvash2/workspace/org.eclipse.cdt.ui/icons/obj16/info_obj.gif";

    private final String[] IMAGE_PATHS = {INFO_PIC_PATH, WARNING_PIC_PATH, ERROR_PIC_PATH};

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object obj, int colIndex)
    {
        //TODO: Re-locate images to this project's directory?

        //Only add an image if we are in the first column
        if(colIndex != 0)
            return null;

        IMarker m = (IMarker)obj;

        File sourceFile = null;
        int sev = MarkerUtilities.getSeverity(m);

        if(sev == IMarker.SEVERITY_INFO     ||
           sev == IMarker.SEVERITY_WARNING  ||
           sev == IMarker.SEVERITY_ERROR)
            sourceFile = new File(IMAGE_PATHS[sev]);

        if(sourceFile == null)
            return null;

        URL url = null;
        try
        {
            url = sourceFile.toURL();
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            //TODO: Should we error out, or just skip the picture and return null?
            e.printStackTrace();
        }
        ImageDescriptor imageDescr = ImageDescriptor.createFromURL(url);
        return imageDescr.createImage();
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
            // To see the columns in order, look in VGPProblemView.java
            switch(index)
            {
                case 0: return String.valueOf(m.getId());                           //Marker ID
                case 1: return getFirstMessageSentense(m);                          //Marker message
                case 2: return m.getResource().getName().toString();                //Name of file/resource
                case 3: return m.getResource().getProjectRelativePath().toString(); //Project-relative path
                case 4: return getLineWithNumber(m);                                //Line number
                case 5: return MarkerUtilities.getMarkerType(m);                    //Marker type
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
        if(fullMessage.length() > MAX_NUM_CHARS)
            return fullMessage.substring(0, MAX_NUM_CHARS-1)+"...";
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
