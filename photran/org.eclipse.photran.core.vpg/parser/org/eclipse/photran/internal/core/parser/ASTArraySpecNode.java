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
public class ASTArraySpecNode extends ASTNode
{
    IASTListNode<ASTAssumedShapeSpecListNode> assumedShapeSpecList; // in ASTArraySpecNode
    ASTAssumedSizeSpecNode assumedSizeSpec; // in ASTArraySpecNode
    IASTListNode<ASTExplicitShapeSpecNode> explicitShapeSpecList; // in ASTArraySpecNode
    IASTListNode<ASTDeferredShapeSpecListNode> deferredShapeSpecList; // in ASTArraySpecNode

    public IASTListNode<ASTAssumedShapeSpecListNode> getAssumedShapeSpecList()
    {
        return this.assumedShapeSpecList;
    }

    public void setAssumedShapeSpecList(IASTListNode<ASTAssumedShapeSpecListNode> newValue)
    {
        this.assumedShapeSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTAssumedSizeSpecNode getAssumedSizeSpec()
    {
        return this.assumedSizeSpec;
    }

    public void setAssumedSizeSpec(ASTAssumedSizeSpecNode newValue)
    {
        this.assumedSizeSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTExplicitShapeSpecNode> getExplicitShapeSpecList()
    {
        return this.explicitShapeSpecList;
    }

    public void setExplicitShapeSpecList(IASTListNode<ASTExplicitShapeSpecNode> newValue)
    {
        this.explicitShapeSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTDeferredShapeSpecListNode> getDeferredShapeSpecList()
    {
        return this.deferredShapeSpecList;
    }

    public void setDeferredShapeSpecList(IASTListNode<ASTDeferredShapeSpecListNode> newValue)
    {
        this.deferredShapeSpecList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTArraySpecNode(this);
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
        case 0:  return this.assumedShapeSpecList;
        case 1:  return this.assumedSizeSpec;
        case 2:  return this.explicitShapeSpecList;
        case 3:  return this.deferredShapeSpecList;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.assumedShapeSpecList = (IASTListNode<ASTAssumedShapeSpecListNode>)value; if (value != null) value.setParent(this); return;
        case 1:  this.assumedSizeSpec = (ASTAssumedSizeSpecNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.explicitShapeSpecList = (IASTListNode<ASTExplicitShapeSpecNode>)value; if (value != null) value.setParent(this); return;
        case 3:  this.deferredShapeSpecList = (IASTListNode<ASTDeferredShapeSpecListNode>)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

