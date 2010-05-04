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
public class ASTFormatEditNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hexConst; // in ASTFormatEditNode
    org.eclipse.photran.internal.core.lexer.Token pConst; // in ASTFormatEditNode
    org.eclipse.photran.internal.core.lexer.Token intConst; // in ASTFormatEditNode
    ASTEditElementNode editElement; // in ASTFormatEditNode

    public org.eclipse.photran.internal.core.lexer.Token getHexConst()
    {
        return this.hexConst;
    }

    public void setHexConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hexConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getPConst()
    {
        return this.pConst;
    }

    public void setPConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.pConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getIntConst()
    {
        return this.intConst;
    }

    public void setIntConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.intConst = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEditElementNode getEditElement()
    {
        return this.editElement;
    }

    public void setEditElement(ASTEditElementNode newValue)
    {
        this.editElement = newValue;
        if (newValue != null) newValue.setParent(this);
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
        case 0:  return this.hexConst;
        case 1:  return this.pConst;
        case 2:  return this.intConst;
        case 3:  return this.editElement;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hexConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.pConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.intConst = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.editElement = (ASTEditElementNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

