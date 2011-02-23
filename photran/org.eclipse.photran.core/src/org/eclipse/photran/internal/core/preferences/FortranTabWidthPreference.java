/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.preferences;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

/**
 * A workspace preference for the (Fortran-specific) tab width.
 * 
 * @author Jeff Overbey
 * 
 * @see FortranIntegerPreference
 * @see FortranPreference
 */
@SuppressWarnings("restriction")
public class FortranTabWidthPreference extends FortranIntegerPreference
{
    public FortranTabWidthPreference(String name, int defaultValue, int upperLimit, int lowerLimit)
    {
        super(name, defaultValue, upperLimit, lowerLimit);
    }

    public static String spaces(int count)
    {
        if (count < 0) throw new IllegalArgumentException();

        switch (count)
        {
            case 0:  return ""; //$NON-NLS-1$
            case 1:  return " "; //$NON-NLS-1$
            case 2:  return "  "; //$NON-NLS-1$
            case 3:  return "   "; //$NON-NLS-1$
            case 4:  return "    "; //$NON-NLS-1$
            case 5:  return "     "; //$NON-NLS-1$
            case 6:  return "      "; //$NON-NLS-1$
            case 7:  return "       "; //$NON-NLS-1$
            case 8:  return "        "; //$NON-NLS-1$
            case 9:  return "         "; //$NON-NLS-1$
            case 10: return "          "; //$NON-NLS-1$
            case 11: return "           "; //$NON-NLS-1$
            case 12: return "            "; //$NON-NLS-1$
            case 13: return "             "; //$NON-NLS-1$
            case 14: return "              "; //$NON-NLS-1$
            case 15: return "               "; //$NON-NLS-1$
            case 16: return "                "; //$NON-NLS-1$
            default: return "                " + spaces(count-16); //$NON-NLS-1$
        }
    }
    
    /**
     * Determines the tab width to use in the Fortran editor.
     * <ol>
     * <li> First, it looks at the custom Fortran editor preference.  If it is
     *      non-zero, this width is used.
     * <li> If the custom preference is not set, the workspace-wide text editor
     *      preference is used instead.
     * </ol>
     */
    @Override public int getValue()
    {
        int customValue = super.getValue();
        if (customValue > 0)
            return customValue;
        else
            return EditorsPlugin.getDefault().getPreferenceStore().getInt(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
    }

    /**
     * @return a string consisting of {@link #getValue()} spaces
     */
    public String getStringOfSpaces()
    {
        return spaces(getValue());
    }
}
