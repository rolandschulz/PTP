package org.eclipse.photran.internal.core.preferences;

import org.eclipse.core.runtime.Preferences;

/**
 * Parent class for preferences which hold a <code>boolean</code> value.
 * 
 * This should be eliminated and <code>FortranPreference</code> parameterized by type once we
 * start using Java 5.
 * 
 * @author joverbey
 */
public abstract class FortranBooleanPreference extends FortranPreference
{
    /**
     * @return the default value for this preference
     */
    public boolean getDefaultValue()
    {
        return false;
    }

    /**
     * Sets the default value for this preference
     * @param preferences
     */
    public void setDefault(Preferences preferences)
    {
        preferences.setDefault(getName(), getDefaultValue());
    }

    /**
     * Sets the value of this preference
     * @param preferences
     */
    public void setValue(Preferences preferences, boolean value)
    {
        preferences.setValue(getName(), value);
    }

    /**
     * @return the value of this preference
     */
    public boolean getValue(Preferences preferences)
    {
        return preferences.getBoolean(getName());
    }
}