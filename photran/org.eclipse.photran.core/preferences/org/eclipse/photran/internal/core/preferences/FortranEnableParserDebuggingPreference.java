package org.eclipse.photran.internal.core.preferences;

/**
 * Encapsulates the Eclipse workspace preference for enabling parser debugging.
 * 
 * @author joverbey
 */
public class FortranEnableParserDebuggingPreference extends FortranBooleanPreference
{
    public String getName()
    {
        return "org.eclipse.photran.fortranEnableParserDebuggingPreference";
    }

    public boolean shouldSetInCore()
    {
        return true;
    }

    public boolean shouldSetInUI()
    {
        return true;
    }
}
