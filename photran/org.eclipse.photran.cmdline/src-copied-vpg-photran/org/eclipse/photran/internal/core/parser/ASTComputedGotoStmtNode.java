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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTComputedGotoStmtNode extends ASTNode implements IObsoleteActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTComputedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTGo; // in ASTComputedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTTo; // in ASTComputedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTGoto; // in ASTComputedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTComputedGotoStmtNode
    IASTListNode<ASTLblRefListNode> lblRefList; // in ASTComputedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTComputedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTComputedGotoStmtNode
    IExpr expr; // in ASTComputedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTComputedGotoStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTLblRefListNode> getLblRefList()
    {
        return this.lblRefList;
    }

    public void setLblRefList(IASTListNode<ASTLblRefListNode> newValue)
    {
        this.lblRefList = newValue;
    }


    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTComputedGotoStmtNode(this);
        visitor.visitIObsoleteActionStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 10;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTGo;
        case 2:  return this.hiddenTTo;
        case 3:  return this.hiddenTGoto;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.lblRefList;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.hiddenTComma;
        case 8:  return this.expr;
        case 9:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTGo = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTTo = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTGoto = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.lblRefList = (IASTListNode<ASTLblRefListNode>)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.expr = (IExpr)value; return;
        case 9:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

