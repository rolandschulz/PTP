package org.eclipse.photran.internal.core.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Provides access to the module paths and include paths for a project.
 * 
 * The user may set these via the Fortran Refactoring Engine Search Paths project properties page.
 * 
 * Valid <b>property names</b> are
 * <ul>
 * <li> {@link SearchPathProperties#MODULE_PATHS_PROPERTY_NAME}
 * <li> {@link SearchPathProperties#INCLUDE_PATHS_PROPERTY_NAME}
 * </ul>
 * 
 * @see org.eclipse.photran.internal.ui.properties.SearchPathsPropertyPage
 * @author Jeff Overbey
 */
public class SearchPathProperties
{
    public static final String MODULE_PATHS_PROPERTY_NAME = "FortranModulePaths";
    public static final String INCLUDE_PATHS_PROPERTY_NAME = "FortranIncludePaths";
    
    /** @return the value of the given property for the given project */
    public static String getProperty(IProject project, String propertyName)
    {
        try
        {
            String result = project.getPersistentProperty(new QualifiedName("", propertyName)); // Could cast to IResource instead
            if (result == null) result = getPropertyDefault(project, propertyName);
            return result;
        }
        catch (CoreException e)
        {
            return null;
        }
    }
    
    /** @return the default value of the given property for the given project (i.e., its value if it has not been
     * explicitly set by the user) */
    public static String getPropertyDefault(IProject project, String propertyName)
    {
        return project.getFullPath().toOSString() + File.pathSeparator;
    }

    /** Sets the given property to the given value in the given project */
    public static void setProperty(IProject project, String propertyName, String value)
    {
        try
        {
            project.setPersistentProperty(new QualifiedName("", propertyName), value);
        }
        catch (CoreException e)
        {
            ;
        }
    }

    
    /* (non-Javadoc)
     * Method declared on ListEditor.
     * Creates a single string from the given array by separating each
     * string with the appropriate OS-specific path separator.
     */
    public static String createList(String[] items)
    {
        StringBuffer path = new StringBuffer("");//$NON-NLS-1$

        for (int i = 0; i < items.length; i++)
        {
            path.append(items[i]);
            path.append(File.pathSeparator);
        }
        return path.toString();
    }

    /* (non-Javadoc)
     * Method declared on ListEditor.
     */
    public static String[] parseString(String stringList)
    {
        StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
        ArrayList v = new ArrayList();
        while (st.hasMoreTokens())
            v.add(st.nextToken());
        return (String[]) v.toArray(new String[v.size()]);
    }
}
