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
public class ASTAllocatedShapeNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasAllocatedShape; // in ASTAllocatedShapeNode
    IASTListNode<ASTSectionSubscriptNode> sectionSubscriptList; // in ASTAllocatedShapeNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAllocatedShapeNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTAllocatedShapeNode
    ASTAllocateCoarraySpecNode allocateCoarraySpec; // in ASTAllocatedShapeNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTAllocatedShapeNode

    public boolean hasAllocatedShape()
    {
        return this.hasAllocatedShape != null;
    }

    public void setHasAllocatedShape(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasAllocatedShape = newValue;
    }


    public IASTListNode<ASTSectionSubscriptNode> getSectionSubscriptList()
    {
        return this.sectionSubscriptList;
    }

    public void setSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.sectionSubscriptList = newValue;
    }


    public ASTAllocateCoarraySpecNode getAllocateCoarraySpec()
    {
        return this.allocateCoarraySpec;
    }

    public void setAllocateCoarraySpec(ASTAllocateCoarraySpecNode newValue)
    {
        this.allocateCoarraySpec = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAllocatedShapeNode(this);
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
        case 0:  return this.hasAllocatedShape;
        case 1:  return this.sectionSubscriptList;
        case 2:  return this.hiddenTRparen;
        case 3:  return this.hiddenTLbracket;
        case 4:  return this.allocateCoarraySpec;
        case 5:  return this.hiddenTRbracket;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasAllocatedShape = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.sectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 2:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.allocateCoarraySpec = (ASTAllocateCoarraySpecNode)value; return;
        case 5:  this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

