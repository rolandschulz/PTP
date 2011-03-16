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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.refactoring.RemoveAssignedGotoRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Handles the Remove Assigned Goto action in the Fortran editor's Refactoring popup menu and in the
 * Refactor menu in the workbench menu bar.
 * White-box test cases for the remove assigned goto refactoring.
 * @author Andrea Dranberg
 * @author John Hammonds
 * @author Rajashekhar Arasanal
 * @author Balaji Ambresh Rajkumar
 * @author Paramvir Singh
 */
public class RemoveAssignedGotoAction extends AbstractFortranRefactoringActionDelegate implements
    IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
    public RemoveAssignedGotoAction()
    {
        super(RemoveAssignedGotoRefactoring.class, FortranRemoveAssignedGotoWizard.class);
    }

    @Override
    protected VPGRefactoring<IFortranAST, Token, PhotranVPG> getRefactoring(List<IFile> files)
    {
        RemoveAssignedGotoRefactoring r = new RemoveAssignedGotoRefactoring();
        r.initialize(files);
        return r;
    }

    /**
     * Creates the user input dialog box that is specific to the RemoveAssignedGotoRefactoring.
     * This class is used by the action class.
     */
    public static class FortranRemoveAssignedGotoWizard extends AbstractFortranRefactoringWizard
    {
        public FortranRemoveAssignedGotoWizard(VPGRefactoring<IFortranAST, Token, PhotranVPG> r)
        {
            super(r);
        }

        /**
         * Sets the refactoring tool to {@link #RemoveAssignedGotoInputPage} for
         * the input wizard.
         */
        @Override
        protected void doAddUserInputPages()
        {
            RemoveAssignedGotoInputPage inputPage = new RemoveAssignedGotoInputPage();
            inputPage.setRefactoring((RemoveAssignedGotoRefactoring)getRefactoring());
            addPage(inputPage);
        }
    }
}
