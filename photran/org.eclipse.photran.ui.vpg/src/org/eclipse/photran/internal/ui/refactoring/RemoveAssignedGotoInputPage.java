/*******************************************************************************
 * Copyright (c) 2010 Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, 
 * Balaji Ambresh Rajkumar and Paramvir Singh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, Balaji Ambresh Rajkumar
 * and Paramvir Singh - Initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import org.eclipse.photran.internal.core.refactoring.RemoveAssignedGotoRefactoring;
import org.eclipse.rephraserengine.ui.refactoring.CustomUserInputPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Creates the input wizard with a YES/NO radio button with the refactoring
 * prompt. Starter code was borrowed from {#link KeywordCaseInputPage}
 * White-box test cases for the remove assigned goto refactoring.
 * @author Andrea Dranberg
 * @author John Hammonds
 * @author Rajashekhar Arasanal
 * @author Balaji Ambresh Rajkumar
 * @author Paramvir Singh
 */
public class RemoveAssignedGotoInputPage extends CustomUserInputPage<RemoveAssignedGotoRefactoring>
{
    protected Button radioYes;
    protected Button radioNo;

    @Override
    public void createControl(Composite parent)
    {
        Composite top = new Composite(parent, SWT.NONE);
        initializeDialogUnits(top);
        setControl(top);

        top.setLayout(new GridLayout(1, false));

        Composite group = top;
        Label instr = new Label(group, SWT.NONE);
        instr.setText(Messages.RemoveAssignGotoInputPage_Prompt);

        radioYes = new Button(group, SWT.RADIO);
        radioYes.setText(Messages.RemoveAssignGotoInputPage_Yes);
        radioYes.setSelection(true);
        radioYes.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e)
            {
                getRefactoring().setDefaultSelected(radioYes.getSelection());
            }
        });

        radioNo = new Button(group, SWT.RADIO);
        radioNo.setText(Messages.RemoveAssignGotoInputPage_No);

        Label lbl = new Label(group, SWT.NONE);
        lbl.setText(Messages.RemoveAssignGotoInputPage_ClickOKMessage);
    }
}
