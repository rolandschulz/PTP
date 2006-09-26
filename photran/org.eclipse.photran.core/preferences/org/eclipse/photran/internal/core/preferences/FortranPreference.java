package org.eclipse.photran.internal.core.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.photran.core.FortranCorePlugin;

/**
 * Parent class for preferences that should be set in the Core and/or UI plug-ins.
 * 
 * This should be parameterized by type, and type-based subclasses like
 * <code>FortranStringPreference</code> and <code>FortranBooleanPreference</code> should be
 * eliminated once we start using Java 5.
 * 
 * @author joverbey
 */
abstract class FortranPreference
{
    private final String prefix, suffix;
    private final Preferences preferences;
    
    public FortranPreference(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
        this.preferences = FortranCorePlugin.getDefault().getPluginPreferences();
    }
    
    protected Preferences getPreferenceStore()
    {
        return preferences;
    }

    protected void savePreferenceStore()
    {
        FortranCorePlugin.getDefault().savePluginPreferences();
    }

    /**
     * @return the name of this preference, e.g., <code>org.eclipse.photran.core.myPreference</code>
     */
    public final String getName()
    {
        return prefix + "." + suffix;
    }

    /**
     * Sets the default value of this preference
     * @param preferences
     */
    public abstract void setDefault();
}
