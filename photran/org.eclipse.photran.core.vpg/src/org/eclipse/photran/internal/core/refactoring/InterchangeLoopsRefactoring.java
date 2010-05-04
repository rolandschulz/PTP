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
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter.Strategy;

/**
 * Interchanges two perfectly-nested DO-loops.
 *
 * @author Tim Yuvashev
 */
public class InterchangeLoopsRefactoring extends FortranEditorRefactoring
{
    private ASTProperLoopConstructNode outerLoop = null;
    private ASTProperLoopConstructNode innerLoop = null;

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        // Change AST to represent DO-loops as ASTProperLoopConstructNodes
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());

        outerLoop = findOuterLoop();
        innerLoop = findInnerLoop();
        if (outerLoop == null || innerLoop == null || outerLoop == innerLoop)
            fail("Please select two perfectly-nested loops to refactor.");

        status.addWarning("WARNING: This is an UNCHECKED TRANSFORMATION and is NOT guaranteed to preserve behavior.  " +
            "Proceed at your own risk.");
    }

    private ASTProperLoopConstructNode findOuterLoop()
    {
        return getLoopNode(this.astOfFileInEditor, this.selectedRegionInEditor);
    }

    private ASTProperLoopConstructNode findInnerLoop()
    {
        if (outerLoop != null)
            return getLoopNode(outerLoop.getBody().findFirstToken(),
                               outerLoop.getBody().findLastToken());
        else
            return null;
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final preconditions
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        swapHeaders();
        swapEndDoStmt();
        swapComments();

        Reindenter.reindent(outerLoop, this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);

        this.addChangeFromModifiedAST(this.fileInEditor, pm);

        vpg.releaseAST(this.fileInEditor);
    }

    protected void swapHeaders()
    {
        ASTLabelDoStmtNode outerHeader = outerLoop.getLoopHeader();
        ASTLabelDoStmtNode innerHeader = innerLoop.getLoopHeader();

        swap(outerHeader, innerHeader);
    }

    protected void swapEndDoStmt()
    {
        ASTEndDoStmtNode outerEnd = outerLoop.getEndDoStmt();
        ASTEndDoStmtNode innerEnd = innerLoop.getEndDoStmt();

        swap(outerEnd, innerEnd);
    }

    protected void swapComments()
    {
        Token outerLoopDoToken = outerLoop.getLoopHeader().findFirstToken();
        Token innerLoopDoToken = innerLoop.getLoopHeader().findFirstToken();

        String outerLoopWhiteText = outerLoopDoToken.getWhiteBefore();
        String innerLoopWhiteText = innerLoopDoToken.getWhiteBefore();

        innerLoopDoToken.setWhiteBefore(outerLoopWhiteText);
        outerLoopDoToken.setWhiteBefore(innerLoopWhiteText);
    }

    protected void swap(ASTNode outerElement, ASTNode innerElement)
    {
        outerElement.setParent(innerLoop);
        innerElement.setParent(outerLoop);
        innerLoop.replaceChild(innerElement, outerElement);
        outerLoop.replaceChild(outerElement, innerElement);
    }

    @Override
    public String getName()
    {
        return "Interchange Loops (Unchecked)";
    }
}
