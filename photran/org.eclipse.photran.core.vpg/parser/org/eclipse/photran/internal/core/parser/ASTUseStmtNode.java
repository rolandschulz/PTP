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
public class ASTUseStmtNode extends ASTNode implements ISpecificationPartConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token useToken; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenTComma1; // in ASTUseStmtNode
    ASTModuleNatureNode moduleNature; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenTColon1; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenTColon2; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTUseStmtNode
    IASTListNode<ASTRenameNode> renameList; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTOnly; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTUseStmtNode
    IASTListNode<ASTOnlyNode> onlyList; // in ASTUseStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTUseStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getUseToken()
    {
        return this.useToken;
    }

    public void setUseToken(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.useToken = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTModuleNatureNode getModuleNature()
    {
        return this.moduleNature;
    }

    public void setModuleNature(ASTModuleNatureNode newValue)
    {
        this.moduleNature = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTRenameNode> getRenameList()
    {
        return this.renameList;
    }

    public void setRenameList(IASTListNode<ASTRenameNode> newValue)
    {
        this.renameList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTOnlyNode> getOnlyList()
    {
        return this.onlyList;
    }

    public void setOnlyList(IASTListNode<ASTOnlyNode> newValue)
    {
        this.onlyList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUseStmtNode(this);
        visitor.visitISpecificationPartConstruct(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 13;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.useToken;
        case 2:  return this.hiddenHiddenTComma1;
        case 3:  return this.moduleNature;
        case 4:  return this.hiddenHiddenTColon1;
        case 5:  return this.hiddenHiddenTColon2;
        case 6:  return this.name;
        case 7:  return this.hiddenTComma;
        case 8:  return this.renameList;
        case 9:  return this.hiddenTOnly;
        case 10: return this.hiddenTColon;
        case 11: return this.onlyList;
        case 12: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.useToken = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenHiddenTComma1 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.moduleNature = (ASTModuleNatureNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenHiddenTColon1 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenHiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.renameList = (IASTListNode<ASTRenameNode>)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTOnly = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.onlyList = (IASTListNode<ASTOnlyNode>)value; if (value != null) value.setParent(this); return;
        case 12: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

