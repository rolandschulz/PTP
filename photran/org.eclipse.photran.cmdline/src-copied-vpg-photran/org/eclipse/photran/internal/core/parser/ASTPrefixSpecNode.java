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

public class ASTPrefixSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isPure; // in ASTPrefixSpecNode
    org.eclipse.photran.internal.core.lexer.Token isRecursive; // in ASTPrefixSpecNode
    org.eclipse.photran.internal.core.lexer.Token isElemental; // in ASTPrefixSpecNode
    ASTTypeSpecNode typeSpec; // in ASTPrefixSpecNode

    public boolean isPure()
    {
        return this.isPure != null;
    }

    public void setIsPure(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPure = newValue;
    }


    public boolean isRecursive()
    {
        return this.isRecursive != null;
    }

    public void setIsRecursive(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isRecursive = newValue;
    }


    public boolean isElemental()
    {
        return this.isElemental != null;
    }

    public void setIsElemental(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isElemental = newValue;
    }


    public ASTTypeSpecNode getTypeSpec()
    {
        return this.typeSpec;
    }

    public void setTypeSpec(ASTTypeSpecNode newValue)
    {
        this.typeSpec = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTPrefixSpecNode(this);
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
        case 0:  return this.isPure;
        case 1:  return this.isRecursive;
        case 2:  return this.isElemental;
        case 3:  return this.typeSpec;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isPure = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.isRecursive = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.isElemental = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.typeSpec = (ASTTypeSpecNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

