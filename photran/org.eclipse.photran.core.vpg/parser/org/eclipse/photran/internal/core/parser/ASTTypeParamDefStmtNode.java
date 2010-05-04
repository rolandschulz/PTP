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
public class ASTTypeParamDefStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTTypeParamDefStmtNode
    ASTTypeSpecNode typeSpec; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTTypeParamDefStmtNode
    ASTTypeParamAttrSpecNode typeParamAttrSpec; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTTypeParamDefStmtNode
    IASTListNode<ASTTypeParamDeclNode> typeParamDeclList; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTTypeParamDefStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTTypeSpecNode getTypeSpec()
    {
        return this.typeSpec;
    }

    public void setTypeSpec(ASTTypeSpecNode newValue)
    {
        this.typeSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTTypeParamAttrSpecNode getTypeParamAttrSpec()
    {
        return this.typeParamAttrSpec;
    }

    public void setTypeParamAttrSpec(ASTTypeParamAttrSpecNode newValue)
    {
        this.typeParamAttrSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTTypeParamDeclNode> getTypeParamDeclList()
    {
        return this.typeParamDeclList;
    }

    public void setTypeParamDeclList(IASTListNode<ASTTypeParamDeclNode> newValue)
    {
        this.typeParamDeclList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeParamDefStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 8;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.typeSpec;
        case 2:  return this.hiddenTComma;
        case 3:  return this.typeParamAttrSpec;
        case 4:  return this.hiddenTColon;
        case 5:  return this.hiddenTColon2;
        case 6:  return this.typeParamDeclList;
        case 7:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.typeSpec = (ASTTypeSpecNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.typeParamAttrSpec = (ASTTypeParamAttrSpecNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.typeParamDeclList = (IASTListNode<ASTTypeParamDeclNode>)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

