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
public class ASTComplexConstNode extends ASTNode implements IUnsignedArithmeticConst
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTComplexConstNode
    IExpr realPart; // in ASTComplexConstNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTComplexConstNode
    IExpr complexPart; // in ASTComplexConstNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTComplexConstNode

    public IExpr getRealPart()
    {
        return this.realPart;
    }

    public void setRealPart(IExpr newValue)
    {
        this.realPart = newValue;
    }


    public IExpr getComplexPart()
    {
        return this.complexPart;
    }

    public void setComplexPart(IExpr newValue)
    {
        this.complexPart = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTComplexConstNode(this);
        visitor.visitIUnsignedArithmeticConst(this);
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
        case 1:  return this.realPart;
        case 2:  return this.hiddenTComma;
        case 3:  return this.complexPart;
        case 4:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.realPart = (IExpr)value; return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.complexPart = (IExpr)value; return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

