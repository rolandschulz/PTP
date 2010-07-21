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
public class ASTOperatorNode extends ASTNode implements IDefinedOperator
{
    org.eclipse.photran.internal.core.lexer.Token hasPlusOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasPowerOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasNotOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token definedUnaryOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasNeOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasEqOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasSlashEqOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasNeqvOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasConcatOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasGtOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasGeOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasTimesOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasLtOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasMinusOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasLeOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasEqEqOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token customDefinedOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasAndOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasDivideOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token definedBinaryOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasOrOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasEqvOp; // in ASTOperatorNode

    public boolean hasPlusOp()
    {
        return this.hasPlusOp != null;
    }

    public void setHasPlusOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPlusOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasPowerOp()
    {
        return this.hasPowerOp != null;
    }

    public void setHasPowerOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPowerOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasNotOp()
    {
        return this.hasNotOp != null;
    }

    public void setHasNotOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNotOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getDefinedUnaryOp()
    {
        return this.definedUnaryOp;
    }

    public void setDefinedUnaryOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.definedUnaryOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasNeOp()
    {
        return this.hasNeOp != null;
    }

    public void setHasNeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNeOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasEqOp()
    {
        return this.hasEqOp != null;
    }

    public void setHasEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasSlashEqOp()
    {
        return this.hasSlashEqOp != null;
    }

    public void setHasSlashEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasSlashEqOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasNeqvOp()
    {
        return this.hasNeqvOp != null;
    }

    public void setHasNeqvOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNeqvOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasConcatOp()
    {
        return this.hasConcatOp != null;
    }

    public void setHasConcatOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasConcatOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasGtOp()
    {
        return this.hasGtOp != null;
    }

    public void setHasGtOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasGtOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasGeOp()
    {
        return this.hasGeOp != null;
    }

    public void setHasGeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasGeOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasTimesOp()
    {
        return this.hasTimesOp != null;
    }

    public void setHasTimesOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasTimesOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasLtOp()
    {
        return this.hasLtOp != null;
    }

    public void setHasLtOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasLtOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasMinusOp()
    {
        return this.hasMinusOp != null;
    }

    public void setHasMinusOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasMinusOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasLeOp()
    {
        return this.hasLeOp != null;
    }

    public void setHasLeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasLeOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasEqEqOp()
    {
        return this.hasEqEqOp != null;
    }

    public void setHasEqEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqEqOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getCustomDefinedOp()
    {
        return this.customDefinedOp;
    }

    public void setCustomDefinedOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.customDefinedOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasAndOp()
    {
        return this.hasAndOp != null;
    }

    public void setHasAndOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasAndOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasDivideOp()
    {
        return this.hasDivideOp != null;
    }

    public void setHasDivideOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasDivideOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getDefinedBinaryOp()
    {
        return this.definedBinaryOp;
    }

    public void setDefinedBinaryOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.definedBinaryOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasOrOp()
    {
        return this.hasOrOp != null;
    }

    public void setHasOrOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasOrOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasEqvOp()
    {
        return this.hasEqvOp != null;
    }

    public void setHasEqvOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqvOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTOperatorNode(this);
        visitor.visitIDefinedOperator(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 22;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hasPlusOp;
        case 1:  return this.hasPowerOp;
        case 2:  return this.hasNotOp;
        case 3:  return this.definedUnaryOp;
        case 4:  return this.hasNeOp;
        case 5:  return this.hasEqOp;
        case 6:  return this.hasSlashEqOp;
        case 7:  return this.hasNeqvOp;
        case 8:  return this.hasConcatOp;
        case 9:  return this.hasGtOp;
        case 10: return this.hasGeOp;
        case 11: return this.hasTimesOp;
        case 12: return this.hasLtOp;
        case 13: return this.hasMinusOp;
        case 14: return this.hasLeOp;
        case 15: return this.hasEqEqOp;
        case 16: return this.customDefinedOp;
        case 17: return this.hasAndOp;
        case 18: return this.hasDivideOp;
        case 19: return this.definedBinaryOp;
        case 20: return this.hasOrOp;
        case 21: return this.hasEqvOp;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasPlusOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hasPowerOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hasNotOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.definedUnaryOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hasNeOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hasEqOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.hasSlashEqOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hasNeqvOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hasConcatOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hasGtOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hasGeOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.hasTimesOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.hasLtOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 13: this.hasMinusOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 14: this.hasLeOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 15: this.hasEqEqOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 16: this.customDefinedOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 17: this.hasAndOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 18: this.hasDivideOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 19: this.definedBinaryOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 20: this.hasOrOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 21: this.hasEqvOp = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

