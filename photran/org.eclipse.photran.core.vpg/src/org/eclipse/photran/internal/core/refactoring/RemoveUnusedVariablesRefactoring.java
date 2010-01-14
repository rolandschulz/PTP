/*******************************************************************************
 * Copyright (c) 2009 UFSM - Universidade Federal de Santa Maria (www.ufsm.br).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTObjectNameNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.MultipleFileFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;

/**
 * Remove Unused Variables: refactoring that removes unused variables in Fortran code,
 * making it more readable, and could generate gains of performance, depending on the
 * compiler used.
 * 
 * @author Gustavo Rissetti
 * @author Timofey Yuvashev
 **/
public class RemoveUnusedVariablesRefactoring extends MultipleFileFortranRefactoring{

    @Override
    public String getName()
    {
        return "Remove Unused Local Variables";
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
        // This refactoring has the prerequisite that the code is Implicit None.
        // You must use the Introduce Implicit None refactoring first.
        try{
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if(ast == null)
                {
                    status.addError("One of the selected files (" + file.getName() +") cannot be parsed.");
                }
                List<ScopingNode> scopes = ast.getRoot().getAllContainedScopes();
                for(ScopingNode scope : scopes)
                {
                    if (!(scope instanceof ASTExecutableProgramNode))
                    {
                        if(!scope.isImplicitNone())
                        {
                            fail("All of the selected files must be 'Implict None'! Please use the 'Introduce Implict None Refactoring' first to introduce the 'Implict None' statements in file "+file.getName()+"!");
                        }
                    }
                }
                vpg.releaseAST(file);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure{
        try{
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if(ast == null)
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

    private ASTTypeDeclarationStmtNode getTypeDeclarationStmtNode(IASTNode node)
    {
        if(node == null)
        {
            return null;
        }        
        if(node instanceof ASTTypeDeclarationStmtNode)
        {
            return (ASTTypeDeclarationStmtNode)node;
        }
        return getTypeDeclarationStmtNode(node.getParent()); 
    }

    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {        
        boolean hasChanged = false;        
        List<ScopingNode> scopes = ast.getRoot().getAllContainedScopes();
        for(ScopingNode scope : scopes)
        {
            assert debug("Scope: " + scope.getClass().getName());
            List<Definition> definitions = scope.getAllDefinitions();
            for(Definition def : definitions)
            {
                if(def.isLocalVariable())
                {                    
                    Set<PhotranTokenRef> references = def.findAllReferences(true);
                    // If the variable has not been referenced throughout the source code,
                    // then it was never used, and should be removed.
                    if(references.isEmpty())
                    {
                        hasChanged = true;
                        assert debug("The variable [" + def.getDeclaredName() + "] was not used and will be removed.");
                        ASTTypeDeclarationStmtNode declarationNode = getTypeDeclarationStmtNode(def.getTokenRef().findToken().getParent());
                        if(declarationNode.getEntityDeclList().size() == 1)
                        {
                            declarationNode.replaceWith("\n");
                        }
                        else
                        {
                            IASTListNode<ASTEntityDeclNode> statementsInNode = declarationNode.getEntityDeclList();                            
                            for(ASTEntityDeclNode statement : statementsInNode)
                            {
                                ASTObjectNameNode objectName = statement.getObjectName();
                                String statementName = objectName.getObjectName().getText(); 
                                if(statementName.equals(def.getDeclaredName()))
                                {
                                    if(!statementsInNode.remove(statement))
                                    {
                                        fail("Sorry, could not complete the operation.");                                        
                                    }                                    
                                    break;
                                }
                            } 
                            //Add a whitespace so that variable names and keywords don't clump together
                            //i.e. "integer x,y" doesn't become "integerx,y"
                            statementsInNode.findFirstToken().setWhiteBefore(" ");
                            declarationNode.setEntityDeclList(statementsInNode);                            
                        }
                    }
                }
            }
        }                        

        if(hasChanged)
        {
            addChangeFromModifiedAST(file, pm);
            status.addInfo("After clicking 'Continue', do the same refactoring again to make sure that all unused variables are removed from file " + file.getName()+"!");
        }
        else
        {
            status.addInfo("All unused variables have been removed from file " + file.getName()+"!");
        }
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        // The change is made in method makeChangesTo(...).
    }
    
    private boolean debug(String msg)
    {
        System.out.println(msg);
        return true;
    }
}
