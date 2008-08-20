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

public class ASTAssumedSizeSpecNode extends ASTNode
{
    IASTListNode<ASTExplicitShapeSpecNode> explicitShapeSpecList; // in ASTAssumedSizeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTAssumedSizeSpecNode
    ASTExprNode lb; // in ASTAssumedSizeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTAssumedSizeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTAssumedSizeSpecNode

    public IASTListNode<ASTExplicitShapeSpecNode> getExplicitShapeSpecList()
    {
        return this.explicitShapeSpecList;
    }

    public void setExplicitShapeSpecList(IASTListNode<ASTExplicitShapeSpecNode> newValue)
    {
        this.explicitShapeSpecList = newValue;
    }


    public ASTExprNode getLb()
    {
        return this.lb;
    }

    public void setLb(ASTExprNode newValue)
    {
        this.lb = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAssumedSizeSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.explicitShapeSpecList;
        case 1:  return this.hiddenTComma;
        case 2:  return this.lb;
        case 3:  return this.hiddenTColon;
        case 4:  return this.hiddenTAsterisk;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.explicitShapeSpecList = (IASTListNode<ASTExplicitShapeSpecNode>)value; return;
        case 1:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.lb = (ASTExprNode)value; return;
        case 3:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

