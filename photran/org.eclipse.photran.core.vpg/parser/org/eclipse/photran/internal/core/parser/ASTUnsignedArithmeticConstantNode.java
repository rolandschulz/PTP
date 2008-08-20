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

public class ASTUnsignedArithmeticConstantNode extends ASTNode
{
    ASTComplexConstNode complexConst; // in ASTUnsignedArithmeticConstantNode
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTUnsignedArithmeticConstantNode
    org.eclipse.photran.internal.core.lexer.Token dblConst; // in ASTUnsignedArithmeticConstantNode
    org.eclipse.photran.internal.core.lexer.Token realConst; // in ASTUnsignedArithmeticConstantNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUnderscore; // in ASTUnsignedArithmeticConstantNode
    org.eclipse.photran.internal.core.lexer.Token intKind; // in ASTUnsignedArithmeticConstantNode
    ASTNamedConstantUseNode namedConstKind; // in ASTUnsignedArithmeticConstantNode

    public ASTComplexConstNode getComplexConst()
    {
        return this.complexConst;
    }

    public void setComplexConst(ASTComplexConstNode newValue)
    {
        this.complexConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getIntConst()
    {
        return this.intConst;
    }

    public void setIntConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getDblConst()
    {
        return this.dblConst;
    }

    public void setDblConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.dblConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getRealConst()
    {
        return this.realConst;
    }

    public void setRealConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.realConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getIntKind()
    {
        return this.intKind;
    }

    public void setIntKind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intKind = newValue;
    }


    public ASTNamedConstantUseNode getNamedConstKind()
    {
        return this.namedConstKind;
    }

    public void setNamedConstKind(ASTNamedConstantUseNode newValue)
    {
        this.namedConstKind = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUnsignedArithmeticConstantNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 7;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.complexConst;
        case 1:  return this.intConst;
        case 2:  return this.dblConst;
        case 3:  return this.realConst;
        case 4:  return this.hiddenTUnderscore;
        case 5:  return this.intKind;
        case 6:  return this.namedConstKind;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.complexConst = (ASTComplexConstNode)value; return;
        case 1:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.dblConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.realConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTUnderscore = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.intKind = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.namedConstKind = (ASTNamedConstantUseNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

