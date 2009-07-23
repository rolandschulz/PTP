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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTAssumedShapeSpecListNode extends ASTNode
{
    IASTListNode<ASTDeferredShapeSpecListNode> deferredShapeSpecList; // in ASTAssumedShapeSpecListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTAssumedShapeSpecListNode
    IExpr lb; // in ASTAssumedShapeSpecListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTAssumedShapeSpecListNode

    public IASTListNode<ASTDeferredShapeSpecListNode> getDeferredShapeSpecList()
    {
        return this.deferredShapeSpecList;
    }

    public void setDeferredShapeSpecList(IASTListNode<ASTDeferredShapeSpecListNode> newValue)
    {
        this.deferredShapeSpecList = newValue;
    }


    public IExpr getLb()
    {
        return this.lb;
    }

    public void setLb(IExpr newValue)
    {
        this.lb = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAssumedShapeSpecListNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.deferredShapeSpecList;
        case 1:  return this.hiddenTComma;
        case 2:  return this.lb;
        case 3:  return this.hiddenTColon;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.deferredShapeSpecList = (IASTListNode<ASTDeferredShapeSpecListNode>)value; return;
        case 1:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.lb = (IExpr)value; return;
        case 3:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

