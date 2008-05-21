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

public class ASTMaskedElseWhereStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTMaskedElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTElse; // in ASTMaskedElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTElsewhere; // in ASTMaskedElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTWhere; // in ASTMaskedElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTMaskedElseWhereStmtNode
    ASTExprNode maskExpr; // in ASTMaskedElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTMaskedElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token endName; // in ASTMaskedElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTMaskedElseWhereStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public ASTExprNode getMaskExpr()
    {
        return this.maskExpr;
    }

    public void setMaskExpr(ASTExprNode newValue)
    {
        this.maskExpr = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getEndName()
    {
        return this.endName;
    }

    public void setEndName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.endName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTMaskedElseWhereStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 9;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTElse;
        case 2:  return this.hiddenTElsewhere;
        case 3:  return this.hiddenTWhere;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.maskExpr;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.endName;
        case 8:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hiddenTElse = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hiddenTElsewhere = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.hiddenTWhere = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.maskExpr = (ASTExprNode)value;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 7:  this.endName = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 8:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

