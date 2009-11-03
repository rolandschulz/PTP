/*******************************************************************************
 * Copyright (c) 2007, 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.analysis.types.TypeChecker;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.ISpecificationStmt;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;


/**
 * Refactoring to extract an expression into a local (temporary) variable.
 *
 * INCOMPLETE
 *
 * @author Jeff Overbey
 */
public class ExtractLocalVariableRefactoring extends SingleFileFortranRefactoring
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private IExpr selectedExpr;
    private IActionStmt enclosingStmt;
    @SuppressWarnings("unchecked") private IASTListNode enclosingStmtList;
    private ScopingNode enclosingScope;

    private String name = null, type = null;

    private ASTTypeDeclarationStmtNode declToInsert = null;

    @Override
    public String getName()
    {
        return "Extract Local Variable";
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Specified Parameters
    ///////////////////////////////////////////////////////////////////////////

    public void setName(String name)
    {
        assert name != null;

        this.name = name;
    }

    public String getType()
    {
        assert type != null;

        return this.type;
    }

    public void setType(String type)
    {
        assert type != null;

        this.type = type;
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
        if (selection == null || !(selection instanceof IExpr))
            fail("Please select an expression to extract.");

        if (!nodeExactlyEnclosesRegion(selection, this.astOfFileInEditor, this.selectedRegionInEditor))
            fail("You have selected part of an expression, but either (1) the part you have selected is not an " +
                 "expression by itself, or (2) extracting that portion of the expression could change the " +
                 "meaning of the larger expression, due to a change in associativity or precedence.");

        selectedExpr = (IExpr)selection;

        enclosingStmt = selectedExpr.findNearestAncestor(IActionStmt.class);
        if (enclosingStmt == null)
            fail("Variables can only be extracted from action statements (e.g., assignments, print statements, etc.).");

        if (!(enclosingStmt.getParent() instanceof IASTListNode)) // Should never happen since <ActionStmt> only under <Body>
            fail("The selected expression is not located in a statement from which a variable can be extracted.");
        enclosingStmtList = (IASTListNode)enclosingStmt.getParent();

        enclosingScope = enclosingStmt.findNearestAncestor(ScopingNode.class);
        if (enclosingScope == null) // Should never happen since <ActionStmt> only under <Body>
            fail("Variables can only be extracted from action statements inside functions, subroutines, and main programs.");

        Type exprType = TypeChecker.getTypeOf(selectedExpr);
        if (exprType == Type.TYPE_ERROR)
            type = "";
        else
            type = exprType.toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert name != null;
        assert type != null;

        status.addWarning("If any functions in the original or extracted expression have side effects, this " +
                          "refactoring may not preserve behavior.");

        // Simple checks -- input validation

        if (name.trim().equals(""))
            fail("Please enter a name for the extracted variable.");

        if (!isValidIdentifier(name))
            fail(name + " is not a valid identifier");

        if (type.trim().equals(""))
            fail("Please enter a type for the extracted variable.");

        if (type.contains(";") || type.contains("!") || type.contains("&"))
            fail("A variable cannot be declared with the given type \"" + type + "\".");

        IBodyConstruct decl = parseLiteralStatementNoFail(type + " :: " + name);
        if (decl == null || !(decl instanceof ASTTypeDeclarationStmtNode))
            fail("A variable cannot be declared with the given type \"" + type + "\".");

        declToInsert = (ASTTypeDeclarationStmtNode)decl;

        // Complex checks -- require program analysis

        checkForConflictingBindings(status);
    }


    private void checkForConflictingBindings(RefactoringStatus status)
    {
        Definition def = arbitraryDefinitionInScope();

        checkForConflictingBindings(new ConflictingBindingErrorHandler(status),
            def,
            Collections.<PhotranTokenRef>emptyList(),
            name);
    }

    private Definition arbitraryDefinitionInScope()
    {
        List<Definition> allDefs = enclosingScope.getAllDefinitions();
        if (allDefs.isEmpty())
            return null;
        else
            return allDefs.get(0);
    }

    private final class ConflictingBindingErrorHandler implements IConflictingBindingCallback
    {
        private final RefactoringStatus status;

        private ConflictingBindingErrorHandler(RefactoringStatus status) { this.status = status; }

        public void addConflictError(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = "The name \"" + conflict.name + "\" conflicts with " + vpg.getDefinitionFor(conflict.tokenRef);
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addError(msg, context);
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = "The name \"" + conflict.name + "\" might conflict with the name of an invoked subprogram";
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addWarning(msg, context);
        }

        public void addReferenceWillChangeError(String newName, Token reference)
        {
            throw new IllegalStateException();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert name != null;
        assert type != null;
        assert declToInsert != null;

        try
        {
            insertDeclaration();
            insertAssignment();
            replaceExpression();

            this.addChangeFromModifiedAST(this.fileInEditor, pm);
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    @SuppressWarnings("unchecked")
    private void insertDeclaration()
    {
        IASTListNode body = enclosingScope.getBody();
        body.add(findIndexToInsertDeclarationIn(body), declToInsert);
        Reindenter.reindent(declToInsert, astOfFileInEditor);
    }

    private int findIndexToInsertDeclarationIn(IASTListNode<? extends IASTNode> body)
    {
        int lastTypeDeclStmt = -1;
        int lastSpecStmt = -1;
        int lastUseStmt = -1;

        for (int i = 0; i < body.size(); i++)
        {
            IASTNode thisStmt = body.get(i);
            if (thisStmt instanceof ASTTypeDeclarationStmtNode)
                lastTypeDeclStmt = i;
            if (thisStmt instanceof ISpecificationStmt)
                lastSpecStmt = i;
            if (thisStmt instanceof ASTUseStmtNode)
                lastUseStmt = i;
        }

        if (lastTypeDeclStmt >= 0)
            return lastTypeDeclStmt + 1;
        else if (lastSpecStmt >= 0)
            return lastSpecStmt + 1;
        else if (lastUseStmt >= 0)
            return lastUseStmt + 1;
        else
            return 0;
    }

    @SuppressWarnings("unchecked")
    private void insertAssignment()
    {
        ASTAssignmentStmtNode assignmentStmt = (ASTAssignmentStmtNode)parseLiteralStatement(name + " = " + selectedExpr);
        enclosingStmtList.insertBefore(enclosingStmt, assignmentStmt);
        Reindenter.reindent(assignmentStmt, astOfFileInEditor);
    }

    private void replaceExpression()
    {
        IExpr variable = parseLiteralExpression(name);
        selectedExpr.replaceWith(variable);
    }
}
