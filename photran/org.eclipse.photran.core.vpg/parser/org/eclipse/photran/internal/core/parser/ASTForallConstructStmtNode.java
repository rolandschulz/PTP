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
public class ASTForallConstructStmtNode extends ASTNodeWithErrorRecoverySymbols
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTForallConstructStmtNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTForallConstructStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTForallConstructStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTForall; // in ASTForallConstructStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTForallConstructStmtNode
    ASTForallTripletSpecListNode forallTripletSpecList; // in ASTForallConstructStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTForallConstructStmtNode
    ASTScalarMaskExprNode scalarMaskExpr; // in ASTForallConstructStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTForallConstructStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTForallConstructStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
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


    public ASTForallTripletSpecListNode getForallTripletSpecList()
    {
        return this.forallTripletSpecList;
    }

    public void setForallTripletSpecList(ASTForallTripletSpecListNode newValue)
    {
        this.forallTripletSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarMaskExprNode getScalarMaskExpr()
    {
        return this.scalarMaskExpr;
    }

    public void setScalarMaskExpr(ASTScalarMaskExprNode newValue)
    {
        this.scalarMaskExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTForallConstructStmtNode(this);
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
        case 1:  return this.name;
        case 2:  return this.hiddenTColon;
        case 3:  return this.hiddenTForall;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.forallTripletSpecList;
        case 6:  return this.hiddenTComma;
        case 7:  return this.scalarMaskExpr;
        case 8:  return this.hiddenTRparen;
        case 9:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTForall = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.forallTripletSpecList = (ASTForallTripletSpecListNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.scalarMaskExpr = (ASTScalarMaskExprNode)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

