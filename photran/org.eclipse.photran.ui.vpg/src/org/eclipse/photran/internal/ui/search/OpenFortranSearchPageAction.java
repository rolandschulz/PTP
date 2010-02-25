/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import org.eclipse.rephraserengine.ui.search.OpenSearchPageAction;

/**
 * An action that opens the Fortran Search dialog.  This appears as the Search &gt; Fortran...
 * menu item.
 * 
 * @author Jeff Overbey
 */
public class OpenFortranSearchPageAction extends OpenSearchPageAction
{
    @Override protected String searchPageID()
    {
        return VPGSearchPage.EXTENSION_ID;
    }
}
