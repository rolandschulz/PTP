/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.internal.ui.cview.CView;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;

/**
 * The Fortran Projects View is just the C/C++ Projects View with a different name.
 * 
 * @author Jeff Overbey
 * @author Matt Scarpino - 7/20/2009 - Updated to access Fortran-specific label provider.
 */
@SuppressWarnings("restriction")
public class FortranView extends CView
{
    public static final String FORTRAN_VIEW_ID = "org.eclipse.photran.ui.FortranView";
    
    protected CUILabelProvider createLabelProvider()
    {
        return new FViewLabelProvider(AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS, AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | CElementImageProvider.SMALL_ICONS);
    }
}