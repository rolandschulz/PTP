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

public class ASTExplicitShapeSpecNode extends ASTNode
{
    ASTExprNode lb; // in ASTExplicitShapeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTExplicitShapeSpecNode
    ASTExprNode ub; // in ASTExplicitShapeSpecNode

    public ASTExprNode getLb()
    {
        return this.lb;
    }

    public void setLb(ASTExprNode newValue)
    {
        this.lb = newValue;
    }


    public ASTExprNode getUb()
    {
        return this.ub;
    }

    public void setUb(ASTExprNode newValue)
    {
        this.ub = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTExplicitShapeSpecNode(this);
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
        case 0:  return this.lb;
        case 1:  return this.hiddenTColon;
        case 2:  return this.ub;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.lb = (ASTExprNode)value;
        case 1:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.ub = (ASTExprNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

