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

public class ASTArraySpecNode extends ASTNode
{
    IASTListNode<ASTDeferredShapeSpecListNode> deferredShapeSpecList; // in ASTArraySpecNode
    ASTAssumedSizeSpecNode assumedSizeSpec; // in ASTArraySpecNode
    IASTListNode<ASTAssumedShapeSpecListNode> assumedShapeSpecList; // in ASTArraySpecNode
    IASTListNode<ASTExplicitShapeSpecNode> explicitShapeSpecList; // in ASTArraySpecNode

    public IASTListNode<ASTDeferredShapeSpecListNode> getDeferredShapeSpecList()
    {
        return this.deferredShapeSpecList;
    }

    public void setDeferredShapeSpecList(IASTListNode<ASTDeferredShapeSpecListNode> newValue)
    {
        this.deferredShapeSpecList = newValue;
    }


    public ASTAssumedSizeSpecNode getAssumedSizeSpec()
    {
        return this.assumedSizeSpec;
    }

    public void setAssumedSizeSpec(ASTAssumedSizeSpecNode newValue)
    {
        this.assumedSizeSpec = newValue;
    }


    public IASTListNode<ASTAssumedShapeSpecListNode> getAssumedShapeSpecList()
    {
        return this.assumedShapeSpecList;
    }

    public void setAssumedShapeSpecList(IASTListNode<ASTAssumedShapeSpecListNode> newValue)
    {
        this.assumedShapeSpecList = newValue;
    }


    public IASTListNode<ASTExplicitShapeSpecNode> getExplicitShapeSpecList()
    {
        return this.explicitShapeSpecList;
    }

    public void setExplicitShapeSpecList(IASTListNode<ASTExplicitShapeSpecNode> newValue)
    {
        this.explicitShapeSpecList = newValue;
    }


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
        case 0:  return this.deferredShapeSpecList;
        case 1:  return this.assumedSizeSpec;
        case 2:  return this.assumedShapeSpecList;
        case 3:  return this.explicitShapeSpecList;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.deferredShapeSpecList = (IASTListNode<ASTDeferredShapeSpecListNode>)value; return;
        case 1:  this.assumedSizeSpec = (ASTAssumedSizeSpecNode)value; return;
        case 2:  this.assumedShapeSpecList = (IASTListNode<ASTAssumedShapeSpecListNode>)value; return;
        case 3:  this.explicitShapeSpecList = (IASTListNode<ASTExplicitShapeSpecNode>)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

