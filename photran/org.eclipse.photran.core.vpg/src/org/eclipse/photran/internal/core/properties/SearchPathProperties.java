/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

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
    public static final String ENABLE_VPG_PROPERTY_NAME = "EnableVPG";
    public static final String ENABLE_DECL_VIEW_PROPERTY_NAME = "EnableDeclView";
    public static final String ENABLE_CONTENT_ASSIST_PROPERTY_NAME = "EnableContentAssist";
    public static final String MODULE_PATHS_PROPERTY_NAME = "FortranModulePaths";
    public static final String INCLUDE_PATHS_PROPERTY_NAME = "FortranIncludePaths";
    
    /** @return the value of the given property for the given project */
    public static String getProperty(IProject project, String propertyName)
    {
        try
        {
            String result = project.getPersistentProperty(new QualifiedName("", propertyName)); // Could cast to IResource instead
            if (result == null) result = getPropertyDefault(project, propertyName);
            
            if (propertyName.equals(ENABLE_DECL_VIEW_PROPERTY_NAME)
                            || propertyName.equals(ENABLE_CONTENT_ASSIST_PROPERTY_NAME))
                return result.equals("true") && getProperty(project, ENABLE_VPG_PROPERTY_NAME).equals("true") ? "true" : "";
            else
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

    public static IPreferenceStore getPropertyStore(final IProject project, final String propertyName)
    {
        return new CustomPropertyStore()
        {
            @Override protected String getProperty()
            {
                return SearchPathProperties.getProperty(project, propertyName);
            }

            @Override protected String getDefault()
            {
                return SearchPathProperties.getPropertyDefault(project, propertyName);
            }

            @Override protected void setProperty(String value)
            {
                SearchPathProperties.setProperty(project, propertyName, value);
            }
        };
    }

    private static abstract class CustomPropertyStore implements IPreferenceStore
    {
        public void addPropertyChangeListener(IPropertyChangeListener listener) {;}
        public boolean contains(String name) {throw new Error();}
        public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {;}
        public boolean getBoolean(String name) { return getProperty().equals("true"); }
        public boolean getDefaultBoolean(String name) {throw new Error();}
        public double getDefaultDouble(String name) {throw new Error();}
        public float getDefaultFloat(String name) {throw new Error();}
        public int getDefaultInt(String name) {throw new Error();}
        public long getDefaultLong(String name) {throw new Error();}
        public String getDefaultString(String name) { return getDefault(); } //////////////
        public double getDouble(String name) {throw new Error();}
        public float getFloat(String name) {throw new Error();}
        public int getInt(String name) {throw new Error();}
        public long getLong(String name) {throw new Error();}
        public String getString(String name) { return getProperty(); } ////////////
        public boolean isDefault(String name) { return getProperty()==null || getProperty().equals(getDefault()); } //////////
        public boolean needsSaving() {return false;}
        public void putValue(String name, String value) { setProperty(value); } //////
        public void removePropertyChangeListener(IPropertyChangeListener listener) {;}
        public void setDefault(String name, double value) {;}
        public void setDefault(String name, float value) {;}
        public void setDefault(String name, int value) {;}
        public void setDefault(String name, long value) {;}
        public void setDefault(String name, String defaultObject) {throw new Error();}
        public void setDefault(String name, boolean value) { setProperty(null); }
        public void setToDefault(String name) { setProperty(null); } ///////////
        public void setValue(String name, double value) {;}
        public void setValue(String name, float value) {;}
        public void setValue(String name, int value) {;}
        public void setValue(String name, long value) {;}
        public void setValue(String name, String value) { setProperty(value); } ///////
        public void setValue(String name, boolean value) { setProperty(value ? "true" : "false"); }
        
        protected abstract String getProperty();
        protected abstract String getDefault();
        protected abstract void setProperty(String value);
    }
}
