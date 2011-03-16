/*******************************************************************************
 * Copyright (c) 2010 Stephen Downs, Robert Samblanet, Kevin Schilling, Jon 
 * Woolwine, and Chad Zamzow
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Stephen Downs, Robert Samblanet, Kevin Schilling, 
 *      Jon Woolwine, and Chad Zamzow
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.core.internal.resources.SavedState;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecSeqNode;
import org.eclipse.photran.internal.core.parser.ASTDatalistNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTSaveStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSavedEntityNode;
import org.eclipse.photran.internal.core.parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVariableNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.core.reindenter.Reindenter;

/**
 * Makes all implicitly saved variables explicitly saved.
 * 
 * @author Stephen Downs
 * @author Robert Samblanet
 * @author Kevin Schilling
 * @author Jon Woolwine
 * @author Chad Zamzow
 */
@SuppressWarnings("all")
public class MakeSaveExplicitRefactoring extends FortranResourceRefactoring
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public String getName()
    {
        return Messages.MakeSaveExplicitRefactoring_Name;
    }
    
    private IFortranAST currAST = null;

    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @see MoveSavedToCommonBlockRefactoring
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
    throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure{
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if(ast == null)
                {
                    status.addError(Messages.bind(Messages.MakeSaveExplicitRefactoring_SelectedFileCannotBeParsed, file.getName()));
                }
                else
                {
                    currAST = ast;
                    makeChangesTo(file, status, pm);
                    vpg.releaseAST(file);
                }
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }    
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {

    }
    
    /**
     * Given an AST, makes the refactoring changes by calling the makeAllSaveAttributesExplicit function 
     * @param file
     * @param status
     * @param pm
     * @throws PreconditionFailure
     */
    private void makeChangesTo(IFile file, RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        for (ScopingNode scope : currAST.getRoot().getAllContainedScopes())
        {
            SavedVariableVisitor savedVariableVisitor = new SavedVariableVisitor();
            scope.accept(savedVariableVisitor);
            if(!savedVariableVisitor.hasGlobalSaveStmt() && !scope.isMainProgram())
            {
                HashSet<String> explicitlySavedVariables = savedVariableVisitor.getExplicitlySavedVariables();
                TreeSet<String> dataVariables = savedVariableVisitor.getDataBlockVariables();
                makeAllSaveAttributesExplicit(scope, explicitlySavedVariables, dataVariables);
            }
        }
        addChangeFromModifiedAST(file, pm);
    }
    
    /**
     * Makes all implicit saves explicit in the given scope
     * @param scope
     * @param explicitlySavedVariables
     * @param dataEntities
     * @throws PreconditionFailure
     */
    private void makeAllSaveAttributesExplicit(ScopingNode scope, HashSet<String> explicitlySavedVariables, TreeSet<String> dataEntities) 
    throws PreconditionFailure
    {
        if(scope.getBody() == null)
        {
            return;
        }
        
        for (IASTNode node : scope.getBody().getChildren())
        {
            if (node instanceof ASTTypeDeclarationStmtNode)
            {
                ASTTypeDeclarationStmtNode declarationNode = (ASTTypeDeclarationStmtNode)node;
                makeImplicitlySavedVariablesExplicitlySaved(scope, declarationNode, dataEntities, explicitlySavedVariables);
            }
        }
        
        //add all implicitly saved data block variables to a save statement
        for(String variable : dataEntities)
        {
            if(!explicitlySavedVariables.contains(variable.toLowerCase()))
            {
                addVariableToSaveStmt(scope, variable);
                explicitlySavedVariables.add(variable.toLowerCase());
            }
        }
    }

    /**
     * Helper function that adds SAVE to a type declaration
     * @param scope
     * @param typeDeclaration
     * @param dataEntities
     * @param savedEntities
     */
    private void makeImplicitlySavedVariablesExplicitlySaved(ScopingNode scope, ASTTypeDeclarationStmtNode typeDeclaration, TreeSet<String> dataEntities, HashSet<String> savedEntities)
    {
        IASTListNode<ASTEntityDeclNode> entityDeclList = typeDeclaration.getEntityDeclList();
        boolean declContainsSavedAndUnsavedVariables = containsUnsavedAndSavedVariables(typeDeclaration, dataEntities);
        for(ASTEntityDeclNode variableDeclaration : entityDeclList)
        {
            if (isImplicitlySaved(scope, variableDeclaration, dataEntities) && 
                !savedEntities.contains(declarationVariableName(variableDeclaration).toLowerCase()))
            {
                if(!declContainsSavedAndUnsavedVariables)
                {
                    String declString = SourcePrinter.getSourceCodeFromASTNode(typeDeclaration);
                    ASTAttrSpecSeqNode attrSpecSeqNode = createSaveAttrSpecSeqNode(!declString.contains("::"));
    
                    // if there is no attrSpecSeq, create a new one and add it to the typeDeclaration
                    if( typeDeclaration.getAttrSpecSeq() == null )
                    {
                        IASTListNode<ASTAttrSpecSeqNode> attrSpecSeq = new ASTListNode<ASTAttrSpecSeqNode>( 1 );  
                        typeDeclaration.setAttrSpecSeq(attrSpecSeq);
                    }
    
                    // add Save attribute to attrSpecSeq
                    typeDeclaration.getAttrSpecSeq().add(attrSpecSeqNode);
                    
                    for(ASTEntityDeclNode decl : typeDeclaration.getEntityDeclList())
                    {
                        savedEntities.add(declarationVariableName(decl).toLowerCase());
                    }
                                        
                    return;
                }
                else
                {
                    String variableName = declarationVariableName(variableDeclaration);
                    savedEntities.add(variableName.toLowerCase());
                    addVariableToSaveStmt(scope, variableName);
                }
            }
        }
    }

    /**
     * Helper function that adds the given variable name to a global save statement in the given scope
     * @param scope
     * @param variableName
     */
    private void addVariableToSaveStmt(ScopingNode scope, String variableName)
    {
        ASTSavedEntityNode savedEntity = new ASTSavedEntityNode();
        Token savedEntityToken = new Token(Terminal.T_IDENT, variableName);
        savedEntity.setVariableName(savedEntityToken);
        for (IASTNode node : scope.getBody().getChildren())
        {
            if(node instanceof ASTSaveStmtNode)
            {
                IASTListNode<ASTSavedEntityNode> variableList = ((ASTSaveStmtNode)node).getVariableList();
                ASTSeparatedListNode<ASTSavedEntityNode> astSeparatedListNode = (ASTSeparatedListNode<ASTSavedEntityNode>)variableList;
                astSeparatedListNode.add(new Token(null, ", "), savedEntity);
                return;
            }
        }
        ASTSaveStmtNode newSaveStmt = (ASTSaveStmtNode)parseLiteralStatement("SAVE " + variableName);
        IASTListNode body = scope.getBody();
        body.add(0, newSaveStmt);
        Reindenter.reindent(newSaveStmt, currAST);
    }

    /**
     * Helper function that checks declaration lists to see if it contains both saved and unsaved variables
     * @param typeDeclaration
     * @param dataEntities
     * @return
     */
    private boolean containsUnsavedAndSavedVariables(ASTTypeDeclarationStmtNode typeDeclaration, TreeSet<String> dataEntities)
    {
        if(typeDeclaration.getEntityDeclList() == null) return false;
        boolean containsSaved = false, containsUnSaved = false;
        
        for(ASTEntityDeclNode decl : typeDeclaration.getEntityDeclList())
        {
            if (decl.getInitialization() == null
                && !dataEntities.contains(declarationVariableName(decl).toLowerCase()))
            {
                containsUnSaved = true;
            }
            else
            {
                containsSaved = true;
            }
        }
        return containsSaved && containsUnSaved;
    }

    /**
     * Helper function that extracts the declaration variable name from a declaration node
     * @param decl
     * @return
     */
    private String declarationVariableName(ASTEntityDeclNode decl)
    {
        return decl.getObjectName().getObjectName().getText();
    }

    /**
     * Helper function that checks to see if a given variable is implicitly saved
     * @param scope
     * @param variableDeclaration
     * @param dataEntities
     * @return
     */
    private boolean isImplicitlySaved(ScopingNode scope, ASTEntityDeclNode variableDeclaration, TreeSet<String> dataEntities)
    {
        return (variableDeclaration.getInitialization() != null ||
            dataEntities.contains(declarationVariableName(variableDeclaration).toLowerCase()));
    }

    /**
     * Creates a new AttrSpecSeqNode with SAVE attribute
     * @return new AttrSpecSeqNode with SAVE attribute
     */
    private ASTAttrSpecSeqNode createSaveAttrSpecSeqNode(boolean addDblColon)
    {
        ASTAttrSpecSeqNode attrSpecSeqNode = new ASTAttrSpecSeqNode();
        ASTAttrSpecNode attrSpecNode = new ASTAttrSpecNode();
        
        Token token;
        if(addDblColon)
        {
            token = new Token(null, ", SAVE ::" );
        }
        else 
        {
            token = new Token(null, ", SAVE" );
        }
            
        attrSpecNode.setIsSave( token );
        attrSpecSeqNode.setAttrSpec(attrSpecNode);
        return attrSpecSeqNode;
    }
    
    /**
     * Saved Variable Visitor class
     * 
     * Iterates through all nodes in a scope. While doing this, it checks three different cases:
     * 1) If the node is a save statement, it checks to see if it is a global save statement.
     * If it isn't, it adds all the saved variables to the explicitlySavedVariables list.
     * 2) If the node is a declaration, it checks to see if it has been explicitly saved. If it
     * has, it adds the variable to the explicitlySavedVariables list.
     * 3) If the node is a data statement, it adds all variables in the data block to the 
     * dataBlockVariables list.
     * 
     * @author ShinSheep
     */
    private class SavedVariableVisitor extends ASTVisitor
    {
        private boolean hasGlobalSaveStmt;
        private HashSet<String> explicitlySavedVariables;
        private TreeSet<String> dataBlockVariables;
        private ASTSaveStmtNode saveStmt;
        
        public TreeSet<String> getDataBlockVariables()
        {
            return dataBlockVariables;
        }

        public HashSet<String> getExplicitlySavedVariables()
        {
            return explicitlySavedVariables;
        }

        public boolean hasGlobalSaveStmt()
        {
            return hasGlobalSaveStmt;
        }

        public SavedVariableVisitor()
        {
            super();
            this.hasGlobalSaveStmt = false;
            explicitlySavedVariables = new HashSet<String>();
            dataBlockVariables = new TreeSet<String>();
            saveStmt = null;
        }

        @Override
        public void visitASTSaveStmtNode(ASTSaveStmtNode node)
        {
            this.saveStmt = node;
            if (node.getVariableList() == null)
            {
                hasGlobalSaveStmt = true;
            }
            else
            {
                for(ASTSavedEntityNode variable : node.getVariableList())
                {
                   explicitlySavedVariables.add(variable.getVariableName().getText().toLowerCase());
                }
            }
        }
        
        @Override
        public void visitASTTypeDeclarationStmtNode(ASTTypeDeclarationStmtNode node)
        {
            IASTListNode<ASTAttrSpecSeqNode> attrSpecSeq = node.getAttrSpecSeq();
            if(attrSpecSeq != null)
            {
                for(ASTAttrSpecSeqNode attrSpecSeqNode : attrSpecSeq)
                {
                    ASTAttrSpecNode attrSpecNode = attrSpecSeqNode.getAttrSpec();
                    if(attrSpecNode != null && attrSpecNode.isSave())
                    {
                        for(ASTEntityDeclNode variable : node.getEntityDeclList())
                        {
                            explicitlySavedVariables.add(variable.getObjectName().getObjectName().getText().toLowerCase());
                        }
                    }
                }
            }
        }
        
        @Override
        public void visitASTDataStmtNode(ASTDataStmtNode node)
        {
            IASTListNode<ASTDatalistNode> dataList = ((ASTDataStmtNode)node).getDatalist();
            for (IASTNode dataEntity : dataList.getChildren())
            {
                ASTDatalistNode dataListNode = (ASTDatalistNode)dataEntity;
                for( IASTNode variableNameNode : dataListNode.getDataStmtSet().getDataStmtObjectList().getChildren())
                {
                    if(variableNameNode instanceof ASTVariableNode)
                    {
                        ASTVariableNode variableNode = (ASTVariableNode)variableNameNode;
                        String variableName = variableNode.getDataRef().get(0).getName().getText();

                        dataBlockVariables.add(variableName.toLowerCase());
                    }
                }
            }      
        }
    }
}