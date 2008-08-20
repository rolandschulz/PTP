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

public class ASTSFDataRefNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTSFDataRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTSFDataRefNode
    IASTListNode<ASTSectionSubscriptNode> primarySectionSubscriptList; // in ASTSFDataRefNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTSFDataRefNode
    org.eclipse.photran.internal.core.lexer.Token hasDerivedTypeComponentName; // in ASTSFDataRefNode
    ASTNameNode componentName; // in ASTSFDataRefNode

    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
    }


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


    public ASTNameNode getComponentName()
    {
        return this.componentName;
    }

    public void setComponentName(ASTNameNode newValue)
    {
        this.componentName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSFDataRefNode(this);
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
        case 0:  return this.name;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.primarySectionSubscriptList;
        case 3:  return this.hiddenTRparen;
        case 4:  return this.hasDerivedTypeComponentName;
        case 5:  return this.componentName;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.primarySectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hasDerivedTypeComponentName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.componentName = (ASTNameNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

