/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.preservation.PreservationAnalysis;
import org.eclipse.rephraserengine.core.preservation.Preserve;

/**
 * Refactoring to delete an unused declaration from a Fortran program.
 *
 * @author Jeff Overbey
 */
public class SafeDeleteRefactoring extends SingleFileFortranRefactoring
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private PreservationAnalysis preservation = null;

    private ScopingNode enclosingScope;

    @Override
    public String getName()
    {
        return "Safe Delete";
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        IASTNode selection = this.findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selection == null)
            fail("Please place the cursor inside a scope.");

        enclosingScope = selection.findNearestAncestor(ScopingNode.class);
        if (enclosingScope == null) // Should never happen since <ActionStmt> only under <Body>
            fail("Please place the cursor inside a scope.");

        if (!(enclosingScope instanceof ASTMainProgramNode)
            //&& !(enclosingScope instanceof ASTModuleNode)
            && !(enclosingScope instanceof ASTSubroutineSubprogramNode)
            && !(enclosingScope instanceof ASTFunctionSubprogramNode))
            fail("Please place the cursor inside a main program or subprogram.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert enclosingScope != null;

        try
        {
            preservation = new PreservationAnalysis(PhotranVPG.getInstance(), pm,
                Preserve.incoming(PhotranVPG.BINDING_EDGE_TYPE));

            preservation.monitor(fileInEditor);
            deleteScope();
            vpg.commitChangesFromAST(fileInEditor);
            preservation.checkForPreservation(status);

            this.addChangeFromModifiedAST(this.fileInEditor, pm);
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    }

    private void deleteScope()
    {
        assert preservation != null;

        preservation.markEpsilon(fileInEditor, enclosingScope);
        enclosingScope.removeFromTree();
    }
}
