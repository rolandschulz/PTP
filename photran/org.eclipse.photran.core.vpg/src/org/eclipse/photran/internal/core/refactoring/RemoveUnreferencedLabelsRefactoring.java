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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.parser.ASTContinueStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLblRefNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

/**
 * Removes the labels that are not being used anymore in the code 
 * 
 * @author Mariano Mendez
 */
public class RemoveUnreferencedLabelsRefactoring extends FortranEditorRefactoring
{
    private Map< String,Integer> labelMap;

    @Override
    public String getName()
    {
        return Messages.RemoveUnreferencedLabelsRefactoring_Name;
    }

    //////////////////////////////////////////////////////////////////////
    //          INITIAL CONDITIONS 
    /////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        labelMap= new HashMap<String,Integer>();
        ensureProjectHasRefactoringEnabled(status);
        collectAllLabels(this.astOfFileInEditor.getRoot());
        collectAllReferences(this.astOfFileInEditor.getRoot());
        if (labelMap.size()==0) 
            fail(Messages.RemoveUnreferencedLabelsRefactoring_ThereMustBeAtLeastOneLabeledStatement);
    }
     
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final preconditions
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        ScopingNode scope = this.astOfFileInEditor.getRoot(); 
      
        scope.accept(new ASTVisitorWithLoops()
        {            
            // Visit IActionStmt Nodes
            @Override public void visitIActionStmt (IActionStmt node)
            {
             // get the statements labeled
                if (node.getLabel()!=null) 
                {
                    String key =node.getLabel().getText();
                    if (labelMap.containsKey(key))
                    {
                        Integer refCount =labelMap.get(key);
                        if (refCount==0) 
                        {
                            // remove the label 
                            node.setLabel(null);
                            if (node instanceof ASTContinueStmtNode)
                            {
                                node.removeFromTree();
                            }
                            Reindenter.reindent(node,astOfFileInEditor,Strategy.REINDENT_EACH_LINE);
                        }
                    }
                }
            }
           
        });
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
        vpg.releaseAST(this.fileInEditor); 
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    
    private void collectAllLabels(ScopingNode scope)
    {
        // Visit the AST 
        scope.accept(new ASTVisitorWithLoops()
        {            
            // Visit IActionStmt Nodes
            @Override public void visitIActionStmt (IActionStmt node)
            {
                // get the statements labeled
                if (node.getLabel()!=null) 
                {
                    String key =node.getLabel().getText();
                    if (!labelMap.containsKey(key)) labelMap.put(key, new Integer(0));
                }
                traverseChildren(node);
            }
        });
    }

    private void collectAllReferences(ScopingNode scope)
    {
        // Visit the AST 
        scope.accept(new ASTVisitorWithLoops()
        {    
            //ASTLblRefNode
            @Override public void visitASTLblRefNode(ASTLblRefNode node)
            {
                // get Label references count
                if (node.getLabel()!=null) 
                {
                    String key = node.getLabel().getText();
                    if (labelMap.containsKey(key))
                        labelMap.put(key,(labelMap.get(key)+1));
                }
                traverseChildren(node);
            }
        });
    }
}
