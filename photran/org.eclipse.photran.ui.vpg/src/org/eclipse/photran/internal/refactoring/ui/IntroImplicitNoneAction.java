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

import org.eclipse.photran.internal.core.refactoring.IntroImplicitNoneRefactoring;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Handles the Introduce Implicit None action in the Fortran editor's Refactoring popup menu
 * and in the Refactor menu in the workbench menu bar.
 * 
 * @author Jeff Overbey
 */
public class IntroImplicitNoneAction
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public IntroImplicitNoneAction()
    {
        super(IntroImplicitNoneRefactoring.class, FortranIntroImplicitNoneRefactoringWizard.class);
    }
    
    public static class FortranIntroImplicitNoneRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected IntroImplicitNoneRefactoring refactoring;
        
        public FortranIntroImplicitNoneRefactoringWizard(IntroImplicitNoneRefactoring r)
        {
            super(r);
            this.refactoring = r;
        }

        protected void doAddUserInputPages()
        {
            // No user input required!
        }
    }
}
