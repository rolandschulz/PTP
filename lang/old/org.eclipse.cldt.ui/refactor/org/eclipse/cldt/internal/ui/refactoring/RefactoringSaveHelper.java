/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.internal.ui.refactoring;

import java.util.Arrays;

import org.eclipse.cldt.internal.ui.refactoring.ListDialog;
import org.eclipse.cldt.internal.ui.util.ExceptionHandler;
import org.eclipse.cldt.internal.ui.viewsupport.ListContentProvider;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.GlobalBuildAction;


public class RefactoringSaveHelper {

	private boolean fFilesSaved;

	public RefactoringSaveHelper() {
		super();
	}

	public boolean saveEditors(Shell shell) {
		IEditorPart[] dirtyEditors= FortranUIPlugin.getDirtyEditors();
		if (dirtyEditors.length == 0)
			return true;
		if (! saveAllDirtyEditors(shell))
			return false;
		try {
			// Save isn't cancelable.
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			IWorkspaceDescription description= workspace.getDescription();
			boolean autoBuild= description.isAutoBuilding();
			description.setAutoBuilding(false);
			workspace.setDescription(description);
			try {
				FortranUIPlugin.getActiveWorkbenchWindow().getWorkbench().saveAllEditors(false);
				fFilesSaved= true;
			} finally {
				description.setAutoBuilding(autoBuild);
				workspace.setDescription(description);
			}
			return true;
		} catch (CoreException e) {
			ExceptionHandler.handle(e, shell, 
				RefactoringMessages.getString("RefactoringStarter.saving"), RefactoringMessages.getString("RefactoringStarter.unexpected_exception"));  //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	public void triggerBuild() {
		if (fFilesSaved && ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding()) {
			new GlobalBuildAction(FortranUIPlugin.getActiveWorkbenchWindow(), IncrementalProjectBuilder.INCREMENTAL_BUILD).run();
		}
	}
	
	private boolean saveAllDirtyEditors(Shell shell) {
		if (RefactoringPreferences.getSaveAllEditors()) //must save everything
			return true;
		ListDialog dialog= new ListDialog(shell) {
			protected Control createDialogArea(Composite parent) {
				Composite result= (Composite) super.createDialogArea(parent);
				final Button check= new Button(result, SWT.CHECK);
				check.setText(RefactoringMessages.getString("RefactoringStarter.always_save")); //$NON-NLS-1$
				check.setSelection(RefactoringPreferences.getSaveAllEditors());
				check.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						RefactoringPreferences.setSaveAllEditors(check.getSelection());
					}
				});
				applyDialogFont(result);		
				return result;
			}
		};
		dialog.setTitle(RefactoringMessages.getString("RefactoringStarter.save_all_resources")); //$NON-NLS-1$
		dialog.setAddCancelButton(true);
		dialog.setLabelProvider(createDialogLabelProvider());
		dialog.setMessage(RefactoringMessages.getString("RefactoringStarter.must_save")); //$NON-NLS-1$
		dialog.setContentProvider(new ListContentProvider());
		dialog.setInput(Arrays.asList(FortranUIPlugin.getDirtyEditors()));
		return dialog.open() == Window.OK;
	}
	
	private ILabelProvider createDialogLabelProvider() {
		return new LabelProvider() {
			public Image getImage(Object element) {
				return ((IEditorPart) element).getTitleImage();
			}
			public String getText(Object element) {
				return ((IEditorPart) element).getTitle();
			}
		};
	}	
}
