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

public class ASTCharLenParamValueNode extends ASTNode
{
    ASTExprNode lengthExpr; // in ASTCharLenParamValueNode
    org.eclipse.photran.internal.core.lexer.Token isAssumedLength; // in ASTCharLenParamValueNode

    public ASTExprNode getLengthExpr()
    {
        return this.lengthExpr;
    }

    public void setLengthExpr(ASTExprNode newValue)
    {
        this.lengthExpr = newValue;
    }


    public boolean isAssumedLength()
    {
        return this.isAssumedLength != null;
    }

    public void setIsAssumedLength(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAssumedLength = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCharLenParamValueNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 2;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.lengthExpr;
        case 1:  return this.isAssumedLength;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lengthExpr = (ASTExprNode)value; return;
        case 1:  this.isAssumedLength = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

