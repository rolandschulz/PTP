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
public class ASTTypeSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isComplex; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isLogical; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isDerivedType; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isDblComplex; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComplex; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isReal; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isDouble; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPrecision; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isCharacter; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token typeName; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenLParen2; // in ASTTypeSpecNode
    IASTListNode<ASTTypeParamSpecNode> typeParamSpecList; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenHiddenRParen2; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isAsterisk; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isInteger; // in ASTTypeSpecNode
    ASTKindSelectorNode kindSelector; // in ASTTypeSpecNode
    ASTCharSelectorNode charSelector; // in ASTTypeSpecNode

    public boolean isComplex()
    {
        return this.isComplex != null;
    }

    public void setIsComplex(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isComplex = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isLogical()
    {
        return this.isLogical != null;
    }

    public void setIsLogical(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isLogical = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isDerivedType()
    {
        return this.isDerivedType != null;
    }

    public void setIsDerivedType(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDerivedType = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isDblComplex()
    {
        return this.isDblComplex != null;
    }

    public void setIsDblComplex(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDblComplex = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isReal()
    {
        return this.isReal != null;
    }

    public void setIsReal(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isReal = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isDouble()
    {
        return this.isDouble != null;
    }

    public void setIsDouble(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDouble = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isCharacter()
    {
        return this.isCharacter != null;
    }

    public void setIsCharacter(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isCharacter = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getTypeName()
    {
        return this.typeName;
    }

    public void setTypeName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.typeName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTTypeParamSpecNode> getTypeParamSpecList()
    {
        return this.typeParamSpecList;
    }

    public void setTypeParamSpecList(IASTListNode<ASTTypeParamSpecNode> newValue)
    {
        this.typeParamSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isAsterisk()
    {
        return this.isAsterisk != null;
    }

    public void setIsAsterisk(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAsterisk = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isInteger()
    {
        return this.isInteger != null;
    }

    public void setIsInteger(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isInteger = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTKindSelectorNode getKindSelector()
    {
        return this.kindSelector;
    }

    public void setKindSelector(ASTKindSelectorNode newValue)
    {
        this.kindSelector = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCharSelectorNode getCharSelector()
    {
        return this.charSelector;
    }

    public void setCharSelector(ASTCharSelectorNode newValue)
    {
        this.charSelector = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 19;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isComplex;
        case 1:  return this.isLogical;
        case 2:  return this.isDerivedType;
        case 3:  return this.isDblComplex;
        case 4:  return this.hiddenTComplex;
        case 5:  return this.isReal;
        case 6:  return this.isDouble;
        case 7:  return this.hiddenTPrecision;
        case 8:  return this.isCharacter;
        case 9:  return this.hiddenTLparen;
        case 10: return this.typeName;
        case 11: return this.hiddenHiddenLParen2;
        case 12: return this.typeParamSpecList;
        case 13: return this.hiddenHiddenRParen2;
        case 14: return this.isAsterisk;
        case 15: return this.hiddenTRparen;
        case 16: return this.isInteger;
        case 17: return this.kindSelector;
        case 18: return this.charSelector;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isComplex = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.isLogical = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.isDerivedType = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.isDblComplex = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTComplex = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.isReal = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.isDouble = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTPrecision = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.isCharacter = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.typeName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenHiddenLParen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.typeParamSpecList = (IASTListNode<ASTTypeParamSpecNode>)value; if (value != null) value.setParent(this); return;
        case 13: this.hiddenHiddenRParen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 14: this.isAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 15: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 16: this.isInteger = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 17: this.kindSelector = (ASTKindSelectorNode)value; if (value != null) value.setParent(this); return;
        case 18: this.charSelector = (ASTCharSelectorNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

