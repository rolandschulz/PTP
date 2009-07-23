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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTDataRefNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTDataRefNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTDataRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTDataRefNode
    org.eclipse.photran.internal.core.lexer.Token hasDerivedTypeComponentName; // in ASTDataRefNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTDataRefNode

    public IASTListNode<ASTSectionSubscriptNode> getPrimarySectionSubscriptList()
    {
        return this.primarySectionSubscriptList;
    }

    public void setPrimarySectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.primarySectionSubscriptList = newValue;
    }


    public boolean hasDerivedTypeComponentName()
    {
        return this.hasDerivedTypeComponentName != null;
    }

    public void setHasDerivedTypeComponentName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasDerivedTypeComponentName = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDataRefNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTLparen;
        case 1:  return this.primarySectionSubscriptList;
        case 2:  return this.hiddenTRparen;
        case 3:  return this.hasDerivedTypeComponentName;
        case 4:  return this.name;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 2:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hasDerivedTypeComponentName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

