package org.eclipse.photran.internal.core.preferences;

/**
 * Encapsulates the Eclipse workspace preference for showing a full parse tree rather than the
 * normal Outline view
 * 
 * @author joverbey
 */
public class FortranShowParseTreePreference extends FortranBooleanPreference
{
    public String getName()
    {
        return "org.eclipse.photran.fortranShowParseTreePreference";
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
