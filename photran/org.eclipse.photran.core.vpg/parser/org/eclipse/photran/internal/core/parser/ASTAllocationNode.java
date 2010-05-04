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
public class ASTAllocationNode extends ASTNode
{
    IASTListNode<ASTAllocateObjectNode> allocateObject; // in ASTAllocationNode
    org.eclipse.photran.internal.core.lexer.Token hasAllocatedShape; // in ASTAllocationNode
    IASTListNode<ASTSectionSubscriptNode> sectionSubscriptList; // in ASTAllocationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAllocationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTAllocationNode
    ASTAllocateCoarraySpecNode allocateCoarraySpec; // in ASTAllocationNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTAllocationNode

    public IASTListNode<ASTAllocateObjectNode> getAllocateObject()
    {
        return this.allocateObject;
    }

    public void setAllocateObject(IASTListNode<ASTAllocateObjectNode> newValue)
    {
        this.allocateObject = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasAllocatedShape()
    {
        return this.hasAllocatedShape != null;
    }

    public void setHasAllocatedShape(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasAllocatedShape = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSectionSubscriptNode> getSectionSubscriptList()
    {
        return this.sectionSubscriptList;
    }

    public void setSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.sectionSubscriptList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTAllocateCoarraySpecNode getAllocateCoarraySpec()
    {
        return this.allocateCoarraySpec;
    }

    public void setAllocateCoarraySpec(ASTAllocateCoarraySpecNode newValue)
    {
        this.allocateCoarraySpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAllocationNode(this);
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
        case 0:  return this.allocateObject;
        case 1:  return this.hasAllocatedShape;
        case 2:  return this.sectionSubscriptList;
        case 3:  return this.hiddenTRparen;
        case 4:  return this.hiddenTLbracket;
        case 5:  return this.allocateCoarraySpec;
        case 6:  return this.hiddenTRbracket;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.allocateObject = (IASTListNode<ASTAllocateObjectNode>)value; if (value != null) value.setParent(this); return;
        case 1:  this.hasAllocatedShape = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.sectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.allocateCoarraySpec = (ASTAllocateCoarraySpecNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

