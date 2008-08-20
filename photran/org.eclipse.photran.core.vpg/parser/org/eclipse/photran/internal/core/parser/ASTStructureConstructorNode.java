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

public class ASTStructureConstructorNode extends ASTNode
{
    ASTTypeNameNode typeName; // in ASTStructureConstructorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTStructureConstructorNode
    IASTListNode<ASTExprNode> exprList; // in ASTStructureConstructorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTStructureConstructorNode

    public ASTTypeNameNode getTypeName()
    {
        return this.typeName;
    }

    public void setTypeName(ASTTypeNameNode newValue)
    {
        this.typeName = newValue;
    }


    public IASTListNode<ASTExprNode> getExprList()
    {
        return this.exprList;
    }

    public void setExprList(IASTListNode<ASTExprNode> newValue)
    {
        this.exprList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTStructureConstructorNode(this);
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
        case 0:  return this.typeName;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.exprList;
        case 3:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.typeName = (ASTTypeNameNode)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.exprList = (IASTListNode<ASTExprNode>)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

