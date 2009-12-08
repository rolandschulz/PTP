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
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.swt.widgets.Composite;

/**
 * JFace Text editor for free-format Fortran source code
 * 
 * @author Jeff Overbey
 */
public class FreeFormFortranEditor extends AbstractFortranEditor
{
    public static final String EDITOR_ID = "org.eclipse.photran.ui.FreeFormFortranEditor";
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Context (Popup) Menu
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected static String CONTEXT_MENU_ID = "#FreeFormFortranEditorContextMenu";
    
    public FreeFormFortranEditor()
    {
        setEditorContextMenuId(CONTEXT_MENU_ID);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Custom Ruler
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean shouldDisplayHorizontalRulerRatherThanFolding()
    {
        return FortranPreferences.ENABLE_FREE_FORM_FOLDING.getValue() == false;
    }

    protected FortranHorizontalRuler getFortranHorizontalRuler(Composite mainComposite)
    {
        return new FortranHorizontalRuler(getVerticalRuler(), mainComposite, false);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Gray Lines
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected int[] getColumnsToDrawVerticalLinesOn()
    {
        return new int[] { 132 };
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Syntax Highlighting
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected ITokenScanner getTokenScanner()
    {
        return new FortranKeywordRuleBasedScanner(false, getSourceViewer());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isFixedForm()
    {
        return false;
    }
}
