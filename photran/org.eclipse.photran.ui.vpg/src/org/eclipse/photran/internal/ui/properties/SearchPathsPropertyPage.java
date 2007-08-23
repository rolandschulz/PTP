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
package org.eclipse.photran.internal.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Fortran Refactoring Engine Search Paths project properties page.  Allows the user to specify module paths and include paths for a project.
 * 
 * <code>org.eclipse.photran.core.analysis.properties.SearchPathProperties</code> serves as a common point of access for these properties.
 * 
 * @see org.eclipse.photran.internal.core.properties.SearchPathProperties
 * @author Jeff Overbey
 */
public class SearchPathsPropertyPage extends PropertyPage
{
    private WorkspacePathEditor modulePathEditor, includePathEditor;
    
    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);
        
        Label l = new Label(composite, SWT.WRAP);
        l.setText("The following specify the paths searched when the refactoring engine "
                  + "attempts to locate modules and INCLUDE files.  These MAY BE DIFFERENT from the "
                  + "paths used by your compiler to build your project.");
        l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        modulePathEditor = new WorkspacePathEditor((IProject)getElement(),
                                             SearchPathProperties.MODULE_PATHS_PROPERTY_NAME,
                                             "Folders to be searched for &modules, in order of preference:",
                                             "Select a folder to be searched for Fortran modules:",
                                             composite);
        modulePathEditor.setPreferenceStore(new CustomPropertyStore()
        {
            @Override protected String getProperty()
            {
                return SearchPathProperties.getProperty((IProject)getElement(), SearchPathProperties.MODULE_PATHS_PROPERTY_NAME);
            }

            @Override protected String getDefault()
            {
                return SearchPathProperties.getPropertyDefault((IProject)getElement(), SearchPathProperties.MODULE_PATHS_PROPERTY_NAME);
            }

            @Override protected void setProperty(String value)
            {
                SearchPathProperties.setProperty((IProject)getElement(), SearchPathProperties.MODULE_PATHS_PROPERTY_NAME, value);
            }
        });
        modulePathEditor.load();

        includePathEditor = new WorkspacePathEditor((IProject)getElement(),
                                                    SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME,
                                             "Folders to be searched for &INCLUDE files, in order of preference:",
                                             "Select a folder to be searched for INCLUDE files:",
                                             composite);
        includePathEditor.setPreferenceStore(new CustomPropertyStore()
        {
            @Override protected String getProperty()
            {
                return SearchPathProperties.getProperty((IProject)getElement(), SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME);
            }

            @Override protected String getDefault()
            {
                return SearchPathProperties.getPropertyDefault((IProject)getElement(), SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME);
            }

            @Override protected void setProperty(String value)
            {
                SearchPathProperties.setProperty((IProject)getElement(), SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME, value);
            }
            
        });
        includePathEditor.load();
        
        return composite;
    }

    protected void performDefaults()
    {
        modulePathEditor.loadDefault();
        includePathEditor.loadDefault();
    }
    
    public boolean performOk()
    {
        modulePathEditor.store();
        includePathEditor.store();
        return true;
    }

    private static abstract class CustomPropertyStore implements IPreferenceStore
    {
        public void addPropertyChangeListener(IPropertyChangeListener listener) {;}
        public boolean contains(String name) {throw new Error();}
        public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {;}
        public boolean getBoolean(String name) {throw new Error();}
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
        public void setDefault(String name, boolean value) {;}
        public void setToDefault(String name) { setProperty(null); } ///////////
        public void setValue(String name, double value) {;}
        public void setValue(String name, float value) {;}
        public void setValue(String name, int value) {;}
        public void setValue(String name, long value) {;}
        public void setValue(String name, String value) { setProperty(value); } ///////
        public void setValue(String name, boolean value) {;}
        
        protected abstract String getProperty();
        protected abstract String getDefault();
        protected abstract void setProperty(String value);
    }
}
