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

public class ASTEnumeratorNode extends ASTNode
{
    ASTNamedConstantNode namedConstant; // in ASTEnumeratorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTEnumeratorNode
    ASTExprNode expr; // in ASTEnumeratorNode

    public ASTNamedConstantNode getNamedConstant()
    {
        return this.namedConstant;
    }

    public void setNamedConstant(ASTNamedConstantNode newValue)
    {
        this.namedConstant = newValue;
    }


    public ASTExprNode getExpr()
    {
        return this.expr;
    }

    public void setExpr(ASTExprNode newValue)
    {
        this.expr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEnumeratorNode(this);
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
        case 0:  return this.namedConstant;
        case 1:  return this.hiddenTEquals;
        case 2:  return this.expr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.namedConstant = (ASTNamedConstantNode)value; return;
        case 1:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.expr = (ASTExprNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

