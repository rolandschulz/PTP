package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for setting the Fortran editor option to replace tabs with spaces.
 * 
 * @author Nicholas Chen
 * @author Cheah Chin Fei
 */
public class FortranFreeFormSpacePreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{

    public final static String SPACES_FOR_TABS_PREF = "spaces_for_tabs_pref";

    public final static String TAB_WIDTH_PREF = "tab_width_pref";

    private static IPreferenceStore store;

    /*
     * Creates the preference page
     */
    public FortranFreeFormSpacePreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(FortranUIPlugin.getDefault().getPreferenceStore());
        setDescription("Select your preferences");
    }

    /**
     * Call this to reset the preference for space
     * This defaults to replace tabs with spaces
     */
    public static void setDefaultSpaces()
    {
        store = FortranUIPlugin.getDefault().getPreferenceStore();
        store.setDefault(SPACES_FOR_TABS_PREF, true);
        store.setDefault(TAB_WIDTH_PREF, 4);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors()
    {
        Composite p = getFieldEditorParent();
        addField(new BooleanFieldEditor(SPACES_FOR_TABS_PREF, "&Insert spaces for tabs", p));
        addField(new IntegerFieldEditor(TAB_WIDTH_PREF, "&Number of spaces representing a tab:", p));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
        if (workbench == null)
        {
        }
    }

    /**
     * 
     * @return if we have enabled spaces for tabs
     */
    public static boolean isTabConversionEnabled()
    {
        return store.getBoolean(SPACES_FOR_TABS_PREF);
    }

    /**
     * 
     * @return the tab width preference
     */
    public static int getTabSize()
    {
        return store.getInt(TAB_WIDTH_PREF);
    }

}