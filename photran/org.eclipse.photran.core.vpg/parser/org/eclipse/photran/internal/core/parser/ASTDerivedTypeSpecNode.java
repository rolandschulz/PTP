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
public class ASTDerivedTypeSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token typeName; // in ASTDerivedTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenLParen2; // in ASTDerivedTypeSpecNode
    IASTListNode<ASTTypeParamSpecNode> typeParamSpecList; // in ASTDerivedTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenRParen2; // in ASTDerivedTypeSpecNode

    public org.eclipse.photran.internal.core.lexer.Token getTypeName()
    {
        return this.typeName;
    }

    public void setTypeName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.typeName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTTypeParamSpecNode> getTypeParamSpecList()
    {
        return this.typeParamSpecList;
    }

    public void setTypeParamSpecList(IASTListNode<ASTTypeParamSpecNode> newValue)
    {
        this.typeParamSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeSpecNode(this);
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
        case 1:  return this.hiddenHiddenLParen2;
        case 2:  return this.typeParamSpecList;
        case 3:  return this.hiddenHiddenRParen2;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.typeName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenHiddenLParen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.typeParamSpecList = (IASTListNode<ASTTypeParamSpecNode>)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenHiddenRParen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

