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
import org.eclipse.photran.internal.core.refactoring.KeywordCaseRefactoring;
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
 * UI action to invoke the Keyword Case Refactoring (unify case of all tokens)
 * Note: This class is modeled after RepObsOpersAction.java
 * 
 * @author Kurt Hendle
 */
public class KeywordCaseAction 
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public KeywordCaseAction()
    {
        super(KeywordCaseRefactoring.class, FortranTokenCaseRefactoringWizard.class);
    }
    
    @Override protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new KeywordCaseRefactoring(files);
    }

    public static class FortranTokenCaseRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected KeywordCaseRefactoring changeKeywordCaseRefactoring;
        
        public FortranTokenCaseRefactoringWizard(KeywordCaseRefactoring r)
        {      
            super(r);
            this.changeKeywordCaseRefactoring = r;
        }
        
        @Override
        protected void doAddUserInputPages() {
            
            addPage(new UserInputWizardPage(changeKeywordCaseRefactoring.getName()) 
            {
                protected Button radioLowerCase;
                protected Button radioUpperCase;
                
                public void createControl(Composite parent) {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);
                    
                    top.setLayout(new GridLayout(1, false));
                    
                    Composite group = top;
                    Label instr = new Label(group, SWT.NONE);
                    instr.setText("Change keywords to:");
                    
                    radioLowerCase = new Button(group, SWT.RADIO);
                    radioLowerCase.setText("Lower Case");
                    radioLowerCase.setSelection(true);
                    radioLowerCase.addSelectionListener(new SelectionListener()
                    {
                        public void widgetDefaultSelected(SelectionEvent e)
                        {
                            widgetSelected(e);
                        }

                        public void widgetSelected(SelectionEvent e)
                        {
                            boolean isChecked = radioLowerCase.getSelection();
                            changeKeywordCaseRefactoring.setLowerCase(isChecked);
                        }
                        
                    });
                    
                    radioUpperCase = new Button(group, SWT.RADIO);
                    radioUpperCase.setText("Upper Case");

                    Label lbl = new Label(group, SWT.NONE);
                    lbl.setText("Click OK to change the case of all keywords in the selected files. " + 
                        "To see what changes will be made, click Preview.");
                }
            });
        }
    }
}