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

public class ASTEquivalenceSetNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTEquivalenceSetNode
    ASTVariableNode variable; // in ASTEquivalenceSetNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTEquivalenceSetNode
    IASTListNode<ASTEquivalenceObjectListNode> equivalentObjects; // in ASTEquivalenceSetNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTEquivalenceSetNode

    public ASTVariableNode getVariable()
    {
        return this.variable;
    }

    public void setVariable(ASTVariableNode newValue)
    {
        this.variable = newValue;
    }


    public IASTListNode<ASTEquivalenceObjectListNode> getEquivalentObjects()
    {
        return this.equivalentObjects;
    }

    public void setEquivalentObjects(IASTListNode<ASTEquivalenceObjectListNode> newValue)
    {
        this.equivalentObjects = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEquivalenceSetNode(this);
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
        case 1:  return this.variable;
        case 2:  return this.hiddenTComma;
        case 3:  return this.equivalentObjects;
        case 4:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.variable = (ASTVariableNode)value;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.equivalentObjects = (IASTListNode<ASTEquivalenceObjectListNode>)value;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

