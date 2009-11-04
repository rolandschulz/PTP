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
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.preservation.PreservationAnalysis;
import org.eclipse.rephraserengine.core.refactorings.UserInputString;

/**
 * Refactoring to add an empty subroutine to a Fortran program.
 *
 * @author Jeff Overbey
 */
public class AddEmptySubprogramRefactoring extends SingleFileFortranRefactoring
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private PreservationAnalysis preservation = null;

    private ScopingNode enclosingScope;

    private String newName = null;

    @Override
    public String getName()
    {
        return "Add Empty Subprogram";
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Specified Parameters
    ///////////////////////////////////////////////////////////////////////////

    @UserInputString(label="Name:")
    public void setName(String name)
    {
        assert name != null;

        this.newName = name;
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

        if (!(enclosingScope instanceof ASTMainProgramNode))
            fail("Please place the cursor inside a main program.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert newName != null;
        assert enclosingScope != null;

        try
        {
            preservation = new PreservationAnalysis(PhotranVPG.getInstance(), pm,
                PhotranVPG.BINDING_EDGE_TYPE);

            preservation.monitor(fileInEditor);
            createNewSubprogram();
            vpg.commitChangesFromAST(fileInEditor);
            preservation.checkForPreservation(status, fileInEditor);

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

    // from ExtractProcedureRefactoring

    private ASTSubroutineSubprogramNode createNewSubprogram()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("subroutine ");
        sb.append(newName);
        sb.append("()\n");

        sb.append("    implicit none\n");

        sb.append("end subroutine\n");

        ASTSubroutineSubprogramNode newSubroutine = (ASTSubroutineSubprogramNode)parseLiteralProgramUnit(sb.toString());

        return insertNewSubprogram(newSubroutine);
    }

    /* The new subprogram must be an internal subprogram if the statements are being extracted
     * from a main program or subprogram that contains other internal subprograms.  Otherwise,
     * references to existing internal subprograms will not carry over to the extracted subprogram.
     */
    private ASTSubroutineSubprogramNode insertNewSubprogram(ASTSubroutineSubprogramNode newSubroutine)
    {
//        if (selection.enclosingScope.isSubprogram())
//            return insertAfterEnclosingSubprogram(newSubroutine);
//        else if (selection.enclosingScope.isMainProgram())
//            return insertAsInternalSubprogramOf((ASTMainProgramNode)selection.enclosingScope, newSubroutine);
//        else
//            throw new IllegalStateException();

            return insertAsInternalSubprogramOf((ASTMainProgramNode)enclosingScope, newSubroutine);
    }

    private ASTSubroutineSubprogramNode insertAsInternalSubprogramOf(
        ASTMainProgramNode program,
        ASTSubroutineSubprogramNode subprogram)
    {
        assert preservation != null;

        if (program.getContainsStmt() == null)
        {
            ASTContainsStmtNode containsStmt = createContainsStmt();
            program.setContainsStmt(containsStmt);
            containsStmt.setParent(program);
            preservation.markAlpha(fileInEditor, program.getContainsStmt());
        }

        if (program.getInternalSubprograms() == null)
        {
            ASTListNode<IInternalSubprogram> internals = new ASTListNode<IInternalSubprogram>();
            program.setInternalSubprograms(internals);
            internals.setParent(program);
        }

        program.getInternalSubprograms().add(subprogram);
        subprogram.setParent(program.getInternalSubprograms());
        preservation.markAlpha(fileInEditor, subprogram);

        //Reindenter.reindent(subprogram, this.astOfFileInEditor);

        return subprogram;
    }
}
