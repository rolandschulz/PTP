/*******************************************************************************
 * Copyright (c) 2009 UFSM - Universidade Federal de Santa Maria (www.ufsm.br).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

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

/**
 * Remove Unused Variables: refactoring that removes unused variables in Fortran code,
 * making it more readable, and could generate gains of performance, depending on the
 * compiler used.
 * 
 * @author Gustavo Rissetti
 * @author Timofey Yuvashev
 * @author Jeff Overbey
 **/
/*
 * TODO - JO - Can we avoid running multiple times?
 * TODO - JO - What about specification stmts?
 */
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
        //removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
        
        ensureAllScopesAreImplicitNone(status);
    }

    /**
     * Checks that all scopes contained in all selected files are IMPLICIT NONE.
     * <p>
     * If they are not, this issues an error, informing the user that this refactoring has
     * the prerequisite that the code is IMPLICIT NONE.
     */
    private void ensureAllScopesAreImplicitNone(RefactoringStatus status)
        throws PreconditionFailure
    {
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if(ast == null)
                {
                    status.addError("One of the selected files (" + file.getName() +") cannot be parsed.");
                }
                else
                {
                    ensureAllScopesAreImplicitNone(file, ast);
                    vpg.releaseAST(file);
                }
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private void ensureAllScopesAreImplicitNone(IFile file, IFortranAST ast)
        throws PreconditionFailure
    {
        for (ScopingNode scope : ast.getRoot().getAllContainedScopes())
            if (!(scope instanceof ASTExecutableProgramNode))
                if (!scope.isImplicitNone())
                    fail("All of the selected files must be IMPLICIT NONE. Please use the "
                        + "Introduce Implict None refactoring first to introduce IMPLICIT "
                        + "NONE statements in the file "+file.getName()+".");
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure{
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if(ast == null)
                {
                    status.addError("One of the selected files (" + file.getName() +") cannot be parsed.");
                }
                else
                {
                    makeChangesTo(file, ast, status, pm);
                    vpg.releaseAST(file);
                }
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }    
    }

    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        boolean hasChanged = false;

        for (ScopingNode scope : ast.getRoot().getAllContainedScopes())
            if (removedUnusedVariablesFromScope(scope))
                hasChanged = true;

        if (hasChanged)
        {
            addChangeFromModifiedAST(file, pm);
            status.addInfo("After clicking 'Continue', do the same refactoring again to make sure that all unused variables are removed from file " + file.getName()+"!");
            status.addWarning("This refactoring does not remove unused variables when their dimentions are specified on another line. I.e. real a /n/n dimention a(10) will not be removed.");
        }
        else
        {
            status.addInfo("All unused variables have been removed from file " + file.getName()+"!");
        }
    }

    private boolean removedUnusedVariablesFromScope(ScopingNode scope)
        throws PreconditionFailure
    {
        assert debug("Scope: " + scope.getClass().getName());
        
        boolean hasChanged = false;
        
        for (Definition def : scope.getAllDefinitions())
        {
            if (def.isLocalVariable() && def.findAllReferences(true).isEmpty())
            {
                removeVariableDeclFor(def);
                hasChanged = true;
            }
        }

        return hasChanged;
    }

    private void removeVariableDeclFor(Definition def) throws PreconditionFailure
    {
        assert debug("The variable [" + def.getDeclaredName() + "] was not used and will be removed.");
        
        ASTTypeDeclarationStmtNode declarationNode = getTypeDeclarationStmtNode(def.getTokenRef().findToken().getParent());
        
        IASTListNode<ASTEntityDeclNode> entityDeclList = declarationNode.getEntityDeclList();
        if (entityDeclList.size() == 1)
        {
            declarationNode.replaceWith("\n");
        }
        else
        {
            removeVariableDeclFromList(def, entityDeclList);
            //declarationNode.setEntityDeclList(entityDeclList); // JO -- redundant
        }
    }

    private void removeVariableDeclFromList(Definition def,
                                            IASTListNode<ASTEntityDeclNode> entityDeclList)
        throws PreconditionFailure
    {
        for (ASTEntityDeclNode decl : entityDeclList)
        {
            // TODO - JO - Can we use pointer comparison rather than text comparison?
            ASTObjectNameNode objectName = decl.getObjectName();
            String declName = objectName.getObjectName().getText(); 
            if (declName.equals(def.getDeclaredName()))
            {
                if (!entityDeclList.remove(decl))
                {
                    fail("Sorry, could not complete the operation.");
                }                                    
                break;
            }
        } 
        
        //Add a whitespace so that variable names and keywords don't clump together
        //i.e. "integer x,y" doesn't become "integerx,y"
        entityDeclList.findFirstToken().setWhiteBefore(" ");
    }

    private ASTTypeDeclarationStmtNode getTypeDeclarationStmtNode(IASTNode node)
    {
        if (node == null)
            return null;
        else if (node instanceof ASTTypeDeclarationStmtNode)
            return (ASTTypeDeclarationStmtNode)node;
        else
            return getTypeDeclarationStmtNode(node.getParent());
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
