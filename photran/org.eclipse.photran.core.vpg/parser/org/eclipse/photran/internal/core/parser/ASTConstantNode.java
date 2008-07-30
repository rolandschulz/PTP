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

public class ASTConstantNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasPlus; // in ASTConstantNode
    ASTNamedConstantUseNode namedConstantKind; // in ASTConstantNode
    ASTStructureConstructorNode structureConstructor; // in ASTConstantNode
    ASTBozLiteralConstantNode bozLiteralConstant; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hasIntKind; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUnderscore; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hollerithConst; // in ASTConstantNode
    org.eclipse.photran.internal.core.lexer.Token hasMinus; // in ASTConstantNode
    ASTUnsignedArithmeticConstantNode unsignedArithmeticConstant; // in ASTConstantNode
    ASTLogicalConstantNode logicalConst; // in ASTConstantNode
    ASTNamedConstantUseNode name; // in ASTConstantNode

    public boolean hasPlus()
    {
        return this.hasPlus != null;
    }

    public void setHasPlus(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPlus = newValue;
    }


    public ASTNamedConstantUseNode getNamedConstantKind()
    {
        return this.namedConstantKind;
    }

    public void setNamedConstantKind(ASTNamedConstantUseNode newValue)
    {
        this.namedConstantKind = newValue;
    }


    public ASTStructureConstructorNode getStructureConstructor()
    {
        return this.structureConstructor;
    }

    public void setStructureConstructor(ASTStructureConstructorNode newValue)
    {
        this.structureConstructor = newValue;
    }


    public ASTBozLiteralConstantNode getBozLiteralConstant()
    {
        return this.bozLiteralConstant;
    }

    public void setBozLiteralConstant(ASTBozLiteralConstantNode newValue)
    {
        this.bozLiteralConstant = newValue;
    }


    public boolean hasIntKind()
    {
        return this.hasIntKind != null;
    }

    public void setHasIntKind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasIntKind = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getHollerithConst()
    {
        return this.hollerithConst;
    }

    public void setHollerithConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hollerithConst = newValue;
    }


    public boolean hasMinus()
    {
        return this.hasMinus != null;
    }

    public void setHasMinus(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasMinus = newValue;
    }


    public ASTUnsignedArithmeticConstantNode getUnsignedArithmeticConstant()
    {
        return this.unsignedArithmeticConstant;
    }

    public void setUnsignedArithmeticConstant(ASTUnsignedArithmeticConstantNode newValue)
    {
        this.unsignedArithmeticConstant = newValue;
    }


    public ASTLogicalConstantNode getLogicalConst()
    {
        return this.logicalConst;
    }

    public void setLogicalConst(ASTLogicalConstantNode newValue)
    {
        this.logicalConst = newValue;
    }


    public ASTNamedConstantUseNode getName()
    {
        return this.name;
    }

    public void setName(ASTNamedConstantUseNode newValue)
    {
        this.name = newValue;
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
        case 1:  return this.namedConstantKind;
        case 2:  return this.structureConstructor;
        case 3:  return this.bozLiteralConstant;
        case 4:  return this.hasIntKind;
        case 5:  return this.hiddenTUnderscore;
        case 6:  return this.stringConst;
        case 7:  return this.hollerithConst;
        case 8:  return this.hasMinus;
        case 9:  return this.unsignedArithmeticConstant;
        case 10: return this.logicalConst;
        case 11: return this.name;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasPlus = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.namedConstantKind = (ASTNamedConstantUseNode)value;
        case 2:  this.structureConstructor = (ASTStructureConstructorNode)value;
        case 3:  this.bozLiteralConstant = (ASTBozLiteralConstantNode)value;
        case 4:  this.hasIntKind = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.hiddenTUnderscore = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 7:  this.hollerithConst = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 8:  this.hasMinus = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 9:  this.unsignedArithmeticConstant = (ASTUnsignedArithmeticConstantNode)value;
        case 10: this.logicalConst = (ASTLogicalConstantNode)value;
        case 11: this.name = (ASTNamedConstantUseNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

