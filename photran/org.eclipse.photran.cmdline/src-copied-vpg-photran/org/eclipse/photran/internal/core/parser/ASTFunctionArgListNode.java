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

public class ASTFunctionArgListNode extends ASTNode
{
    IASTListNode<ASTSectionSubscriptNode> sectionSubscriptList; // in ASTFunctionArgListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTFunctionArgListNode
    ASTFunctionArgNode functionArg; // in ASTFunctionArgListNode

    public IASTListNode<ASTSectionSubscriptNode> getSectionSubscriptList()
    {
        return this.sectionSubscriptList;
    }

    public void setSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.sectionSubscriptList = newValue;
    }


    public ASTFunctionArgNode getFunctionArg()
    {
        return this.functionArg;
    }

    public void setFunctionArg(ASTFunctionArgNode newValue)
    {
        this.functionArg = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFunctionArgListNode(this);
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
        case 0:  return this.sectionSubscriptList;
        case 1:  return this.hiddenTComma;
        case 2:  return this.functionArg;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.sectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 1:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.functionArg = (ASTFunctionArgNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

