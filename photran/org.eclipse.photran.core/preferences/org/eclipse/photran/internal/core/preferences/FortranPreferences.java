package org.eclipse.photran.internal.core.preferences;

import java.lang.reflect.Field;

import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.swt.graphics.RGB;

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
    private FortranPreferences() {}

    public static void initializeDefaults()
    {
        try
        {
            Field[] fields = FortranPreferences.class.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                FortranPreference pref = (FortranPreference)fields[i].get(null);
                pref.setDefault();
            }
        }
        catch (IllegalAccessException e)
        {
            throw new Error(e);
        }
    }

    public static boolean respondToPreferenceChange(String property)
    {
        try
        {
            Field[] fields = FortranPreferences.class.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                FortranPreference pref = (FortranPreference)fields[i].get(null);
                if (property.equals(pref.getName()))
                    return true;
            }
            return false;
        }
        catch (IllegalAccessException e)
        {
            throw new Error(e);
        }
    }
    
    public static void savePreferenceStore()
    {
        FortranCorePlugin.getDefault().savePluginPreferences();
    }

    public static final FortranBooleanPreference SHOW_PARSE_TREE = new FortranBooleanPreference("parsetree", false);

    public static final FortranRGBPreference COLOR_COMMENTS    = new FortranRGBPreference("comments",    new RGB(63,  127, 95 ));
    public static final FortranRGBPreference COLOR_IDENTIFIERS = new FortranRGBPreference("identifiers", new RGB(0,   0,   192));
    public static final FortranRGBPreference COLOR_INTRINSICS  = new FortranRGBPreference("intrinsics",  new RGB(96,  0,   192));
    public static final FortranRGBPreference COLOR_KEYWORDS    = new FortranRGBPreference("keywords",    new RGB(127, 0,   85 ));
    public static final FortranRGBPreference COLOR_STRINGS     = new FortranRGBPreference("strings",     new RGB(42,  0,   255));
}
