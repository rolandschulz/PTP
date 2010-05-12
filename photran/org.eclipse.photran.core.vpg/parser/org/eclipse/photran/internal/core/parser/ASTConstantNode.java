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
public class ASTConstantNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasPlus; // in ASTConstantNode
    ASTLogicalConstNode logicalConst; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hasIntKind; // in ASTConstantNode
    ASTNamedConstantUseNode namedConstantKind; // in ASTConstantNode
    ASTNamedConstantUseNode name; // in ASTConstantNode
    ASTBozLiteralConstNode bozLiteralConstant; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hasMinus; // in ASTConstantNode
    IUnsignedArithmeticConst unsignedArithmeticConstant; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hollerithConst; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUnderscore; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTConstantNode
    ASTStructureConstructorNode structureConstructor; // in ASTConstantNode

    public boolean hasPlus()
    {
        return this.hasPlus != null;
    }

    public void setHasPlus(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPlus = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLogicalConstNode getLogicalConst()
    {
        return this.logicalConst;
    }

    public void setLogicalConst(ASTLogicalConstNode newValue)
    {
        this.logicalConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasIntKind()
    {
        return this.hasIntKind != null;
    }

    public void setHasIntKind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasIntKind = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNamedConstantUseNode getNamedConstantKind()
    {
        return this.namedConstantKind;
    }

    public void setNamedConstantKind(ASTNamedConstantUseNode newValue)
    {
        this.namedConstantKind = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNamedConstantUseNode getName()
    {
        return this.name;
    }

    public void setName(ASTNamedConstantUseNode newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTBozLiteralConstNode getBozLiteralConstant()
    {
        return this.bozLiteralConstant;
    }

    public void setBozLiteralConstant(ASTBozLiteralConstNode newValue)
    {
        this.bozLiteralConstant = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasMinus()
    {
        return this.hasMinus != null;
    }

    public void setHasMinus(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasMinus = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IUnsignedArithmeticConst getUnsignedArithmeticConstant()
    {
        return this.unsignedArithmeticConstant;
    }

    public void setUnsignedArithmeticConstant(IUnsignedArithmeticConst newValue)
    {
        this.unsignedArithmeticConstant = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getHollerithConst()
    {
        return this.hollerithConst;
    }

    public void setHollerithConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hollerithConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTStructureConstructorNode getStructureConstructor()
    {
        return this.structureConstructor;
    }

    public void setStructureConstructor(ASTStructureConstructorNode newValue)
    {
        this.structureConstructor = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTConstantNode(this);
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
        case 0:  return this.hasPlus;
        case 1:  return this.logicalConst;
        case 2:  return this.hasIntKind;
        case 3:  return this.namedConstantKind;
        case 4:  return this.name;
        case 5:  return this.bozLiteralConstant;
        case 6:  return this.hasMinus;
        case 7:  return this.unsignedArithmeticConstant;
        case 8:  return this.hollerithConst;
        case 9:  return this.hiddenTUnderscore;
        case 10: return this.stringConst;
        case 11: return this.structureConstructor;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasPlus = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.logicalConst = (ASTLogicalConstNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.hasIntKind = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.namedConstantKind = (ASTNamedConstantUseNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.name = (ASTNamedConstantUseNode)value; if (value != null) value.setParent(this); return;
        case 5:  this.bozLiteralConstant = (ASTBozLiteralConstNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hasMinus = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.unsignedArithmeticConstant = (IUnsignedArithmeticConst)value; if (value != null) value.setParent(this); return;
        case 8:  this.hollerithConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTUnderscore = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.structureConstructor = (ASTStructureConstructorNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

