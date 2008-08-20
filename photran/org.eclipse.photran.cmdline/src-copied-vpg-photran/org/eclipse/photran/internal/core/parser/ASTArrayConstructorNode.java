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

public class ASTArrayConstructorNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparenslash; // in ASTArrayConstructorNode
    IASTListNode<ASTAcValueNode> acValueList; // in ASTArrayConstructorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlashrparen; // in ASTArrayConstructorNode

    public IASTListNode<ASTAcValueNode> getAcValueList()
    {
        return this.acValueList;
    }

    public void setAcValueList(IASTListNode<ASTAcValueNode> newValue)
    {
        this.acValueList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTArrayConstructorNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTLparenslash;
        case 1:  return this.acValueList;
        case 2:  return this.hiddenTSlashrparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparenslash = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.acValueList = (IASTListNode<ASTAcValueNode>)value; return;
        case 2:  this.hiddenTSlashrparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

