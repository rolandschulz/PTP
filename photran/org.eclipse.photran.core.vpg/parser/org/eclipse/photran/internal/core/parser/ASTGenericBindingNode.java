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
public class ASTGenericBindingNode extends ASTNode implements IProcBindingStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTGenericBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTGeneric; // in ASTGenericBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTGenericBindingNode
    ASTAccessSpecNode accessSpec; // in ASTGenericBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTGenericBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTGenericBindingNode
    ASTGenericNameNode genericName; // in ASTGenericBindingNode
    ASTGenericSpecNode genericSpec; // in ASTGenericBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEqgreaterthan; // in ASTGenericBindingNode
    IASTListNode<org.eclipse.photran.internal.core.lexer.Token> bindingNameList; // in ASTGenericBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTGenericBindingNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTAccessSpecNode getAccessSpec()
    {
        return this.accessSpec;
    }

    public void setAccessSpec(ASTAccessSpecNode newValue)
    {
        this.accessSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTGenericNameNode getGenericName()
    {
        return this.genericName;
    }

    public void setGenericName(ASTGenericNameNode newValue)
    {
        this.genericName = newValue;
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


    public IASTListNode<org.eclipse.photran.internal.core.lexer.Token> getBindingNameList()
    {
        return this.bindingNameList;
    }

    public void setBindingNameList(IASTListNode<org.eclipse.photran.internal.core.lexer.Token> newValue)
    {
        this.bindingNameList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTGenericBindingNode(this);
        visitor.visitIProcBindingStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 11;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTGeneric;
        case 2:  return this.hiddenTComma;
        case 3:  return this.accessSpec;
        case 4:  return this.hiddenTColon;
        case 5:  return this.hiddenTColon2;
        case 6:  return this.genericName;
        case 7:  return this.genericSpec;
        case 8:  return this.hiddenTEqgreaterthan;
        case 9:  return this.bindingNameList;
        case 10: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTGeneric = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.accessSpec = (ASTAccessSpecNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.genericName = (ASTGenericNameNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.genericSpec = (ASTGenericSpecNode)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTEqgreaterthan = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.bindingNameList = (IASTListNode<org.eclipse.photran.internal.core.lexer.Token>)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

