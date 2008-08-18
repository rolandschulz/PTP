/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockListNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNameNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockObjectNode;
import org.eclipse.photran.internal.core.parser.ASTCommonStmtNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.ISpecificationStmt;
import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranRefactoring;

/**
 * Refactoring to move a COMMON block into a module.
 * 
 * @author Jeff Overbey
 */
public class MoveCommonToModuleRefactoring extends FortranRefactoring
{
    private static final String CRLF = System.getProperty("line.separator");

    private ASTCommonBlockNode commonBlockToMove = null;
    private String nameOfCommonBlockToMove = null;
    private String newModuleName = null;
    private Set<IFile> affectedFiles = null;

    public MoveCommonToModuleRefactoring(IFile file, ITextSelection selection)
    {
        super(file, selection);
    }
    
    @Override
    public String getName()
    {
        return "Move COMMON Block to Module";
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Accessible Parameters
    ///////////////////////////////////////////////////////////////////////////

    public String getSuggestedNewModuleName()
    {
        assert commonBlockToMove != null && nameOfCommonBlockToMove != null;
        
        return nameOfCommonBlockToMove.equals("") ? "common" : nameOfCommonBlockToMove;
    }
    
    public void setNewModuleName(String name)
    {
        assert name != null;
        
        this.newModuleName = name;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        findEnclosingCommonBlock();
        determineEnclosingCommonBlockName();
        determineAffectedFiles();
        // TODO: Make sure all uses of this common block are identical (names, types)
        // so that USE-ing the module will not cause naming conflicts
    }

    private void findEnclosingCommonBlock() throws PreconditionFailure
    {
        Token enclosingToken = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (enclosingToken == null)
            fail("Please select a COMMON statement.");
        
        commonBlockToMove = enclosingToken.findNearestAncestor(ASTCommonBlockNode.class);
        if (commonBlockToMove == null)
            fail("Please select a COMMON statement.");
    }
    
    private void determineEnclosingCommonBlockName()
    {
        assert commonBlockToMove != null;
        
        nameOfCommonBlockToMove = getCommonBlockName(commonBlockToMove);
    }
    
    private String getCommonBlockName(ASTCommonBlockNode commonBlockToMove)
    {
        assert commonBlockToMove != null;
        
        ASTCommonBlockNameNode commonBlockNameToken = commonBlockToMove.getName();
        if (commonBlockNameToken != null)
            return commonBlockNameToken.getCommonBlockName().getText();
        else
            return "";
    }
    
    private void determineAffectedFiles() throws PreconditionFailure
    {
        assert commonBlockToMove != null && nameOfCommonBlockToMove != null;
        
        affectedFiles = new HashSet<IFile>();
        affectedFiles.addAll(vpg.findFilesThatUseCommonBlock(nameOfCommonBlockToMove));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert commonBlockToMove != null && nameOfCommonBlockToMove != null && affectedFiles != null;
        assert newModuleName != null;
        
        if (!isValidIdentifier(newModuleName)) fail(newModuleName + " is not a valid identifier");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert commonBlockToMove != null && nameOfCommonBlockToMove != null && affectedFiles != null;
        assert newModuleName != null;
        
        try
        {
            for (IFile file : affectedFiles)
            {
                if (file.equals(fileInEditor))
                    createModule();

                replaceCommonBlockWithModuleUseIn(file);
                addChangeFromModifiedAST(file, pm);
            }
        }
        finally
        {
        	vpg.releaseAllASTs();
        }
    }

    private void createModule()
    {
        ASTModuleNode module = createEmptyModule();
        
        for (ASTCommonBlockObjectNode obj : commonBlockToMove.getCommonBlockObjectList())
            populateModuleWithDeclarations(module, obj.getVariableName());
        
        addModuleAtBeginningOfFile(module);
    }

    private ASTModuleNode createEmptyModule()
    {
        String moduleSource =
            "module " + newModuleName + CRLF +
            "    implicit none" + CRLF +
            "end module " + newModuleName + CRLF;
        return (ASTModuleNode)parseLiteralProgramUnit(moduleSource);
    }

    private void populateModuleWithDeclarations(ASTModuleNode module, Token variable)
    {
        List<Definition> defs = variable.resolveBinding();
        if (defs.size() != 1) throw new Error(); // TODO

        Definition def = defs.get(0);

        ASTTypeDeclarationStmtNode typeDecl = def.getTokenRef().findToken().findNearestAncestor(ASTTypeDeclarationStmtNode.class);
        if (typeDecl != null)
        {
            module.getModuleBody().add(typeDecl);
            typeDecl.removeFromTree();
        }

        for (PhotranTokenRef tokRef : def.findAllReferences())
        {
            ISpecificationStmt enclosingSpecStmt = tokRef.findToken().findNearestAncestor(ISpecificationStmt.class);
            if (enclosingSpecStmt != null && !(enclosingSpecStmt instanceof ASTCommonStmtNode))
            {
                module.getModuleBody().add(enclosingSpecStmt);
                enclosingSpecStmt.removeFromTree();
            }
        }
    }

    private void addModuleAtBeginningOfFile(ASTModuleNode module)
    {
        astOfFileInEditor.getRoot().getProgramUnitList().add(0, module);
    }

    private void replaceCommonBlockWithModuleUseIn(IFile file)
    {
        IFortranAST ast = vpg.acquireTransientAST(file);
        ast.accept(new CommonBlockReplacer());
    }
    
    private class CommonBlockReplacer extends GenericASTVisitor
    {
        private ASTUseStmtNode useStmt = (ASTUseStmtNode)parseLiteralStatement("use " + newModuleName);
        
        @SuppressWarnings("unchecked")
        @Override
        public void visitASTCommonBlockNode(ASTCommonBlockNode node)
        {
            if (commonBlockHasSameName(node))
            {
                ASTCommonStmtNode enclosingCommonStmt = node.findNearestAncestor(ASTCommonStmtNode.class);
                
                if (containsOnlyOneCommonBlock(enclosingCommonStmt))
                {
                    enclosingCommonStmt.replaceWith(useStmt);
                }
                else
                {
                    node.findNearestAncestor(ASTCommonBlockListNode.class).removeFromTree();
                    ASTListNode body = (ASTListNode)enclosingCommonStmt.getParent();
                    body.add(body.indexOf(enclosingCommonStmt), useStmt);
                }
            }
        }

        private boolean commonBlockHasSameName(ASTCommonBlockNode commonBlock)
        {
            String cnameOfThisCommonBlock = PhotranVPG.canonicalizeIdentifier(getCommonBlockName(commonBlock));
            String cnameOfCommonBlockToMove = PhotranVPG.canonicalizeIdentifier(nameOfCommonBlockToMove);
            return cnameOfThisCommonBlock.equals(cnameOfCommonBlockToMove);
        }

        private boolean containsOnlyOneCommonBlock(ASTCommonStmtNode enclosingCommonStmt)
        {
            return enclosingCommonStmt.getCommonBlockList().size() == 1;
        }
    }
}
