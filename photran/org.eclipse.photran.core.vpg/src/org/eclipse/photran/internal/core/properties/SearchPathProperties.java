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

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

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
 * Modified by Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 * Modified by Timofey Yuvashev
 */
@SuppressWarnings("restriction")
public class SearchPathProperties
{
    public static final String ENABLE_VPG_PROPERTY_NAME = "EnableVPG";
    public static final String ENABLE_DECL_VIEW_PROPERTY_NAME = "EnableDeclView";
    public static final String ENABLE_CONTENT_ASSIST_PROPERTY_NAME = "EnableContentAssist";
    public static final String ENABLE_HOVER_TIP_PROPERTY_NAME = "EnableHoverTip";
    public static final String MODULE_PATHS_PROPERTY_NAME = "FortranModulePaths";
    public static final String INCLUDE_PATHS_PROPERTY_NAME = "FortranIncludePaths";
    
    
    protected static ProjectScope pScope = null;
    public static ScopedPreferenceStore scopedStore = null;
    
    protected static void initProjScope(IProject proj)
    {
        pScope = new ProjectScope(proj);
        scopedStore = new ScopedPreferenceStore(pScope, "scoped_pref_store");
        
        
        String includeName = SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME;
        String moduleName = SearchPathProperties.MODULE_PATHS_PROPERTY_NAME;
        String defaultDir = proj.getFullPath().toOSString() + File.pathSeparator;
        
        //Set the default include and module paths to the root of the project
        scopedStore.setDefault(includeName, 
                               defaultDir);
        scopedStore.setDefault(moduleName, 
                               defaultDir);
    }
    
    /** @return the value of the given property for the project containing the given file */
    public static String getProperty(IFile file, String propertyName)
    {
        if (file == null || file.getProject() == null)
            return "";
        else
            return getProperty(file.getProject(), propertyName);
    }
    
    public static void setProject(IProject project)
    {
        if(pScope == null || scopedStore == null)
            initProjScope(project);
        
        //HACK: A way to check that we are looking at the correct project scope.
        IPath currPath = pScope.getLocation();
        IPath projPath = project.getLocation();
        
        //If the path to the current project is different then the one passed in --
        // we need to re-set the project
        if(currPath == null || !currPath.equals(projPath.append(EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME)))
            initProjScope(project);
    }
    
    /** @return the value of the given property for the given project */
    public static String getProperty(IProject project, String propertyName)
    {
        setProject(project);
        String result = scopedStore.getString(propertyName);

        if (propertyName.equals(ENABLE_DECL_VIEW_PROPERTY_NAME)
                || propertyName.equals(ENABLE_CONTENT_ASSIST_PROPERTY_NAME))
            return result.equals("true") && getProperty(project, ENABLE_VPG_PROPERTY_NAME).equals("true") ? "true" : "";
        else
            return result == null ? "" : result;   
    }
    
    /** @return the default value of the given property for the given project (i.e., its value if it has not been
     * explicitly set by the user) */
    public static String getPropertyDefault(IProject project, String propertyName)
    {
        setProject(project);
        return scopedStore.getDefaultString(propertyName);
    }

    /** Sets the given property to the given value in the given project */
    public static void setProperty(IProject project, String propertyName, String value)
    {
        setProject(project);
        scopedStore.setValue(propertyName, value);
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
        ArrayList<String> v = new ArrayList<String>();
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
