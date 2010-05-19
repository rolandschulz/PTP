/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAccessSpecNode;
import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecSeqNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTGenericNameNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicStmtNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTObjectNameNode;
import org.eclipse.photran.internal.core.parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;

/**
 *
 * @author Kurt Hendle
 */
public class MakePrivateEntityPublicRefactoring extends FortranEditorRefactoring
{
    //used by all forms
    private int numPrivateEnt = 0;
    private String identName = null;
    private ASTAccessSpecNode accessNodeSpec = null;
    //simple access statement
    private ASTAccessStmtNode accessNode = null;
    private ASTGenericNameNode identifierNode = null;
    //access statement in declaration attributes
    private String selectedVarType;
    private String varSpecAttrs = "";
    private ASTTypeDeclarationStmtNode declarationStmtNode = null;
    private ASTEntityDeclNode entDeclNode = null;
    private ASTObjectNameNode identNameNode = null;
    //subroutine inside module with private statement by itself
    private ASTSubroutineSubprogramNode subroutineNode = null;
    //function inside module with private statement by itself
    private ASTFunctionSubprogramNode functionNode = null;
    //private statement by itself
    private ASTAccessStmtNode lonePrivateNode = null;
    private boolean entireProgramPriv = false;

    @Override
    public String getName()
    {
        return "Make Private Entity Public";
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        Token token = findEnclosingToken();

        checkForUnsupportedType(token);

        identName = this.selectedRegionInEditor.getText();
        if(identName.equals(""))
            fail("Please select a private entity name.");

        //see if any of the supported type of nodes exist and get their info
        accessNode = token.findNearestAncestor(ASTAccessStmtNode.class);
        if(accessNode != null)
        {
            accessNodeSpec = accessNode.getAccessSpec();
            identifierNode = token.findNearestAncestor(ASTGenericNameNode.class);
            numPrivateEnt = accessNode.getAccessIdList().size();
        }
        else
            readDeclarationStmtNode(token);

        if(declarationStmtNode == null && accessNode == null)
            checkForSubroutineStmtNode(token);

        if(subroutineNode == null && accessNode == null)
            checkForFunctionStmtNode(token);

        if(accessNodeSpec == null)
            fail("No private entities selected.");

        if(accessNodeSpec.isPublic())
            fail("Public entity is selected. Please select a Private entity.");
    }

    //modified from RenameRefactoring.java
    private Token findEnclosingToken() throws PreconditionFailure
    {
        Token selectedToken = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selectedToken == null)
            fail("Please select a private entity name (highlight the entity name)");
        return selectedToken;
    }

    //checks for intrinsics, externals, and interfaces - unsupported types
    private void checkForUnsupportedType(Token token) throws PreconditionFailure
    {
      //check for an interface, intrinsic or external statement
        ASTIntrinsicStmtNode intrinsic = token.findNearestAncestor(ASTIntrinsicStmtNode.class);
        ASTExternalStmtNode external = token.findNearestAncestor(ASTExternalStmtNode.class);
        ASTInterfaceBlockNode interfaceNode = token.findNearestAncestor(ASTInterfaceBlockNode.class);

        if(intrinsic != null)
            fail("Refactoring does not support Intrinsic entities.");
        else if(external != null)
            fail("Refactoring does not support External entities.");
        else if(interfaceNode != null)
            fail("Refactoring does not support Interface declarations.");
    }

    //parses information from a declaration statement
    private void readDeclarationStmtNode(Token token) throws PreconditionFailure
    {
        selectedVarType = vpg.getDefinitionFor(token.getTokenRef()).getType().toString();

        declarationStmtNode = token.findNearestAncestor(ASTTypeDeclarationStmtNode.class);
        entDeclNode = token.findNearestAncestor(ASTEntityDeclNode.class);

        if(declarationStmtNode == null || entDeclNode == null)
            return;//fail("Could not find private entity declaration.");

        if(declarationStmtNode.getAttrSpecSeq() != null)
        {
            for(ASTAttrSpecSeqNode attrNode : declarationStmtNode.getAttrSpecSeq())
            {
                ASTAttrSpecNode specNode = attrNode.getAttrSpec();
                if(!specNode.toString().trim().equals("private"))
                {
                    varSpecAttrs += ",";
                    varSpecAttrs += specNode.toString();
                }

                if(specNode.getAccessSpec() != null)
                    accessNodeSpec = specNode.getAccessSpec();
            }
        }

        if(accessNodeSpec == null)
            checkIfEntireProgramPriv(token); //see if a PRIVATE statement exists alone

        identNameNode = token.findNearestAncestor(ASTObjectNameNode.class);
        if(accessNodeSpec != null || entireProgramPriv)
            numPrivateEnt = declarationStmtNode.getEntityDeclList().size();
    }

    private void checkForSubroutineStmtNode(Token token) throws PreconditionFailure
    {
        subroutineNode = token.findNearestAncestor(ASTSubroutineSubprogramNode.class);

        if(subroutineNode != null)
        {
            checkIfEntireProgramPriv(token);
            numPrivateEnt = 1;
        }
    }

    private void checkForFunctionStmtNode(Token token)
    {
        functionNode = token.findNearestAncestor(ASTFunctionSubprogramNode.class);

        if(functionNode != null)
        {
            checkIfEntireProgramPriv(token);
            numPrivateEnt = 1;
        }
    }

    @SuppressWarnings("unchecked")
    private void checkIfEntireProgramPriv(Token token)
    {
        ASTListNode progBody = null;
        //set which body to check
        if(declarationStmtNode != null)
            progBody = (ASTListNode)declarationStmtNode.getParent();
        else if(subroutineNode != null)
        {   //need to distinguish between module and main program node containing subroutine
            if(token.findNearestAncestor(ASTMainProgramNode.class) != null)
                progBody = (ASTListNode) ((ASTMainProgramNode)subroutineNode.getParent().getParent()).getBody();
            else
                progBody = (ASTListNode)subroutineNode.getParent();
        }
        else if(functionNode != null)
        {   //also need to distinguish between module/main program
            if(token.findNearestAncestor(ASTMainProgramNode.class) != null)
                progBody = (ASTListNode) ((ASTMainProgramNode)functionNode.getParent().getParent()).getBody();
            else
                progBody = (ASTListNode)functionNode.getParent();
        }

        for(int i=0; i<progBody.size(); i++)
        {
            if(progBody.get(i) instanceof ASTAccessStmtNode)
            {
                ASTAccessStmtNode node = (ASTAccessStmtNode)progBody.get(i);
                if(node.getAccessIdList() == null && node.getAccessSpec().isPrivate())
                {
                    accessNodeSpec = node.getAccessSpec();
                    lonePrivateNode = node;
                    entireProgramPriv = true;
                }

            }
        }
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        IFortranAST ast = vpg.acquirePermanentAST(fileInEditor);
        if(ast == null) return;

        if(numPrivateEnt == 1)
            changePrivateToPublic(ast);
        else if(numPrivateEnt > 1)
        {
            createPublicNode(ast);
            removeIdentifierFromPrivateList();
        }

        addChangeFromModifiedAST(fileInEditor, pm);
        vpg.releaseAST(fileInEditor);
    }

    private void changePrivateToPublic(IFortranAST ast)
    {
        if(entireProgramPriv)
        {  //whole program/subprogram is private so need to change the node to declare public
            handleDeclarationSubroutineOrFunction(ast);
        }
        else
        {   //simply switch private to public
            Token newToken = new Token(Terminal.T_PUBLIC, "public");
            accessNodeSpec.setIsPrivate(null);
            accessNodeSpec.setIsPublic(newToken);

            if(accessNode != null)
                Reindenter.reindent(accessNode, ast);
            else
                Reindenter.reindent(declarationStmtNode, ast);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleDeclarationSubroutineOrFunction(IFortranAST ast)
    {
        if(declarationStmtNode != null)
        {
            ASTTypeDeclarationStmtNode newStmtNode =
                (ASTTypeDeclarationStmtNode) parseLiteralStatement(selectedVarType + ", public" +
                    varSpecAttrs + " :: " + identNameNode.getObjectName().getText() +
                    System.getProperty("line.separator"));

            ASTListNode body = (ASTListNode)declarationStmtNode.getParent();
            body.replaceChild(declarationStmtNode, newStmtNode);
            Reindenter.reindent(newStmtNode, ast);
        }
        else if(subroutineNode != null || functionNode != null)
        {
            ASTAccessStmtNode newStmtNode = (ASTAccessStmtNode)parseLiteralStatement("public " +
                identName + System.getProperty("line.separator"));

            ASTListNode body = (ASTListNode)lonePrivateNode.getParent();
            body.insertAfter(lonePrivateNode, newStmtNode);
            Reindenter.reindent(newStmtNode, ast);
        }
    }

    @SuppressWarnings("unchecked")
    private void createPublicNode(IFortranAST ast)
    {
        if(accessNode != null)
        {
            //make a new public ASTAccessStmtNode
            ASTAccessStmtNode newStmtNode = (ASTAccessStmtNode)parseLiteralStatement("public " +
                identifierNode.getGenericName().getText()+System.getProperty("line.separator"));

            //insert the public node into the program body in AST
            ASTListNode body = (ASTListNode)accessNode.getParent();
            body.insertAfter(accessNode, newStmtNode);
            Reindenter.reindent(newStmtNode, ast);
        }
        else
        {
            ASTTypeDeclarationStmtNode newStmtNode =
                (ASTTypeDeclarationStmtNode) parseLiteralStatement(selectedVarType + ", public" +
                    varSpecAttrs + " :: " + identNameNode.getObjectName().getText() +
                    System.getProperty("line.separator"));

            ASTListNode body = (ASTListNode)declarationStmtNode.getParent();
            body.insertAfter(declarationStmtNode, newStmtNode);
            Reindenter.reindent(newStmtNode, ast);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeIdentifierFromPrivateList()
    {
        if(accessNode != null)
        {
            ASTSeparatedListNode list = (ASTSeparatedListNode)accessNode.getAccessIdList();
            list.remove(identifierNode); //remove old entry
        }
        else
        {
            ASTSeparatedListNode list = (ASTSeparatedListNode)declarationStmtNode.getEntityDeclList();
            list.remove(entDeclNode);
        }
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        //nothing to do here
    }
}
