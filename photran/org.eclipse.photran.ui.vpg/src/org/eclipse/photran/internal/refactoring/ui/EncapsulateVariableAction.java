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
import org.eclipse.photran.internal.core.refactoring.EncapsulateVariableRefactoring;
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
 * 
 * @author Tim
 */
public class EncapsulateVariableAction 
        extends AbstractFortranRefactoringActionDelegate
        implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{

    /**
     * @param refactoringClass
     * @param wizardClass
     */
    public EncapsulateVariableAction()
    {
        super(EncapsulateVariableRefactoring.class, EncapsulateVariableRefactoringWizard.class);
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.refactoring.ui.AbstractFortranRefactoringActionDelegate#getRefactoring(java.util.ArrayList)
     */
    @Override
    protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new EncapsulateVariableRefactoring(
                getFortranEditor().getIFile(),
                getFortranEditor().getSelection());
    }
    
    public static class EncapsulateVariableRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected EncapsulateVariableRefactoring refactoring;
        protected Text getterText;
        protected Text setterText;
        /**
         * @param r
         */
        public EncapsulateVariableRefactoringWizard(EncapsulateVariableRefactoring r)
        {
            super(r);
            refactoring = r;
        }
        
        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(refactoring.getName())
            {
                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);
                
                    top.setLayout(new GridLayout(2, false));
                    
                    Composite group = top;
                    Label lblGet = new Label(group, SWT.NONE);
                    lblGet.setText("Getter method name ");
                    
                    getterText = new Text(group, SWT.BORDER);
                    getterText.setText(refactoring.getDefaultGetterName());
                    getterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    getterText.selectAll();
                    getterText.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            refactoring.setGetterName(getterText.getText());
                        }
                    });
                    
                    Label lblSet = new Label(group, SWT.NONE);
                    lblSet.setText("Setter method name ");
                    
                    setterText = new Text(group, SWT.BORDER);
                    setterText.setText(refactoring.getDefaultSetterName());
                    setterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    setterText.selectAll();
                    setterText.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            refactoring.setSetterName(setterText.getText());
                        }
                    });
                /*
                    Label lbl = new Label(top, SWT.NONE);
                    String message = "Click OK to encapsulate selected variable" +
                                     "\nTo see what changes will be made, click Preview.";
                    lbl.setText(message);*/
                    
                }
            });
        }
        
    }

}
