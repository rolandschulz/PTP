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

import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings({ "unchecked", "unused" })
public class ASTOnlyNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token newName; // in ASTOnlyNode
    ASTGenericSpecNode genericSpec; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token isOperator; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTOnlyNode
    IDefinedOperator newOp; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token isRenamed; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTOperator; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen2; // in ASTOnlyNode
    IDefinedOperator oldOp; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTOnlyNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen2; // in ASTOnlyNode

    public org.eclipse.photran.internal.core.lexer.Token getNewName()
    {
        return this.newName;
    }

    public void setNewName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.newName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTGenericSpecNode getGenericSpec()
    {
        return this.genericSpec;
    }

    public void setGenericSpec(ASTGenericSpecNode newValue)
    {
        this.genericSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isOperator()
    {
        return this.isOperator != null;
    }

    public void setIsOperator(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isOperator = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IDefinedOperator getNewOp()
    {
        return this.newOp;
    }

    public void setNewOp(IDefinedOperator newValue)
    {
        this.newOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isRenamed()
    {
        return this.isRenamed != null;
    }

    public void setIsRenamed(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isRenamed = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IDefinedOperator getOldOp()
    {
        return this.oldOp;
    }

    public void setOldOp(IDefinedOperator newValue)
    {
        this.oldOp = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTOnlyNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 12;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.newName;
        case 1:  return this.genericSpec;
        case 2:  return this.isOperator;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.newOp;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.isRenamed;
        case 7:  return this.hiddenTOperator;
        case 8:  return this.hiddenTLparen2;
        case 9:  return this.oldOp;
        case 10: return this.name;
        case 11: return this.hiddenTRparen2;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.newName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.genericSpec = (ASTGenericSpecNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.isOperator = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.newOp = (IDefinedOperator)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.isRenamed = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTOperator = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.oldOp = (IDefinedOperator)value; if (value != null) value.setParent(this); return;
        case 10: this.name = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenTRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

