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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTBodyNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTImplicitStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourceEditor;

/**
 * Refactoring to add an IMPLICIT NONE statement and explicit declarations for all implicitly-declared variables
 * into a scope and all nested scopes (where needed).
 * 
 * @author Jeff Overbey
 */
public class IntroImplicitNoneRefactoring extends FortranRefactoring
{
    private ScopingNode selectedScope = null;

    public IntroImplicitNoneRefactoring(IFile file, ITextSelection selection)
    {
        super(file, selection);
    }

    @Override
    public String getName()
    {
        return "Introduce Implicit None";
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        Token token = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        selectedScope = token == null ? null : token.getEnclosingScope();
        
        if (selectedScope == null)
        	fail("To Introduce Implicit None, first place the cursor inside a subprogram, module, or main program.");

        if (selectedScope.isImplicitNone())
        	fail("Implicit variables are already disallowed in the selected scope.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    /*
     * Change creation is complicated by the fact that, due to the internal structure of the AST,
     * we cannot call any accessor methods on AST nodes after the tree has been modified.  Therefore, we
     * must determine what nodes to delete, design the new nodes to insert, and determine where to insert
     * them *before* making any changes to the AST.  This is done by the constructModifications() method;
     * the actual changes are placed into a list of Runnables, which is executed after constructModifications()
     * completes.  Notice that there are no AST accessor methods executed by any of the Runnables; only
     * pointers to specific nodes are referenced.
     */
    
    @Override
    protected void doCreateChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException
    {
        assert this.selectedScope != null;

        for (Runnable command : constructModifications())
            command.run();
        		
        this.addChangeFromModifiedAST(this.fileInEditor, progressMonitor);
        vpg.releaseAllASTs();
    }

    private List<Runnable> constructModifications()
    {
    	List<Runnable> commands = new LinkedList<Runnable>();

    	for (ScopingNode scope : selectedScope.getAllContainedScopes())
    	{
            if (!scope.isImplicitNone()
                    && !(scope instanceof ASTExecutableProgramNode)
                    && !(scope instanceof ASTDerivedTypeDefNode))
            {
            	ASTImplicitStmtNode implicitStmt = findExistingImplicitStatement(scope);
                if (implicitStmt != null) commands.add(cut(implicitStmt));
            	
                ASTBodyNode newStmts = constructDeclarations(scope);
                commands.add(insert(newStmts, scope));
            }
    	}
    	
    	return commands;
    }

    private ASTImplicitStmtNode findExistingImplicitStatement(final ScopingNode scope)
    {
        try
        {
            scope.visitTopDownUsing(new ASTVisitor()
            {
                @Override
                public void visitASTImplicitStmtNode(ASTImplicitStmtNode node)
                {
                    if (node.getTImplicit().getEnclosingScope() == scope)
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
    
    private ASTBodyNode constructDeclarations(final ScopingNode scope)
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

	private Runnable cut(final ASTImplicitStmtNode implicitStmt)
	{
		return new Runnable()
		{
			public void run()
			{
				SourceEditor.cut(implicitStmt);
			}
		};
	}

	private Runnable insert(final ASTBodyNode stmtSeq, final ScopingNode scope)
	{
        final InteriorNode headerStmt = scope.getHeaderStmt();
		
		return new Runnable()
		{
			public void run()
			{
				SourceEditor.pasteAfterHeaderStmt(stmtSeq, scope, headerStmt, astOfFileInEditor);
			}
		};
	}
}
