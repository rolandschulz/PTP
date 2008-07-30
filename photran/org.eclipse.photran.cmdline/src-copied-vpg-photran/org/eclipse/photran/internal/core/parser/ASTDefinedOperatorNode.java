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

public class ASTDefinedOperatorNode extends ASTNode
{
    ASTOperatorNode orOp; // in ASTDefinedOperatorNode
    ASTOperatorNode multOp; // in ASTDefinedOperatorNode
    ASTOperatorNode equivOp; // in ASTDefinedOperatorNode
    ASTOperatorNode andOp; // in ASTDefinedOperatorNode
    org.eclipse.photran.internal.core.lexer.Token customDefinedOp; // in ASTDefinedOperatorNode
    ASTOperatorNode relOp; // in ASTDefinedOperatorNode
    ASTOperatorNode powerOp; // in ASTDefinedOperatorNode
    ASTOperatorNode notOp; // in ASTDefinedOperatorNode
    ASTOperatorNode concatOp; // in ASTDefinedOperatorNode
    ASTOperatorNode addOp; // in ASTDefinedOperatorNode

    public ASTOperatorNode getOrOp()
    {
        return this.orOp;
    }

    public void setOrOp(ASTOperatorNode newValue)
    {
        this.orOp = newValue;
    }


    public ASTOperatorNode getMultOp()
    {
        return this.multOp;
    }

    public void setMultOp(ASTOperatorNode newValue)
    {
        this.multOp = newValue;
    }


    public ASTOperatorNode getEquivOp()
    {
        return this.equivOp;
    }

    public void setEquivOp(ASTOperatorNode newValue)
    {
        this.equivOp = newValue;
    }


    public ASTOperatorNode getAndOp()
    {
        return this.andOp;
    }

    public void setAndOp(ASTOperatorNode newValue)
    {
        this.andOp = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getCustomDefinedOp()
    {
        return this.customDefinedOp;
    }

    public void setCustomDefinedOp(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.customDefinedOp = newValue;
    }


    public ASTOperatorNode getRelOp()
    {
        return this.relOp;
    }

    public void setRelOp(ASTOperatorNode newValue)
    {
        this.relOp = newValue;
    }


    public ASTOperatorNode getPowerOp()
    {
        return this.powerOp;
    }

    public void setPowerOp(ASTOperatorNode newValue)
    {
        this.powerOp = newValue;
    }


    public ASTOperatorNode getNotOp()
    {
        return this.notOp;
    }

    public void setNotOp(ASTOperatorNode newValue)
    {
        this.notOp = newValue;
    }


    public ASTOperatorNode getConcatOp()
    {
        return this.concatOp;
    }

    public void setConcatOp(ASTOperatorNode newValue)
    {
        this.concatOp = newValue;
    }


    public ASTOperatorNode getAddOp()
    {
        return this.addOp;
    }

    public void setAddOp(ASTOperatorNode newValue)
    {
        this.addOp = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDefinedOperatorNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 10;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.orOp;
        case 1:  return this.multOp;
        case 2:  return this.equivOp;
        case 3:  return this.andOp;
        case 4:  return this.customDefinedOp;
        case 5:  return this.relOp;
        case 6:  return this.powerOp;
        case 7:  return this.notOp;
        case 8:  return this.concatOp;
        case 9:  return this.addOp;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.orOp = (ASTOperatorNode)value;
        case 1:  this.multOp = (ASTOperatorNode)value;
        case 2:  this.equivOp = (ASTOperatorNode)value;
        case 3:  this.andOp = (ASTOperatorNode)value;
        case 4:  this.customDefinedOp = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.relOp = (ASTOperatorNode)value;
        case 6:  this.powerOp = (ASTOperatorNode)value;
        case 7:  this.notOp = (ASTOperatorNode)value;
        case 8:  this.concatOp = (ASTOperatorNode)value;
        case 9:  this.addOp = (ASTOperatorNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

