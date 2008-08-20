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

public class ASTEditElementNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token floatConst; // in ASTEditElementNode
    org.eclipse.photran.internal.core.lexer.Token hollerith; // in ASTEditElementNode
    org.eclipse.photran.internal.core.lexer.Token identifier; // in ASTEditElementNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTEditElementNode
    IASTListNode<ASTFmtSpecNode> fmtSpec; // in ASTEditElementNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTEditElementNode
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTEditElementNode

    public org.eclipse.photran.internal.core.lexer.Token getFloatConst()
    {
        return this.floatConst;
    }

    public void setFloatConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.floatConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getHollerith()
    {
        return this.hollerith;
    }

    public void setHollerith(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hollerith = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getIdentifier()
    {
        return this.identifier;
    }

    public void setIdentifier(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.identifier = newValue;
    }


    public IASTListNode<ASTFmtSpecNode> getFmtSpec()
    {
        return this.fmtSpec;
    }

    public void setFmtSpec(IASTListNode<ASTFmtSpecNode> newValue)
    {
        this.fmtSpec = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEditElementNode(this);
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
        case 0:  return this.floatConst;
        case 1:  return this.hollerith;
        case 2:  return this.identifier;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.fmtSpec;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.stringConst;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.floatConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hollerith = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.identifier = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.fmtSpec = (IASTListNode<ASTFmtSpecNode>)value; return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

