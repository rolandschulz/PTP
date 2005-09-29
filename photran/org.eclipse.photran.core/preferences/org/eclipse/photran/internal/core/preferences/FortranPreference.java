package org.eclipse.photran.internal.core.preferences;

import org.eclipse.core.runtime.Preferences;

/**
 * Parent class for preferences that should be set in the Core and/or UI plug-ins.
 * 
 * This should be parameterized by type, and type-based subclasses like
 * <code>FortranStringPreference</code> and <code>FortranBooleanPreference</code> should be
 * eliminated once we start using Java 5.
 * 
 * @author joverbey
 */
public abstract class FortranPreference
{
    /**
     * @return the name of this preference, e.g., <code>org.eclipse.photran.core.myPreference</code>
     */
    public abstract String getName();

    /**
     * @return <code>true</code> iff the preference should be stored in the core plugin's
     *         preferences
     */
    public abstract boolean shouldSetInCore();

    /**
     * @return <code>true</code> iff the preference should be stored in the UI plugin's
     *         preferences
     */
    public abstract boolean shouldSetInUI();

    /**
     * Sets the default value for this preference
     * @param preferences
     */
    public abstract void setDefault(Preferences preferences);
}
