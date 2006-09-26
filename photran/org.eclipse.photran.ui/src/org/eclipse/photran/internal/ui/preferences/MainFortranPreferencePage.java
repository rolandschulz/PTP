package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * Top-level Fortran preference page
 * 
 * @author joverbey
 */
public class MainFortranPreferencePage extends AbstractFortranPreferencePage
{
    protected void setDescription()
    {
    }

    protected void initializeDefaults()
    {
        FortranPreferences.SHOW_PARSE_TREE.setDefault();
    }

    protected void createFieldEditors()
    {
        addField(new BooleanFieldEditor(FortranPreferences.SHOW_PARSE_TREE.getName(),
                                        "(Debugging) Show entire parse tree rather than Outline view",
                                        getFieldEditorParent()));
    }
}