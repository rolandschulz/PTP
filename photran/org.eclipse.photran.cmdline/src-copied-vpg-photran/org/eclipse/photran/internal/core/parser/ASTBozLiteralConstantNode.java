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

public class ASTBozLiteralConstantNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token binaryConst; // in ASTBozLiteralConstantNode
    org.eclipse.photran.internal.core.lexer.Token octalConst; // in ASTBozLiteralConstantNode
    org.eclipse.photran.internal.core.lexer.Token hexConst; // in ASTBozLiteralConstantNode

    public org.eclipse.photran.internal.core.lexer.Token getBinaryConst()
    {
        return this.binaryConst;
    }

    public void setBinaryConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.binaryConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getOctalConst()
    {
        return this.octalConst;
    }

    public void setOctalConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.octalConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getHexConst()
    {
        return this.hexConst;
    }

    public void setHexConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hexConst = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTBozLiteralConstantNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.binaryConst;
        case 1:  return this.octalConst;
        case 2:  return this.hexConst;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.binaryConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.octalConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hexConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

