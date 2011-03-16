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
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTPauseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTPrintStmtNode;
import org.eclipse.photran.internal.core.parser.ASTReadStmtNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;

/**
 * This feature address the replacement of the PAUSE statement with a PRINT and READ statement.
 * Execution of a PAUSE statement may be different on different platforms. The refactoring assumes
 * the most basic functionality: it replaces the PAUSE statement with a PRINT statement that
 * displays the message of the PAUSE statement, immediately followed by a READ statement that waits
 * for any input from the user.
 * 
 * User Selection Requirements: The PAUSE statement to be replaced.
 * 
 * @author Rita Chow (chow15), Jerry Hsiao (jhsiao2), Mark Mozolewski (mozolews), Chamil Wijenayaka
 *         (wijenay2), Nicola Hall (nfhall2)
 */
public class RemovePauseStmtRefactoring extends FortranEditorRefactoring
{
    private ASTPauseStmtNode selectedPauseStmt = null;

    /**
     * Preconditions checks: Check if project has refactoring is enabled. Checks that the user
     * selects a PAUSE statement for refactoring. Failure to meet these conditions will result in a
     * fail-initial condition.
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        // Check if selected is a PAUSE statement
        selectedPauseStmt = findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor, ASTPauseStmtNode.class);
        if (selectedPauseStmt == null)
            fail(Messages.RemovePauseStmtRefactoring_PleaseSelectAPauseStatement);
    }

    /**
     * FinalConditions no final checks required.
     */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
    }

    /**
     * Performs main refactoring (delegated to other methods).
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        changePauseStmt();
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
    }


    /**
     * Insert modified PRINT statement and build READ() AST node to insert in to the program after
     * the PRINT statement.
     */
    private void changePauseStmt()
    {
        String pauseMessage = "\'\'"; //$NON-NLS-1$;

        // Contents of the PRINT statement to maintain.
        if (selectedPauseStmt.getStringConst() != null)
            pauseMessage = selectedPauseStmt.getStringConst().getText();

        // Build new AST node for modified PRINT statement and new READ statement.
        String indent = selectedPauseStmt.findFirstToken().getWhiteBefore();

        ASTPrintStmtNode printStmt = (ASTPrintStmtNode)parseLiteralStatement(
            indent + "PRINT *, " + pauseMessage + selectedPauseStmt.findLastToken().getWhiteBefore() + EOL); //$NON-NLS-1$
        if (selectedPauseStmt.getLabel() != null)
            printStmt.setLabel(selectedPauseStmt.getLabel());
        selectedPauseStmt.replaceWith(printStmt);

        indent = indent.substring(indent.lastIndexOf('\n')+1);
        ASTReadStmtNode readStmt = (ASTReadStmtNode)parseLiteralStatement(
            indent + "READ (*, *)" + EOL); //$NON-NLS-1$
        @SuppressWarnings("unchecked")
        ASTListNode<ASTNode> listNode = (ASTListNode<ASTNode>)selectedPauseStmt.getParent();
        listNode.insertAfter(printStmt, readStmt);
    }

    /**
     * Provide GUI refactoring label.
     */
    @Override
    public String getName()
    {
        return Messages.RemovePauseStmtRefactoring_Name;
    }
}
