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
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.ISpecificationPartConstruct;
import org.eclipse.photran.internal.core.parser.ISpecificationStmt;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;


/**
 * Refactoring to extract an expression into a local (temporary) variable.
 *
 * INCOMPLETE
 *
 * @author Jeff Overbey
 * @author Ashley Kasza - externalized strings
 */
public class ExtractLocalVariableRefactoring extends FortranEditorRefactoring
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private IExpr selectedExpr;
    private IActionStmt enclosingStmt;
    @SuppressWarnings("rawtypes") private IASTListNode enclosingStmtList;
    private ScopingNode enclosingScope;

    private String decl = null;

    private ASTTypeDeclarationStmtNode declToInsert = null;
    private String name;

    @Override
    public String getName()
    {
        return Messages.ExtractLocalVariableRefactoring_Name;
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Specified Parameters
    ///////////////////////////////////////////////////////////////////////////

    public void setDecl(String decl)
    {
        assert decl != null;

        this.decl = decl;
    }

    public String getDecl()
    {
        assert decl != null;

        return this.decl;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("rawtypes")
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        IASTNode selection = findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selection == null || !(selection instanceof IExpr))
            fail(Messages.ExtractLocalVariableRefactoring_SelectExpressionToExtract);

        if (!nodeExactlyEnclosesRegion(selection, this.astOfFileInEditor, this.selectedRegionInEditor))
            fail(Messages.ExtractLocalVariableRefactoring_ErrorSelectingPartOfExpression);

        selectedExpr = (IExpr)selection;

        enclosingStmt = selectedExpr.findNearestAncestor(IActionStmt.class);
        if (enclosingStmt == null)
            fail(Messages.ExtractLocalVariableRefactoring_VarsExtractedOnlyFromActionStmt);

        if (!(enclosingStmt.getParent() instanceof IASTListNode)) // Should never happen since <ActionStmt> only under <Body>
            fail(Messages.ExtractLocalVariableRefactoring_ExpressionNotInExtractableStmt);
        enclosingStmtList = (IASTListNode)enclosingStmt.getParent();

        enclosingScope = enclosingStmt.findNearestAncestor(ScopingNode.class);
        if (enclosingScope == null) // Should never happen since <ActionStmt> only under <Body>
            fail(Messages.ExtractLocalVariableRefactoring_VarsOnlyExtractedFromStmtsIn);

        Type exprType = TypeChecker.getTypeOf(selectedExpr);
        if (exprType == Type.TYPE_ERROR)
        {
            status.addWarning(Messages.ExtractLocalVariableRefactoring_ExpressionTypeNotBeAutoDetermined);
            decl = "real :: newName"; //$NON-NLS-1$
        }
        else
        {
            decl = exprType.toString() + " :: newName"; //$NON-NLS-1$
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert decl != null;

        status.addWarning(Messages.ExtractLocalVariableRefactoring_ExtractionMayNotPreserveBehavior);

        // Simple checks -- input validation

        if (this.decl.trim().equals("")) //$NON-NLS-1$
            fail(Messages.ExtractLocalVariableRefactoring_EnterDeclarationForExtractedVar);

        IBodyConstruct decl = parseLiteralStatementNoFail(this.decl);
        if (decl == null || !(decl instanceof ASTTypeDeclarationStmtNode))
            fail(Messages.bind(Messages.ExtractLocalVariableRefactoring_InvalidTypeDeclStmt, this.decl));

        declToInsert = (ASTTypeDeclarationStmtNode)decl;

        if (declToInsert.getEntityDeclList() == null
            || declToInsert.getEntityDeclList().size() != 1)
            fail(Messages.ExtractLocalVariableRefactoring_DeclarationDoesNotDeclareSingleVar);

        name = declToInsert.getEntityDeclList().get(0).getObjectName().getObjectName().getText();

        if (declToInsert.getEntityDeclList().get(0).getInitialization() != null)
            fail(Messages.ExtractLocalVariableRefactoring_DeclarationMustNotContainInitialization);

        // Complex checks -- require program analysis

        checkForConflictingBindings(pm, status);
    }


    private void checkForConflictingBindings(IProgressMonitor pm, RefactoringStatus status)
    {
        Definition def = arbitraryDefinitionInScope();
        if (def == null) return; // No declarations in scope, so the new one can't conflict

        checkForConflictingBindings(pm,
            new ConflictingBindingErrorHandler(status),
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

            String msg = Messages.bind(Messages.ExtractLocalVariableRefactoring_NameConflictsWith, conflict.name, vpg.getDefinitionFor(conflict.tokenRef));
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addError(msg, context);
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = Messages.bind(Messages.ExtractLocalVariableRefactoring_NameMightConflictWithSubprogram, conflict.name);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
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
            if (thisStmt instanceof ISpecificationPartConstruct
                    || thisStmt instanceof ISpecificationStmt)
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
        IExpr expr = (IExpr)selectedExpr.clone();
        expr.findFirstToken().setWhiteBefore(""); //$NON-NLS-1$
        
        ASTAssignmentStmtNode assignmentStmt = (ASTAssignmentStmtNode)parseLiteralStatement(name + " = " + expr); //$NON-NLS-1$
        enclosingStmtList.insertBefore(enclosingStmt, assignmentStmt);
        Reindenter.reindent(assignmentStmt, astOfFileInEditor);
    }

    private void replaceExpression()
    {
        IExpr variable = parseLiteralExpression(name.trim());
        variable.findFirstToken().setWhiteBefore(selectedExpr.findFirstToken().getWhiteBefore());
        selectedExpr.replaceWith(variable);
    }
}
