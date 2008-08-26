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

public class ASTOperatorNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasEqOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasNotOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasPlusOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasGeOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasConcatOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasAndOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasEqEqOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasOrOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token definedUnaryOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasLtOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasMinusOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasLeOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasTimesOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasGtOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token definedBinaryOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasPowerOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasEqvOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasNeOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasDivideOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasNeqvOp; // in ASTOperatorNode
    org.eclipse.photran.internal.core.lexer.Token hasSlashEqOp; // in ASTOperatorNode

    public boolean hasEqOp()
    {
        return this.hasEqOp != null;
    }

    public void setHasEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqOp = newValue;
    }


    public boolean hasNotOp()
    {
        return this.hasNotOp != null;
    }

    public void setHasNotOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNotOp = newValue;
    }


    public boolean hasPlusOp()
    {
        return this.hasPlusOp != null;
    }

    public void setHasPlusOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPlusOp = newValue;
    }


    public boolean hasGeOp()
    {
        return this.hasGeOp != null;
    }

    public void setHasGeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasGeOp = newValue;
    }


    public boolean hasConcatOp()
    {
        return this.hasConcatOp != null;
    }

    public void setHasConcatOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasConcatOp = newValue;
    }


    public boolean hasAndOp()
    {
        return this.hasAndOp != null;
    }

    public void setHasAndOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasAndOp = newValue;
    }


    public boolean hasEqEqOp()
    {
        return this.hasEqEqOp != null;
    }

    public void setHasEqEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqEqOp = newValue;
    }


    public boolean hasOrOp()
    {
        return this.hasOrOp != null;
    }

    public void setHasOrOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasOrOp = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getDefinedUnaryOp()
    {
        return this.definedUnaryOp;
    }

    public void setDefinedUnaryOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.definedUnaryOp = newValue;
    }


    public boolean hasLtOp()
    {
        return this.hasLtOp != null;
    }

    public void setHasLtOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasLtOp = newValue;
    }


    public boolean hasMinusOp()
    {
        return this.hasMinusOp != null;
    }

    public void setHasMinusOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasMinusOp = newValue;
    }


    public boolean hasLeOp()
    {
        return this.hasLeOp != null;
    }

    public void setHasLeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasLeOp = newValue;
    }


    public boolean hasTimesOp()
    {
        return this.hasTimesOp != null;
    }

    public void setHasTimesOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasTimesOp = newValue;
    }


    public boolean hasGtOp()
    {
        return this.hasGtOp != null;
    }

    public void setHasGtOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasGtOp = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getDefinedBinaryOp()
    {
        return this.definedBinaryOp;
    }

    public void setDefinedBinaryOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.definedBinaryOp = newValue;
    }


    public boolean hasPowerOp()
    {
        return this.hasPowerOp != null;
    }

    public void setHasPowerOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasPowerOp = newValue;
    }


    public boolean hasEqvOp()
    {
        return this.hasEqvOp != null;
    }

    public void setHasEqvOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasEqvOp = newValue;
    }


    public boolean hasNeOp()
    {
        return this.hasNeOp != null;
    }

    public void setHasNeOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNeOp = newValue;
    }


    public boolean hasDivideOp()
    {
        return this.hasDivideOp != null;
    }

    public void setHasDivideOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasDivideOp = newValue;
    }


    public boolean hasNeqvOp()
    {
        return this.hasNeqvOp != null;
    }

    public void setHasNeqvOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasNeqvOp = newValue;
    }


    public boolean hasSlashEqOp()
    {
        return this.hasSlashEqOp != null;
    }

    public void setHasSlashEqOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasSlashEqOp = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTOperatorNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 21;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hasEqOp;
        case 1:  return this.hasNotOp;
        case 2:  return this.hasPlusOp;
        case 3:  return this.hasGeOp;
        case 4:  return this.hasConcatOp;
        case 5:  return this.hasAndOp;
        case 6:  return this.hasEqEqOp;
        case 7:  return this.hasOrOp;
        case 8:  return this.definedUnaryOp;
        case 9:  return this.hasLtOp;
        case 10: return this.hasMinusOp;
        case 11: return this.hasLeOp;
        case 12: return this.hasTimesOp;
        case 13: return this.hasGtOp;
        case 14: return this.definedBinaryOp;
        case 15: return this.hasPowerOp;
        case 16: return this.hasEqvOp;
        case 17: return this.hasNeOp;
        case 18: return this.hasDivideOp;
        case 19: return this.hasNeqvOp;
        case 20: return this.hasSlashEqOp;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasEqOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hasNotOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hasPlusOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hasGeOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hasConcatOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hasAndOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.hasEqEqOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hasOrOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.definedUnaryOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.hasLtOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.hasMinusOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 11: this.hasLeOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 12: this.hasTimesOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 13: this.hasGtOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 14: this.definedBinaryOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 15: this.hasPowerOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 16: this.hasEqvOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 17: this.hasNeOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 18: this.hasDivideOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 19: this.hasNeqvOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 20: this.hasSlashEqOp = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

