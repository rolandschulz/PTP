package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * Fortran Editor preference page
 * 
 * @author joverbey
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