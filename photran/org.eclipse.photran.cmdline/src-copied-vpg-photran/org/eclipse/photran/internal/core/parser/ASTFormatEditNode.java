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

public class ASTFormatEditNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token pConst; // in ASTFormatEditNode
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTFormatEditNode
    ASTEditElementNode editElement; // in ASTFormatEditNode
    org.eclipse.photran.internal.core.lexer.Token hexConst; // in ASTFormatEditNode

    public org.eclipse.photran.internal.core.lexer.Token getPConst()
    {
        return this.pConst;
    }

    public void setPConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.pConst = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getIntConst()
    {
        return this.intConst;
    }

    public void setIntConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intConst = newValue;
    }


    public ASTEditElementNode getEditElement()
    {
        return this.editElement;
    }

    public void setEditElement(ASTEditElementNode newValue)
    {
        this.editElement = newValue;
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
        visitor.visitASTFormatEditNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.pConst;
        case 1:  return this.intConst;
        case 2:  return this.editElement;
        case 3:  return this.hexConst;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.pConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.editElement = (ASTEditElementNode)value; return;
        case 3:  this.hexConst = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

