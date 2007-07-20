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
package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * Fortran Editor preference page
 * 
 * @author Jeff Overbey
 */
public class EditorPreferencePage extends AbstractFortranPreferencePage
{
    ColorFieldEditor cx, sx;
    protected void createFieldEditors()
    {
        addField(new ColorFieldEditor(FortranPreferences.COLOR_COMMENTS.getName(),
                                      "Comments",
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_IDENTIFIERS.getName(),
                                      "Identifiers",
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_INTRINSICS.getName(),
                                      "Intrinsics",
                                      getFieldEditorParent()));
        addField(cx=new ColorFieldEditor(FortranPreferences.COLOR_KEYWORDS.getName(),
                                      "Keywords",
                                      getFieldEditorParent()));
        addField(sx=new ColorFieldEditor(FortranPreferences.COLOR_STRINGS.getName(),
                                      "Strings",
                                      getFieldEditorParent()));
    }
}