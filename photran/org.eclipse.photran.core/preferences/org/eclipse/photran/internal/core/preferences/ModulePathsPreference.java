package org.eclipse.photran.internal.core.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Encapsulates the Eclipse workspace preference for setting module paths.
 * 
 * @author joverbey
 */
public class ModulePathsPreference extends FortranPreference
{
    private FortranStringPreference internalStringPreference = new FortranStringPreference("modulepaths", "");
    
    public ModulePathsPreference()
    {
        super("modulepaths", "modulepaths");
    }
    
    /**
     * Parses the value of the MODULE_PATHS preference into a list of paths.
     * 
     * @param pathList
     * @return <code>String[]</code>
     */
    public static String[] parsePathList(String pathList)
    {
        // Copied verbatim from PathEditor#parseString
        StringTokenizer st = new StringTokenizer(pathList, File.pathSeparator + "\n\r");//$NON-NLS-1$
        ArrayList v = new ArrayList();
        while (st.hasMoreElements())
            v.add(st.nextElement());
        return (String[])v.toArray(new String[v.size()]);
    }

    /**
     * Grabs the current list of module paths, parses it, and returns a list of paths.
     * 
     * @return
     */
    public String[] parseCurrentValue()
    {
        return parsePathList(internalStringPreference.getValue());
    }

    public void setDefault()
    {
        internalStringPreference.setDefault();
    }
}
