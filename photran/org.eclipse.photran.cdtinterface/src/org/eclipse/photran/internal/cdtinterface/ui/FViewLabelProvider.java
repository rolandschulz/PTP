/*******************************************************************************
 * Copyright (c) 2009 Matthew Scarpino, Eclipse Engineering LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.ui;

import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.ui.cview.CViewLabelProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.internal.cdtinterface.CDTInterfacePlugin;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Provides images to the {@link FortranView}.
 * <p>
 * Specifically, this class checks for the contentType of source files in the view and returns the
 * Photran icon for files with Fortran-based content.
 * 
 * @author Matt Scarpino
 */
@SuppressWarnings("restriction")
public class FViewLabelProvider extends CViewLabelProvider
{
    private static final String FORTRAN_FILE_ICON = "icons/obj16/f_file_obj.gif"; //$NON-NLS-1$

    private Image fortranFileImage;

    public FViewLabelProvider(int textFlags, int imageFlags)
    {
        super(textFlags, imageFlags);
    }

    // This is something of a hack. Originally I tried using AdapterFactory objects and
    // WorkspaceAdapters, but they never seemed to work. This works, but it's not particularly
    // elegant and the image only shows up in the Fortran navigator.
    @Override
    public Image getImage(Object element)
    {
        if (isFortranFile(element))
            return fortranFileImage();
        else
            return super.getImage(element);
    }

    private boolean isFortranFile(Object element)
    {
        if (element instanceof TranslationUnit && ((TranslationUnit)element).getFile() != null)
        {
            String fileName = ((TranslationUnit)element).getFile().getName();
            IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
            return contentType.isKindOf(FortranCorePlugin.fortranContentType());
        }
        else return false;
    }

    private Image fortranFileImage()
    {
        if (fortranFileImage == null)
            fortranFileImage = CDTInterfacePlugin.getImageDescriptor(FORTRAN_FILE_ICON).createImage();

        return fortranFileImage;
    }

    @Override
    public void dispose()
    {
        if (fortranFileImage != null)
            fortranFileImage.dispose();
        super.dispose();
    }
}