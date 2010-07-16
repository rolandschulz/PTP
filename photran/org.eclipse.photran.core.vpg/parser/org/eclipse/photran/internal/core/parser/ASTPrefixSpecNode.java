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
public class ASTPrefixSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isImpure; // in ASTPrefixSpecNode
    org.eclipse.photran.internal.core.lexer.Token isRecursive; // in ASTPrefixSpecNode
    org.eclipse.photran.internal.core.lexer.Token isElemental; // in ASTPrefixSpecNode
    ASTTypeSpecNode typeSpec; // in ASTPrefixSpecNode
    org.eclipse.photran.internal.core.lexer.Token isPure; // in ASTPrefixSpecNode
    org.eclipse.photran.internal.core.lexer.Token isModule; // in ASTPrefixSpecNode

    public boolean isImpure()
    {
        return this.isImpure != null;
    }

    public void setIsImpure(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isImpure = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isRecursive()
    {
        return this.isRecursive != null;
    }

    public void setIsRecursive(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isRecursive = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isElemental()
    {
        return this.isElemental != null;
    }

    public void setIsElemental(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isElemental = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTTypeSpecNode getTypeSpec()
    {
        return this.typeSpec;
    }

    public void setTypeSpec(ASTTypeSpecNode newValue)
    {
        this.typeSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isPure()
    {
        return this.isPure != null;
    }

    public void setIsPure(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPure = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isModule()
    {
        return this.isModule != null;
    }

    public void setIsModule(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isModule = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTPrefixSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isImpure;
        case 1:  return this.isRecursive;
        case 2:  return this.isElemental;
        case 3:  return this.typeSpec;
        case 4:  return this.isPure;
        case 5:  return this.isModule;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isImpure = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.isRecursive = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.isElemental = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.typeSpec = (ASTTypeSpecNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.isPure = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.isModule = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

