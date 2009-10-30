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

import org.eclipse.photran.internal.core.refactoring.KeywordCaseRefactoring;
import org.eclipse.rephraserengine.ui.refactoring.CustomUserInputPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * User input wizard page for the Change Keyword Case refactoring
 *
 * @author Kurt Hendle, Jeff Overbey
 */
public class KeywordCaseInputPage extends CustomUserInputPage<KeywordCaseRefactoring>
{
    protected Button radioLowerCase;
    protected Button radioUpperCase;

    @Override
    public void createControl(Composite parent)
    {
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
                getRefactoring().setLowerCase(isChecked);
            }
        });

        radioUpperCase = new Button(group, SWT.RADIO);
        radioUpperCase.setText("Upper Case");

        Label lbl = new Label(group, SWT.NONE);
        lbl.setText("Click OK to change the case of all keywords in the selected files. " +
            "To see what changes will be made, click Preview.");
    }
}
