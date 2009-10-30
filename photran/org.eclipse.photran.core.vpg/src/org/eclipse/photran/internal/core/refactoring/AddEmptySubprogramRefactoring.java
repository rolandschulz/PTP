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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.rephraserengine.core.refactorings.UserInputString;
import org.eclipse.rephraserengine.internal.core.preservation.Model;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Alpha;

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

    private ScopingNode enclosingScope;

    private String newName = null;

    private List<PrimitiveOp> primitiveOps;

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

    @SuppressWarnings("unchecked")
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
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert newName != null;
        assert enclosingScope != null;

        try
        {
            Model initial = new Model(vpg, PhotranVPG.getFilenameForIFile(fileInEditor));
            System.out.println("INITIAL MODEL:");
            System.out.println(initial);

            primitiveOps = new LinkedList<PrimitiveOp>();
            ASTSubroutineSubprogramNode newSubprogram = createNewSubprogram();

            System.out.println("BEFORE ALPHA MERGE:");
            System.out.println(primitiveOps);

            System.out.println("AFTER ALPHA MERGE:");
            mergeAlphas();
            System.out.println(primitiveOps);

            assert primitiveOps.size() == 1; // FIXME HACK

            initial.inormalize(primitiveOps.get(0));
            System.out.println("NORMALIZED INITIAL MODEL:");
            System.out.println(initial);

            this.addChangeFromModifiedAST(this.fileInEditor, pm);
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private void mergeAlphas() // FIXME HACK
    {
        Alpha alpha1 = (Alpha)primitiveOps.get(0);
        Alpha alpha2 = (Alpha)primitiveOps.get(1);
        if (alpha1.j.lb != alpha2.j.lb) return;

        primitiveOps.remove(1);
        primitiveOps.remove(0);

        primitiveOps.add(0, PrimitiveOp.alpha(
            alpha1.j.lb,
            alpha1.j.lb+alpha1.j.cardinality()+alpha2.j.cardinality()));
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

    private ASTSubroutineSubprogramNode insertAsInternalSubprogramOf(ASTMainProgramNode program, ASTSubroutineSubprogramNode subprogram)
    {
        if (program.getContainsStmt() == null)
        {
            ASTContainsStmtNode containsStmt = createContainsStmt();
            program.setContainsStmt(containsStmt);
            containsStmt.setParent(program);
            markAlpha(program.getContainsStmt(), program);
        }

        if (program.getInternalSubprograms() == null)
        {
            ASTListNode<IInternalSubprogram> internals = new ASTListNode<IInternalSubprogram>();
            program.setInternalSubprograms(internals);
            internals.setParent(program);
        }

        program.getInternalSubprograms().add(subprogram);
        subprogram.setParent(program.getInternalSubprograms());
        markAlpha(subprogram, program);

        //Reindenter.reindent(subprogram, this.astOfFileInEditor);

        return subprogram;
    }

    private void markAlpha(IASTNode node, IASTNode inAST)
    {
        assert primitiveOps != null;

        if (node == null) return;

        assert inAST != null;

        Token first = node.findFirstToken();
        Token last = node.findLastToken();
        if (first == null || last == null) return;

        Token previous = findLastTokenBefore(first, inAST);
        Token next = findFirstTokenAfter(last, inAST);
        if (previous == null || next == null) return; // FIXME should handle BOF, EOF

        int offset = previous.getFileOffset() + previous.getLength() + previous.getWhiteAfter().length();
        int length = node.toString().length();
        primitiveOps.add(PrimitiveOp.alpha(offset, offset+length));

        node.accept(new GenericASTVisitor()
        {
            @Override public void visitToken(Token token)
            {
                token.setFileOffset(-1);
            }
        });
    }

    private Token findLastTokenBefore(final Token target, IASTNode inAST)
    {
        class TokenFinder extends GenericASTVisitor
        {
            private Token lastToken = null;
            private Token result = null;

            @Override public void visitToken(Token thisToken)
            {
                if (thisToken == target)
                    result = lastToken;

                if (thisToken.getFileOffset() >= 0) // Skip tokens added in markAlpha above
                    lastToken = thisToken;
            }
        }

        TokenFinder t = new TokenFinder();
        inAST.accept(t);
        return t.result;
    }

    // from Definition

    private Token findFirstTokenAfter(final Token target, IASTNode inAST)
    {
        class TokenFinder extends GenericASTVisitor
        {
            private Token lastToken = null;
            private Token result = null;

            @Override public void visitToken(Token thisToken)
            {
                if (lastToken == target)
                    result = thisToken;

                lastToken = thisToken;
            }
        }

        TokenFinder t = new TokenFinder();
        inAST.accept(t);
        return t.result;
    }
}
