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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTImplicitStmtNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.MultipleFileFortranRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;

/**
 * Refactoring to add an IMPLICIT NONE statement and explicit declarations for all implicitly-declared variables
 * into a scope and all nested scopes (where needed).
 * 
 * @author Jeff Overbey, Timofey Yuvashev
 */
public class IntroImplicitNoneRefactoring extends MultipleFileFortranRefactoring
{
    public IntroImplicitNoneRefactoring(ArrayList<IFile> myFiles)
    {
        super(myFiles);
    }
    
    @Override
    public String getName()
    {
        return "Introduce Implicit None";
    }

    /*public String getScopeDescription()
    {
    	return selectedScope == null || selectedScope.getHeaderStmt() == null
    		? "the selected scope..."
    		: "\n" + SourcePrinter.getSourceCodeFromASTNode(selectedScope.getHeaderStmt());
    }*/
    
    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions & Change Creation
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        logVPGErrors(status);
        
        try
        {
            for(IFile f : this.selectedFiles)
            {
                IFortranAST tempAST = this.vpg.acquirePermanentAST(f);
                if (tempAST == null)
                    status.addError("One of the selected files (" + f.getName() +") cannot be parsed.");
                introduceImplicitNoneInFile(pm, tempAST.getRoot(), tempAST, f);
                vpg.releaseAST(f);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void introduceImplicitNoneInFile(IProgressMonitor progressMonitor, 
                                             ScopingNode scopeNode, 
                                             IFortranAST ast, 
                                             IFile file)
    {
        assert scopeNode != null;
        //Get all scopes contained in the file
        List<ScopingNode> nodeList = scopeNode.getAllContainedScopes();
        
        for (ScopingNode scope : nodeList)
        {
            if (!(scope instanceof ASTExecutableProgramNode)
                &&  !(scope instanceof ASTDerivedTypeDefNode)
                &&  !scope.isImplicitNone())
            {
                ASTImplicitStmtNode implicitStmt = findExistingImplicitStatement(scope);
                if (implicitStmt != null) implicitStmt.removeFromTree();
                
                IASTListNode<IBodyConstruct> newDeclarations = constructDeclarations(scope);
                IASTListNode<IASTNode> body = (IASTListNode<IASTNode>)scope.getBody();
                body.addAll(0, newDeclarations);
                Reindenter.reindent(newDeclarations, ast);
            }
        }
            
        this.addChangeFromModifiedAST(file, progressMonitor);
    }

    private ASTImplicitStmtNode findExistingImplicitStatement(final ScopingNode scope)
    {
        try
        {
            scope.accept(new GenericASTVisitor()
            {
                @Override
                public void visitASTImplicitStmtNode(ASTImplicitStmtNode node)
                {
                    if (node.getImplicitToken().getEnclosingScope() == scope)
                        throw new Notification(node);
                }
            });
        }
        catch (Notification n)
        {
            return (ASTImplicitStmtNode)n.getResult();
        }
        return null;
    }
    
    private IASTListNode<IBodyConstruct> constructDeclarations(final ScopingNode scope)
    {
        final ArrayList<Definition> definitions = new ArrayList<Definition>(16);
        
        for (Definition def : scope.getAllDefinitions())
        	if (def != null && def.isImplicit())
        		definitions.add(def);
        
        StringBuilder newStmts = new StringBuilder();
        newStmts.append("implicit none" + EOL);
        for (Definition def : sort(definitions))
        	newStmts.append(constructDeclaration(def));
        return parseLiteralStatementSequence(newStmts.toString());
    }

    private ArrayList<Definition> sort(ArrayList<Definition> array)
    {
        for (int indexOfElementToInsert = 1; indexOfElementToInsert < array.size(); indexOfElementToInsert++)
        {
            Definition def = array.get(indexOfElementToInsert);
            int targetIndex = findIndexToInsertAt(array, indexOfElementToInsert);
            for (int i = indexOfElementToInsert - 1; i >= targetIndex; i--)
                array.set(i+1, array.get(i));
            array.set(targetIndex, def);
        }
        return array;
    }

    private int findIndexToInsertAt(ArrayList<Definition> array, int indexOfElementToInsert)
    {
        for (int beforeIndex = 0; beforeIndex < indexOfElementToInsert; beforeIndex++)
            if (array.get(indexOfElementToInsert).getCanonicalizedName().compareTo(array.get(beforeIndex).getCanonicalizedName()) < 0)
                return beforeIndex;
        
        return indexOfElementToInsert;
    }

	private String constructDeclaration(final Definition def)
	{
		Type type = def.getType();
		String typeString = type == null ? "type(unknown)" : type.toString(); // TODO
		return typeString + " :: " + def.getCanonicalizedName() + EOL;
	}

    @Override
    protected void doCreateChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException
    {
        // Change creation done in #doCheckFinalConditions
    }
}
