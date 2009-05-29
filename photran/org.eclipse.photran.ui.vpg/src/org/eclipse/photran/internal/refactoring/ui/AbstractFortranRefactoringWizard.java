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
package org.eclipse.photran.internal.refactoring.ui;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;

/**
 * This is the base class for most (all?) of the wizard dialogs for Fortran refactorings.
 * <p>
 * It extends the LTK's RefactoringWizard class, supplying a typical set of options.
 * 
 * @author Jeff Overbey
 */
public abstract class AbstractFortranRefactoringWizard extends RefactoringWizard
{
    public AbstractFortranRefactoringWizard(AbstractFortranRefactoring r)
    {
        // CHECK_INITIAL_CONDITIONS_ON_OPEN causes the initial conditions to be checked
        // twice, which leads to duplicate and missing error messages
        // (missing if, say, an INCLUDE could not be found, but the AST was already
        // loaded into the FortranWorkspace on the second invocation)
        super(r, DIALOG_BASED_USER_INTERFACE /*| CHECK_INITIAL_CONDITIONS_ON_OPEN*/);
        setNeedsProgressMonitor(true);
        setChangeCreationCancelable(false);
        setWindowTitle(getRefactoring().getName());
    }

    @Override
    protected final void addUserInputPages()
    {
        setDefaultPageTitle(getRefactoring().getName());
        doAddUserInputPages();
    }
    
    protected abstract void doAddUserInputPages();
}
