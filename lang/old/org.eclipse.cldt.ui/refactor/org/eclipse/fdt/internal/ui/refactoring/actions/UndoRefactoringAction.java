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

package org.eclipse.fdt.internal.ui.refactoring.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.fdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.fdt.internal.corext.refactoring.base.IUndoManager;
import org.eclipse.fdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.fdt.internal.corext.refactoring.base.UndoManagerAdapter;
import org.eclipse.fdt.internal.ui.editor.CEditor;
import org.eclipse.fdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchSite;

public class UndoRefactoringAction extends UndoManagerAction {

	private int fPatternLength;
	private CEditor fEditor;

	public UndoRefactoringAction(CEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
	}

	public UndoRefactoringAction(IWorkbenchSite site) {
		super(site);
		init(site.getWorkbenchWindow());		
	}

	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected String getName() {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		return RefactoringMessages.getString("UndoRefactoringAction.name"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected IRunnableWithProgress createOperation(final ChangeContext context) {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		return new IRunnableWithProgress(){
			public void run(IProgressMonitor pm) throws InvocationTargetException {
				try {
					setPreflightStatus(Refactoring.getUndoManager().performUndo(context, pm));
				} catch (CModelException e) {
					throw new InvocationTargetException(e);			
				} catch (ChangeAbortException e) {
					throw new InvocationTargetException(e);
				}
			}

		};
	}
	
	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected UndoManagerAdapter createUndoManagerListener() {
		return new UndoManagerAdapter() {
			public void undoStackChanged(IUndoManager manager) {
				IAction action= getAction();
				if (action == null)
					return;
				boolean enabled= false;
				String text= null;
				if (manager.anythingToUndo()) {
					enabled= true;
					text= getActionText();
				} else {
					text= RefactoringMessages.getString("UndoRefactoringAction.label"); //$NON-NLS-1$
				}
				action.setEnabled(enabled);
				action.setText(text);
			}
		};
	}	
	/* (non-Javadoc)
	 */
	public void selectionChanged(ISelection s) {
		selectionChanged(this, s);
	}	
	/* (non-Javadoc)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		if (!isHooked()) {
			hookListener(this);
		}
		fPatternLength= RefactoringMessages.getString("UndoRefactoringAction.extendedLabel").length(); //$NON-NLS-1$
		IUndoManager undoManager = Refactoring.getUndoManager();
		if (undoManager.anythingToUndo()) {
			if (undoManager.peekUndoName() != null)
				action.setText(getActionText());
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
	}	
	
	private String getActionText() {
		return shortenText(RefactoringMessages.getFormattedString(
			"UndoRefactoringAction.extendedLabel", //$NON-NLS-1$
			Refactoring.getUndoManager().peekUndoName()), fPatternLength);
	}	
}
