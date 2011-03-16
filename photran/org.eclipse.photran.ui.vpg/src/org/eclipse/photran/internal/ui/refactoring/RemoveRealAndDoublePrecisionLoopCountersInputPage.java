/*******************************************************************************
 * Copyright (c) 2010 Rita Chow, Nicola Hall, Jerry Hsiao, Mark Mozolewski, Chamil Wijenayaka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rita Chow - Initial Implementation
 *    Nicola Hall - Initial Implementation
 *    Jerry Hsiao - Initial Implementation
 *    Mark Mozolewski - Initial Implementation
 *    Chamil Wijenayaka - Initial Implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import org.eclipse.photran.internal.core.refactoring.RemoveRealAndDoublePrecisionLoopCountersRefactoring;
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
 * @author Rita Chow (chow15), Jerry Hsiao (jhsiao2), Mark Mozolewski (mozolews), Chamil Wijenayaka
 *         (wijenay2), Nicola Hall (nfhall2)
 */
public class RemoveRealAndDoublePrecisionLoopCountersInputPage extends
    CustomUserInputPage<RemoveRealAndDoublePrecisionLoopCountersRefactoring>
{
    protected Button doWhileLoop;

    protected Button doLoop;

    @Override
    public void createControl(Composite parent)
    {
        Composite top = new Composite(parent, SWT.NONE);
        initializeDialogUnits(top);
        setControl(top);

        top.setLayout(new GridLayout(1, false));

        Composite group = top;
        Label instr = new Label(group, SWT.NONE);
        instr
            .setText(Messages.RemoveRealAndDoublePrecisionLoopCountersInputPage_ReplaceRealDoublePrecisionLoopCounter);

        doWhileLoop = new Button(group, SWT.RADIO);
        doWhileLoop
            .setText(Messages.RemoveRealAndDoublePrecisionLoopCountersInputPage_ReplaceWithDoLoop);
        doWhileLoop.setSelection(true);
        doWhileLoop.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e)
            {
                boolean isChecked = doLoop.getSelection();
                getRefactoring().setShouldReplaceWithDoWhileLoop(isChecked);
            }
        });

        doLoop = new Button(group, SWT.RADIO);
        doLoop
            .setText(Messages.RemoveRealAndDoublePrecisionLoopCountersInputPage_ReplaceWithDoWhileLoop);

        Label ok = new Label(group, SWT.NONE);
        ok.setText("\n" + Messages.RemoveRealAndDoublePrecisionLoopCountersInputPage_ClickOKMessage); //$NON-NLS-1$
        Label preview = new Label(group, SWT.NONE);
        preview
            .setText(Messages.RemoveRealAndDoublePrecisionLoopCountersInputPage_ClickPreviewMessage);
    }
}
