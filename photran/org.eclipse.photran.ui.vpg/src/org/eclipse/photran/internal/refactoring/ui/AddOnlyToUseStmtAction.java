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
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.internal.core.refactoring.AddOnlyToUseStmtRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
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
 * 
 * @author Kurt Hendle
 */
public class AddOnlyToUseStmtAction extends AbstractFortranRefactoringActionDelegate 
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public AddOnlyToUseStmtAction()
    {
        super(AddOnlyToUseStmtRefactoring.class, FortranAddOnlyToUseStmtRefactoringWizard.class);
    }
    
    @Override
    protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new AddOnlyToUseStmtRefactoring(getFortranEditor().getIFile(), getFortranEditor().getSelection());
    }
    
    public static class FortranAddOnlyToUseStmtRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected AddOnlyToUseStmtRefactoring addOnly;
        
        public FortranAddOnlyToUseStmtRefactoringWizard(AddOnlyToUseStmtRefactoring r)
        {
            super(r);
            this.addOnly = r;
        }

        @Override
        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(addOnly.getName())
            {
                protected ArrayList<String> entities = addOnly.getModuleEntityList();
                protected HashMap<Integer, String> newOnlyList = addOnly.getNewOnlyList();
                protected ArrayList<Button> checkList = new ArrayList<Button>();
                protected Button check;
                
                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);
                    
                    top.setLayout(new GridLayout(1,false));
                    Composite group = top;
                    
                    Label lbl = new Label(group, SWT.NONE);
                    lbl.setText("Select the module entities to add to the ONLY clause.");
                    
                    for(int i=0; i<addOnly.getNumEntitiesInModule(); i++)
                    {
                        check = new Button(group, SWT.CHECK);
                        check.setText(entities.get(i));
                        
                        if(newOnlyList.containsValue(entities.get(i))){
                            check.setSelection(true);
                            //addOnly.addToOnlyList(i, entities.get(i));
                        }
                        
                        checkList.add(check);
                    }
                    
                    //turns out need to add listeners last to make this work correctly
                    for(int i=0; i<addOnly.getNumEntitiesInModule(); i++)
                    {
                        final int index = i;
                        checkList.get(i).addSelectionListener(new SelectionListener()
                        {
                            public void widgetDefaultSelected(SelectionEvent e)
                            {
                                widgetSelected(e);
                            }

                            public void widgetSelected(SelectionEvent e)
                            {
                                boolean isChecked = checkList.get(index).getSelection();
                                
                                if(isChecked)
                                {
                                    addOnly.addToOnlyList(entities.get(index));
                                }
                                else //if(!isChecked)
                                {
                                    addOnly.removeFromOnlyList(entities.get(index));
                                }
                            }
                        });
                    }
                    
                    Label instruct = new Label(top, SWT.NONE);
                    instruct.setText("Click OK to add the ONLY clause to the selected USE statement." +
                            "To see what changes will be made, click Preview.");
                }
            });
        }
    }
}
