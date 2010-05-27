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
package org.eclipse.photran.internal.ui.refactoring;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.refactoring.ExtractProcedureRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Handles the Extract Procedure action in the Fortran editor's Refactoring popup menu
 * and in the Refactor menu in the workbench menu bar.
 *
 * @author Jeff Overbey
 */
public class ExtractProcedureAction
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public ExtractProcedureAction()
    {
        super(ExtractProcedureRefactoring.class, FortranExtractProcedureRefactoringWizard.class);
    }

    @Override
    protected VPGRefactoring<IFortranAST, Token, PhotranVPG> getRefactoring(List<IFile> files)
    {
        ExtractProcedureRefactoring r = new ExtractProcedureRefactoring();
        r.initialize(
            getFortranEditor().getIFile(),
            getFortranEditor().getSelection());
        return r;
    }

    public static class FortranExtractProcedureRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected ExtractProcedureRefactoring extractRefactoring;

        public FortranExtractProcedureRefactoringWizard(ExtractProcedureRefactoring r)
        {
            super(r);
            this.extractRefactoring = r;
        }

        @Override protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(extractRefactoring.getName())
            {
                protected Text newNameField;

                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);

                    top.setLayout(new GridLayout(2, false));

                    Composite group = top;
                    Label lbl = new Label(group, SWT.NONE);
                    lbl.setText("Extract selected code to a subroutine named ");

                    newNameField = new Text(group, SWT.BORDER);
                    newNameField.setText("");
                    newNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    newNameField.selectAll();
                    newNameField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            extractRefactoring.setName(newNameField.getText());
                        }
                    });

                    // Call once for sure, just in case the user doesn't modify the text
                    extractRefactoring.setName(newNameField.getText());

                    newNameField.setFocus();
                }
            });
        }
    }
}
