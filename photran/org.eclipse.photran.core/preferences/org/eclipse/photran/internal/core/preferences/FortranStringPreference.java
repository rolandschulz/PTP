package org.eclipse.photran.internal.core.preferences;

public final class FortranStringPreference extends FortranPreference
{
    private String defaultValue;
    
    public FortranStringPreference(String name, String defaultValue)
    {
        super("string", name);
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
    public void setValue(String value)
    {
        getPreferenceStore().setValue(getName(), value);
        savePreferenceStore();
    }

    /**
     * @return the value of this preference
     */
    public String getValue()
    {
        return getPreferenceStore().getString(getName());
    }
}