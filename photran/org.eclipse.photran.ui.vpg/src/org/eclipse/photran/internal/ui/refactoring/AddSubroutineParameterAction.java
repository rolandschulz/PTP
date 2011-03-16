/*******************************************************************************
 * Copyright (c) 2010 Joe Handzik, Joe Gonzales, Marc Celani, and Jason Patel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Joe Handzik, Joe Gonzales, Marc Celani, and Jason Patel - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTSubroutineParNode;
import org.eclipse.photran.internal.core.refactoring.AddSubroutineParameterRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Handles the Add Subroutine Parameter action in the Fortran editor's Refactoring popup menu and in
 * the Refactor menu in the workbench menu bar.
 * 
 * @author Joe Handzik, Joe Gonzales, Marc Celani, Jason Patel
 */
public class AddSubroutineParameterAction extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public AddSubroutineParameterAction()
    {
        super(AddSubroutineParameterRefactoring.class,
            FortranAddSubroutineParameterRefactoringWizard.class);
    }

    @Override
    protected VPGRefactoring<IFortranAST, Token, PhotranVPG> getRefactoring(List<IFile> files)
    {
        AddSubroutineParameterRefactoring r = new AddSubroutineParameterRefactoring();
        r.initialize(getFortranEditor().getIFile(), getFortranEditor().getSelection());
        return r;
    }

    public static class FortranAddSubroutineParameterRefactoringWizard extends
        AbstractFortranRefactoringWizard
    {
        protected AddSubroutineParameterRefactoring addSubRefactoring;

        public FortranAddSubroutineParameterRefactoringWizard(AddSubroutineParameterRefactoring r)
        {
            super(r);
            this.addSubRefactoring = r;
        }

        @Override
        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(addSubRefactoring.getName())
            {
                protected Text declField;

                protected Text locationField;

                protected Text defaultField;

                public void createControl(Composite parent)
                {
                    Composite group = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(group);
                    setControl(group);
                    group.setLayout(new GridLayout(3, false));

                    GridData threeCol = new GridData();
                    threeCol.horizontalSpan = 3;

                    Label lbl = new Label(group, SWT.NONE);
                    lbl.setText(Messages.AddSubroutineParameterAction_DeclarationLabel);
                    Label lbl3 = new Label(group, SWT.NONE);
                    lbl3.setText(Messages.AddSubroutineParameterAction_DefaultLabel);
                    Label lbl2 = new Label(group, SWT.NONE);
                    lbl2.setText(Messages.AddSubroutineParameterAction_LocationLabel);

                    declField = new Text(group, SWT.BORDER);
                    declField.setText(addSubRefactoring.getDeclaration());
                    declField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    declField.selectAll();
                    declField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            addSubRefactoring.setDeclaration(declField.getText());
                        }
                    });

                    defaultField = new Text(group, SWT.BORDER);
                    defaultField.setText(addSubRefactoring.getDefault());
                    defaultField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    defaultField.selectAll();
                    defaultField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            addSubRefactoring.setDefaultValue(defaultField.getText());
                        }
                    });

                    locationField = new Text(group, SWT.BORDER);
                    locationField.setText(String.valueOf(addSubRefactoring.getPosition()));
                    locationField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    locationField.selectAll();
                    locationField.addModifyListener(new ModifyListener()
                    {
                        public void modifyText(ModifyEvent e)
                        {
                            addSubRefactoring.setPosition(Integer.parseInt(locationField.getText()));
                        }
                    });

                    // creates the table that lists the variables that exist in the subroutine
                    List<ASTSubroutineParNode> parList = addSubRefactoring.getOldParameterList();

                    GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
                    gridData.horizontalSpan = 3;
                    gridData.horizontalAlignment = GridData.FILL;

                    Table table = new Table(group, SWT.BORDER);
                    table.setLinesVisible(true);
                    table.setHeaderVisible(true);
                    table.setLayoutData(gridData);
                    for (int i = 0; i < parList.size(); i++)
                    {
                        TableColumn column = new TableColumn(table, SWT.NONE);
                        column.setText(String.valueOf(i));
                    }
                    TableItem item = new TableItem(table, SWT.NONE);
                    for (int i = 0; i < parList.size(); i++)
                    {
                        item.setText(i, parList.get(i).getVariableName().getText());
                    }
                    for (int i = 0; i < parList.size(); i++)
                    {
                        table.getColumn(i).pack();
                    }

                    // Call once for sure, just in case the user doesn't modify the text
                    // addSubRefactoring.setDeclaration(declField.getText());

                    int offset = declField.getText().indexOf(":: "); //$NON-NLS-1$
                    if (offset < 0)
                        offset = 0;
                    else
                        offset = offset + ":: ".length(); //$NON-NLS-1$
                    declField.setSelection(offset, declField.getText().length());
                    declField.setFocus();
                }
            });
        }
    }
}
