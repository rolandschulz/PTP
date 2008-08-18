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

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.internal.core.refactoring.MoveCommonToModuleRefactoring;
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
 * Handles the Move COMMON Block to Module action in the Fortran editor's Refactoring popup menu
 * and in the Refactor menu in the workbench menu bar.
 * 
 * @author Jeff Overbey
 */
public class MoveCommonToModuleAction
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public MoveCommonToModuleAction()
    {
        super(MoveCommonToModuleRefactoring.class, FortranMoveCommonToModuleRefactoringWizard.class);
    }
    
    public static class FortranMoveCommonToModuleRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected MoveCommonToModuleRefactoring refactoring;
        
        public FortranMoveCommonToModuleRefactoringWizard(MoveCommonToModuleRefactoring r)
        {
            super(r);
            this.refactoring = r;
        }

        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(refactoring.getName())
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
                    lbl.setText("Create a module named ");
                    
                    newNameField = new Text(group, SWT.BORDER);
                    newNameField.setText(refactoring.getSuggestedNewModuleName());
                    newNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    newNameField.selectAll();
                    newNameField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            refactoring.setNewModuleName(newNameField.getText());
                        }
                    });
                    
                    // Call once for sure, just in case the user doesn't modify the text
                    refactoring.setNewModuleName(newNameField.getText());
                    
                    newNameField.setFocus();
                }
            });
        }
    }
}
