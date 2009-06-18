/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UFSM - Universidade Federal de Santa Maria (www.ufsm.br)
 *     UNIJUI - Universidade Regional do Noroeste do Estado do Rio Grande do Sul (www.unijui.edu.br)
 *     UIUC (modified to use MultipleFileFortranRefactoring)
 *******************************************************************************/
package org.eclipse.photran.internal.refactoring.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.internal.core.refactoring.RepObsOpersRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * UI action to invoke the Replace Obsolete Operators refactoring
 * 
 * @author Bruno B. Boniati
 * @author Jeff Overbey
 */
public class RepObsOpersAction
    extends AbstractFortranRefactoringActionDelegate
    implements IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public RepObsOpersAction()
    {
        super(RepObsOpersRefactoring.class, FortranRepObsOpersRefactoringWizard.class);
    }
    
    @Override protected AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files)
    {
        return new RepObsOpersRefactoring(files);
    }

    public static class FortranRepObsOpersRefactoringWizard extends AbstractFortranRefactoringWizard
    {
        protected RepObsOpersRefactoring replaceObsoleteOperatorsRefactoring;
        
        public FortranRepObsOpersRefactoringWizard(RepObsOpersRefactoring r)
        {      
            super(r);
            this.replaceObsoleteOperatorsRefactoring = r;
        }

        @Override
        protected void doAddUserInputPages() {
            addPage(new UserInputWizardPage(replaceObsoleteOperatorsRefactoring.getName()) {

            public void createControl(Composite parent) {
                Composite top = new Composite(parent, SWT.NONE);
                initializeDialogUnits(top);
                setControl(top);

                top.setLayout(new GridLayout(1, false));

                Label lbl = new Label(top, SWT.NONE);
                lbl.setText("Clique OK to replace obsolete operators in the selected files. To see what changes will be made, click Preview.");
            }
            });
        }
    }
}
