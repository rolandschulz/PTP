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

import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings({ "unchecked", "unused" })
public class ASTCommonBlockNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlash; // in ASTCommonBlockNode
    ASTCommonBlockNameNode name; // in ASTCommonBlockNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlash2; // in ASTCommonBlockNode
    IASTListNode<ASTCommonBlockObjectNode> commonBlockObjectList; // in ASTCommonBlockNode

    public ASTCommonBlockNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTCommonBlockNameNode newValue)
    {
        this.name = newValue;
    }


    public IASTListNode<ASTCommonBlockObjectNode> getCommonBlockObjectList()
    {
        return this.commonBlockObjectList;
    }

    public void setCommonBlockObjectList(IASTListNode<ASTCommonBlockObjectNode> newValue)
    {
        this.commonBlockObjectList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCommonBlockNode(this);
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
        case 0:  return this.hiddenTSlash;
        case 1:  return this.name;
        case 2:  return this.hiddenTSlash2;
        case 3:  return this.commonBlockObjectList;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTSlash = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.name = (ASTCommonBlockNameNode)value; return;
        case 2:  this.hiddenTSlash2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.commonBlockObjectList = (IASTListNode<ASTCommonBlockObjectNode>)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

