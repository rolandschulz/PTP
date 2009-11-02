/*******************************************************************************
 * Copyright (c) 2009 Matthew Scarpino, Eclipse Engineering LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.ui.cview.CViewLabelProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.cdtinterface.CDTInterfacePlugin;
import org.eclipse.swt.graphics.Image;

/**
 * This class provides images to the FortranView. Specifically, it checks for the contentType
 * of source files in the view and returns the Photran icon for files with Fortran-based content.
 * 
 * @author Matt Scarpino
 */
@SuppressWarnings("restriction")
public class FViewLabelProvider extends CViewLabelProvider
{
    public static final String FIXED_FORM_CONTENT_TYPE = "org.eclipse.photran.core.fixedFormFortranSource";
    public static final String FREE_FORM_CONTENT_TYPE = "org.eclipse.photran.core.freeFormFortranSource";
 
    Image fortranFileImage;
    
    public FViewLabelProvider(int textFlags, int imageFlags)
    {
        super(textFlags, imageFlags);
    }

    // This is something of a hack. Originally I tried using AdapterFactory objects and WorkspaceAdapters,
    // but they never seemed to work. This works, but it's not particularly elegant and the image only
    // shows up in the Fortran navigator.
    public Image getImage(Object element) 
    {
        if (element instanceof TranslationUnit) 
        {
            String fileName = ((TranslationUnit)element).getFile().getName();
            IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
            if(contentType.getId().equals(FIXED_FORM_CONTENT_TYPE) || contentType.getId().equals(FREE_FORM_CONTENT_TYPE))          
            {
                if(fortranFileImage == null) {
                    fortranFileImage = CDTInterfacePlugin.getImageDescriptor("icons/obj16/f_file_obj.gif").createImage();
                }
                return fortranFileImage;
            }
        }
        return super.getImage(element);
    }
    
    public void dispose() {
        if(fortranFileImage != null)
            fortranFileImage.dispose();
        super.dispose();
    }
}
