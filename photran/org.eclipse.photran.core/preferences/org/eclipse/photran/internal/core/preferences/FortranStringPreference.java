package org.eclipse.photran.internal.core.preferences;

import org.eclipse.core.runtime.Preferences;

/**
 * Parent class for preferences which hold a <code>String</code> value.
 * 
 * This should be eliminated ando <code>FortranPreference</code> parameterized by type once we
 * start using Java 5.
 * 
 * @author joverbey
 */
public abstract class FortranStringPreference extends FortranPreference
{
    /**
     * @return the default value for this preference
     */
    public abstract String getDefaultValue();

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
    public void setValue(Preferences preferences, String value)
    {
        preferences.setValue(getName(), value);
    }

    /**
     * @return the value of this preference
     */
    public String getValue(Preferences preferences)
    {
        return preferences.getString(getName());
    }
}