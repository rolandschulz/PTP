package org.eclipse.photran.internal.ui.preferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.preferences.FortranEnableParserDebuggingPreference;
import org.eclipse.photran.internal.core.preferences.FortranFixedFormExtensionListPreference;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.preferences.FortranShowParseTreePreference;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.widgets.Composite;
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
    protected FortranFixedFormExtensionListPreference extensionListPreference = FortranPreferences.FIXED_FORM_EXTENSION_LIST;

    protected FortranEnableParserDebuggingPreference enableParserDebuggingPreference = FortranPreferences.ENABLE_PARSER_DEBUGGING;

    protected FortranShowParseTreePreference showParseTreePreference = FortranPreferences.SHOW_PARSE_TREE;

    protected Preferences corePreferences = FortranCorePlugin.getDefault().getPluginPreferences();

    protected Preferences uiPreferences = FortranUIPlugin.getDefault().getPluginPreferences();

    public FortranParserPreferencePage()
    {
        super(GRID); // Grid layout
        setPreferenceStore(FortranUIPlugin.getDefault().getPreferenceStore());
        // setDescription("Fortran parser preferences");
        initializeDefaults();
    }

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults()
    {
        extensionListPreference.setDefault(uiPreferences);
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
        addField(new ExtensionListEditor(extensionListPreference.getName(),
            "What file &extensions should correspond to fixed source form?", getFieldEditorParent()));

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

        String currentString = extensionListPreference.getValue(uiPreferences);
        extensionListPreference.setValue(corePreferences, currentString);

        boolean currentBool = enableParserDebuggingPreference.getValue(uiPreferences);
        enableParserDebuggingPreference.setValue(corePreferences, currentBool);

        currentBool = showParseTreePreference.getValue(uiPreferences);
        showParseTreePreference.setValue(corePreferences, currentBool);

        return true;
    }

    // ----- NESTED CLASS ------------------------------------------------

    private static final class ExtensionListEditor extends ListEditor
    {
        private ExtensionListEditor(String name, String labelText, Composite parent)
        {
            super(name, labelText, parent);
        }

        // Combines the given list of items into a single string. This
        // method is the converse of parseString.
        protected String createList(String[] items)
        {
            return FortranFixedFormExtensionListPreference
                .combineExtensionListIntoSingleString(items);
        }

        // Creates and returns a new item for the list.
        protected String getNewInputObject()
        {
            final Pattern extensionPattern = Pattern.compile("\\.?([A-Za-z0-9_\\-]+)");

            InputDialog dialog = new InputDialog(getShell(), "File Extension",
                "Enter a file extension that should correspond to fixed source form:", "",
                new IInputValidator()
                {
                    public String isValid(String newText)
                    {
                        if (extensionPattern.matcher(newText).matches())
                            return null;
                        else if (newText.equals("") || newText.equals("."))
                            return "The extension must contain at least one character";
                        else
                            return "The extension should only contain letters, numbers, underscores, and hyphens; it should contain at least one character.";
                    }
                });
            dialog.setBlockOnOpen(true);
            if (dialog.open() != InputDialog.OK) return null;

            Matcher extensionMatcher = extensionPattern.matcher(dialog.getValue());
            if (!extensionMatcher.find()) return null;
            String extension = extensionMatcher.group(1);
            if (!extension.startsWith(".")) extension = "." + extension;
            return extension;
        }

        // Splits the given string into a list of strings. This method
        // is the converse of createList.
        protected String[] parseString(String stringList)
        {
            return FortranFixedFormExtensionListPreference.parseExtensionList(stringList);
        }
    }
}
