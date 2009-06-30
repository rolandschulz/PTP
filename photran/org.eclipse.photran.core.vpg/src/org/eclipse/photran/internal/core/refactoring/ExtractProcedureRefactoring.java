/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.GenericASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.Token.FakeToken;
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;

/**
 * Refactoring to extract a sequence of statements into a new subroutine,
 * replacing the sequence with a call to that subroutine.
 * 
 * @author Jeff Overbey
 */
/*
 * (Eventually, this should be generalized to extract either a subroutine or function,
 * hence the name "extract procedure.")
 */
public class ExtractProcedureRefactoring extends SingleFileFortranRefactoring
{
	private StatementSequence selection = null;
	private Set<Definition> localVarsToPassInAsParams = new TreeSet<Definition>();
	private String newName = null;
	
    public ExtractProcedureRefactoring(IFile file, ITextSelection selection)
    {
        super(file, selection);
        //System.out.println(this.fileInEditor.getName());
    }
    
    @Override
    public String getName()
    {
        return "Extract Procedure";
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
        
    	selection = this.findEnclosingStatementSequence(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selection == null || selection.selectedStmts.isEmpty())
            fail("Please select a sequence of contiguous statements to extract.");
        
        if (selection.enclosingScope == null)
            fail("INTERNAL ERROR: Unable to locate enclosing scope");
        
        if (!selection.enclosingScope.isSubprogram() && !selection.enclosingScope.isMainProgram())
            fail("Statements can only be extracted from inside a subprogram or main program.");
        
        for (IASTNode stmt : selection.selectedStmts)
            if (!(stmt instanceof IBodyConstruct))
                fail("The statement \"" + stmt.toString().trim() + "\" cannot be extracted");
        
        checkForLabels(status);
        
        for (IASTNode stmt : selection.selectedStmts)
            if (!(stmt instanceof IExecutionPartConstruct))
                fail("Only executable statements can be extracted; the statement \"" + stmt.toString().trim() + "\" cannot");
        
        determineParameters();
    }
    
    private void checkForLabels(RefactoringStatus status)
    {
        Pattern numericLabel = Pattern.compile("[0-9]+");
        
        for (IASTNode stmt : selection.enclosingScope.getBody())
        {
            if (numericLabel.matcher(stmt.findFirstToken().getText()).matches())
            {
                status.addWarning("This procedure contains labels; the extracted " +
                    "subprogram may not be correct if the selected statements reference " +
                    "a label outside the extracted statements.",
                    createContext(stmt.findFirstToken().getTokenRef()));
                return;
            }
        }
    }

    private void determineParameters()
    {
        // TODO: This does not consider local variables used in variable declarations.
        //     integer, parameter :: SIZE = 5
        //     real :: matrix(SIZE, SIZE)
        //     matrix(:, :) = 0.0  ! <<<<< EXTRACT THIS STATEMENT
        // will not extract the SIZE variable
        
        
//        Set<Definition> varsUsedBeforeSelection = localVariablesUsedIn(stmtSeq.precedingStmts);
        Set<Definition> varsUsedInSelection = localVariablesUsedIn(selection.selectedStmts);
//        Set<Definition> varsUsedAfterSelection = localVariablesUsedIn(stmtSeq.followingStmts);

        for (Definition localVar : varsUsedInSelection)
        {
//            if (varsUsedBeforeSelection.contains(localVar) || varsUsedAfterSelection.contains(localVar))
                localVarsToPassInAsParams.add(localVar);
//            else
//                localVarsToMoveToExtractedSubprogram.add(localVar);
        }
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
        final Set<Definition> result = new HashSet<Definition>();
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

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert selection != null && selection.enclosingScope.isSubprogram();
        assert newName != null;

        checkIfSubprogramNameIsValid();
        checkIfSubprogramNameWillConflict(status);
    }

    private void checkIfSubprogramNameIsValid() throws PreconditionFailure
    {
        if (!isValidIdentifier(newName))
            fail(newName + " is not a valid identifier");
    }
    
    private void checkIfSubprogramNameWillConflict(RefactoringStatus status)
    {
        ScopingNode enclosingSubprogram = selection.enclosingScope;
        
        ScopingNode outerScope = enclosingSubprogram.findNearestAncestor(ScopingNode.class);
        if (outerScope == null)
            throw new Error("INTERNAL ERROR: No outer scope");
        
        FakeToken newSubprogramName = new FakeToken(enclosingSubprogram.getNameToken(), newName);
        List<PhotranTokenRef> conflictingDefs = outerScope.manuallyResolveNoImplicits(newSubprogramName);
        if (!conflictingDefs.isEmpty())
        {
            PhotranTokenRef conflict = conflictingDefs.get(0);
            Token conflictToken = conflict.findToken();
            status.addError("The name \"" + newName + "\""
                + " conflicts with \"" + conflictToken.getText() + "\" on line " + conflictToken.getLine()
                + " in " + conflict.getFilename(),
                createContext(conflict)); // Highlight problematic declaration
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert selection != null && selection.enclosingScope.isSubprogram();
        assert newName != null;

        ASTSubroutineSubprogramNode newSubprogram = createNewSubprogram();
        insertSubroutineCall();
        moveStatementsIntoBodyOf(newSubprogram);
        
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
    }

    private ASTSubroutineSubprogramNode createNewSubprogram()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n");
        sb.append("subroutine ");
        sb.append(newName);
        sb.append(parameterList());
        sb.append("\n");
        
        sb.append("    implicit none\n");
        
        sb.append(parameterDeclarations());
        
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
        if (selection.enclosingScope.isSubprogram())
            return insertAfterEnclosingSubprogram(newSubroutine);
        else if (selection.enclosingScope.isMainProgram())
            return insertAsInternalSubprogramOf((ASTMainProgramNode)selection.enclosingScope, newSubroutine);
        else
            throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    private ASTSubroutineSubprogramNode insertAfterEnclosingSubprogram(ASTSubroutineSubprogramNode newSubroutine)
    {
        ScopingNode enclosingSubprogram = selection.enclosingScope;
        
        IASTNode parent = enclosingSubprogram.getParent();
        if (!(parent instanceof IASTListNode))
            throw new Error("INTERNAL ERROR: Subprogram parent is not IASTListNode");
        
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
        
        Reindenter.reindent(subprogram, this.astOfFileInEditor);

        return subprogram;
    }

    private String parameterList()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("(");
        
        int i = 0;
        for (Definition var : localVarsToPassInAsParams)
        {
            if (i++ > 0) sb.append(", ");
            sb.append(var.getDeclaredName());
        }
        
        sb.append(")");
        
        return sb.toString();
    }

    private String parameterDeclarations()
    {
        StringBuilder sb = new StringBuilder();
        
        for (Definition var : localVarsToPassInAsParams)
        {
            sb.append("    ");
            sb.append(declarationOf(var));
            sb.append("\n");
        }
        
        return sb.toString();
    }

    private String declarationOf(Definition var)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(var.getType().toString());
        // TODO: Pointer, target
        sb.append(" :: ");
        sb.append(var.getDeclaredName());
        
        if (var.getArraySpec() != null)
            sb.append(var.getArraySpec());
        
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void insertSubroutineCall()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("call ");
        sb.append(newName);
        sb.append(parameterList());
        sb.append("\n");
        
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
