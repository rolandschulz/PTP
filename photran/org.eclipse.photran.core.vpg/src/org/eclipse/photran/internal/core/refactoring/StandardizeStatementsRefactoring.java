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
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try{
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

    private boolean points(String s)
    {        
        for(int i=0; i<s.length()-1; i++)
        {
            char p1 = s.charAt(i);
            char p2 = s.charAt(i+1);
            if(p1 == '!' || p2 == '!')
            {
                return false;
            }
            else if(p1 == ':' && p2 ==':')
            {
                return true;
            }
        }
        return false;
    }

    private void setNodeType(IASTNode node, ASTTypeSpecNode type_node)
    {
        String type = ((ASTTypeDeclarationStmtNode)node).getTypeSpec().toString().trim();
        String[] typeWithoutComments = type.split("\n");
        type = typeWithoutComments[typeWithoutComments.length - 1].trim();
        Token text_type = new Token(null, type);
        type_node.setIsInteger(text_type);
    }
    
    private String addTwoColons(ASTTypeDeclarationStmtNode new_statement)
    {
        String source = SourcePrinter.getSourceCodeFromASTNode(new_statement);
        int position_type = new_statement.getTypeSpec().toString().length();
        String twoPoints = "";
        String source_1 = source.substring(0, position_type);
        String source_2 = source.substring(position_type,source.length());
        if (!points(source_2)) 
        {
            twoPoints = " ::";
        }
        // New statement, with the two points (::).
        source = source_1+twoPoints+source_2;
        return source;
    }
    
    private void populateStatementsList(IASTListNode<IASTNode> body, List<ASTTypeDeclarationStmtNode> statements)
    {
        for(IASTNode node : body)
        {
            if(node instanceof ASTTypeDeclarationStmtNode)
            {
                IASTListNode<ASTEntityDeclNode> variables = ((ASTTypeDeclarationStmtNode)node).getEntityDeclList();
                ASTTypeSpecNode type_node = new ASTTypeSpecNode(); 
                
                // Get statement type.
                setNodeType(node, type_node);
                
                for(int i=0; i<variables.size(); i++)
                {
                    ASTTypeDeclarationStmtNode new_statement = (ASTTypeDeclarationStmtNode)node.clone();
                    if(i>0)
                    {
                        new_statement.setTypeSpec(type_node);
                    }                                
                    IASTListNode<ASTEntityDeclNode> new_variable =  (IASTListNode<ASTEntityDeclNode>)variables.clone();
                    List<ASTEntityDeclNode> list_variables_to_remove = new LinkedList<ASTEntityDeclNode>();
                    for(int j=0; j<variables.size(); j++)
                    {
                        if(j != i)
                        {                                        
                            list_variables_to_remove.add(new_variable.get(j));
                        }
                    }
                    new_variable.removeAll(list_variables_to_remove);                                                               
                    new_statement.setEntityDeclList(new_variable);
                    
                    // Put the two points (::) if the original statement does not.
                    String source = addTwoColons(new_statement);
                                                  
                    new_statement = (ASTTypeDeclarationStmtNode)parseLiteralStatement(source);
                    // Adds a reference to the old statement.
                    statements.add((ASTTypeDeclarationStmtNode)node);
                    // Adds a new declaration.
                    statements.add(new_statement);
                }
            }
        }
    }
    
    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {            
        List<ScopingNode> scopes = ast.getRoot().getAllContainedScopes();
        for(ScopingNode scope : scopes)
        {                
            if (!(scope instanceof ASTExecutableProgramNode) && !(scope instanceof ASTDerivedTypeDefNode))
            {
                IASTListNode<IASTNode> body = (IASTListNode<IASTNode>)scope.getBody();                    
                List<ASTTypeDeclarationStmtNode> statements = new LinkedList<ASTTypeDeclarationStmtNode>(); 
                
                populateStatementsList(body, statements);
                // Insert the new standard statements in the AST.
                for(int i = 0; i<statements.size(); i+=2)
                {
                    body.insertBefore(statements.get(i), statements.get(i+1));
                    Reindenter.reindent(statements.get(i+1), ast);
                }
                // Remove the old statements that were outside the standard.
                for(int i = 0; i<statements.size(); i+=2)
                {
                    ASTTypeDeclarationStmtNode delete = statements.get(i);
                    if(body.contains(delete))
                    {
                        delete.removeFromTree();
                    }
                }
            }
        }                        
        // Adds changes in AST.
        addChangeFromModifiedAST(file, pm);
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        // The change is made in method makeChangesTo(...).
    }
}
