/*******************************************************************************
 * Copyright (c) 2007, 2009 University of Illinois at Urbana-Champaign and others.
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
import org.eclipse.photran.internal.core.refactoring.ExtractLocalVariableRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
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
 * Handles the Extract Local Variable action in the Fortran editor's Refactoring popup menu
 * and in the Refactor menu in the workbench menu bar.
 *
 * @author Jeff Overbey
 */
public class ExtractLocalVariableAction
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public ExtractLocalVariableAction()
    {
        super(ExtractLocalVariableRefactoring.class, FortranExtractLocalVariableRefactoringWizard.class);
    }

    @Override
    protected AbstractFortranRefactoring getRefactoring(List<IFile> files)
    {
        ExtractLocalVariableRefactoring r = new ExtractLocalVariableRefactoring();
        r.initialize(
            getFortranEditor().getIFile(),
            getFortranEditor().getSelection());
        return r;
    }

    public static class FortranExtractLocalVariableRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected ExtractLocalVariableRefactoring extractRefactoring;

        public FortranExtractLocalVariableRefactoringWizard(ExtractLocalVariableRefactoring r)
        {
            super(r);
            this.extractRefactoring = r;
        }

        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(extractRefactoring.getName())
            {
                protected Text declField;

                public void createControl(Composite parent)
                {
                    Composite group = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(group);
                    setControl(group);
                    group.setLayout(new GridLayout(2, false));

                    GridData twoCol = new GridData();
                    twoCol.horizontalSpan = 2;

                    Label lbl = new Label(group, SWT.NONE);
                    lbl.setText("Declaration:");

                    declField = new Text(group, SWT.BORDER);
                    declField.setText(extractRefactoring.getDecl());
                    declField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    declField.selectAll();
                    declField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            extractRefactoring.setDecl(declField.getText());
                        }
                    });

                    // Call once for sure, just in case the user doesn't modify the text
                    extractRefactoring.setDecl(declField.getText());

                    int offset = declField.getText().indexOf(":: ");
                    if (offset < 0) offset = 0; else offset = offset + ":: ".length();
                    declField.setSelection(offset, declField.getText().length());
                    declField.setFocus();
                }
            });
        }
    }
}
