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

public class ASTSubroutineArgNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTSubroutineArgNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEquals; // in ASTSubroutineArgNode
    IExpr expr; // in ASTSubroutineArgNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTSubroutineArgNode
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTSubroutineArgNode
    org.eclipse.photran.internal.core.lexer.Token hollerith; // in ASTSubroutineArgNode

    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
    }


    public IExpr getExpr()
    {
        return this.expr;
    }

    public void setExpr(IExpr newValue)
    {
        this.expr = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getHollerith()
    {
        return this.hollerith;
    }

    public void setHollerith(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hollerith = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSubroutineArgNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.name;
        case 1:  return this.hiddenTEquals;
        case 2:  return this.expr;
        case 3:  return this.hiddenTAsterisk;
        case 4:  return this.label;
        case 5:  return this.hollerith;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTEquals = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.expr = (IExpr)value; return;
        case 3:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hollerith = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

