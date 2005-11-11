package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.preferences.FortranEnableParserDebuggingPreference;
import org.eclipse.photran.internal.core.preferences.FortranModulePathsPreference;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.preferences.FortranShowParseTreePreference;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The Fortran Parser preference page, which allows the user to determine what file extensions
 * should be associated with fixed source form.
 * <p>
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage </samp>, we can use the field support built into
 * JFace that allows us to create a page that is small and knows how to save, restore and apply
 * itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * 
 * @author Jeff Overbey based on sample code by Nicholas Chun Yuan Chen and Chin Fei Cheah
 */
public class FortranParserPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{
    protected FortranModulePathsPreference modulePathsPreference = FortranPreferences.MODULE_PATHS;

    protected FortranEnableParserDebuggingPreference enableParserDebuggingPreference = FortranPreferences.ENABLE_PARSER_DEBUGGING;

    protected FortranShowParseTreePreference showParseTreePreference = FortranPreferences.SHOW_PARSE_TREE;

    protected Preferences corePreferences = FortranCorePlugin.getDefault().getPluginPreferences();

    protected Preferences uiPreferences = FortranUIPlugin.getDefault().getPluginPreferences();

    public FortranParserPreferencePage()
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
        enableParserDebuggingPreference.setDefault(uiPreferences);
        showParseTreePreference.setDefault(uiPreferences);
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

        addField(new BooleanFieldEditor(enableParserDebuggingPreference.getName(),
            "(Debugging) Enable parser &debugging output", getFieldEditorParent()));

        addField(new BooleanFieldEditor(showParseTreePreference.getName(),
            "(Debugging) Show entire parse tree rather than Outline view", getFieldEditorParent()));
    }

    public void init(IWorkbench workbench)
    {
    }

    public boolean performOk()
    {
        if (!super.performOk()) return false;

        String currentValue = modulePathsPreference.getValue(uiPreferences);
        modulePathsPreference.setValue(corePreferences, currentValue);

        boolean currentBool = enableParserDebuggingPreference.getValue(uiPreferences);
        enableParserDebuggingPreference.setValue(corePreferences, currentBool);

        currentBool = showParseTreePreference.getValue(uiPreferences);
        showParseTreePreference.setValue(corePreferences, currentBool);

        return true;
    }
}
