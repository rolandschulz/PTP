package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.preferences.FortranModulePathsPreference;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The Fortran Module Paths preference page, which allows the user to determine directories should
 * be searched for modules when a USE statement is found. Theoretically, this information is useful
 * to the parser and the managed build system.
 * <p>
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into
 * JFace that allows us to create a page that is small and knows how to save, restore and apply
 * itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * 
 * @author Jeff Overbey based on sample code by Nicholas Chun Yuan Chen and Chin Fei Cheah
 */

public class FortranModulePathsPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{
    protected FortranModulePathsPreference modulePathsPreference = FortranPreferences.MODULE_PATHS;

    protected Preferences corePreferences = FortranCorePlugin.getDefault().getPluginPreferences();

    protected Preferences uiPreferences = FortranUIPlugin.getDefault().getPluginPreferences();

    public FortranModulePathsPreferencePage()
    {
        super(GRID); // Grid layout
        setPreferenceStore(FortranUIPlugin.getDefault().getPreferenceStore());
        setDescription("When a USE or INCLUDE statement is encountered in a "
            + "Fortran program, or when an #include directive is found, "
            + "the following directories will be searched, in the order listed, "
            + "for the indicated file or module.");
        initializeDefaults();
    }

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults()
    {
        modulePathsPreference.setDefault(uiPreferences);
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    public void createFieldEditors()
    {
        addField(new PathEditor(modulePathsPreference.getName(), "&Module Paths",
            "Select a directory to be searched for Fortran modules", getFieldEditorParent()));
    }

    public void init(IWorkbench workbench)
    {
    }

    public boolean performOk()
    {
        if (!super.performOk()) return false;
        String currentValue = modulePathsPreference.getValue(uiPreferences);
        modulePathsPreference.setValue(corePreferences, currentValue);
        return true;
    }
}
