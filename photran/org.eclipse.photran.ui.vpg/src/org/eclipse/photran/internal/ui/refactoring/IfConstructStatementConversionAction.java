/*******************************************************************************
 * Copyright (c) 2010 Zeeshan Ansari, Mark Chen, Burim Isai, Waseem Sheikh, Mumtaz Vauhkonen. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zeeshan Ansari
 *     Mark Chen
 *     Burim Isai
 *     Waseem Sheihk
 *     Mumtaz Vauhkonen
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.refactoring.IfConstructStatementConversionRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Handles the If Statement/Construct action in the Fortran editor's Refactoring popup menu and in
 * the Refactor menu in the workbench menu bar.
 * 
 * @author Zeeshan Ansari
 * @author Mark Chen
 * @author Mumtaz Vauhkonrn
 * @author Burim Isai
 * @author Waseem Sheikh
 */
public class IfConstructStatementConversionAction extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public IfConstructStatementConversionAction()
    {
        super(IfConstructStatementConversionRefactoring.class,
            FortranIfConstructStatementConversionWizard.class);
    }

    @Override
    protected VPGRefactoring<IFortranAST, Token, PhotranVPG> getRefactoring(List<IFile> files)
    {
        IfConstructStatementConversionRefactoring r = new IfConstructStatementConversionRefactoring();
        r.initialize(getFortranEditor().getIFile(), getFortranEditor().getSelection());
        return r;
    }

    public static class FortranIfConstructStatementConversionWizard extends
        AbstractFortranRefactoringWizard
    {
        protected IfConstructStatementConversionRefactoring ifConstructStatementConversionRefactoring;

        public FortranIfConstructStatementConversionWizard(
            IfConstructStatementConversionRefactoring r)
        {
            super(r);
            this.ifConstructStatementConversionRefactoring = r;
        }

        @Override
        protected void doAddUserInputPages()
        {
            if (ifConstructStatementConversionRefactoring.isStmtNode())
            {
                addPage(new UserInputWizardPage(ifConstructStatementConversionRefactoring.getName())
                {
                    protected Button shouldAddEmptyElseBlock;

                    public void createControl(Composite parent)
                    {
                        Composite top = new Composite(parent, SWT.NONE);
                        initializeDialogUnits(top);
                        setControl(top);
                        top.setLayout(new GridLayout(2, false));
                        Composite group = top;
                        new Label(group, SWT.NONE).setText(""); //$NON-NLS-1$

                        shouldAddEmptyElseBlock = new Button(group, SWT.CHECK);
                        shouldAddEmptyElseBlock.setText(Messages.IfConstructStatementConversionAction_AddEmptyElseBlock);
                        shouldAddEmptyElseBlock.setSelection(false);
                        shouldAddEmptyElseBlock.addSelectionListener(new SelectionListener()
                        {
                            public void widgetDefaultSelected(SelectionEvent e)
                            {
                                widgetSelected(e);
                            }

                            public void widgetSelected(SelectionEvent e)
                            {
                                if (shouldAddEmptyElseBlock.getSelection())
                                    ifConstructStatementConversionRefactoring.setAddEmptyElseBlock();
                            }
                        });
                    }
                });
            }
            else
            {
                addPage(new UserInputWizardPage(ifConstructStatementConversionRefactoring.getName())
                {
                    public void createControl(Composite parent)
                    {
                        Composite top = new Composite(parent, SWT.NONE);
                        initializeDialogUnits(top);
                        setControl(top);

                        top.setLayout(new GridLayout(1, false));

                        Label lbl = new Label(top, SWT.NONE);
                        lbl.setText(Messages.bind(
                            Messages.RefactoringAction_ClickOKToRunTheRefactoring,
                            ifConstructStatementConversionRefactoring.getName()));
                    }
                });
            }
        }
    }
}
