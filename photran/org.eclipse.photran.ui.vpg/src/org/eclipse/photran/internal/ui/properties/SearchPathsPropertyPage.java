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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
 * Modified by Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 */
public class SearchPathsPropertyPage extends PropertyPage
{
    private BooleanFieldEditor enableVPG, enableDeclView, enableContentAssist, enableHoverTip;
    private WorkspacePathEditor modulePathEditor, includePathEditor;
    private boolean showMessage = false;

    
    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent)
    {
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

        enableVPG = new BooleanFieldEditor("IgnoreThis", "Enable Fortran analysis/refactoring", composite)
        {
            @Override protected void valueChanged(boolean oldValue, boolean newValue)
            {
                enableDeclView.setEnabled(newValue, composite);
                enableContentAssist.setEnabled(newValue, composite);
                enableHoverTip.setEnabled(newValue, composite);                
                
                showMessage = (oldValue != newValue);
            }
        };
        enableVPG.setPreferenceStore(SearchPathProperties.getPropertyStore((IProject)getElement(),
                                                                           SearchPathProperties.ENABLE_VPG_PROPERTY_NAME));
        enableVPG.load();

        enableDeclView = new BooleanFieldEditor("IgnoreThis", "Enable Fortran Declaration view", composite)
        {
            @Override protected void valueChanged(boolean oldValue, boolean newValue)
            {
                showMessage = (oldValue != newValue);
            }
        };
        enableDeclView.setPreferenceStore(SearchPathProperties.getPropertyStore((IProject)getElement(),
                                                                                SearchPathProperties.ENABLE_DECL_VIEW_PROPERTY_NAME));
        enableDeclView.load();

        enableContentAssist = new BooleanFieldEditor("IgnoreThis", "Enable Fortran content assist (Ctrl+Space)", composite)
        {
            @Override protected void valueChanged(boolean oldValue, boolean newValue)
            {
                showMessage = (oldValue != newValue);
            }
        };
        enableContentAssist.setPreferenceStore(SearchPathProperties.getPropertyStore((IProject)getElement(),
                                                                                     SearchPathProperties.ENABLE_CONTENT_ASSIST_PROPERTY_NAME));
        enableContentAssist.load();
        
        enableHoverTip = new BooleanFieldEditor("IgnoreThis", "Enable Fortran Hover tips", composite)
        {
            @Override protected void valueChanged(boolean oldValue, boolean newValue)
            {
                showMessage = (oldValue != newValue);
            }
        };
        
        
        enableHoverTip.setPreferenceStore(SearchPathProperties.getPropertyStore((IProject)getElement(),
            SearchPathProperties.ENABLE_HOVER_TIP_PROPERTY_NAME));
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
        modulePathEditor.setPreferenceStore(SearchPathProperties.getPropertyStore(
             (IProject)getElement(),
             SearchPathProperties.MODULE_PATHS_PROPERTY_NAME));
        modulePathEditor.load();

        includePathEditor = new WorkspacePathEditor((IProject)getElement(),
                                                    SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME,
                                             "Folders to be searched for &INCLUDE files, in order of preference:",
                                             "Select a folder to be searched for INCLUDE files:",
                                             composite);
        includePathEditor.setPreferenceStore(SearchPathProperties.getPropertyStore((IProject)getElement(),
                                                                                   SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME));
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
        
        MessageDialog.openInformation(getShell(), "Preferences Changed", "You may need to close and re-open any " +
            "Fortran editors for the new settings to take effect.");
        showMessage = false;
        
        return true;
    }
}
