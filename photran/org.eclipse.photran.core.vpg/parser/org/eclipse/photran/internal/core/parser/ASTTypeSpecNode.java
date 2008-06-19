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

public class ASTTypeSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isLogical; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isDouble; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPrecision; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isReal; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isCharacter; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isComplex; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isDerivedType; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token typeName; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTTypeSpecNode
    ASTCharSelectorNode charSelector; // in ASTTypeSpecNode
    org.eclipse.photran.internal.core.lexer.Token isInteger; // in ASTTypeSpecNode
    ASTKindSelectorNode kindSelector; // in ASTTypeSpecNode

    public boolean isLogical()
    {
        return this.isLogical != null;
    }

    public void setIsLogical(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isLogical = newValue;
    }


    public boolean isDouble()
    {
        return this.isDouble != null;
    }

    public void setIsDouble(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDouble = newValue;
    }


    public boolean isReal()
    {
        return this.isReal != null;
    }

    public void setIsReal(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isReal = newValue;
    }


    public boolean isCharacter()
    {
        return this.isCharacter != null;
    }

    public void setIsCharacter(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isCharacter = newValue;
    }


    public boolean isComplex()
    {
        return this.isComplex != null;
    }

    public void setIsComplex(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isComplex = newValue;
    }


    public boolean isDerivedType()
    {
        return this.isDerivedType != null;
    }

    public void setIsDerivedType(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDerivedType = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getTypeName()
    {
        return this.typeName;
    }

    public void setTypeName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.typeName = newValue;
    }


    public ASTCharSelectorNode getCharSelector()
    {
        return this.charSelector;
    }

    public void setCharSelector(ASTCharSelectorNode newValue)
    {
        this.charSelector = newValue;
    }


    public boolean isInteger()
    {
        return this.isInteger != null;
    }

    public void setIsInteger(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isInteger = newValue;
    }


    public ASTKindSelectorNode getKindSelector()
    {
        return this.kindSelector;
    }

    public void setKindSelector(ASTKindSelectorNode newValue)
    {
        this.kindSelector = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 13;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isLogical;
        case 1:  return this.isDouble;
        case 2:  return this.hiddenTPrecision;
        case 3:  return this.isReal;
        case 4:  return this.isCharacter;
        case 5:  return this.isComplex;
        case 6:  return this.isDerivedType;
        case 7:  return this.hiddenTLparen;
        case 8:  return this.typeName;
        case 9:  return this.hiddenTRparen;
        case 10: return this.charSelector;
        case 11: return this.isInteger;
        case 12: return this.kindSelector;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isLogical = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.isDouble = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hiddenTPrecision = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.isReal = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.isCharacter = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.isComplex = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.isDerivedType = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 7:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 8:  this.typeName = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 9:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 10: this.charSelector = (ASTCharSelectorNode)value;
        case 11: this.isInteger = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 12: this.kindSelector = (ASTKindSelectorNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

