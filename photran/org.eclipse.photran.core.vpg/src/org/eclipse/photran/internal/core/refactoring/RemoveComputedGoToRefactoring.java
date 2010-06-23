/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.parser.ASTComputedGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLblRefListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;

/**
 * Refactoring which replaces a computed GOTO statement with a SELECT CASE statement with nested
 * GOTO statements.
 * 
 * @author Rui Wang
 */
public class RemoveComputedGoToRefactoring extends FortranEditorRefactoring
{
    private ASTComputedGotoStmtNode computedGoto = null;
    
    @Override
    public String getName()
    {
        return Messages.RemoveComputedGoToRefactoring_Name;
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        ASTNode oldNode = getNode(this.astOfFileInEditor, this.selectedRegionInEditor, ASTComputedGotoStmtNode.class);
        if (oldNode == null)
            fail(Messages.RemoveComputedGoToRefactoring_PleaseSelectComputedGotoStmt);
        else
            computedGoto = (ASTComputedGotoStmtNode)oldNode;
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final preconditions
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        IASTNode newNode = makeSelectStmt();

        computedGoto.replaceWith(newNode);
        copyCommentsFromOldNode(newNode);
        Reindenter.reindent(newNode, astOfFileInEditor);

        this.addChangeFromModifiedAST(this.fileInEditor, pm);

        vpg.releaseAST(this.fileInEditor);
    }
    
    private IASTNode makeSelectStmt()
    {
        removeLeadingSpacesFrom(computedGoto.getExpr());
        
        StringBuilder stmt = new StringBuilder(256);
        
        stmt.append("select case ("); //$NON-NLS-1$
        stmt.append(computedGoto.getExpr());
        stmt.append(")\n"); //$NON-NLS-1$
        
        int i = 1;
        for (ASTLblRefListNode refListNode : computedGoto.getLblRefList())
        {
            stmt.append("case ("); //$NON-NLS-1$
            stmt.append(i);
            stmt.append(")\n        go to "); //$NON-NLS-1$
            stmt.append(refListNode.getLabel().getText());
            stmt.append("\n"); //$NON-NLS-1$
            i++;
        }
        stmt.append("end select"); //$NON-NLS-1$
        
        return parseLiteralStatementNoFail(stmt.toString());
    }

    private void removeLeadingSpacesFrom(IExpr expr)
    {
        expr.findFirstToken().setWhiteBefore(""); //$NON-NLS-1$
    }

    private void copyCommentsFromOldNode(IASTNode newNode)
    {
        newNode.findFirstToken().setWhiteBefore(computedGoto.findFirstToken().getWhiteBefore());
    }
}
