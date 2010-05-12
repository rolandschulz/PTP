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
public class ASTSubroutineStmtNode extends ASTNodeWithErrorRecoverySymbols implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTSubroutineStmtNode
    IASTListNode<ASTPrefixSpecNode> prefixSpecList; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSubroutine; // in ASTSubroutineStmtNode
    ASTSubroutineNameNode subroutineName; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTSubroutineStmtNode
    IASTListNode<ASTSubroutineParNode> subroutinePars; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTBind; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen2; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIdent; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen2; // in ASTSubroutineStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTSubroutineStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTPrefixSpecNode> getPrefixSpecList()
    {
        return this.prefixSpecList;
    }

    public void setPrefixSpecList(IASTListNode<ASTPrefixSpecNode> newValue)
    {
        this.prefixSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTSubroutineNameNode getSubroutineName()
    {
        return this.subroutineName;
    }

    public void setSubroutineName(ASTSubroutineNameNode newValue)
    {
        this.subroutineName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSubroutineParNode> getSubroutinePars()
    {
        return this.subroutinePars;
    }

    public void setSubroutinePars(IASTListNode<ASTSubroutineParNode> newValue)
    {
        this.subroutinePars = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSubroutineStmtNode(this);
        visitor.visitIActionStmt(this);
        visitor.visitIActionStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 12;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.prefixSpecList;
        case 2:  return this.hiddenTSubroutine;
        case 3:  return this.subroutineName;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.subroutinePars;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.hiddenTBind;
        case 8:  return this.hiddenTLparen2;
        case 9:  return this.hiddenTIdent;
        case 10: return this.hiddenTRparen2;
        case 11: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.prefixSpecList = (IASTListNode<ASTPrefixSpecNode>)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTSubroutine = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.subroutineName = (ASTSubroutineNameNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.subroutinePars = (IASTListNode<ASTSubroutineParNode>)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTBind = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTIdent = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

