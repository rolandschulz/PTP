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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Fortran Editor preference page
 * 
 * @author Jeff Overbey
 */
public class EditorPreferencePage extends AbstractFortranPreferencePage
{
    protected void createFieldEditors()
    {
        addField(new BooleanFieldEditor(FortranPreferences.ENABLE_FREE_FORM_FOLDING.getName(),
                                        "Enable folding rather than ruler in free-form Fortran editors",
                                        getFieldEditorParent()));

        addField(new BooleanFieldEditor(FortranPreferences.ENABLE_FIXED_FORM_FOLDING.getName(),
                                        "Enable folding rather than ruler in fixed-form Fortran editors",
                                        getFieldEditorParent()));
        
        addField(new Separator(getFieldEditorParent()));
        
        addField(new ColorFieldEditor(FortranPreferences.COLOR_COMMENTS.getName(),
                                      "Comments",
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_IDENTIFIERS.getName(),
                                      "Identifiers",
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_INTRINSICS.getName(),
                                      "Intrinsics",
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_KEYWORDS.getName(),
                                      "Keywords",
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_STRINGS.getName(),
                                      "Strings",
                                      getFieldEditorParent()));
    }
    
    private static class Separator extends FieldEditor
    {
        private Label label;
        
        public Separator(Composite parent)
        {
            this.createControl(parent);
        }
        
        protected void adjustForNumColumns(int numColumns)
        {
            ((GridData)label.getLayoutData()).horizontalSpan = 2;
        }

        protected void doFillIntoGrid(Composite parent, int numColumns)
        {
            label = new Label(parent, SWT.SEPARATOR);
            label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns, 1));
        }

        public int getNumberOfControls()
        {
            return 1;
        }
        
        protected void doLoad() {}
        protected void doLoadDefault() {}
        protected void doStore() {}
    }
}