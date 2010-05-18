/*******************************************************************************
 * Copyright (c) 2007,2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Base class for Source Form and Search Path project properties.
 * 
 * @author Jeff Overbey
 * @author Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 * @author Timofey Yuvashev
 */
@SuppressWarnings("restriction")
public abstract class AbstractProperties
{
    private static final String SEPARATOR = "\n"; //$NON-NLS-1$
    
    private ProjectScope projScope = null;
    private ScopedPreferenceStore preferenceStore = null;
    
    private void initProjScope(IProject proj)
    {
        projScope = new ProjectScope(proj);
        preferenceStore = new ScopedPreferenceStore(projScope, FortranCorePlugin.PLUGIN_ID);
        
        initializeDefaults(proj, preferenceStore);
    }
    
    /**
     * Initializes the given preference store with a set of default values (via
     * {@link IPreferenceStore#setDefault(String, String)} and similar methods).
     */
    protected abstract void initializeDefaults(IProject proj, IPreferenceStore prefStore);
    
    /** @return the value of the given property for the project containing the given file */
    public String getProperty(IFile file, String propertyName)
    {
        if (file == null || file.getProject() == null)
            return ""; //$NON-NLS-1$
        else
            return getProperty(file.getProject(), propertyName);
    }

    /** @return the value of the given property for the project containing the given file */
    public String[] getListProperty(IFile file, String propertyName)
    {
        return parseString(getProperty(file, propertyName));
    }

    protected void setProject(IProject project)
    {
        if (projScope == null || preferenceStore == null)
            initProjScope(project);
        
        //HACK: A way to check that we are looking at the correct project scope.
        IPath currPath = projScope.getLocation();
        IPath projPath = project.getLocation();
        
        //If the path to the current project is different then the one passed in --
        // we need to re-set the project
        if (currPath == null
            || projPath == null
            || !currPath.equals(projPath.append(EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME)))
            initProjScope(project);
    }
    
    /** @return the value of the given property for the given project */
    public String getProperty(IProject project, String propertyName)
    {
        setProject(project);
        return preferenceStore.getString(propertyName);
    }

    /** @return the value of the given property for the given project */
    public String[] getListProperty(IProject project, String propertyName)
    {
        return parseString(getProperty(project, propertyName));
    }
    
    /** @return the default value of the given property for the given project (i.e., its value if it has not been
     * explicitly set by the user) */
    public String getPropertyDefault(IProject project, String propertyName)
    {
        setProject(project);
        return preferenceStore.getDefaultString(propertyName);
    }

    /** Sets the given property to the given value in the given project */
    public void setProperty(IProject project, String propertyName, String value)
    {
        setProject(project);
        preferenceStore.setValue(propertyName, value);
    }

    /** Sets the given property to the given value in the given project */
    public void setListProperty(IProject project, String propertyName, String[] values)
    {
        setProject(project);
        preferenceStore.setValue(propertyName, createList(values));
    }
    
    /* Method declared on ListEditor.
     * Creates a single string from the given array by separating each
     * string with the appropriate OS-specific path separator.
     */
    public static String createList(String[] items)
    {
        StringBuffer path = new StringBuffer("");//$NON-NLS-1$
        if (items != null)
        {
            for (int i = 0; i < items.length; i++)
            {
                path.append(items[i]);
                path.append(SEPARATOR);
            }
        }
        return path.toString();
    }

    /* Method declared on ListEditor.
     */
    public static String[] parseString(String stringList)
    {
        if (stringList == null) return new String[0];
        StringTokenizer st = new StringTokenizer(stringList, SEPARATOR + "\n\r");//$NON-NLS-1$
        ArrayList<String> v = new ArrayList<String>();
        while (st.hasMoreTokens())
            v.add(st.nextToken());
        return (String[]) v.toArray(new String[v.size()]);
    }

    public IPreferenceStore getPropertyStore(final IProject project, final String propertyName)
    {
        return new CustomPropertyStore()
        {
            @Override protected String getProperty()
            {
                return AbstractProperties.this.getProperty(project, propertyName);
            }

            @Override protected String getDefault()
            {
                return AbstractProperties.this.getPropertyDefault(project, propertyName);
            }

            @Override protected void setProperty(String value)
            {
                AbstractProperties.this.setProperty(project, propertyName, value);
            }
        };
    }

    private abstract class CustomPropertyStore implements IPreferenceStore
    {
        public void addPropertyChangeListener(IPropertyChangeListener listener) {;}
        public boolean contains(String name) {throw new Error();}
        public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {;}
        public boolean getBoolean(String name) { return getProperty().equals("true"); } //$NON-NLS-1$
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
        public void setValue(String name, boolean value) { setProperty(value ? "true" : "false"); } //$NON-NLS-1$ //$NON-NLS-2$
        
        protected abstract String getProperty();
        protected abstract String getDefault();
        protected abstract void setProperty(String value);
    }
    
    /** @return the {@link IPreferenceStore} */
    public IPreferenceStore getPropertyStore()
    {
        return preferenceStore;
    }

    /** Saves these project properties (writing them to disk) */
    public void save() throws IOException
    {
        preferenceStore.save();
    }
}
