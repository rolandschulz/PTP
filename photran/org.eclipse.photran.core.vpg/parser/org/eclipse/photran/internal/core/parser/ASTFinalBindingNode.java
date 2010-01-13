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
public class ASTFinalBindingNode extends ASTNode implements IProcBindingStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTFinalBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFinal; // in ASTFinalBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTFinalBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTFinalBindingNode
    IASTListNode<org.eclipse.photran.internal.core.lexer.Token> finalSubroutineNameList; // in ASTFinalBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTFinalBindingNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<org.eclipse.photran.internal.core.lexer.Token> getFinalSubroutineNameList()
    {
        return this.finalSubroutineNameList;
    }

    public void setFinalSubroutineNameList(IASTListNode<org.eclipse.photran.internal.core.lexer.Token> newValue)
    {
        this.finalSubroutineNameList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFinalBindingNode(this);
        visitor.visitIProcBindingStmt(this);
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
        case 0:  return this.label;
        case 1:  return this.hiddenTFinal;
        case 2:  return this.hiddenTColon;
        case 3:  return this.hiddenTColon2;
        case 4:  return this.finalSubroutineNameList;
        case 5:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTFinal = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.finalSubroutineNameList = (IASTListNode<org.eclipse.photran.internal.core.lexer.Token>)value; return;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

