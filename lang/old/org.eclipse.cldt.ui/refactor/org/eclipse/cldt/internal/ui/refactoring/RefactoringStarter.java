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


import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.window.Window;



import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.cldt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cldt.internal.ui.refactoring.CheckConditionsOperation;
import org.eclipse.cldt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.cldt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.cldt.internal.ui.refactoring.RefactoringWizard;
import org.eclipse.cldt.internal.ui.refactoring.RefactoringWizardDialog;
import org.eclipse.cldt.internal.ui.refactoring.RefactoringWizardDialog2;
import org.eclipse.cldt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.cldt.internal.ui.util.ExceptionHandler;

/**
 * A helper class to activate the UI of a refactoring
 */
public class RefactoringStarter {
	
	private RefactoringSaveHelper fSaveHelper= new RefactoringSaveHelper();

	public Object activate(Refactoring refactoring, RefactoringWizard wizard, Shell parent, String dialogTitle, boolean mustSaveEditors) throws CModelException {
		if (! canActivate(mustSaveEditors, parent))
			return null;
		RefactoringStatus activationStatus= checkActivation(refactoring);
		if (activationStatus.hasFatalError()){
			return RefactoringErrorDialogUtil.open(dialogTitle, activationStatus, parent);
		} else {
			wizard.setActivationStatus(activationStatus);
			Dialog dialog;
			if (wizard.hasMultiPageUserInput()){
				dialog= new RefactoringWizardDialog(parent, wizard);
			}
			else { 
				dialog= new RefactoringWizardDialog2(parent, wizard);
			}
			if (dialog.open() == Window.CANCEL)
				fSaveHelper.triggerBuild();
			return null;	
		} 
	}
		
	private RefactoringStatus checkActivation(Refactoring refactoring){		
		try {
			CheckConditionsOperation cco= new CheckConditionsOperation(refactoring, CheckConditionsOperation.ACTIVATION);
			IRunnableContext context= new BusyIndicatorRunnableContext();
			context.run(false, false, cco);
			return cco.getStatus();
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, "Error", RefactoringMessages.getString("RefactoringStarter.unexpected_exception"));//$NON-NLS-1$ //$NON-NLS-2$
			return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.getString("RefactoringStarter.unexpected_exception"));//$NON-NLS-1$
		} catch (InterruptedException e) {
			Assert.isTrue(false);
			return null;
		}
	}
	
	private boolean canActivate(boolean mustSaveEditors, Shell shell) {
		return ! mustSaveEditors || fSaveHelper.saveEditors(shell);
	}
}
