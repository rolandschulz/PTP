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

import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;

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
    ASTLabelDoStmtNode loopHeader;
    IASTListNode<IExecutionPartConstruct> body = new ASTListNode<IExecutionPartConstruct>();
    ASTEndDoStmtNode endDoStmt;

    public ASTLabelDoStmtNode getLoopHeader()
    {
        return this.loopHeader;
    }

    public IASTListNode<IExecutionPartConstruct> getBody()
    {
        return this.body;
    }

    public ASTEndDoStmtNode getEndDoStmt()
    {
        return this.endDoStmt;
    }

    public void accept(IASTVisitor visitor)
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
        default: return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.loopHeader = (ASTLabelDoStmtNode)value;
        case 1:  this.body = (IASTListNode<IExecutionPartConstruct>)value;
        case 2:  this.endDoStmt = (ASTEndDoStmtNode)value;
        default: throw new IllegalArgumentException();
        }
    }
}