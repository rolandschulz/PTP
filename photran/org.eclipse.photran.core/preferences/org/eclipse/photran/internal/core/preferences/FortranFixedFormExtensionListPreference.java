package org.eclipse.photran.internal.core.preferences;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.photran.core.FortranCorePlugin;

/**
 * Encapsulates the Eclipse workspace preference for setting fixed form file extensions.
 * 
 * @author joverbey
 */
public class FortranFixedFormExtensionListPreference extends FortranStringPreference
{
    public String getName()
    {
        return "org.eclipse.photran.fortranFixedFormExtensionPreference";
    }

    public String getDefaultValue()
    {
        return ".f:.for:.ftn:.fix:.fpp:.F:.FOR:.FTN:.FIX:.FPP";
    }

    public boolean shouldSetInCore()
    {
        return true;
    }

    public boolean shouldSetInUI()
    {
        return true;
    }

    /**
     * This is NOT the name of a preference value! It is the separator character used by the
     * FIXED_FORM_EXTENSION_LIST_PREFERENCE preference.
     */
    protected static final String EXTENSION_LIST_SEPARATOR = ":";

    /**
     * Combines a list of extensions into a single String suitable for storage as a preference.
     * Opposite of <code>parseExtensionList</code>.
     * @param extensions
     * @return
     */
    public static String combineExtensionListIntoSingleString(String[] extensions)
    {
        StringBuffer combinedList = new StringBuffer("");//$NON-NLS-1$

        for (int i = 0; i < extensions.length; i++)
        {
            combinedList.append(extensions[i]);
            combinedList.append(EXTENSION_LIST_SEPARATOR);
        }
        return combinedList.toString();
    }

    /**
     * Parses the value of the FIXED_FORM_EXTENSION_LIST preference into a list of file extensions,
     * each starting with a period. Opposite of <code>combineExtensionListIntoSingleString</code>.
     * 
     * @param extensionList
     * @return <code>String[]</code>
     */
    public static String[] parseExtensionList(String extensionList)
    {
        StringTokenizer st = new StringTokenizer(extensionList, EXTENSION_LIST_SEPARATOR);
        ArrayList v = new ArrayList();
        while (st.hasMoreElements())
            v.add(st.nextElement());
        return (String[])v.toArray(new String[v.size()]);
    }

    /**
     * Grabs the current list of extensions, parses it, and returns a list of extensions, each
     * starting with a period.
     * 
     * @return
     */
    public String[] parseCurrentValue()
    {
        return parseExtensionList(getValue(FortranCorePlugin.getDefault().getPluginPreferences()));
    }
}
