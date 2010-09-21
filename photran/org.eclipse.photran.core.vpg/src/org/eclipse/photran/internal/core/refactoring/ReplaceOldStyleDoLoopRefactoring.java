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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLblRefNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

/**
 * Refactoring to replace old DO-LOOP formats by adding a more structured 
 * format construction with the END - DO statement . It works with Shared Do Loop Termination too. 
 *  
 * @author Mariano Mendez
 */
public class ReplaceOldStyleDoLoopRefactoring extends FortranEditorRefactoring
{
    private List<ASTProperLoopConstructNode> loopList= new LinkedList<ASTProperLoopConstructNode>();
    private List<IActionStmt> lblList= new LinkedList<IActionStmt>();
    
    @Override
    public String getName()
    {
        return Messages.ReplaceOldStyleDoLoopRefactoring_Name;
    }
    
    //////////////////////////////////////////////////////////////////////
    //          INITIAL CONDITIONS 
    /////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        // Change AST to represent DO-loops as ASTProperLoopConstructNodes
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());
        // Collect All Loops and all labels 
        collectAllLoopsAndLabelsIn(this.astOfFileInEditor.getRoot());
        //must have at last one OldStyle Do-Loop
        if (getOldStyleDoLoopCount()==0) 
            fail(Messages.ReplaceOldStyleDoLoopRefactoring_ThereMustBeAtLeastOneOldStyleDoLoop);
        // for each oldStyle DoLoop
        
        for(ASTProperLoopConstructNode node : loopList)
        {
            if (isOldStyleDoLoop(node))
            {
               ASTLabelDoStmtNode loopHeader=node.getLoopHeader();
                //there must be exactly one statement with the given "Label"
               int labelCount=getCountForLabel(loopHeader.getLblRef().getLabel());
               String labelName=loopHeader.getLblRef().getLabel().getText();
                if (labelCount>1)
                    fail(
                        Messages.bind(
                            Messages.ReplaceOldStyleDoLoopRefactoring_AmbiguousLabel,
                            labelName
                        )
                    );
                else if (labelCount<1)
                     fail(
                         Messages.bind(
                             Messages.ReplaceOldStyleDoLoopRefactoring_MissingLabel,
                             labelName
                         )  
                     );                
                if (!isSharedDoLoop(node))
                {
                    // it must be at the same level of the nesting as ASTDoStmt 
                    // then the grandpa of the labeledStmt (must be the loopBody) == father of LoopHeader  
                    IActionStmt labeledStmt= getLabeledStatement(loopHeader.getLblRef().getLabel());
                    IASTNode loopBody=labeledStmt.getParent();
                    if (loopBody.getParent()!=loopHeader.getParent())
                        fail(
                            Messages.bind(
                                Messages.ReplaceOldStyleDoLoopRefactoring_EndOfLoopError,
                                labelName
                            )  
                        );
                }     
            }    
        }
    
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
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        // For Each Old Style DoLoop in the list
        for(ASTProperLoopConstructNode node : loopList)
        {
            if (isOldStyleDoLoop(node))
            {
                ASTEndDoStmtNode newNode = (ASTEndDoStmtNode) parseLiteralStatement ("END DO" + EOL); //$NON-NLS-1$
                // Add and END DO Statement
                node.setEndDoStmt(newNode);
                // Remove from the Loop Header the label Reference 
                node.getLoopHeader().setLblRef(null);
                
                // Re-indent the node
                Reindenter.reindent( node, this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);
            }    
        }    
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
        vpg.releaseAST(this.fileInEditor);
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    //  AUXILIARY METHODS
    //////////////////////////////////////////////////////////////////////////////
    
    private void collectAllLoopsAndLabelsIn(ScopingNode scope)
    {
        //(Code taken and modified from LoopReplacer )
        scope.accept(new ASTVisitorWithLoops()
        {
            @Override public void visitASTProperLoopConstructNode (ASTProperLoopConstructNode node)
            {
                //Get loops
                    loopList.add(0, node);
                    traverseChildren(node);
            }
            
            @Override public void visitIActionStmt (IActionStmt node)
            {
                // get the statements labeled
                if(node.getLabel()!=null) 
                    lblList.add(0, node);
                traverseChildren(node);
            }
        });
    }
    
    
    private boolean isSharedDoLoop( ASTProperLoopConstructNode node)
    {  
        IASTNode parentNode= node.getParent();
        if( (parentNode instanceof IASTListNode<?>) &&  (parentNode.getParent() instanceof ASTProperLoopConstructNode) )
        {   
            // if is a Body get parent 
            parentNode=parentNode.getParent();
            if ( parentNode instanceof ASTProperLoopConstructNode) 
            {
                ASTLblRefNode headerNodeLabelRef=node.getLoopHeader().getLblRef();
                if (headerNodeLabelRef!=null) 
                {
                    Token headerNodeLabel=headerNodeLabelRef.getLabel();
                    //
                    ASTLblRefNode parentNodeRefLbl= ((ASTProperLoopConstructNode)parentNode).getLoopHeader().getLblRef();
                    if (parentNodeRefLbl!=null) 
                    {
                        Token parentNodeLabel=parentNodeRefLbl.getLabel();
                        if (parentNodeLabel.getText().equals(headerNodeLabel.getText())) return true;
                        else return false;
                    }
                    else return false ;
                } else return false;
            }
        }
        else 
        {
            // check the children 
            for (IASTNode child : node.getBody())
            {
                if (child instanceof ASTProperLoopConstructNode)
                {
                    
                    ASTLblRefNode headerLabelRef =node.getLoopHeader().getLblRef();
                    if (headerLabelRef !=null) 
                    {
                        Token headerLabel=headerLabelRef.getLabel();
                        ASTLblRefNode childLabelRefNode= ((ASTProperLoopConstructNode)child).getLoopHeader().getLblRef();
                        if (childLabelRefNode!= null) 
                        {
                            Token childLabel=childLabelRefNode.getLabel();
                            if (childLabel.getText().equals(headerLabel.getText())) return true;
                            else return false;
                        }
                        else return false;
                    }else return false;
                }    
            }                 
        }
        
        return false;
    }
 
    
    private int getOldStyleDoLoopCount()
    {
        int count = 0;
        
        for(ASTProperLoopConstructNode loopNode : loopList){
            if (isOldStyleDoLoop(loopNode))count++;
        }        
        return count;
    }    
    
    private boolean isOldStyleDoLoop(ASTProperLoopConstructNode node)
    {
        return (node.getEndDoStmt()==null 
               && node.getLoopHeader().getLblRef()!=null);
    }
    
    private int getCountForLabel(Token label)
    {
        int count = 0 ;
        Iterator<IActionStmt> it = lblList.iterator(); 
                
        while (it.hasNext())
        {
            IActionStmt node =it.next();
            if(label.getText().equals(node.getLabel().getText())) count++;
        }
        return count;
    }
    
    private IActionStmt getLabeledStatement(Token label)
    {
        Iterator<IActionStmt> it = lblList.iterator(); 
        
        while (it.hasNext())
        {
            IActionStmt node =it.next();
            if(label.getText().equals(node.getLabel().getText())) return node;
        }
        return null;
    }   
}
