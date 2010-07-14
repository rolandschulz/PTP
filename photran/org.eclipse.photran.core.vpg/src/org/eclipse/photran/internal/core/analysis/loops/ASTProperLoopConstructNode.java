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

import java.util.HashSet;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExitStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;

/**
 * A custom AST node for Fortran DO loops.
 * <p>
 * Due to a deficiency in the parser, DO-constructs are not recognized as a single construct; DO and END DO statements
 * are recognized as ordinary statements on par with the statements comprising their body.
 * <p>
 * This class provides a &quot;proper&quot; representation of DO-loops.  Call
 * {@link LoopReplacer#replaceAllLoopsIn(org.eclipse.photran.internal.core.analysis.binding.ScopingNode)} to identify
 * the loops in an AST and replace them with {@link ASTProperLoopConstructNode}s.  After this has been done, the AST
 * can be visited using an {@link IASTVisitorWithLoops}.
 * 
 * @author Jeff Overbey
 * 
 * @see LoopReplacer
 * @see IASTVisitorWithLoops
 */
public class ASTProperLoopConstructNode extends ASTNode implements IExecutableConstruct
{
    private ASTLabelDoStmtNode loopHeader;
    private IASTListNode<IExecutionPartConstruct> body;
    private ASTEndDoStmtNode endDoStmt;
    
    public ASTProperLoopConstructNode()
    {
        body = new ASTListNode<IExecutionPartConstruct>();
        body.setParent(this);
    }

    public ASTLabelDoStmtNode getLoopHeader()
    {
        return this.loopHeader;
    }
    
    void setLoopHeader(ASTLabelDoStmtNode header)
    {
        this.loopHeader = header;
        if (header != null) header.setParent(this);
    }

    public IASTListNode<IExecutionPartConstruct> getBody()
    {
        return this.body;
    }

    public ASTEndDoStmtNode getEndDoStmt()
    {
        return this.endDoStmt;
    }
    
    void setEndDoStmt(ASTEndDoStmtNode endDoStmt)
    {
        this.endDoStmt = endDoStmt;
        if (endDoStmt != null) endDoStmt.setParent(this);
    }

    @Override public void accept(IASTVisitor visitor)
    {
        if (visitor instanceof IASTVisitorWithLoops)
            ((IASTVisitorWithLoops)visitor).visitASTProperLoopConstructNode(this);
        visitor.visitIExecutableConstruct(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.loopHeader;
        case 1:  return this.body;
        case 2:  return this.endDoStmt;
        default: throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.loopHeader = (ASTLabelDoStmtNode)value; if (loopHeader != null) loopHeader.setParent(this); break;
        case 1:  this.body = (IASTListNode<IExecutionPartConstruct>)value; if (body != null) body.setParent(this); break;
        case 2:  this.endDoStmt = (ASTEndDoStmtNode)value; if (endDoStmt != null) endDoStmt.setParent(this); break;
        default: throw new IllegalArgumentException();
        }
    }
    
    // UTILITY METHODS ////////////////////////////////////////////////////////////////////////////
    
    public boolean isDoWhileLoop()
    {
        return getLoopHeader().getLoopControl().getVariableName() == null;
    }
    
    public Token getIndexVariable()
    {
        return getLoopHeader().getLoopControl().getVariableName();
    }
    
    public HashSet<ASTExitStmtNode> getExits()
    {
        final HashSet<ASTExitStmtNode> exits = new HashSet<ASTExitStmtNode>();
        
        getBody().accept(new ASTVisitorWithLoops(){
           @Override public void visitASTExitStmtNode(ASTExitStmtNode node)
           {
               exits.add(node);
           }
        });
        
        return exits;
    }
}