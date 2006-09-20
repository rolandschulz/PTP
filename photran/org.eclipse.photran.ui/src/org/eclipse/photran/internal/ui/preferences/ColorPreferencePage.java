package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for setting Fortran editor syntax coloring preferences
 * 
 * @author Nicholas Chen
 * @author Cheah Chin Fei
 */

public class ColorPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{

    public static final String F90_STRING_CONSTANTS_COLOR_PREF = "__fortran_string_constants_color_pref";

    public static final String F90_IDENTIFIER_COLOR_PREF = "__fortran_identifier_color_pref";

    public static final String F90_INTRINSIC_COLOR_PREF = "__fortran_intrinsic_color_pref";

    public static final String F90_KEYWORD_COLOR_PREF = "__fortran_keyword_color_pref";

    public static final String F90_COMMENT_COLOR_PREF = "__fortran_comment__color_pref";

    public ColorPreferencePage()
    {
        super(GRID);
        setPreferenceStore(FortranUIPlugin.getDefault().getPreferenceStore());
        setDescription("Change the color preferences for the fortran editor");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    public void createFieldEditors()
    {
        addField(new ColorFieldEditor(F90_IDENTIFIER_COLOR_PREF, "Identifiers", getFieldEditorParent()));
        addField(new ColorFieldEditor(F90_INTRINSIC_COLOR_PREF, "Intrinsics", getFieldEditorParent()));
        addField(new ColorFieldEditor(F90_KEYWORD_COLOR_PREF, "Keywords", getFieldEditorParent()));
        addField(new ColorFieldEditor(F90_STRING_CONSTANTS_COLOR_PREF, "Strings", getFieldEditorParent()));
        addField(new ColorFieldEditor(F90_COMMENT_COLOR_PREF, "Other", getFieldEditorParent()));

    }

    /**
     * Restores the default colors when the user hits the Defaults buttons
     * 
     */
    public static void setDefaultColors()
    {
        final IPreferenceStore store = FortranUIPlugin.getDefault()
                        .getPreferenceStore();
        // PreferenceConverter.setDefault(store, F90_COMMENT_COLOR_PREF, new RGB(63, 95, 191));
        // PreferenceConverter.setDefault(store, F90_IDENTIFIER_COLOR_PREF, new RGB(0, 0, 0));
        // PreferenceConverter.setDefault(store, F90_KEYWORD_COLOR_PREF, new RGB(127, 0, 85));
        // PreferenceConverter.setDefault(store, F90_STRING_CONSTANTS_COLOR_PREF, new RGB(42, 0, 255));
        PreferenceConverter.setDefault(store, F90_COMMENT_COLOR_PREF,          new RGB(63,  127, 95));
        PreferenceConverter.setDefault(store, F90_IDENTIFIER_COLOR_PREF,       new RGB(0,   0,   192));
        PreferenceConverter.setDefault(store, F90_INTRINSIC_COLOR_PREF,        new RGB(96,  0,   192));
        PreferenceConverter.setDefault(store, F90_KEYWORD_COLOR_PREF,          new RGB(127, 0,   85));
        PreferenceConverter.setDefault(store, F90_STRING_CONSTANTS_COLOR_PREF, new RGB(42,  0,   255));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    public static boolean respondToPreferenceChange(PropertyChangeEvent event)
    {
        return (event.getProperty().equals(F90_COMMENT_COLOR_PREF)
            || event.getProperty().equals(F90_IDENTIFIER_COLOR_PREF)
            || event.getProperty().equals(F90_INTRINSIC_COLOR_PREF)
            || event.getProperty().equals(F90_KEYWORD_COLOR_PREF)
            || event.getProperty().equals(F90_STRING_CONSTANTS_COLOR_PREF));
    }
}