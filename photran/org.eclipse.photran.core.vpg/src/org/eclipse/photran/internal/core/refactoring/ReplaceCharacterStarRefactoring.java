/*******************************************************************************
 * Copyright (c) 2010 Mariano Mendez and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mariano Mendez - Initial API and implementation
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
import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTCharSelectorNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTInitializationNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.core.reindenter.Reindenter;

/**
 * This refactoring removes old-style CHARACTER*n declarations, replacing them with
 * new-style CHARACTER(LEN=n) declarations.
 * 
 * @author Mariano Mendez
 */
public class ReplaceCharacterStarRefactoring extends FortranEditorRefactoring
{
    @Override
    public String getName()
    {
        return Messages.ReplaceCharacterToCharacterLenRefactoring_Name;
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
         
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
        
        //iterateThroughAllTypeDeclarationStmtNodes(this.astOfFileInEditor.getRoot());
        CharacterNodesVisitor characterVisitor = new CharacterNodesVisitor();
        this.astOfFileInEditor.accept(characterVisitor);
        
        // if there is not any character * a message is shown 
        if (characterVisitor.getList().size()<1) 
            fail(Messages.ReplaceCharacterToCharacterLenRefactoring_CharacterStarDeclNotSelected); 
    }
    
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final preconditions
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        IFile file = this.fileInEditor;
        IFortranAST ast = vpg.acquirePermanentAST(file);
        List<ScopingNode> scopes = ast.getRoot().getAllContainedScopes();
        for (ScopingNode scope : scopes)
            if (!(scope instanceof ASTExecutableProgramNode) && !(scope instanceof ASTDerivedTypeDefNode))
                removeOldCharacterDecl((IASTListNode<IASTNode>)scope.getBody(),ast);
        
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
        vpg.releaseAST(this.fileInEditor);
    }
    
    private void removeOldCharacterDecl(IASTListNode<IASTNode> body , IFortranAST ast)
    {
        // to removes all character declaration from a scope
        // first creates a list of Character Declaration 
        List<ASTTypeDeclarationStmtNode> typeCharDeclStmts = createCharTypeDeclStmtList(body, ast);
        insertNewStmts(typeCharDeclStmts, body,ast);
        removeOldStmts(typeCharDeclStmts, body);
    }
    
    private List<ASTTypeDeclarationStmtNode> createCharTypeDeclStmtList(IASTListNode<IASTNode> body , IFortranAST ast)
    {
        List<ASTTypeDeclarationStmtNode> statements = new LinkedList<ASTTypeDeclarationStmtNode>();
        CharacterNodesVisitor charVitor= new CharacterNodesVisitor();
        ast.accept(charVitor);
        for ( IASTNode node : body )
        {
            if (node instanceof ASTTypeDeclarationStmtNode  &&   charVitor.getList().contains(node) ) 
                changeOldCharStyleDecl((ASTTypeDeclarationStmtNode)node, statements);
        }
        return statements;
    }
   
    
    private void changeOldCharStyleDecl(ASTTypeDeclarationStmtNode typeDeclStmt, List<ASTTypeDeclarationStmtNode> statements)
    {
        // changes the format of old character Declaration
        IASTListNode<ASTEntityDeclNode> variables = typeDeclStmt.getEntityDeclList();
        
        for (int i=0; i<variables.size(); i++)
        {
            ASTTypeDeclarationStmtNode newStmt = createNewVariableDeclaration(typeDeclStmt, i);
            // Add a reference to the old statement (this will have an even-numbered index in the list)
            statements.add((ASTTypeDeclarationStmtNode)typeDeclStmt);
            // Then add the new declaration (this will have an odd-numbered index in the list)
            // before insert the new statement it must be rewritten to character(Len= )    
            statements.add(newStmt);
        }
    }
    
   
    // Borrowed from Standarize Statements
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
        // replace old Style Character
        newStmt=characterToCharacterLen(newStmt);
        return newStmt;
        
    }
    
    private ASTTypeDeclarationStmtNode characterToCharacterLen ( ASTTypeDeclarationStmtNode Stmt)
    {
        String length=""; //$NON-NLS-1$
        String literalIniDec=""; //$NON-NLS-1$
        ASTTypeSpecNode type=Stmt.getTypeSpec();
        ASTEntityDeclNode declNode=Stmt.getEntityDeclList().get(0);
        if (Stmt.getTypeSpec().getCharSelector()!= null)
        { 
            if (declNode.getCharLength() ==null)  length=type.getCharSelector().getConstIntLength().getText(); // is a: character * 10 aString
            else 
            {
                 // is a: character *10  First, Second*5  <----- is the second case  composed declaration
                length=declNode.getCharLength().getConstIntLength().getText();
                declNode.getCharLength().removeFromTree(); // remove from tree length
                type.getCharSelector().removeFromTree();  // remove from tree character selector 
            }
        }
        else 
        {
            if (declNode.getCharLength() !=null) // is a : character string*10
            {     
                length=declNode.getCharLength().getConstIntLength().getText();
                declNode.getCharLength().removeFromTree();
            }
            else 
            {
                String strType=type.getCharacterToken().getText();
                if (strType.contains("*")) length=strType.substring(strType.indexOf("*")+1);//  is :character* //$NON-NLS-1$ //$NON-NLS-2$
                else length="1"; //$NON-NLS-1$ //  is : character
            }
        }
        String source1= "character(len=" + length + ")" +"::"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$     
        literalIniDec=getLiteralDeclaration(Stmt.getEntityDeclList().get(0).getInitialization());
        String source2 =  getIdentifier(Stmt.getEntityDeclList().get(0));
        String commentsBefore=Stmt.findFirstToken().getWhiteBefore();
        String commentsAfter=Stmt.findLastToken().getWhiteBefore();
        String literalStmt=commentsBefore+source1+source2 + literalIniDec +commentsAfter;
        Stmt=(ASTTypeDeclarationStmtNode)parseLiteralStatement(literalStmt);
        return Stmt;
    }

    private String getIdentifier(ASTEntityDeclNode declNode)
    {
       String varName=declNode.getObjectName().getObjectName().getText();
       ASTArraySpecNode  arraySpec= declNode.getArraySpec();
       
       if (arraySpec!=null) varName= varName +"(" +arraySpec.toString() +")" ;   //$NON-NLS-1$ //$NON-NLS-2$
       return varName;
    }
    
    private String getLiteralDeclaration(ASTInitializationNode initializationNode)
    {
        if (initializationNode!=null)
        {
            // the character is initialized
            String iniStr= initializationNode.getAssignedExpr().toString();
            return  "=" + iniStr ; //$NON-NLS-1$ 
        }
        return ""; //$NON-NLS-1$
    }
    
    // Borrowed from Standarize Statements
    private ASTTypeSpecNode createTypeSpecNodeFrom(ASTTypeDeclarationStmtNode typeDeclStmt)
    {
        ASTTypeSpecNode typeNode = new ASTTypeSpecNode();
        String type = typeDeclStmt.getTypeSpec().toString().trim();
        String[] typeWithoutComments = type.split("\n"); //$NON-NLS-1$
        type = typeWithoutComments[typeWithoutComments.length - 1].trim();
        typeNode.setIsInteger(new Token(Terminal.T_INTEGER, type));
        return typeNode;
    }

    // Borrowed from Standarize Statements
    private String addTwoColons(ASTTypeDeclarationStmtNode newStmt)
    {
        String source = SourcePrinter.getSourceCodeFromASTNode(newStmt);
        int position_type = newStmt.getTypeSpec().toString().length();
        String twoPoints = ""; //$NON-NLS-1$
        String source_1 = source.substring(0, position_type);
        String source_2 = source.substring(position_type,source.length());
        if (!containsColonColon(source_2)) 
        {
            twoPoints = " :: "; //$NON-NLS-1$
        }
        // New statement, with the two points (::).
        source = source_1+twoPoints+source_2.trim();
        return source;
    }
    
     
    // Borrowed from Standarize Statements
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

    // Borrowed from Standarize Statements
    private void insertNewStmts(List<ASTTypeDeclarationStmtNode> typeDeclStmts,
        IASTListNode<IASTNode> body, IFortranAST ast)
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
    // Borrowed from Standarize Statements
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
    
        
    private static final class CharacterNodesVisitor extends ASTVisitor
    {
        private List<ASTTypeDeclarationStmtNode> oldCcharDeclaStmtList= new LinkedList<ASTTypeDeclarationStmtNode>();
      
        @Override 
        public void visitASTTypeDeclarationStmtNode (ASTTypeDeclarationStmtNode node)
        {
            // If the declaration type is a Character
            // is a character* declaration type then I include it in the list
            ASTTypeSpecNode specTypeNode=node.getTypeSpec(); 
            if (specTypeNode!= null && specTypeNode.isCharacter()) 
            {
                ASTCharSelectorNode charSelectorNode = specTypeNode.getCharSelector();
                // Is a character !                  
                if (charSelectorNode!=null)
                {
                    if (isAnOldCharacterDecl(charSelectorNode))
                    {
                        // put the node in the list is a Character *
                        oldCcharDeclaStmtList.add(node);
                    }
                }
                else oldCcharDeclaStmtList.add(node);
            }
        }
        
        public List<ASTTypeDeclarationStmtNode> getList() 
        {
            return this.oldCcharDeclaStmtList;
        } 
        
        private boolean isAnOldCharacterDecl(ASTCharSelectorNode node)
        {
            return  ! node.isAssumedLength() 
                    && ! node.isColon()  
                    && ! (node.getConstIntLength()==null) 
                    && (node.getLengthExpr()== null)
                    && (node.getKindExpr()==null)
                    && (node.getKindExpr2()==null);
        }  
        
    }
    
}
