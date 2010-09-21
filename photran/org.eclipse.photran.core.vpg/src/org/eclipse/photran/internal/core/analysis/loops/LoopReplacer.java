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
package org.eclipse.photran.internal.core.analysis.loops;
 
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.ASTDoConstructNode;
import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.IObsoleteActionStmt;

/**
 * Identifies DO-loops in a Fortran AST and replaces them with {@link ASTProperLoopConstructNode}s.
 * <p>
 * Due to a deficiency in the parser, DO-constructs are not recognized as a single construct; DO and END DO statements
 * are recognized as ordinary statements on par with the statements comprising their body.
 * <p>
 * {@link ASTProperLoopConstructNode} provides a &quot;proper&quot; representation of DO-loops.  Invoking
 * {@link #replaceAllLoopsIn(ScopingNode)} will identify the loops in an AST and replace them with
 * {@link ASTProperLoopConstructNode}s.  After this has been done, the AST can be visited using an
 * {@link IASTVisitorWithLoops}.
 * 
 * @author Jeff Overbey
 * @author Mariano Mendez
 * 
 * @see LoopReplacer
 * @see IASTVisitorWithLoops
 */
public class LoopReplacer
{
    public static void replaceAllLoopsIn(ScopingNode scope)
    {
        new LoopReplacer().replaceLoopsFromLastToFirstIn(scope);
    }
    
    /** A list of all the loops in scope, from last to first */
    private List<ASTDoConstructNode> queue = new LinkedList<ASTDoConstructNode>();
    
    private void replaceLoopsFromLastToFirstIn(ScopingNode scope)
    {
        collectLoopsIn(scope);
        while (!queue.isEmpty())
            replaceLoop(queue.remove(0));
    }

    private void collectLoopsIn(ScopingNode scope)
    {
        scope.accept(new ASTVisitor()
        {
            @Override public void visitASTDoConstructNode(ASTDoConstructNode node)
            {
                // Collect all DoLoopsStmt 
                    queue.add(0, node);
            }
        });
    }

    private void replaceLoop(ASTDoConstructNode loopToReplace)
    {
        // Save ancestor nodes, since parent pointers will be changed when we manipulate
        // the AST in #buildASTProperLoopConstructNode below.
        IASTNode oldParent = loopToReplace.getParent();
        ScopingNode scope = loopToReplace.findNearestAncestor(ScopingNode.class);
        
        // Now manipulate the AST
        ASTProperLoopConstructNode newLoop = buildASTProperLoopConstructNode(loopToReplace, scope);
        loopToReplace.replaceWith(newLoop);
        newLoop.setParent(oldParent);
    }

    private ASTProperLoopConstructNode buildASTProperLoopConstructNode(ASTDoConstructNode loopToReplace, ScopingNode scope)
    {
        ASTLabelDoStmtNode lastLoopHeader = loopToReplace.getLabelDoStmt();
        
        // First, remove siblings of lastLoopHeader that should actually be in the loop body
        ASTProperLoopConstructBuilder nodeBuilder = new ASTProperLoopConstructBuilder(lastLoopHeader);
        scope.accept(nodeBuilder);

        // We needed to keep the loop header in the AST so that that ASTProperLoopConstructBuilder could find it
        // Now that it's finished, we can move the loop header into the ASTProperLoopConstructNode
        lastLoopHeader.removeFromTree();
        nodeBuilder.result.setLoopHeader(lastLoopHeader);

        return nodeBuilder.result;
    }
    
    private class ASTProperLoopConstructBuilder extends ASTVisitorWithLoops
    {
        private final ASTProperLoopConstructNode result = new ASTProperLoopConstructNode();
        private final ASTLabelDoStmtNode loopHeader;
        private final IASTNode doConstructNode;
        private final IASTNode listEnclosingDoConstructNode;
        private boolean loopHeaderFound = false;
        private IASTNode oldStyleEndLoopRef=null;
        
        // First, save ancestor nodes, since parent pointers will be changed when we
        // manipulate the AST in the #visit methods below
        public ASTProperLoopConstructBuilder(ASTLabelDoStmtNode loopHeader)
        {
            this.loopHeader = loopHeader;
            this.doConstructNode = loopHeader.getParent();
            this.listEnclosingDoConstructNode = doConstructNode.getParent();
            this.oldStyleEndLoopRef=null;
        }
        
        // Start accumulating body statements when we find the loop header
        @Override public void visitASTLabelDoStmtNode(ASTLabelDoStmtNode node)
        {
            if (node == loopHeader)
                loopHeaderFound = true;
            
            traverseChildren(node);
        }

        // Accumulate all statements between the loop header and the END DO stmt
        @Override public void visitIExecutionPartConstruct(IExecutionPartConstruct node)
        {
            if (shouldBeInLoopBody(node))
            {
                node.removeFromTree();
                this.result.getBody().add(node);
            }
        }
        
        @Override public void visitIExecutableConstruct(IExecutableConstruct node)
        {
            visitIExecutionPartConstruct(node);
        }

        @Override public void visitIActionStmt(IActionStmt node)
        {
            //  Obtain a reference to the end of the old Style Loop Node  
            visitIExecutionPartConstruct(node);
            if (isOldStyleDoLoopEnd(node)) 
            {
                this.result.setEndDoStmt(null);
                this.oldStyleEndLoopRef=node; 
            }
            //traverseChildren(node);
        }
        
        @Override public void visitIObsoleteActionStmt(IObsoleteActionStmt node)
        {
            visitIExecutionPartConstruct(node);
        }

        private boolean shouldBeInLoopBody(IExecutionPartConstruct node)
        {
            return loopHeaderFound
                && !endDoStmtFound()
                && !oldStyleEndLoopFound()
                && !isLoopHeader(node)
                && isCurrentlySiblingOfLoopHeader(node);
        }
        
        private boolean isOldStyleDoLoopEnd( IActionStmt node  )
        {
            if ( (node.getLabel()!=null) && (this.loopHeader.getLblRef()!=null) )
            {
                return loopHeaderFound 
                    && !endDoStmtFound()   
                    && !(node.getParent() == this.listEnclosingDoConstructNode)  
                    && this.loopHeader.getLblRef().getLabel().getText().equals(node.getLabel().getText()) ;
            }
            return false;
        }

        
        private boolean isCurrentlySiblingOfLoopHeader(IExecutionPartConstruct node)
        {
            return node.getParent() == listEnclosingDoConstructNode;
        }

        // Don't accumulate either the ASTLabelDoStmtNode or the ASTDoConstructNode in the body; these are the header
        private boolean isLoopHeader(IExecutionPartConstruct node)
        {
            return node == loopHeader || node == doConstructNode;
        }

        // Stop accumulating body statements as soon as we find an END DO stmt
        @Override public void visitASTEndDoStmtNode(ASTEndDoStmtNode node)
        {
          if (loopHeaderFound && !endDoStmtFound() && (node.getParent() == listEnclosingDoConstructNode) && !oldStyleEndLoopFound() )
            {
                node.removeFromTree();
                this.result.setEndDoStmt(node);
            }
            
            traverseChildren(node);
        }

        private boolean endDoStmtFound()
        {
            return this.result.getEndDoStmt() != null;
        }
        
        private boolean oldStyleEndLoopFound()
        {
            return this.oldStyleEndLoopRef != null;
        }
        
        
        @Override public void visitASTProperLoopConstructNode(ASTProperLoopConstructNode node)
        {
            // Do not traverse child statements of nested loops
            // Except if you are working with a Shared Do Loop Termination
            // you need to know where is the ending Loop 
            
            if (node.getLoopHeader().getLblRef()==null) return;
            if (this.loopHeader.getLblRef()==null) return;
            
            String nodeLabel=node.getLoopHeader().getLblRef().getLabel().getText();
            String headerLabel= this.loopHeader.getLblRef().getLabel().getText();
            
            if ( !endDoStmtFound() && !oldStyleEndLoopFound()  && nodeLabel.equals(headerLabel) ) 
            {
                visitIExecutionPartConstruct(node);
                this.oldStyleEndLoopRef=node.getLoopHeader().getLblRef();    
            }  
        }
    }
}
