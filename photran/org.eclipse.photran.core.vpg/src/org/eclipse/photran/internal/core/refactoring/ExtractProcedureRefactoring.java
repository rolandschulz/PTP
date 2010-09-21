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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.GenericASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.Token.FakeToken;
import org.eclipse.photran.internal.core.parser.ASTArrayDeclaratorNode;
import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecSeqNode;
import org.eclipse.photran.internal.core.parser.ASTCharSelectorNode;
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDimensionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTObjectNameNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Refactoring to extract a sequence of statements into a new subroutine,
 * replacing the sequence with a call to that subroutine.
 *
 * @author Jeff Overbey
 * @author Abhishek Sharma - Bug 313369
 */
/*
 * (Eventually, this should be generalized to extract either a subroutine or function,
 * hence the name "extract procedure.")
 */
public class ExtractProcedureRefactoring extends FortranEditorRefactoring
{
	private StatementSequence selection = null;
	private List<Definition> localVarsToPassInAsParams = new LinkedList<Definition>();
	private String newName = null;

    @Override
    public String getName()
    {
        return Messages.ExtractProcedureRefactoring_Name;
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Specified Parameters
    ///////////////////////////////////////////////////////////////////////////

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

        // Ensure that partial loops won't be extracted
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());

    	selection = findEnclosingStatementSequence(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selection == null || selection.selectedStmts.isEmpty())
            fail(Messages.ExtractProcedureRefactoring_PleaseSelectContiguousStatements);

        if (selection.enclosingScope == null)
            fail("INTERNAL ERROR: Unable to locate enclosing scope"); //$NON-NLS-1$

        if (!selection.enclosingScope.isSubprogram() && !selection.enclosingScope.isMainProgram())
            fail(Messages.ExtractProcedureRefactoring_CanOnlyExtractFromSubprogramOrMainProgram);

        for (IASTNode stmt : selection.selectedStmts)
            if (!(stmt instanceof IBodyConstruct))
                fail(
                    Messages.bind(
                        Messages.ExtractProcedureRefactoring_StatementCannotBeExtracted,
                        stmt.toString().trim()));

        checkForLabels(status);

        for (IASTNode stmt : selection.selectedStmts)
            if (!(stmt instanceof IExecutionPartConstruct))
                fail(
                    Messages.bind(
                        Messages.ExtractProcedureRefactoring_OnlyExecutableStatementsCanBeExtracted,
                        stmt.toString().trim()));

        determineParameters();

        for (Definition param : localVarsToPassInAsParams)
            if (param.isPointer())
                fail(Messages.ExtractProcedureRefactoring_ExtractionWouldRequirePointerParameter);
    }

    private void checkForLabels(RefactoringStatus status)
    {
        Pattern numericLabel = Pattern.compile("[0-9]+"); //$NON-NLS-1$

        for (IASTNode stmt : selection.enclosingScope.getBody())
        {
            if (numericLabel.matcher(stmt.findFirstToken().getText()).matches())
            {
                status.addWarning(
                    Messages.ExtractProcedureRefactoring_ProcedureContainsLabels,
                    createContext(stmt.findFirstToken().getTokenRef()));
                return;
            }
        }
    }

    private void determineParameters()
    {
        localVarsToPassInAsParams.addAll(localVariablesUsedIn(selection.selectedStmts));
        localVarsToPassInAsParams.addAll(0, localVarsReferencedInDecls());
    }

    private List<Definition> localVarsReferencedInDecls()
    {
        List<Definition> result = new LinkedList<Definition>();

        for (Set<Definition> addlVars = addlLocalVariablesReferencedIn(localVarsToPassInAsParams);
             !addlVars.isEmpty();
             addlVars = addlLocalVariablesReferencedIn(result))
        {
            result.addAll(0, addlVars);
        }

        return result;
    }

    private Set<Definition> localVariablesUsedIn(List<IASTNode> stmts)
    {
        Set<Definition> result = new TreeSet<Definition>();

        for (IASTNode stmt : selection.selectedStmts)
            result.addAll(localVariablesUsedIn(stmt));

        return result;
    }

    private Set<Definition> localVariablesUsedIn(IASTNode node)
    {
        final Set<Definition> result = new TreeSet<Definition>();
        node.accept(new GenericASTVisitorWithLoops()
        {
            @Override public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT)
                    for (Definition def : token.resolveBinding())
                        if (def.isLocalVariable())
                            result.add(def);
            }
        });
        return result;
    }

    /**
     * The extraction
     * <pre>
     * integer, parameter :: FIVE = 5
     * integer, parameter :: SIZE = FIVE
     * real :: matrix(SIZE, SIZE)
     * matrix(:, :) = 0.0  ! <<<<< EXTRACT THIS STATEMENT
     * </pre>
     * will initially compute the set of local variables to be { matrix }.
     * However, the declaration of matrix uses the declaration of SIZE,
     * so the &quot;closure&quot; is { matrix, SIZE }, since this is the
     * minimal set of variables that must be passed to the new procedure.
     */
    private Set<Definition> addlLocalVariablesReferencedIn(Collection<Definition> vars)
    {
        final Set<Definition> result = new TreeSet<Definition>();

        for (Definition def : vars)
        {
            ASTArraySpecNode arraySpec = findArraySpec(def);
            if (arraySpec != null)
                result.addAll(localVariablesUsedIn(arraySpec));
        }

        return result;
    }

    private ASTArraySpecNode findArraySpec(Definition def)
    {
        ASTArraySpecNode arraySpec = findArraySpecInTypeDecl(def);
        if (arraySpec != null)
            return arraySpec;

        arraySpec = findArraySpecInDimensionStmt(def);
        return arraySpec;
    }

    private ASTArraySpecNode findArraySpecInTypeDecl(Definition def)
    {
        ASTTypeDeclarationStmtNode typeDecl = findTypeDeclaration(def);
        if (typeDecl != null)
        {
            if (typeDecl.getAttrSpecSeq() != null)
                for (ASTAttrSpecSeqNode attrSpecSeq : typeDecl.getAttrSpecSeq())
                    if (attrSpecSeq.getAttrSpec().isDimension())
                        return attrSpecSeq.getAttrSpec().getArraySpec();

            for (ASTEntityDeclNode decl : typeDecl.getEntityDeclList())
                if (decl.getArraySpec() != null && matches(decl.getObjectName(), def))
                    return decl.getArraySpec();
        }

        return null;
    }

    private ASTTypeDeclarationStmtNode findTypeDeclaration(Definition def)
    {
        return def.getTokenRef().findToken().findNearestAncestor(ASTTypeDeclarationStmtNode.class);
    }

    private ASTArraySpecNode findArraySpecInDimensionStmt(final Definition def)
    {
        class Visitor extends GenericASTVisitor
        {
            private ASTArraySpecNode result = null;

            @Override public void visitASTDimensionStmtNode(ASTDimensionStmtNode node)
            {
                for (ASTArrayDeclaratorNode arrayDeclarator : node.getArrayDeclaratorList())
                    if (matches(arrayDeclarator, def))
                        result = arrayDeclarator.getArraySpec();
            }
        }

        ScopingNode scope = def.getTokenRef().findToken().findNearestAncestor(ScopingNode.class);
        Visitor v = new Visitor();
        scope.accept(v);
        return v.result;
    }

    private boolean matches(ASTArrayDeclaratorNode arrayDeclarator, Definition def)
    {
        String declVar = PhotranVPG.canonicalizeIdentifier(arrayDeclarator.getVariableName().getText());
        String targetVar = def.getCanonicalizedName();
        return declVar.equals(targetVar);
    }

    private boolean matches(ASTObjectNameNode objectName, Definition def)
    {
        String declVar = PhotranVPG.canonicalizeIdentifier(objectName.getObjectName().getText());
        String targetVar = def.getCanonicalizedName();
        return declVar.equals(targetVar);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert selection != null && (selection.enclosingScope.isSubprogram() || selection.enclosingScope.isMainProgram());
        assert newName != null;

        checkIfSubprogramNameIsValid();
        checkIfSubprogramNameWillConflict(status);
    }

    private void checkIfSubprogramNameIsValid() throws PreconditionFailure
    {
        if (!isValidIdentifier(newName))
            fail(Messages.bind(Messages.ExtractProcedureRefactoring_InvalidIdentifier, newName));
    }

    private void checkIfSubprogramNameWillConflict(RefactoringStatus status)
    {
        ScopingNode enclosingSubprogram = selection.enclosingScope;

        ScopingNode outerScope = enclosingSubprogram.findNearestAncestor(ScopingNode.class);
        if (outerScope == null)
            throw new Error("INTERNAL ERROR: No outer scope"); //$NON-NLS-1$

        FakeToken newSubprogramName = new FakeToken(enclosingSubprogram.getNameToken(), newName);
        List<PhotranTokenRef> conflictingDefs = outerScope.manuallyResolveNoImplicits(newSubprogramName);
        if (!conflictingDefs.isEmpty())
        {
            PhotranTokenRef conflict = conflictingDefs.get(0);
            Token conflictToken = conflict.findToken();
            status.addError(
                Messages.bind(
                    Messages.ExtractProcedureRefactoring_NameConflicts,
                    new Object[] {
                        newName,
                        conflictToken.getText(),
                        conflictToken.getLine(),
                        conflict.getFilename()
                    }),
                createContext(conflict)); // Highlight problematic declaration
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert selection != null && (selection.enclosingScope.isSubprogram() || selection.enclosingScope.isMainProgram());
        assert newName != null;

        try
        {
            ASTSubroutineSubprogramNode newSubprogram = createNewSubprogram();
            insertSubroutineCall();
            moveStatementsIntoBodyOf(newSubprogram);

            this.addChangeFromModifiedAST(this.fileInEditor, pm);
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private ASTSubroutineSubprogramNode createNewSubprogram()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("\n"); //$NON-NLS-1$
        sb.append("subroutine "); //$NON-NLS-1$
        sb.append(newName);
        sb.append(parameterList());
        sb.append("\n"); //$NON-NLS-1$

        sb.append("    implicit none\n"); //$NON-NLS-1$

        sb.append(parameterDeclarations());

        sb.append("end subroutine\n"); //$NON-NLS-1$

        ASTSubroutineSubprogramNode newSubroutine = (ASTSubroutineSubprogramNode)parseLiteralProgramUnit(sb.toString());

        return insertNewSubprogram(newSubroutine);
    }

    /* The new subprogram must be an internal subprogram if the statements are being extracted
     * from a main program or subprogram that contains other internal subprograms.  Otherwise,
     * references to existing internal subprograms will not carry over to the extracted subprogram.
     */
    private ASTSubroutineSubprogramNode insertNewSubprogram(ASTSubroutineSubprogramNode newSubroutine)
    {
        if (selection.enclosingScope.isSubprogram())
            return insertAfterEnclosingSubprogram(newSubroutine);
        else if (selection.enclosingScope.isMainProgram())
            return insertAsInternalSubprogramOf((ASTMainProgramNode)selection.enclosingScope, newSubroutine);
        else
            throw new IllegalStateException();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ASTSubroutineSubprogramNode insertAfterEnclosingSubprogram(ASTSubroutineSubprogramNode newSubroutine)
    {
        ScopingNode enclosingSubprogram = selection.enclosingScope;

        IASTNode parent = enclosingSubprogram.getParent();
        if (!(parent instanceof IASTListNode))
            throw new Error("INTERNAL ERROR: Subprogram parent is not IASTListNode"); //$NON-NLS-1$

        ((IASTListNode)parent).insertAfter(enclosingSubprogram, newSubroutine);

        Reindenter.reindent(newSubroutine, this.astOfFileInEditor);

        return newSubroutine;
    }

    private ASTSubroutineSubprogramNode insertAsInternalSubprogramOf(ASTMainProgramNode program, ASTSubroutineSubprogramNode subprogram)
    {
        if (program.getContainsStmt() == null)
        {
            ASTContainsStmtNode containsStmt = createContainsStmt();
            program.setContainsStmt(containsStmt);
            containsStmt.setParent(program);
        }

        if (program.getInternalSubprograms() == null)
        {
            ASTListNode<IInternalSubprogram> internals = new ASTListNode<IInternalSubprogram>();
            program.setInternalSubprograms(internals);
            internals.setParent(program);
        }

        program.getInternalSubprograms().add(subprogram);
        subprogram.setParent(program.getInternalSubprograms());

        Reindenter.reindent(subprogram, this.astOfFileInEditor);

        return subprogram;
    }

    private String parameterList()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("("); //$NON-NLS-1$

        int i = 0;
        for (Definition var : localVarsToPassInAsParams)
        {
            if (i++ > 0) sb.append(", "); //$NON-NLS-1$
            sb.append(var.getDeclaredName());
        }

        sb.append(")"); //$NON-NLS-1$

        return sb.toString();
    }

    private String parameterDeclarations()
    {
        StringBuilder sb = new StringBuilder();

        for (Definition var : localVarsToPassInAsParams)
        {
            sb.append("    "); //$NON-NLS-1$
            sb.append(declarationOf(var));
            sb.append("\n"); //$NON-NLS-1$
        }

        return sb.toString();
    }

    private String declarationOf(Definition var)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(var.getType().toString());
        if (var.getType().equals(Type.CHARACTER))
            sb.append(getCharSelector(var));

        if (var.isAllocatable()) sb.append(", allocatable"); //$NON-NLS-1$
        if (var.isIntentIn() && !var.isIntentOut()) sb.append(", intent(in)"); //$NON-NLS-1$
        if (!var.isIntentIn() && var.isIntentOut()) sb.append(", intent(out)"); //$NON-NLS-1$
        if (var.isPointer()) sb.append(", pointer"); //$NON-NLS-1$
        if (var.isTarget()) sb.append(", target"); //$NON-NLS-1$

        sb.append(" :: "); //$NON-NLS-1$
        sb.append(var.getDeclaredName());

        if (var.getArraySpec() != null)
            sb.append(var.getArraySpec());

        return sb.toString();
    }

    private String getCharSelector(Definition var)
    {
        ASTTypeDeclarationStmtNode typeDeclStmt = var.getTokenRef().findToken().findNearestAncestor(ASTTypeDeclarationStmtNode.class);
        if (typeDeclStmt != null)
        {
            ASTCharSelectorNode charSelector = typeDeclStmt.getTypeSpec().getCharSelector();
            if (charSelector != null)
            {
//                sb.append("(LEN="); //$NON-NLS-1$
//                sb.append(charSelector.getLengthExpr().toString());
//                sb.append(")"); //$NON-NLS-1$
                return charSelector.toString();
            }
        }
        return ""; //$NON-NLS-1$
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void insertSubroutineCall()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("call "); //$NON-NLS-1$
        sb.append(newName);
        sb.append(parameterList());
        sb.append("\n"); //$NON-NLS-1$

        IBodyConstruct callStmt = parseLiteralStatement(sb.toString());
        ((IASTListNode)selection.listContainingStmts).insertBefore(selection.firstStmt(), callStmt);
        callStmt.setParent(selection.listContainingStmts);
        Reindenter.reindent(callStmt, this.astOfFileInEditor);
    }

    private void moveStatementsIntoBodyOf(ASTSubroutineSubprogramNode newSubprogram)
    {
        for (IASTNode stmt : selection.selectedStmts)
        {
            assert stmt instanceof IBodyConstruct;

            stmt.removeFromTree();
            newSubprogram.getBody().add((IBodyConstruct)stmt);
            stmt.setParent(newSubprogram.getBody());
        }

        Reindenter.reindent(selection.firstToken(), selection.lastToken(), this.astOfFileInEditor);
    }
}
