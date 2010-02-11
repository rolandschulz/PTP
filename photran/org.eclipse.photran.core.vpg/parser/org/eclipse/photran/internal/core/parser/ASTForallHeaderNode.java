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
public class ASTForallHeaderNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTForallHeaderNode
    ASTForallTripletSpecListNode forallTripletSpecList; // in ASTForallHeaderNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTForallHeaderNode
    ASTScalarMaskExprNode scalarMaskExpr; // in ASTForallHeaderNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTForallHeaderNode

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
        visitor.visitASTForallHeaderNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTLparen;
        case 1:  return this.forallTripletSpecList;
        case 2:  return this.hiddenTComma;
        case 3:  return this.scalarMaskExpr;
        case 4:  return this.hiddenTRparen;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.forallTripletSpecList = (ASTForallTripletSpecListNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.scalarMaskExpr = (ASTScalarMaskExprNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

