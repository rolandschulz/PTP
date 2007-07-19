package org.eclipse.photran.internal.core.preferences;

/**
 * Parent class for preferences which hold a boolean value.
 * 
 * @author Jeff Overbey
 */
public final class FortranBooleanPreference extends FortranPreference
{
    private boolean defaultValue;
    
    public FortranBooleanPreference(String name, boolean defaultValue)
    {
        super("boolean", name);
        this.defaultValue = defaultValue;
    }
    
    public void setDefault()
    {
        getPreferenceStore().setDefault(getName(), defaultValue);
    }

    /**
     * Sets the value of this preference
     * @param preferences
     */
    public void setValue(boolean value)
    {
        getPreferenceStore().setValue(getName(), value);
        savePreferenceStore();
    }

    /**
     * @return the value of this preference
     */
    public boolean getValue()
    {
        return getPreferenceStore().getBoolean(getName());
    }
}