/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.refactoring.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.internal.core.refactoring.MakePrivateEntityPublicRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * 
 * @author Kurt Hendle
 */
public class MakePrivateEntityPublicAction extends AbstractFortranRefactoringActionDelegate 
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public MakePrivateEntityPublicAction()
    {
        super(MakePrivateEntityPublicRefactoring.class, FortranMakePrivateEntityPublicRefactoringWizard.class);
    }
    
    @Override
    protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new MakePrivateEntityPublicRefactoring(getFortranEditor().getIFile(), getFortranEditor().getSelection());
    }
    
    public static class FortranMakePrivateEntityPublicRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected MakePrivateEntityPublicRefactoring publicToPrivate;
        
        public FortranMakePrivateEntityPublicRefactoringWizard(MakePrivateEntityPublicRefactoring r)
        {
            super(r);
            this.publicToPrivate = r;
        }

        @Override
        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(publicToPrivate.getName())
            {
                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);
                    
                    top.setLayout(new GridLayout(1,false));
                    
                    Label lbl = new Label(top, SWT.NONE);
                    lbl.setText("Click OK to make the selected Private Entity Public." +
                    		"To see what changes will be made, click Preview.");
                }
            });
        }
    }
}
