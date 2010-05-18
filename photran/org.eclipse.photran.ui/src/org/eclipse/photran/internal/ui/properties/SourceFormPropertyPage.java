/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.sourceform.SourceFormProperties;
import org.eclipse.photran.internal.ui.FortranUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * The Fortran &gt; Source Forms project property page.
 * 
 * @author Jeff Overbey
 */
public class SourceFormPropertyPage extends FortranPropertyPage
{
    private FortranSourceFormEditor sourceForms;
    
    private SourceFormProperties properties;
    
    @Override
    protected Control createContents(Composite parent)
    {
        IProject proj = (IProject)getElement();
        properties = new SourceFormProperties(proj);
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
        
        // See org.eclipse.ui.internal.dialogs.ContentTypesPreferencePage
        // and org.eclipse.cdt.ui.dialogs.DocCommentOwnerBlock
        Link link= new Link(composite, SWT.NONE);
        link.setText(Messages.SourceFormPropertyPage_LinkText);
        link.setLayoutData(GridDataFactory.swtDefaults()
                                          .align(SWT.FILL, SWT.TOP)
                                          .grab(true,false)
                                          .create());
        link.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(
                    composite.getShell(),
                    "org.eclipse.ui.preferencePages.ContentTypes", //$NON-NLS-1$
                    null,
                    null).open();
            }
        });
        
        sourceForms = new FortranSourceFormEditor(
            SourceFormProperties.SOURCE_FORMS_PROPERTY_NAME,
            Messages.SourceFormPropertyPage_SourceFormFilenameAssocsLabel, 
            parent);
        
        sourceForms.setPreferenceStore(scopedStore);
        sourceForms.load();
        
        return composite;
    }

    public void performDefaults()
    {
        sourceForms.loadDefault();
    }
    
    @Override public boolean doPerformOk()
    {
        sourceForms.store();
        
        try
        {
            properties.save();
        }
        catch (IOException e)
        {
            FortranUIPlugin.log(e);
            MessageDialog.openError(getShell(),
                Messages.SourceFormPropertyPage_ErrorTitle,
                Messages.SourceFormPropertyPage_PropertiesCouldNotBeSaved +
                e.getClass().getName() + ": " + //$NON-NLS-1$
                e.getMessage());
        }
        
        touchProject();
        
        return true;
    }

    /**
     * Updates the timestamps on all Fortran source files in the project in order to
     * force them to be re-indexed.
     */
    private void touchProject()
    {
        try
        {
            IProject proj = (IProject)getElement();
            proj.accept(new IResourceVisitor()
            {
                public boolean visit(IResource resource) throws CoreException
                {
                    if (resource instanceof IFile
                        && FortranCorePlugin.hasFortranContentType((IFile)resource))
                    {
                        touch((IFile)resource);
                    }
                    return true;
                }
            });
        }
        catch (CoreException e)
        {
            FortranCorePlugin.log(Messages.SourceFormPropertyPage_ErrorTouchingProject, e);
        }
    }

    private void touch(IFile file)
    {
        try
        {
            file.touch(new NullProgressMonitor());
        }
        catch (CoreException e)
        {
            FortranCorePlugin.log(Messages.SourceFormPropertyPage_ErrorTouchingFile, e);
        }
    }
}
