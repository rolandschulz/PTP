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
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.IObsoleteActionStmt;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

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
                if (!isHeaderForOldStyleLoop(node))
                    queue.add(0, node);
            }
    
            /** An old-style loop would be, for example, DO 100 I = 3, 5 */
            private boolean isHeaderForOldStyleLoop(ASTDoConstructNode node)
            {
                return node.getLabelDoStmt().getLblRef() != null;
            }
        });
    }

    private void replaceLoop(ASTDoConstructNode loopToReplace)
    {
        ASTProperLoopConstructNode newLoop = buildASTProperLoopConstructNode(loopToReplace.getLabelDoStmt());
        removeAndReparentNodesIn(newLoop);
        
        IASTNode parent = loopToReplace.getParent();
        loopToReplace.replaceWith(newLoop);
        newLoop.setParent(parent);
    }

    private ASTProperLoopConstructNode buildASTProperLoopConstructNode(ASTLabelDoStmtNode lastLoopHeader)
    {
        ASTProperLoopConstructBuilder nodeBuilder = new ASTProperLoopConstructBuilder(lastLoopHeader);
        lastLoopHeader.findNearestAncestor(ScopingNode.class).accept(nodeBuilder);
        return nodeBuilder.result;
    }
    
    private class ASTProperLoopConstructBuilder extends GenericASTVisitor
    {
        private ASTProperLoopConstructNode result = new ASTProperLoopConstructNode();
        private boolean loopHeaderFound = false;
        
        // Set the loop header before we start the traversal
        public ASTProperLoopConstructBuilder(ASTLabelDoStmtNode loopHeader)
        {
            this.result.loopHeader = loopHeader;
        }
        
        // Start accumulating body statements when we find the loop header
        @Override public void visitASTLabelDoStmtNode(ASTLabelDoStmtNode node)
        {
            if (node == this.result.loopHeader)
                loopHeaderFound = true;
        }

        // Accumulate all statements between the loop header and the END DO stmt
        @Override public void visitIExecutionPartConstruct(IExecutionPartConstruct node)
        {
            if (shouldBeInLoopBody(node))
                this.result.body.add(node);
        }
        
        @Override public void visitIExecutableConstruct(IExecutableConstruct node)
        {
            visitIExecutionPartConstruct(node);
        }

        @Override public void visitIActionStmt(IActionStmt node)
        {
            visitIExecutionPartConstruct(node);
        }
        
        @Override public void visitIObsoleteActionStmt(IObsoleteActionStmt node)
        {
            visitIExecutionPartConstruct(node);
        }

        private boolean shouldBeInLoopBody(IExecutionPartConstruct node)
        {
            return loopHeaderFound
                && !endDoStmtFound()
                && !isLoopHeader(node)
                && isCurrentlySiblingOfLoopHeader(node);
        }
        
        private boolean isCurrentlySiblingOfLoopHeader(IExecutionPartConstruct node)
        {
            IASTNode doConstructNode = this.result.loopHeader.getParent();
            return node.getParent() == doConstructNode.getParent();
        }

        // Don't accumulate either the ASTLabelDoStmtNode or the ASTDoConstructNode in the body; these are the header
        private boolean isLoopHeader(IExecutionPartConstruct node)
        {
            return node == this.result.loopHeader || node == this.result.loopHeader.getParent();
        }

        // Stop accumulating body statements as soon as we find an END DO stmt
        @Override public void visitASTEndDoStmtNode(ASTEndDoStmtNode node)
        {
            if (loopHeaderFound && !endDoStmtFound() && !(node.getParent() instanceof ASTProperLoopConstructNode))
                this.result.endDoStmt = node;
        }

        private boolean endDoStmtFound()
        {
            return this.result.endDoStmt != null;
        }
    }

    private void removeAndReparentNodesIn(ASTProperLoopConstructNode newLoop)
    {
        removeAndReparent(newLoop, newLoop.getLoopHeader());
        for (IExecutionPartConstruct stmt : newLoop.getBody())
            removeAndReparent(newLoop.getBody(), stmt);
        removeAndReparent(newLoop, newLoop.getEndDoStmt());
    }

    private void removeAndReparent(IASTNode newParent, IASTNode stmt)
    {
        stmt.removeFromTree();
        stmt.setParent(newParent);
    }
}
