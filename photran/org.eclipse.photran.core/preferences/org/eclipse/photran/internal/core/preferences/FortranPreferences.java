package org.eclipse.photran.internal.core.preferences;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.Preferences;

/**
 * Collection of all Fortran-specific preferences
 * 
 * FIXME-Jeff: Make sure we call
 * <code>FortranPreferences.initializeDefaults(getPluginPreferences());</code>
 * 
 * @author joverbey
 */
public final class FortranPreferences
{
    private FortranPreferences()
    {
        ;
    }

    public static void initializeDefaults(Preferences preferences) throws Exception
    {
        Field[] fields = FortranPreferences.class.getFields();
        for (int i = 0; i < fields.length; i++)
        {
            FortranPreference pref = (FortranPreference)fields[i].get(null);
            pref.setDefault(preferences);
        }
    }

    public static final FortranFixedFormExtensionListPreference FIXED_FORM_EXTENSION_LIST = new FortranFixedFormExtensionListPreference();

    public static final FortranModulePathsPreference MODULE_PATHS = new FortranModulePathsPreference();

    public static final FortranEnableParserDebuggingPreference ENABLE_PARSER_DEBUGGING = new FortranEnableParserDebuggingPreference();

    public static final FortranShowParseTreePreference SHOW_PARSE_TREE = new FortranShowParseTreePreference();
}
