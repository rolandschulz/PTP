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
package org.eclipse.photran.ui.vpg;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
import org.eclipse.photran.internal.refactoring.ui.AbstractFortranRefactoringActionDelegate;
import org.eclipse.photran.internal.refactoring.ui.AbstractFortranRefactoringWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Jeff Overbey
 */
public class EasterEggAction
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{   
    public EasterEggAction()
    {
        super(EasterEgg.class, EasterEggWizard.class);
    }
    
    @Override
    protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new EasterEgg(
            getFortranEditor().getIFile(),
            getFortranEditor().getSelection());
    }
    
    public static class EasterEggWizard extends AbstractFortranRefactoringWizard
    {
        protected EasterEgg transformation;
        
        public EasterEggWizard(EasterEgg r)
        {
            super(r);
            this.transformation = r;
        }

        protected void doAddUserInputPages()
        {
        	addPage(new UserInputWizardPage(transformation.getName())
            {
                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);
                
                    top.setLayout(new GridLayout(1, false));
                
                    Label lbl = new Label(top, SWT.NONE);
                    lbl.setText("Congratulations!  You found an Easter Egg.");
                }
            });
        }
    }
}
