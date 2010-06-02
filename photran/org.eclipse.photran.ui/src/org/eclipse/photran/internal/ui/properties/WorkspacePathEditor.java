/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.photran.internal.core.properties.AbstractProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * A field editor to edit workspace paths.  Based on <code>org.eclipse.jface.preference.PathEditor</code>.
 * 
 * @author Jeff Overbey
 * 
 * @see org.eclipse.jface.preference.PathEditor
 */
public class WorkspacePathEditor extends ListEditor
{
    private IProject project;

    /**
     * The special label text for directory chooser, 
     * or <code>null</code> if none.
     */
    private String dirChooserLabelText;

    /**
     * Creates a new path field editor 
     */
    protected WorkspacePathEditor()
    {
    }

    /**
     * Creates a path field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param dirChooserLabelText the label text displayed for the directory chooser
     * @param parent the parent of the field editor's control
     */
    public WorkspacePathEditor(IProject project, String name, String labelText, String dirChooserLabelText, Composite parent)
    {
        parent = new Composite(parent, SWT.NONE);
        init(name, labelText);
        this.project = project;
        this.dirChooserLabelText = dirChooserLabelText;
        createControl(parent);
    }

    /* (non-Javadoc)
     * Method declared on ListEditor.
     * Creates a new path element by means of a directory dialog.
     */
    @Override protected String getNewInputObject()
    {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), project, false, dirChooserLabelText);
        if (dialog.open() == ContainerSelectionDialog.OK)
        {
            Path result = null;
            
            Object[] res = dialog.getResult();
            if (res.length == 1) result = (Path)res[0];

            // Remember the selection for the next time the dialog is opened
            IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(result);
            if (!resource.exists())
                MessageDialog.openError(getShell(), UIMessages.WorkspacePathEditor_ErrorTitle, UIMessages.WorkspacePathEditor_ResourceDoesNotExist);
            else if (!(resource instanceof IContainer))
                MessageDialog.openError(getShell(), UIMessages.WorkspacePathEditor_ErrorTitle, UIMessages.WorkspacePathEditor_ResourceIsNotAContainer);
            else if (result != null)
                return result.toOSString();
        }
        
        return null;
    }

    @Override
    protected String createList(String[] items)
    {
        return AbstractProperties.createList(items);
    }

    @Override
    protected String[] parseString(String stringList)
    {
        return AbstractProperties.parseString(stringList);
    }
}
