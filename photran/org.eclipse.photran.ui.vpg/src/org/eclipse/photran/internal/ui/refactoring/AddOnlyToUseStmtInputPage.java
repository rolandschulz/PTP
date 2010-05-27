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
package org.eclipse.photran.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.photran.internal.core.refactoring.AddOnlyToUseStmtRefactoring;
import org.eclipse.rephraserengine.ui.refactoring.CustomUserInputPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 *
 * @author Kurt Hendle
 */
public class AddOnlyToUseStmtInputPage extends CustomUserInputPage<AddOnlyToUseStmtRefactoring>
{
    protected ArrayList<String> entities;
    protected HashMap<Integer, String> newOnlyList;
    protected ArrayList<Button> checkList;
    protected Button check;

    @Override public void createControl(Composite parent)
    {
        entities = getRefactoring().getModuleEntityList();
        newOnlyList = getRefactoring().getNewOnlyList();
        checkList = new ArrayList<Button>();

        Composite top = new Composite(parent, SWT.NONE);
        initializeDialogUnits(top);
        setControl(top);

        top.setLayout(new GridLayout(1,false));
        Composite group = top;

        Label lbl = new Label(group, SWT.NONE);
        lbl.setText("Select the module entities to add to the ONLY clause.");

        for(int i=0; i<getRefactoring().getNumEntitiesInModule(); i++)
        {
            check = new Button(group, SWT.CHECK);
            check.setText(entities.get(i));

            if(newOnlyList.containsValue(entities.get(i))){
                check.setSelection(true);
                //getRefactoring().addToOnlyList(i, entities.get(i));
            }

            checkList.add(check);
        }

        //turns out need to add listeners last to make this work correctly
        for(int i=0; i<getRefactoring().getNumEntitiesInModule(); i++)
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
                        getRefactoring().addToOnlyList(entities.get(index));
                    }
                    else //if(!isChecked)
                    {
                        getRefactoring().removeFromOnlyList(entities.get(index));
                    }
                }
            });
        }

        Label instruct = new Label(top, SWT.NONE);
        instruct.setText("Click OK to add the ONLY clause to the selected USE statement." +
                "To see what changes will be made, click Preview.");
    }
}
