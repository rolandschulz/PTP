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

import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranRefactoring;

/**
 * Refactoring to extract an expression into a local (temporary) variable.
 * 
 * INCOMPLETE
 * 
 * @author Jeff Overbey
 */
public class ExtractLocalVariableRefactoring //extends FortranRefactoring
{
//    ///////////////////////////////////////////////////////////////////////////
//    // Fields
//    ///////////////////////////////////////////////////////////////////////////
//    
//    private String name = null, type = null;
//    private InteriorNode subexpression;
//    private ASTExpressionNode enclosingExpr;
//    private IActionStmt enclosingAction;
//    private ASTBodyNode enclosingBody;
//    //private Type subexprType;
//
//    public ExtractLocalVariableRefactoring(IFile file, ITextSelection selection)
//    {
//        super(file, selection);
//    }
//    
//    @Override
//    public String getName()
//    {
//        return "Extract Local Variable";
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // User-Specified Parameters
//    ///////////////////////////////////////////////////////////////////////////
//
//    public void setName(String name)
//    {
//        assert name != null;
//        
//        this.name = name;
//    }
//
//    public void setType(String type)
//    {
//        assert type != null;
//        
//        this.type = type;
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Initial Preconditions
//    ///////////////////////////////////////////////////////////////////////////
//    
//    @Override
//    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
//    {
//        subexpression = this.findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor);
//        enclosingExpr = (ASTExpressionNode)this.findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor, Nonterminal.EXPR, false);
//        enclosingAction = (ASTActionStmtNode)this.findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor, Nonterminal.ACTION_STMT, false);
//        enclosingBody = (ASTBodyNode)this.findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor, Nonterminal.BODY, false);
//        
//        if (subexpression == null) fail("Please select an expression to extract.");
//        if (!nodeExactlyEnclosesRegion(subexpression, this.astOfFileInEditor, this.selectedRegionInEditor)) fail("Please select a complete expression to extract.");
//        if (enclosingExpr == null) fail("The selected text is not part of an expression.  Please select part of an expression to extract.");
//        if (enclosingAction == null) fail("Variables can only be extracted from action statements (e.g., assignments, print statements, etc.).");
//        if (enclosingBody == null) fail("Variables can only be extracted from action statements inside functions, subroutines, and main programs."); // Should never happen since <ActionStmt> only under <Body>
//        
//        //subexprType = ensureNodeTypeChecks(subexpression);
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Final Preconditions
//    ///////////////////////////////////////////////////////////////////////////
//    
//    @Override
//    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
//    {
//        assert name != null;
//        assert type != null;
//        
//        String error = "A variable cannot be declared with the given type \"" + type + "\".";
//        
//        if (type.contains(";") || type.contains("!")) fail(error);
//        if (parseDeclaration(type, "this_is_an_arbitrary_variable_name") == null) fail(error);
//
//        // TODO: ensureIsValidIdentifier(name);
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Change
//    ///////////////////////////////////////////////////////////////////////////
//    
//    @Override
//    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
//    {
//        assert name != null;
//        assert type != null;
//        
//        insertDeclaration();
//        insertAssignment();
//        replaceExpression();
//        
//        this.addChangeFromModifiedAST(this.fileInEditor, pm);
//    }
//
//    private ASTBodyConstructNode parseDeclaration(String type, String name)
//    {
//        return parseLiteralStatement(type + " :: " + name);
//    }
//
//    private void insertDeclaration()
//    {
//        ASTBodyConstructNode insertAfter = findDeclarationInsertionPoint();
//        ASTBodyConstructNode decl = parseDeclaration(type, name);
//        if (insertAfter != null)
//            SourceEditor.pasteAsLastChild(decl, insertAfter, this.astOfFileInEditor, true);
//        else
//            SourceEditor.pasteAsFirstChild(decl, enclosingBody, this.astOfFileInEditor, true);
//    }
//
//    private ASTBodyConstructNode findDeclarationInsertionPoint()
//    {
//        ASTBodyConstructNode lastSpecStmt = null;
//        
//        for (int i = 0; i < enclosingBody.size(); i++)
//        {
//            ASTBodyConstructNode thisStmt = enclosingBody.getBodyConstruct(i);
//            if (thisStmt.getSpecificationPartConstruct() != null)
//                lastSpecStmt = thisStmt;
//        }
//        
//        return lastSpecStmt;
//    }
//
//    private void insertAssignment()
//    {
//        ASTBodyConstructNode assignment = parseLiteralStatement(name + " = " + SourcePrinter.getSourceCodeFromASTNode(subexpression));
//        SourceEditor.pasteAsFirstChild(assignment, enclosingAction, this.astOfFileInEditor, true);
//    }
//
//    private void replaceExpression()
//    {
//        ASTExprNode variable = parseLiteralExpression(name);
//        SourceEditor.replace(subexpression, variable);
//    }
}
