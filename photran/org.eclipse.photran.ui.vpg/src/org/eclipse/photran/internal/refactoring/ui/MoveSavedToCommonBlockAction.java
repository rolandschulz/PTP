/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.refactoring.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.internal.core.refactoring.MoveSavedToCommonBlockRefactoring;
import org.eclipse.photran.internal.core.refactoring.RenameRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Handles the Move Saved to Common Block action in the Fortran editor's Refactoring popup menu
 * and in the Refactor menu in the workbench menu bar.
 * 
 * @author Stas Negara
 */
public class MoveSavedToCommonBlockAction
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public MoveSavedToCommonBlockAction()
    {
        super(MoveSavedToCommonBlockRefactoring.class, FortranMoveSavedToCommonBlockRefactoringWizard.class);
    }
    
    @Override
    protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new MoveSavedToCommonBlockRefactoring(
            getFortranEditor().getIFile(),
            getFortranEditor().getSelection());
    }
    
    public static class FortranMoveSavedToCommonBlockRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected MoveSavedToCommonBlockRefactoring moveSavedToCommonBlockRefactoring;
        
        public FortranMoveSavedToCommonBlockRefactoringWizard(MoveSavedToCommonBlockRefactoring r)
        {
            super(r);
            this.moveSavedToCommonBlockRefactoring = r;
        }


        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(moveSavedToCommonBlockRefactoring.getName())
            {
                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);
                
                    top.setLayout(new GridLayout(1, false));
                
                    Label lbl = new Label(top, SWT.NONE);
                    lbl.setText("Click OK to move saved variables from subprogram \""
                                + moveSavedToCommonBlockRefactoring.getSubprogramName() + "\"");
                }
            });
        }
    }
}
