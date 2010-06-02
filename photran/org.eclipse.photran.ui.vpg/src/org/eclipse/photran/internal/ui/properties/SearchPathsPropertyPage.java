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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.ui.FortranUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Fortran Refactoring Engine Search Paths project properties page.  Allows the user to specify module paths and include paths for a project.
 * 
 * <code>org.eclipse.photran.core.analysis.properties.SearchPathProperties</code> serves as a common point of access for these properties.
 * 
 * @see org.eclipse.photran.internal.core.properties.SearchPathProperties
 * @author Jeff Overbey
 * @author Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 * @author Timofey Yuvashev
 */
public class SearchPathsPropertyPage extends FortranPropertyPage
{
    private FortranBooleanFieldEditor enableVPG, enableDeclView, enableContentAssist, enableHoverTip;
    private WorkspacePathEditor modulePathEditor, includePathEditor;
    
    private SearchPathProperties properties;
    
    /**
     * @see PreferencePage#createContents(Composite)
     */
    @Override protected Control createContents(Composite parent)
    {
        IProject proj = (IProject)getElement();
        properties = new SearchPathProperties(proj);
        IPreferenceStore scopedStore = properties.getPropertyStore();
        scopedStore.addPropertyChangeListener(new IPropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                setDirty();
            }
        });
        
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);
        
        
        Label l = new Label(composite, SWT.WRAP);
        l.setText(Messages.SearchPathsPropertyPage_EnableButtonsDescription);
        l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        enableVPG = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_VPG_PROPERTY_NAME, 
                                                  Messages.SearchPathsPropertyPage_EnableAnalysisRefactoring, 
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
        properties.setProperty(proj, SearchPathProperties.ENABLE_VPG_PROPERTY_NAME, 
                                     String.valueOf(enableVPG.getBooleanValue()));

        enableDeclView = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_DECL_VIEW_PROPERTY_NAME, 
                                                Messages.SearchPathsPropertyPage_EnableDeclarationView, 
                                                composite);
     
        enableDeclView.setPreferenceStore(scopedStore);
        enableDeclView.load();

        enableContentAssist = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_CONTENT_ASSIST_PROPERTY_NAME, 
                                                     Messages.SearchPathsPropertyPage_EnableContentAssist, 
                                                     composite);
      
        enableContentAssist.setPreferenceStore(scopedStore);
        enableContentAssist.load();
        
        enableHoverTip = new FortranBooleanFieldEditor(SearchPathProperties.ENABLE_HOVER_TIP_PROPERTY_NAME, 
                                                Messages.SearchPathsPropertyPage_EnableHoverTips, 
                                                composite);
       
        enableHoverTip.setPreferenceStore(scopedStore);
        enableHoverTip.load();
        
        
        enableDeclView.setEnabled(enableVPG.getBooleanValue(), composite);
        enableContentAssist.setEnabled(enableVPG.getBooleanValue(), composite);
        enableHoverTip.setEnabled(enableVPG.getBooleanValue(), composite);
        
        
        
        l = new Label(composite, SWT.WRAP);
        l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        l = new Label(composite, SWT.WRAP);
        l.setText(Messages.SearchPathsPropertyPage_PathsDescription);
        l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        modulePathEditor = new WorkspacePathEditor((IProject)getElement(),
                                             SearchPathProperties.MODULE_PATHS_PROPERTY_NAME,
                                             Messages.SearchPathsPropertyPage_FoldersToBeSearchedForModules,
                                             Messages.SearchPathsPropertyPage_SelectAFolderToBeSearchedForModules,
                                             composite);
        modulePathEditor.setPreferenceStore(scopedStore);
        modulePathEditor.load();

        includePathEditor = new WorkspacePathEditor((IProject)getElement(),
                                                    SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME,
                                             Messages.SearchPathsPropertyPage_FoldersToBeSearchedForIncludes,
                                             Messages.SearchPathsPropertyPage_SelectAFolderToBeSearchedForIncludes,
                                             composite);
        
        includePathEditor.setPreferenceStore(scopedStore);
        includePathEditor.load();
        
        return composite;
    }

    @Override public void performDefaults()
    {
        enableVPG.loadDefault();
        enableDeclView.loadDefault();
        enableContentAssist.loadDefault();
        enableHoverTip.loadDefault();
        modulePathEditor.loadDefault();
        includePathEditor.loadDefault();
    }
    
    @Override public boolean doPerformOk()
    {
        enableVPG.store();
        enableDeclView.store();
        enableContentAssist.store();
        enableHoverTip.store();
        modulePathEditor.store();
        includePathEditor.store();
        
        try
        {
            properties.save();
        }
        catch (IOException e)
        {
            FortranUIPlugin.log(e);
            MessageDialog.openError(getShell(),
                Messages.SearchPathsPropertyPage_ErrorSavingProperties,
                Messages.SearchPathsPropertyPage_PropertiesCouldNotBeSaved +
                "\n" + e.getClass().getName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                e.getMessage());
        }
        
        PhotranVPG.getInstance().queueJobToEnsureVPGIsUpToDate();
        
        return true;
    }
}
