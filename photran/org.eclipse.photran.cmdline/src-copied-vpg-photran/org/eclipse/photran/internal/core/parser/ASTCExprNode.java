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
package org.eclipse.photran.internal.core.parser;

import java.io.PrintStream;
import java.util.Iterator;

import java.util.List;

import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

public class ASTCExprNode extends ASTNode
{
    ASTCExprNode lhsExpr; // in ASTCExprNode
    ASTOperatorNode concatOp; // in ASTCExprNode
    ASTCPrimaryNode rhsPrimary; // in ASTCExprNode

    public ASTCExprNode getLhsExpr()
    {
        return this.lhsExpr;
    }

    public void setLhsExpr(ASTCExprNode newValue)
    {
        this.lhsExpr = newValue;
    }


    public ASTOperatorNode getConcatOp()
    {
        return this.concatOp;
    }

    public void setConcatOp(ASTOperatorNode newValue)
    {
        this.concatOp = newValue;
    }


    public ASTCPrimaryNode getRhsPrimary()
    {
        return this.rhsPrimary;
    }

    public void setRhsPrimary(ASTCPrimaryNode newValue)
    {
        this.rhsPrimary = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCExprNode(this);
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
        case 0:  return this.lhsExpr;
        case 1:  return this.concatOp;
        case 2:  return this.rhsPrimary;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lhsExpr = (ASTCExprNode)value;
        case 1:  this.concatOp = (ASTOperatorNode)value;
        case 2:  this.rhsPrimary = (ASTCPrimaryNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

