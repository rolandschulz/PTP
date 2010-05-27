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
public class ASTDataComponentDefStmtNode extends ASTNode implements IComponentDefStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTDataComponentDefStmtNode
    ASTTypeSpecNode typeSpec; // in ASTDataComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTDataComponentDefStmtNode
    IASTListNode<ASTComponentAttrSpecNode> componentAttrSpecList; // in ASTDataComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTDataComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTDataComponentDefStmtNode
    IASTListNode<ASTComponentDeclNode> componentDeclList; // in ASTDataComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTDataComponentDefStmtNode

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


    public IASTListNode<ASTComponentAttrSpecNode> getComponentAttrSpecList()
    {
        return this.componentAttrSpecList;
    }

    public void setComponentAttrSpecList(IASTListNode<ASTComponentAttrSpecNode> newValue)
    {
        this.componentAttrSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTComponentDeclNode> getComponentDeclList()
    {
        return this.componentDeclList;
    }

    public void setComponentDeclList(IASTListNode<ASTComponentDeclNode> newValue)
    {
        this.componentDeclList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDataComponentDefStmtNode(this);
        visitor.visitIComponentDefStmt(this);
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
        case 3:  return this.componentAttrSpecList;
        case 4:  return this.hiddenTColon;
        case 5:  return this.hiddenTColon2;
        case 6:  return this.componentDeclList;
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
        case 3:  this.componentAttrSpecList = (IASTListNode<ASTComponentAttrSpecNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.componentDeclList = (IASTListNode<ASTComponentDeclNode>)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

