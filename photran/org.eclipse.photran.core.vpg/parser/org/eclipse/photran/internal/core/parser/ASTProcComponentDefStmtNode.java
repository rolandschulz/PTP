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
public class ASTProcComponentDefStmtNode extends ASTNode implements IComponentDefStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTProcComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTProcedure; // in ASTProcComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTProcComponentDefStmtNode
    ASTProcInterfaceNode procInterface; // in ASTProcComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTProcComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTProcComponentDefStmtNode
    IASTListNode<ASTProcComponentAttrSpecNode> procComponentAttrSpecList; // in ASTProcComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTProcComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTProcComponentDefStmtNode
    IASTListNode<ASTProcDeclNode> procDeclList; // in ASTProcComponentDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTProcComponentDefStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTProcInterfaceNode getProcInterface()
    {
        return this.procInterface;
    }

    public void setProcInterface(ASTProcInterfaceNode newValue)
    {
        this.procInterface = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTProcComponentAttrSpecNode> getProcComponentAttrSpecList()
    {
        return this.procComponentAttrSpecList;
    }

    public void setProcComponentAttrSpecList(IASTListNode<ASTProcComponentAttrSpecNode> newValue)
    {
        this.procComponentAttrSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTProcDeclNode> getProcDeclList()
    {
        return this.procDeclList;
    }

    public void setProcDeclList(IASTListNode<ASTProcDeclNode> newValue)
    {
        this.procDeclList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTProcComponentDefStmtNode(this);
        visitor.visitIComponentDefStmt(this);
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
        case 1:  return this.hiddenTProcedure;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.procInterface;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.hiddenTComma;
        case 6:  return this.procComponentAttrSpecList;
        case 7:  return this.hiddenTColon;
        case 8:  return this.hiddenTColon2;
        case 9:  return this.procDeclList;
        case 10: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTProcedure = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.procInterface = (ASTProcInterfaceNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.procComponentAttrSpecList = (IASTListNode<ASTProcComponentAttrSpecNode>)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.procDeclList = (IASTListNode<ASTProcDeclNode>)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

