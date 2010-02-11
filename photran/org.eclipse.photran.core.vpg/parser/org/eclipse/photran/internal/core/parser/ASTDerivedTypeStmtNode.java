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
public class ASTDerivedTypeStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTType; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTDerivedTypeStmtNode
    IASTListNode<ASTTypeAttrSpecNode> typeAttrSpecList; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token typeName; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTDerivedTypeStmtNode
    IASTListNode<ASTTypeParamNameNode> typeParamNameList; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTDerivedTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTDerivedTypeStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTTypeAttrSpecNode> getTypeAttrSpecList()
    {
        return this.typeAttrSpecList;
    }

    public void setTypeAttrSpecList(IASTListNode<ASTTypeAttrSpecNode> newValue)
    {
        this.typeAttrSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getTypeName()
    {
        return this.typeName;
    }

    public void setTypeName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.typeName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTTypeParamNameNode> getTypeParamNameList()
    {
        return this.typeParamNameList;
    }

    public void setTypeParamNameList(IASTListNode<ASTTypeParamNameNode> newValue)
    {
        this.typeParamNameList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 11;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTType;
        case 2:  return this.hiddenTComma;
        case 3:  return this.typeAttrSpecList;
        case 4:  return this.hiddenTColon;
        case 5:  return this.hiddenTColon2;
        case 6:  return this.typeName;
        case 7:  return this.hiddenTLparen;
        case 8:  return this.typeParamNameList;
        case 9:  return this.hiddenTRparen;
        case 10: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTType = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.typeAttrSpecList = (IASTListNode<ASTTypeAttrSpecNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.typeName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.typeParamNameList = (IASTListNode<ASTTypeParamNameNode>)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

