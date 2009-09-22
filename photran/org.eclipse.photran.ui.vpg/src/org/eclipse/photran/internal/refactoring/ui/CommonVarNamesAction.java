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
import org.eclipse.photran.internal.core.refactoring.CommonVarNamesRefactoring;
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
 * UI action to make COMMON block variable names consistent between program,
 * modules, subroutines, etc. 
 * Note: This class is modeled after RepObsOpersAction.java
 * 
 * @author Kurt Hendle
 */
public class CommonVarNamesAction extends AbstractFortranRefactoringActionDelegate 
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public CommonVarNamesAction()
    {
        super(CommonVarNamesRefactoring.class, FortranCommonVarNamesRefactoringWizard.class);
    }
    
    /* Similar to MoveSavedToCommonBlockAction.java */
    @Override protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new CommonVarNamesRefactoring(getFortranEditor().getIFile(), getFortranEditor().getSelection());
    }
    
    public static class FortranCommonVarNamesRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected CommonVarNamesRefactoring consistentCommonVarNames;
        
        public FortranCommonVarNamesRefactoringWizard(CommonVarNamesRefactoring r)
        {
            super(r);
            this.consistentCommonVarNames = r;
        }

        /* auto-generated method */
        @Override
        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(consistentCommonVarNames.getName())
            {
                protected ArrayList<Label> lblList = new ArrayList<Label>();
                protected ArrayList<Text> textList = new ArrayList<Text>();
                protected Label lbl;
                protected Text newNameField;
                
                protected ArrayList<String> oldNames = consistentCommonVarNames.getOldVarNames();
                protected ArrayList<String> newNames = consistentCommonVarNames.getNewVarNames();
                
                /* auto-generated method */
                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);
                    
                    top.setLayout(new GridLayout(2, true));
                    Composite group = top;
                    
                    Label headerLeft = new Label(group, SWT.NONE);
                    headerLeft.setText("Original Name");
                    
                    Label headerRight = new Label(group, SWT.NONE);
                    headerRight.setText("New Name");
                    
                    for(int i=0; i<consistentCommonVarNames.getNumCommonVars(); i++)
                    {
                        final int index = i;    //to allow use in listener
                        
                        lbl = new Label(group, SWT.NONE);
                        lbl.setText(oldNames.get(i));
                        lblList.add(lbl);
                        
                        newNameField = new Text(group, SWT.BORDER);
                        newNameField.setText(newNames.get(i));
                        newNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                        textList.add(newNameField);
                        
                        newNameField.addModifyListener(new ModifyListener()
                        {
                            public void modifyText(ModifyEvent e)
                            {
                                consistentCommonVarNames.modifyNewName(index, textList.get(index).getText());
                            }
                        });
                        //like in rename, call again to be sure
                        consistentCommonVarNames.modifyNewName(index, textList.get(index).getText());
                    }
                    /*Label blank = new Label(top, SWT.NONE);
                    blank.setText("");
                    Label instruct = new Label(top, SWT.NONE);
                    instruct.setText("Click OK to make COMMON block variable names consistent in this project. " +
                    	"\nTo see what changes will be made, click Preview." +
                    	"\n\nNOTE: This refactoring will not function correctly with Fortran implicit" +
                    	"typing and should only be used with an 'implicit none' statement and explicit" +
                    	"type declarations.");*/
                }
            });
        }
    }
}
