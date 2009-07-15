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
package org.eclipse.photran.internal.refactoring.ui;

import java.util.ArrayList;

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
    protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new ExtractLocalVariableRefactoring(
            getFortranEditor().getIFile(),
            getFortranEditor().getSelection());
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
                protected Text nameField, typeField;
        
                public void createControl(Composite parent)
                {
                    Composite group = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(group);
                    setControl(group);
                    group.setLayout(new GridLayout(2, false));
                
                    GridData twoCol = new GridData();
                    twoCol.horizontalSpan = 2;
                    
                    Label lbl = new Label(group, SWT.NONE);
                    lbl.setText("Type:");

                    typeField = new Text(group, SWT.BORDER);
                    typeField.setText(extractRefactoring.getType());
                    typeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    typeField.selectAll();
                    typeField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            extractRefactoring.setType(typeField.getText());
                        }
                    });

                    Label lbl2 = new Label(group, SWT.NONE);
                    lbl2.setText("Name:");
                    
                    nameField = new Text(group, SWT.BORDER);
                    nameField.setText("");
                    nameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    nameField.selectAll();
                    nameField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            extractRefactoring.setName(nameField.getText());
                        }
                    });

                    // Call once for sure, just in case the user doesn't modify the text
                    extractRefactoring.setType(typeField.getText());
                    extractRefactoring.setName(nameField.getText());
                    
                    typeField.setFocus();
                }
            });
        }
    }
}
