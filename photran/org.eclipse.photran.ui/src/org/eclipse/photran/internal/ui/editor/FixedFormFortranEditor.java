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
import org.eclipse.swt.widgets.Composite;

/**
 * JFace Text editor for fixed-format Fortran source code
 * 
 * @author Jeff Overbey
 */
public class FixedFormFortranEditor extends AbstractFortranEditor
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Context (Popup) Menu
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected static String CONTEXT_MENU_ID = "#FixedFormFortranEditorContextMenu";
    
    public FixedFormFortranEditor()
    {
        setEditorContextMenuId(CONTEXT_MENU_ID);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Custom Ruler
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected FortranHorizontalRuler getFortranHorizontalRuler(Composite mainComposite)
    {
        return new FortranHorizontalRuler(getVerticalRuler(), mainComposite, true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Gray Lines
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected int[] getColumnsToDrawVerticalLinesOn()
    {
        return new int[] { 5, 6, 72 };
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Syntax Highlighting
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected ITokenScanner getTokenScanner()
    {
        return new FortranKeywordRuleBasedScanner(true, getSourceViewer());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isFixedForm()
    {
        return true;
    }
}
