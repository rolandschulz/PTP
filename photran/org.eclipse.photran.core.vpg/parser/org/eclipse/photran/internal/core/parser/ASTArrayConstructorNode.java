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

import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings("all")
public class ASTArrayConstructorNode extends ASTNode implements IExpr
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTArrayConstructorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparenslash; // in ASTArrayConstructorNode
    IASTListNode<ASTAcValueNode> acValueList; // in ASTArrayConstructorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlashrparen; // in ASTArrayConstructorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTArrayConstructorNode

    public IASTListNode<ASTAcValueNode> getAcValueList()
    {
        return this.acValueList;
    }

    public void setAcValueList(IASTListNode<ASTAcValueNode> newValue)
    {
        this.acValueList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTArrayConstructorNode(this);
        visitor.visitIExpr(this);
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
        case 0:  return this.hiddenTLbracket;
        case 1:  return this.hiddenTLparenslash;
        case 2:  return this.acValueList;
        case 3:  return this.hiddenTSlashrparen;
        case 4:  return this.hiddenTRbracket;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTLparenslash = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.acValueList = (IASTListNode<ASTAcValueNode>)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTSlashrparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

