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

package org.eclipse.fdt.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.resource.ImageDescriptor;


import org.eclipse.fdt.internal.corext.refactoring.IRenameRefactoring;
import org.eclipse.fdt.internal.corext.refactoring.RefactoringStyles;
import org.eclipse.fdt.internal.corext.refactoring.RenameRefactoring;
import org.eclipse.fdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.fdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.fdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.fdt.internal.ui.refactoring.RefactoringWizard;

public class RenameRefactoringWizard extends RefactoringWizard {
	
	private final String fInputPageDescription;
	private final String fPageContextHelpId;
	private final ImageDescriptor fInputPageImageDescriptor;
	
	public RenameRefactoringWizard(String defaultPageTitle, String inputPageDescription, 
			ImageDescriptor inputPageImageDescriptor, String pageContextHelpId) {
		super();
		setDefaultPageTitle(defaultPageTitle);
		fInputPageDescription= inputPageDescription;
		fInputPageImageDescriptor= inputPageImageDescriptor;
		fPageContextHelpId= pageContextHelpId;
	}
	
	protected boolean hasPreviewPage() {
		Refactoring refactoring= getRefactoring();
		if (refactoring instanceof RenameRefactoring) {
			return (((RenameRefactoring)refactoring).getStyle() & RefactoringStyles.NEEDS_PREVIEW) != 0;
		}
		return super.hasPreviewPage();
	}
	
	/* non java-doc
	 * @see RefactoringWizard#addUserInputPages
	 */ 
	protected void addUserInputPages() {
		String initialSetting= getRenameRefactoring().getCurrentName();
		RenameInputWizardPage inputPage= createInputPage(fInputPageDescription, initialSetting);
		inputPage.setImageDescriptor(fInputPageImageDescriptor);
		addPage(inputPage);
	}

	private IRenameRefactoring getRenameRefactoring() {
		return (IRenameRefactoring)getRefactoring();	
	}
	
	protected RenameInputWizardPage createInputPage(String message, String initialSetting) {
		return new RenameInputWizardPage(message, fPageContextHelpId, true, initialSetting) {
			protected RefactoringStatus validateTextField(String text) {
				return validateNewName(text);
			}	
		};
	}
	
	protected RefactoringStatus validateNewName(String newName) {
		IRenameRefactoring ref= getRenameRefactoring();
		ref.setNewName(newName);
		try{
			return ref.checkNewName(newName);
		} catch (CoreException e){
			//XXX: should log the exception
			String msg= e.getMessage() == null ? "": e.getMessage(); //$NON-NLS-1$
			return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.getFormattedString("RenameRefactoringWizard.internal_error", msg));//$NON-NLS-1$
		}	
	}
}
