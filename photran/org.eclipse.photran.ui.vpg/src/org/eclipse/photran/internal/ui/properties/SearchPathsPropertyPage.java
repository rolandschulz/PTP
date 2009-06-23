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

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Fortran Refactoring Engine Search Paths project properties page.  Allows the user to specify module paths and include paths for a project.
 * 
 * <code>org.eclipse.photran.core.analysis.properties.SearchPathProperties</code> serves as a common point of access for these properties.
 * 
 * @see org.eclipse.photran.internal.core.properties.SearchPathProperties
 * @author Jeff Overbey
 * Modified by Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 * Modified by Timofey Yuvashev
 */

public class SearchPathsPropertyPage extends PropertyPage
{
    
    /*
     * This class is needed for only one reason: there is no setters in BooleanFieldEditor
     *  for the button/check-box. So I cannot programmatically set whether or not the button
     *  is checked, which I need to be able to do. So the only reason I created this class
     *  is to provide the said functionality.
     */
    public static class FortranBooleanFieldEditor extends BooleanFieldEditor
    {
        //DO NOT SET THIS VALUE TO ANYTHING!! This field is initialized via getChangeControl(Composite parent)
        // which is called from super() constructor. Since fields are initialized AFTER the super() constructor
        // is called, if you set this to, let's say NULL, then AFTER it was already initialized to a proper value
        // by the super() constructor, it will get re-set to NULL, and you don't want that. So, don't set this
        // field to anything!
        private Button myCheckBox;
        
        public FortranBooleanFieldEditor(String enableVpgPropertyName, 
            String string,
            Composite composite)
        {
            super(enableVpgPropertyName, string, composite);
        }

        ////// !! Copied from BooleanFieldEditor !! /////
        /**
         * Returns the change button for this field editor.
         * @param parent The Composite to create the receiver in.
         *
         * @return the change button
         */
        @Override protected Button getChangeControl(Composite parent) 
        {
            if (myCheckBox == null) {
                myCheckBox = new Button(parent, SWT.CHECK | SWT.LEFT);
                myCheckBox.setFont(parent.getFont());
                myCheckBox.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        boolean isSelected = myCheckBox.getSelection();
                        valueChanged(!isSelected, isSelected);
                    }
                });
                myCheckBox.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent event) {
                        myCheckBox = null;
                    }
                });
            } else {
                checkParent(myCheckBox, parent);
            }
            return myCheckBox;
        }  
        
        public void setValue(boolean value)
        {
            if(myCheckBox != null)
                myCheckBox.setSelection(value);
        }
    }
    

    private FortranBooleanFieldEditor enableVPG, enableDeclView, enableContentAssist, enableHoverTip;
    private WorkspacePathEditor modulePathEditor, includePathEditor;
    
    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent)
    {
        IProject proj = (IProject)getElement(); 
        
        SearchPathProperties.setProject(proj);
        ScopedPreferenceStore scopedStore = SearchPathProperties.scopedStore;
              
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);
        
        
        Label l = new Label(composite, SWT.WRAP);
        l.setText("To enable Open Declaration, Find All References, the Fortran Declaration\n"
                  + "view, content assist, and refactoring in Fortran programs, check the\n"
                  + "following box.  A program database (index) will be updated every time\n"
                  + "a Fortran file is created or saved.");
        l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        enableVPG = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_VPG_PROPERTY_NAME, 
                                                  "Enable Fortran analysis/refactoring", 
                                                  composite)
        {
            @Override protected void valueChanged(boolean oldValue, boolean newValue)
            {
                enableDeclView.setEnabled(newValue, composite);
                enableContentAssist.setEnabled(newValue, composite);
                enableHoverTip.setEnabled(newValue, composite); 
                
                enableDeclView.setValue(newValue);   
                enableContentAssist.setValue(newValue);
                enableHoverTip.setValue(newValue);                
            }
        }; 
        
        enableVPG.setPreferenceStore(scopedStore);
        enableVPG.load();
        SearchPathProperties.setProperty(proj, 
                                         SearchPathProperties.ENABLE_VPG_PROPERTY_NAME, 
                                         String.valueOf(enableVPG.getBooleanValue()));

        enableDeclView = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_DECL_VIEW_PROPERTY_NAME, 
                                                "Enable Fortran Declaration view", 
                                                composite);
     
        enableDeclView.setPreferenceStore(scopedStore);
        enableDeclView.load();

        enableContentAssist = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_CONTENT_ASSIST_PROPERTY_NAME, 
                                                     "Enable Fortran content assist (Ctrl+Space)", 
                                                     composite);
      
        enableContentAssist.setPreferenceStore(scopedStore);
        enableContentAssist.load();
        
        enableHoverTip = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_HOVER_TIP_PROPERTY_NAME, 
                                                "Enable Fortran Hover tips", 
                                                composite);
       
        enableHoverTip.setPreferenceStore(scopedStore);
        enableHoverTip.load();
        
        
        enableDeclView.setEnabled(enableVPG.getBooleanValue(), composite);
        enableContentAssist.setEnabled(enableVPG.getBooleanValue(), composite);
        enableHoverTip.setEnabled(enableVPG.getBooleanValue(), composite);
        
        
        
        l = new Label(composite, SWT.WRAP);
        l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        l = new Label(composite, SWT.WRAP);
        l.setText("The following specify the paths searched for modules\n"
                  + "and INCLUDE files during analysis and refactoring.\n"
                  + "These MAY BE DIFFERENT from the settings used by\n"
                  + "your compiler to build your project.");
        l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        modulePathEditor = new WorkspacePathEditor((IProject)getElement(),
                                             SearchPathProperties.MODULE_PATHS_PROPERTY_NAME,
                                             "Folders to be searched for &modules, in order of preference:",
                                             "Select a folder to be searched for Fortran modules:",
                                             composite);
        modulePathEditor.setPreferenceStore(scopedStore);
        modulePathEditor.load();

        includePathEditor = new WorkspacePathEditor((IProject)getElement(),
                                                    SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME,
                                             "Folders to be searched for &INCLUDE files, in order of preference:",
                                             "Select a folder to be searched for INCLUDE files:",
                                             composite);
        
        includePathEditor.setPreferenceStore(scopedStore);
        includePathEditor.load();
        
        return composite;
    }

    public void performDefaults()
    {
        enableVPG.loadDefault();
        enableDeclView.loadDefault();
        enableContentAssist.loadDefault();
        enableHoverTip.loadDefault();
        modulePathEditor.loadDefault();
        includePathEditor.loadDefault();
    }
    
    public boolean performOk()
    {
        enableVPG.store();
        enableDeclView.store();
        enableContentAssist.store();
        enableHoverTip.store();
        modulePathEditor.store();
        includePathEditor.store();
        
        ScopedPreferenceStore scopedStore = SearchPathProperties.scopedStore;
        try
        {
            scopedStore.save();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        MessageDialog.openInformation(getShell(), "Preferences Changed", "You may need to close and re-open any " +
            "Fortran editors for the new settings to take effect.");
        
        return true;
    }
}
