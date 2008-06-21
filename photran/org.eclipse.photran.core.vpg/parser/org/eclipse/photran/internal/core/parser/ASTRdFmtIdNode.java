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

public class ASTRdFmtIdNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasLeOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasLtOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasOrOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasPlusOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasPowerOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasEqOp; // in ASTRdFmtIdNode
    ASTCOperandNode primary1; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasGeOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasEqEqOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token definedUnaryOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasTimesOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token definedBinaryOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasDivideOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasAndOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasMinusOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasNeqvOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasNotOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasEqvOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTRdFmtIdNode
    ASTUFExprNode formatIdExpr; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasConcatOp; // in ASTRdFmtIdNode
    ASTCPrimaryNode primary2; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token formatIsAsterisk; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasSlashEqOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasNeOp; // in ASTRdFmtIdNode
    org.eclipse.photran.internal.core.lexer.Token hasGtOp; // in ASTRdFmtIdNode

    public boolean hasLeOp()
    {
        return this.hasLeOp != null;
    }

    public void setHasLeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasLeOp = newValue;
    }


    public boolean hasLtOp()
    {
        return this.hasLtOp != null;
    }

    public void setHasLtOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasLtOp = newValue;
    }


    public boolean hasOrOp()
    {
        return this.hasOrOp != null;
    }

    public void setHasOrOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasOrOp = newValue;
    }


    public boolean hasPlusOp()
    {
        return this.hasPlusOp != null;
    }

    public void setHasPlusOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPlusOp = newValue;
    }


    public boolean hasPowerOp()
    {
        return this.hasPowerOp != null;
    }

    public void setHasPowerOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPowerOp = newValue;
    }


    public boolean hasEqOp()
    {
        return this.hasEqOp != null;
    }

    public void setHasEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqOp = newValue;
    }


    public ASTCOperandNode getPrimary1()
    {
        return this.primary1;
    }

    public void setPrimary1(ASTCOperandNode newValue)
    {
        this.primary1 = newValue;
    }


    public boolean hasGeOp()
    {
        return this.hasGeOp != null;
    }

    public void setHasGeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasGeOp = newValue;
    }


    public boolean hasEqEqOp()
    {
        return this.hasEqEqOp != null;
    }

    public void setHasEqEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqEqOp = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getDefinedUnaryOp()
    {
        return this.definedUnaryOp;
    }

    public void setDefinedUnaryOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.definedUnaryOp = newValue;
    }


    public boolean hasTimesOp()
    {
        return this.hasTimesOp != null;
    }

    public void setHasTimesOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasTimesOp = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getDefinedBinaryOp()
    {
        return this.definedBinaryOp;
    }

    public void setDefinedBinaryOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.definedBinaryOp = newValue;
    }


    public boolean hasDivideOp()
    {
        return this.hasDivideOp != null;
    }

    public void setHasDivideOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasDivideOp = newValue;
    }


    public boolean hasAndOp()
    {
        return this.hasAndOp != null;
    }

    public void setHasAndOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasAndOp = newValue;
    }


    public boolean hasMinusOp()
    {
        return this.hasMinusOp != null;
    }

    public void setHasMinusOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasMinusOp = newValue;
    }


    public boolean hasNeqvOp()
    {
        return this.hasNeqvOp != null;
    }

    public void setHasNeqvOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNeqvOp = newValue;
    }


    public boolean hasNotOp()
    {
        return this.hasNotOp != null;
    }

    public void setHasNotOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNotOp = newValue;
    }


    public boolean hasEqvOp()
    {
        return this.hasEqvOp != null;
    }

    public void setHasEqvOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqvOp = newValue;
    }


    public ASTUFExprNode getFormatIdExpr()
    {
        return this.formatIdExpr;
    }

    public void setFormatIdExpr(ASTUFExprNode newValue)
    {
        this.formatIdExpr = newValue;
    }


    public boolean hasConcatOp()
    {
        return this.hasConcatOp != null;
    }

    public void setHasConcatOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasConcatOp = newValue;
    }


    public ASTCPrimaryNode getPrimary2()
    {
        return this.primary2;
    }

    public void setPrimary2(ASTCPrimaryNode newValue)
    {
        this.primary2 = newValue;
    }


    public boolean formatIsAsterisk()
    {
        return this.formatIsAsterisk != null;
    }

    public void setFormatIsAsterisk(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.formatIsAsterisk = newValue;
    }


    public boolean hasSlashEqOp()
    {
        return this.hasSlashEqOp != null;
    }

    public void setHasSlashEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasSlashEqOp = newValue;
    }


    public boolean hasNeOp()
    {
        return this.hasNeOp != null;
    }

    public void setHasNeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNeOp = newValue;
    }


    public boolean hasGtOp()
    {
        return this.hasGtOp != null;
    }

    public void setHasGtOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasGtOp = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTRdFmtIdNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 28;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hasLeOp;
        case 1:  return this.hasLtOp;
        case 2:  return this.hasOrOp;
        case 3:  return this.hasPlusOp;
        case 4:  return this.hasPowerOp;
        case 5:  return this.hasEqOp;
        case 6:  return this.primary1;
        case 7:  return this.hasGeOp;
        case 8:  return this.hasEqEqOp;
        case 9:  return this.definedUnaryOp;
        case 10: return this.hasTimesOp;
        case 11: return this.label;
        case 12: return this.definedBinaryOp;
        case 13: return this.hasDivideOp;
        case 14: return this.hasAndOp;
        case 15: return this.hasMinusOp;
        case 16: return this.hasNeqvOp;
        case 17: return this.hasNotOp;
        case 18: return this.hasEqvOp;
        case 19: return this.hiddenTLparen;
        case 20: return this.formatIdExpr;
        case 21: return this.hiddenTRparen;
        case 22: return this.hasConcatOp;
        case 23: return this.primary2;
        case 24: return this.formatIsAsterisk;
        case 25: return this.hasSlashEqOp;
        case 26: return this.hasNeOp;
        case 27: return this.hasGtOp;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasLeOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hasLtOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hasOrOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.hasPlusOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.hasPowerOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.hasEqOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.primary1 = (ASTCOperandNode)value;
        case 7:  this.hasGeOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 8:  this.hasEqEqOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 9:  this.definedUnaryOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 10: this.hasTimesOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 11: this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 12: this.definedBinaryOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 13: this.hasDivideOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 14: this.hasAndOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 15: this.hasMinusOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 16: this.hasNeqvOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 17: this.hasNotOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 18: this.hasEqvOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 19: this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 20: this.formatIdExpr = (ASTUFExprNode)value;
        case 21: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 22: this.hasConcatOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 23: this.primary2 = (ASTCPrimaryNode)value;
        case 24: this.formatIsAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 25: this.hasSlashEqOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 26: this.hasNeOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 27: this.hasGtOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

