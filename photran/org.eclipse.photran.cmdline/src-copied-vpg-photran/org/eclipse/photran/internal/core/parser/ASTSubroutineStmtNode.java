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

public class ASTSubroutineStmtNode extends ASTNodeWithErrorRecoverySymbols
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
    }


    public IASTListNode<ASTPrefixSpecNode> getPrefixSpecList()
    {
        return this.prefixSpecList;
    }

    public void setPrefixSpecList(IASTListNode<ASTPrefixSpecNode> newValue)
    {
        this.prefixSpecList = newValue;
    }


    public ASTSubroutineNameNode getSubroutineName()
    {
        return this.subroutineName;
    }

    public void setSubroutineName(ASTSubroutineNameNode newValue)
    {
        this.subroutineName = newValue;
    }


    public IASTListNode<ASTSubroutineParNode> getSubroutinePars()
    {
        return this.subroutinePars;
    }

    public void setSubroutinePars(IASTListNode<ASTSubroutineParNode> newValue)
    {
        this.subroutinePars = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSubroutineStmtNode(this);
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
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.prefixSpecList = (IASTListNode<ASTPrefixSpecNode>)value; return;
        case 2:  this.hiddenTSubroutine = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.subroutineName = (ASTSubroutineNameNode)value; return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.subroutinePars = (IASTListNode<ASTSubroutineParNode>)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTBind = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.hiddenTLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.hiddenTIdent = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.hiddenTRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 11: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

