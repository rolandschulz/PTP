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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * Fortran Editor preference page
 * 
 * @author Jeff Overbey
 */
public class EditorPreferencePage extends AbstractFortranPreferencePage
{
    private final int MAX_FIXED_FILE_FORMAT_OFFSET = 999;

    @Override protected void createFieldEditors()
    {
        addField(new BooleanFieldEditor(FortranPreferences.ENABLE_FREE_FORM_FOLDING.getName(),
                                        Messages.EditorPreferencePage_EnableFolding,
                                        getFieldEditorParent()));

        /* Fixed form folding does not seem to work, so disable it for now
        addField(new BooleanFieldEditor(FortranPreferences.ENABLE_FIXED_FORM_FOLDING.getName(),
                                        "Enable folding rather than ruler in fixed-form Fortran editors",
                                        getFieldEditorParent()));
        */

        addField(new BooleanFieldEditor(FortranPreferences.CONVERT_TABS_TO_SPACES.getName(),
            "Convert tabs to spaces",
            getFieldEditorParent()));

        IntegerFieldEditor intEditor = new IntegerFieldEditor(FortranPreferences.FIXED_FORM_COMMENT_COLUMN.getName(),
                                                              Messages.EditorPreferencePage_FixedFormLineLength,
                                                               getFieldEditorParent());
        intEditor.setValidRange(72, MAX_FIXED_FILE_FORMAT_OFFSET);
        intEditor.setTextLimit(3);
        addField(intEditor);
        
        //addField(new Separator(getFieldEditorParent()));
        
        addField(new ColorFieldEditor(FortranPreferences.COLOR_COMMENTS.getName(),
                                      Messages.EditorPreferencePage_CommentsFieldLabel,
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_IDENTIFIERS.getName(),
                                      Messages.EditorPreferencePage_IdentifiersFieldLabel,
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_INTRINSICS.getName(),
                                      Messages.EditorPreferencePage_IntrinsicsFieldLabel,
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_KEYWORDS.getName(),
                                      Messages.EditorPreferencePage_KeywordsFieldLabel,
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_STRINGS.getName(),
                                      Messages.EditorPreferencePage_StringsFieldLabel,
                                      getFieldEditorParent()));
        addField(new ColorFieldEditor(FortranPreferences.COLOR_CPP.getName(),
                                      Messages.EditorPreferencePage_CPPDirectivesFieldLabel,
                                      getFieldEditorParent()));
    }
    
//    private static class Separator extends FieldEditor
//    {
//        private Label label;
//        
//        public Separator(Composite parent)
//        {
//            this.createControl(parent);
//        }
//        
//        protected void adjustForNumColumns(int numColumns)
//        {
//            ((GridData)label.getLayoutData()).horizontalSpan = 2;
//        }
//
//        protected void doFillIntoGrid(Composite parent, int numColumns)
//        {
//            label = new Label(parent, SWT.SEPARATOR);
//            label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns, 1));
//        }
//
//        public int getNumberOfControls()
//        {
//            return 1;
//        }
//        
//        protected void doLoad() {}
//        protected void doLoadDefault() {}
//        protected void doStore() {}
//    }
}