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
import java.util.LinkedList;
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
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNameNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockObjectNode;
import org.eclipse.photran.internal.core.parser.ASTCommonStmtNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.ISpecificationPartConstruct;
import org.eclipse.photran.internal.core.parser.ISpecificationStmt;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;

/**
 * Refactoring to move a COMMON block into a module.
 * 
 * @author Jeff Overbey
 */
public class MoveCommonToModuleRefactoring extends SingleFileFortranRefactoring
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
        ensureProjectHasRefactoringEnabled(status);
        
        findEnclosingCommonBlock();
        determineEnclosingCommonBlockName();
        determineAffectedFiles();
        
        // TODO: Require implicit none?
        
        // TODO: Make sure all uses of this common block are identical (names, types)
        // so that USE-ing the module will not cause naming conflicts
    }

    private void findEnclosingCommonBlock() throws PreconditionFailure
    {
        Token enclosingToken = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (enclosingToken == null)
            fail("Please select a variable or block in a COMMON statement.");
        
        commonBlockToMove = enclosingToken.findNearestAncestor(ASTCommonBlockNode.class);
        if (commonBlockToMove == null)
            fail("Please select a variable or block in a COMMON statement.");
    }
    
    private void determineEnclosingCommonBlockName()
    {
        nameOfCommonBlockToMove = getCommonBlockName(commonBlockToMove);
    }
    
    private String getCommonBlockName(ASTCommonBlockNode commonBlockToMove)
    {
        ASTCommonBlockNameNode commonBlockNameToken = commonBlockToMove.getName();
        if (commonBlockNameToken != null)
            return commonBlockNameToken.getCommonBlockName().getText();
        else
            return "";
    }
    
    private void determineAffectedFiles() throws PreconditionFailure
    {
        affectedFiles = new HashSet<IFile>();
        affectedFiles.add(fileInEditor);
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
        
        for (IFile file : affectedFiles)
        {
            if (file.equals(fileInEditor))
                createModule();

            replaceCommonBlockWithModuleUseIn(file);
            addChangeFromModifiedAST(file, pm);
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
        List<ISpecificationPartConstruct> specificationStmts = findSpecificationStmtsFor(variable);
        module.getModuleBody().addAll(specificationStmts);
    }

    private List<ISpecificationPartConstruct> findSpecificationStmtsFor(Token variable)
    {
        List<Definition> defs = variable.resolveBinding();
        if (defs.size() == 1)
            return findSpecificationStmtsFor(defs.get(0));
        else
            throw new Error(); // TODO
    }

    private List<ISpecificationPartConstruct> findSpecificationStmtsFor(Definition def)
    {
        List<ISpecificationPartConstruct> result = new LinkedList<ISpecificationPartConstruct>();
        
        ASTTypeDeclarationStmtNode typeDecl = def.getTokenRef().findToken().findNearestAncestor(ASTTypeDeclarationStmtNode.class);
        if (typeDecl != null)
            result.add(typeDecl);

        for (PhotranTokenRef tokRef : def.findAllReferences(false))
        {
            ISpecificationStmt enclosingSpecStmt = tokRef.findToken().findNearestAncestor(ISpecificationStmt.class);
            if (enclosingSpecStmt != null && !(enclosingSpecStmt instanceof ASTCommonStmtNode))
                result.add(enclosingSpecStmt);
        }
        
        return result;
    }

    private void addModuleAtBeginningOfFile(ASTModuleNode module)
    {
        astOfFileInEditor.getRoot().getProgramUnitList().add(0, module);
    }

    private void replaceCommonBlockWithModuleUseIn(IFile file)
    {
        IFortranAST ast = vpg.acquireTransientAST(file);
        
        for (ASTCommonBlockNode commonBlock : findCommonBlocksWithCorrectNameIn(ast))
        {
            removeSpecificationStmtsForCommonBlockVars(commonBlock);
            
            ASTCommonStmtNode enclosingCommonStmt = commonBlock.findNearestAncestor(ASTCommonStmtNode.class);
            
            addUseStmtAtBeginningOfScopeContaining(enclosingCommonStmt, ast);

            if (commonStmtContainsOnlyOneCommonBlock(enclosingCommonStmt))
                enclosingCommonStmt.removeFromTree();
            else
                commonBlock.removeFromTree();
        }
    }
    
    private List<ASTCommonBlockNode> findCommonBlocksWithCorrectNameIn(IFortranAST ast)
    {
        // Note that we must traverse the tree *before* we start changing it
        // to prevent ConcurrentModificationExceptions on list nodes
        
        final List<ASTCommonBlockNode> result = new LinkedList<ASTCommonBlockNode>();
        ast.accept(new GenericASTVisitor()
        {
            @Override
            public void visitASTCommonBlockNode(ASTCommonBlockNode commonBlock)
            {
                if (commonBlockHasSameName(commonBlock))
                    result.add(commonBlock);
            }
        });
        return result;
    }
    
    private void removeSpecificationStmtsForCommonBlockVars(ASTCommonBlockNode commonBlock)
    {
        // TODO: Should only remove this variable, not the entire spec statement
        //   common a, b
        //   integer :: a, b
        // will cause an IllegalStateException since the decl statement will be removed twice
        
        for (ASTCommonBlockObjectNode obj : commonBlock.getCommonBlockObjectList())
            for (ISpecificationPartConstruct specStmt : findSpecificationStmtsFor(obj.getVariableName()))
                specStmt.removeFromTree();
    }

    @SuppressWarnings("unchecked")
    private void addUseStmtAtBeginningOfScopeContaining(ASTCommonStmtNode enclosingCommonStmt, IFortranAST ast)
    {
        ASTUseStmtNode useStmt = (ASTUseStmtNode)parseLiteralStatement("use " + newModuleName);
        
        ScopingNode enclosingScope = enclosingCommonStmt.findNearestAncestor(ScopingNode.class);
        ((IASTListNode<IASTNode>)enclosingScope.getBody()).add(0, useStmt);
        
        Reindenter.reindent(useStmt, ast);
    }

    private boolean commonBlockHasSameName(ASTCommonBlockNode commonBlock)
    {
        String cnameOfThisCommonBlock = PhotranVPG.canonicalizeIdentifier(getCommonBlockName(commonBlock));
        String cnameOfCommonBlockToMove = PhotranVPG.canonicalizeIdentifier(nameOfCommonBlockToMove);
        return cnameOfThisCommonBlock.equals(cnameOfCommonBlockToMove);
    }

    private boolean commonStmtContainsOnlyOneCommonBlock(ASTCommonStmtNode enclosingCommonStmt)
    {
        return enclosingCommonStmt.getCommonBlockList().size() == 1;
    }
}
