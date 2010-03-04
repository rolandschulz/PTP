/*******************************************************************************
 * Copyright (c) 2009 UFSM - Universidade Federal de Santa Maria (www.ufsm.br).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.MultipleFileFortranRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;

/**
 * Standardize Statements: refactoring that rewrites variables declarations,
 * transforming them into simple statements, always containing the two points (::)
 * that are characteristic of Fortran, making the code more readable.
 * 
 * @author Gustavo Rissetti
 * @author Timofey Yuvashev
 * @author Jeff Overbey
 **/
public class StandardizeStatementsRefactoring extends MultipleFileFortranRefactoring{

    @Override
    public String getName()
    {
        return "Standardize Statements";
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if (ast == null)
                {
                    status.addError("One of the selected files (" + file.getName() +") cannot be parsed.");
                }
                makeChangesTo(file, ast, status, pm);
                vpg.releaseAST(file);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {            
        List<ScopingNode> scopes = ast.getRoot().getAllContainedScopes();
        for (ScopingNode scope : scopes)
            if (!(scope instanceof ASTExecutableProgramNode) && !(scope instanceof ASTDerivedTypeDefNode))
                standardizeStmtsInScope((IASTListNode<IASTNode>)scope.getBody(), ast);

        addChangeFromModifiedAST(file, pm);
    }

    private void standardizeStmtsInScope(IASTListNode<IASTNode> body, IFortranAST ast)
    {
        List<ASTTypeDeclarationStmtNode> typeDeclStmts = createTypeDeclStmtList(body);
        insertNewStmts(typeDeclStmts, body, ast);
        removeOldStmts(typeDeclStmts, body);
    }
    
    /**
     *  @return a list of {@link ASTTypeDeclarationStmtNode}s where the nodes at odd-numbered
     *  indices are old statements to remove and those at even-numbered indices are the new,
     *  standardized statements to insert
     */
    private List<ASTTypeDeclarationStmtNode> createTypeDeclStmtList(IASTListNode<IASTNode> body)
    {
        List<ASTTypeDeclarationStmtNode> statements = new LinkedList<ASTTypeDeclarationStmtNode>(); 
        
        for (IASTNode node : body)
            if (node instanceof ASTTypeDeclarationStmtNode)
                standardizeTypeDeclStmt((ASTTypeDeclarationStmtNode)node, statements);
        
        return statements;
    }

    private void standardizeTypeDeclStmt(ASTTypeDeclarationStmtNode typeDeclStmt, List<ASTTypeDeclarationStmtNode> statements)
    {
        IASTListNode<ASTEntityDeclNode> variables = typeDeclStmt.getEntityDeclList();
        
        for (int i=0; i<variables.size(); i++)
        {
            ASTTypeDeclarationStmtNode newStmt = createNewVariableDeclaration(typeDeclStmt, i);
            
            // Add a reference to the old statement (this will have an even-numbered index in the list)
            statements.add((ASTTypeDeclarationStmtNode)typeDeclStmt);
            
            // Then add the new declaration (this will have an odd-numbered index in the list)
            statements.add(newStmt);
        }
    }

    @SuppressWarnings("unchecked")
    private ASTTypeDeclarationStmtNode createNewVariableDeclaration(ASTTypeDeclarationStmtNode typeDeclStmt, int i)
    {
        IASTListNode<ASTEntityDeclNode> variables = typeDeclStmt.getEntityDeclList();
        
        ASTTypeDeclarationStmtNode newStmt = (ASTTypeDeclarationStmtNode)typeDeclStmt.clone();
        if (i>0) newStmt.setTypeSpec(createTypeSpecNodeFrom(typeDeclStmt));

        IASTListNode<ASTEntityDeclNode> newVariable =  (IASTListNode<ASTEntityDeclNode>)variables.clone();
        List<ASTEntityDeclNode> listOfVariablesToRemove = new LinkedList<ASTEntityDeclNode>();
        for (int j=0; j<variables.size(); j++)
            if (j != i)
                listOfVariablesToRemove.add(newVariable.get(j));
        newVariable.removeAll(listOfVariablesToRemove);
        newStmt.setEntityDeclList(newVariable);
        
        // Insert "::" if the original statement does not contain that already
        String source = addTwoColons(newStmt);
                                      
        newStmt = (ASTTypeDeclarationStmtNode)parseLiteralStatement(source);
        return newStmt;
    }

    private ASTTypeSpecNode createTypeSpecNodeFrom(ASTTypeDeclarationStmtNode typeDeclStmt)
    {
        ASTTypeSpecNode typeNode = new ASTTypeSpecNode();
        String type = typeDeclStmt.getTypeSpec().toString().trim();
        String[] typeWithoutComments = type.split("\n");
        type = typeWithoutComments[typeWithoutComments.length - 1].trim();
        typeNode.setIsInteger(new Token(Terminal.T_INTEGER, type));
        return typeNode;
    }

    private String addTwoColons(ASTTypeDeclarationStmtNode newStmt)
    {
        String source = SourcePrinter.getSourceCodeFromASTNode(newStmt);
        int position_type = newStmt.getTypeSpec().toString().length();
        String twoPoints = "";
        String source_1 = source.substring(0, position_type);
        String source_2 = source.substring(position_type,source.length());
        if (!containsColonColon(source_2)) 
        {
            twoPoints = " :: ";
        }
        // New statement, with the two points (::).
        source = source_1+twoPoints+source_2.trim();
        return source;
    }
    
    /** @return true iff <code>s</code> contains :: outside a comment */
    private boolean containsColonColon(String s)
    {        
        for (int i=0; i<s.length()-1; i++)
        {
            char p1 = s.charAt(i);
            char p2 = s.charAt(i+1);
            if (p1 == '!' || p2 == '!')
            {
                return false;
            }
            else if (p1 == ':' && p2 ==':')
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Insert the new, standardized statements in the AST.
     * <p>
     * These have odd-numbered indices in the list (see {@link #createTypeDeclStmtList(IASTListNode)})
     */
    private void insertNewStmts(List<ASTTypeDeclarationStmtNode> typeDeclStmts,
                                IASTListNode<IASTNode> body,
                                IFortranAST ast)
    {
        for (int i = 0; i<typeDeclStmts.size(); i+=2)
        {
            body.insertBefore(typeDeclStmts.get(i), typeDeclStmts.get(i+1));
            Reindenter.reindent(typeDeclStmts.get(i+1), ast);
        }
    }

    /**
     * Removes the old statements from the AST.
     * <p>
     * These have even-numbered indices in the list (see {@link #createTypeDeclStmtList(IASTListNode)})
     */
    private void removeOldStmts(List<ASTTypeDeclarationStmtNode> typeDeclStmts,
                                IASTListNode<IASTNode> body)
    {
        for (int i = 0; i<typeDeclStmts.size(); i+=2)
        {
            ASTTypeDeclarationStmtNode delete = typeDeclStmts.get(i);
            if (body.contains(delete))
            {
                delete.removeFromTree();
            }
        }
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        // The change is made in method makeChangesTo(...).
    }
}
