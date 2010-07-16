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
public class ASTProcComponentAttrSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isPointer; // in ASTProcComponentAttrSpecNode
    ASTAccessSpecNode accessSpec; // in ASTProcComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isPass; // in ASTProcComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTProcComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token argName; // in ASTProcComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTProcComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isNoPass; // in ASTProcComponentAttrSpecNode

    public boolean isPointer()
    {
        return this.isPointer != null;
    }

    public void setIsPointer(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPointer = newValue;
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


    public boolean isPass()
    {
        return this.isPass != null;
    }

    public void setIsPass(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPass = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getArgName()
    {
        return this.argName;
    }

    public void setArgName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.argName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isNoPass()
    {
        return this.isNoPass != null;
    }

    public void setIsNoPass(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isNoPass = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTProcComponentAttrSpecNode(this);
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
        case 0:  return this.isPointer;
        case 1:  return this.accessSpec;
        case 2:  return this.isPass;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.argName;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.isNoPass;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isPointer = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.accessSpec = (ASTAccessSpecNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.isPass = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.argName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.isNoPass = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

