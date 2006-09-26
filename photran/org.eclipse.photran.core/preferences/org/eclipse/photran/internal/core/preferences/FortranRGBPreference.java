package org.eclipse.photran.internal.core.preferences;

import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * Parent class for preferences which hold an <code>RGB</code> value.
 * 
 * This should be eliminated and <code>FortranPreference</code> parameterized by type once we
 * start using Java 5.
 * 
 * @author joverbey
 */
public final class FortranRGBPreference extends FortranPreference
{
    private RGB defaultValue;
    
    public FortranRGBPreference(String name, RGB defaultValue)
    {
        super("rgb", name);
        this.defaultValue = defaultValue;
    }
    
    public final void setDefault()
    {
        getPreferenceStore().setDefault(getName(), StringConverter.asString(defaultValue));
    }

    /**
     * Sets the value of this preference
     * @param preferences
     */
    public void setValue(RGB value)
    {
        getPreferenceStore().setValue(getName(), StringConverter.asString(value));
        savePreferenceStore();
    }

    /**
     * @return the value of this preference
     */
    public RGB getValue()
    {
        try
        {
            return StringConverter.asRGB(getPreferenceStore().getString(getName()));
        }
        catch (DataFormatException e)
        {
            setValue(defaultValue);
            return defaultValue;
        }
    }
}