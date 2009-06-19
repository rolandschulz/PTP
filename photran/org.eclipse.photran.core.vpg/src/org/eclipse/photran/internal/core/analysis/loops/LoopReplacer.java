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

import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.ASTDoConstructNode;
import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
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
    private LoopReplacer() {;}
    
    public static void replaceAllLoopsIn(ScopingNode scope)
    {
        while (replaceLastLoopIn(scope))
            replaceLastLoopIn(scope);
    }
    
    private static boolean replaceLastLoopIn(ScopingNode scope)
    {
        ASTDoConstructNode lastLoopHeader = findLastLoopHeaderIn(scope);
        if (lastLoopHeader == null) return false;
        
        ASTProperLoopConstructNode newLoop = buildASTProperLoopConstructNode(lastLoopHeader.getLabelDoStmt());
        removeAndReparentNodesIn(newLoop);
        
        IASTNode parent = lastLoopHeader.getParent();
        lastLoopHeader.replaceWith(newLoop);
        newLoop.setParent(parent);
        
        return true;
    }

    private static ASTDoConstructNode findLastLoopHeaderIn(ScopingNode scope)
    {
        LastLoopHeaderFinder finder = new LastLoopHeaderFinder();
        scope.accept(finder);
        return finder.result;
    }
    
    private static class LastLoopHeaderFinder extends ASTVisitor
    {
        public ASTDoConstructNode result = null;
        
        @Override public void visitASTDoConstructNode(ASTDoConstructNode node)
        {
            this.result = node;
        }
    }

    private static ASTProperLoopConstructNode buildASTProperLoopConstructNode(ASTLabelDoStmtNode lastLoopHeader)
    {
        ASTProperLoopConstructBuilder endDoStmtFinder = new ASTProperLoopConstructBuilder(lastLoopHeader);
        lastLoopHeader.findNearestAncestor(ScopingNode.class).accept(endDoStmtFinder);
        return endDoStmtFinder.result;
    }
    
    private static class ASTProperLoopConstructBuilder extends GenericASTVisitor
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
        @Override public void visitIExecutableConstruct(IExecutableConstruct node)
        {
            if (shouldBeInLoopBody(node))
                this.result.body.add(node);
        }

        private boolean shouldBeInLoopBody(IExecutableConstruct node)
        {
            return loopHeaderFound
                && !endDoStmtFound()
                && !isLoopHeader(node)
                && isCurrentlySiblingOfLoopHeader(node);
        }
        
        private boolean isCurrentlySiblingOfLoopHeader(IExecutableConstruct node)
        {
            IASTNode doConstructNode = this.result.loopHeader.getParent();
            return node.getParent() == doConstructNode.getParent();
        }

        @Override public void visitIActionStmt(IActionStmt node)
        {
            visitIExecutableConstruct(node);
        }
        
        @Override public void visitIObsoleteActionStmt(IObsoleteActionStmt node)
        {
            visitIExecutableConstruct(node);
        }

        // Don't accumulate either the ASTLabelDoStmtNode or the ASTDoConstructNode in the body; these are the header
        private boolean isLoopHeader(IExecutableConstruct node)
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

    private static void removeAndReparentNodesIn(ASTProperLoopConstructNode newLoop)
    {
        removeAndReparent(newLoop, newLoop.getLoopHeader());
        for (IExecutableConstruct stmt : newLoop.getBody())
            removeAndReparent(newLoop.getBody(), stmt);
        removeAndReparent(newLoop, newLoop.getEndDoStmt());
    }

    private static void removeAndReparent(IASTNode newParent, IASTNode stmt)
    {
        stmt.removeFromTree();
        stmt.setParent(newParent);
    }
}
