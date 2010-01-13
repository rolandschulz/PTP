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
public class ASTCoarraySpecNode extends ASTNode
{
    IASTListNode<ASTDeferredCoshapeSpecListNode> deferredCoshapeSpecList; // in ASTCoarraySpecNode
    ASTExplicitCoshapeSpecNode explicitCoshapeSpec; // in ASTCoarraySpecNode

    public IASTListNode<ASTDeferredCoshapeSpecListNode> getDeferredCoshapeSpecList()
    {
        return this.deferredCoshapeSpecList;
    }

    public void setDeferredCoshapeSpecList(IASTListNode<ASTDeferredCoshapeSpecListNode> newValue)
    {
        this.deferredCoshapeSpecList = newValue;
    }


    public ASTExplicitCoshapeSpecNode getExplicitCoshapeSpec()
    {
        return this.explicitCoshapeSpec;
    }

    public void setExplicitCoshapeSpec(ASTExplicitCoshapeSpecNode newValue)
    {
        this.explicitCoshapeSpec = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCoarraySpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 2;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.deferredCoshapeSpecList;
        case 1:  return this.explicitCoshapeSpec;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.deferredCoshapeSpecList = (IASTListNode<ASTDeferredCoshapeSpecListNode>)value; return;
        case 1:  this.explicitCoshapeSpec = (ASTExplicitCoshapeSpecNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

