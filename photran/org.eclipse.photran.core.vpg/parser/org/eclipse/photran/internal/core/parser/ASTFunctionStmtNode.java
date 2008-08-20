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

public class ASTFunctionStmtNode extends ASTNodeWithErrorRecoverySymbols
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTFunctionStmtNode
    IASTListNode<ASTPrefixSpecNode> prefixSpecList; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFunction; // in ASTFunctionStmtNode
    ASTFunctionNameNode functionName; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTFunctionStmtNode
    IASTListNode<ASTFunctionParNode> functionPars; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hasResultClause; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen2; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen2; // in ASTFunctionStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTFunctionStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTPrefixSpecNode> getPrefixSpecList()
    {
        return this.prefixSpecList;
    }

    public void setPrefixSpecList(IASTListNode<ASTPrefixSpecNode> newValue)
    {
        this.prefixSpecList = newValue;
    }


    public ASTFunctionNameNode getFunctionName()
    {
        return this.functionName;
    }

    public void setFunctionName(ASTFunctionNameNode newValue)
    {
        this.functionName = newValue;
    }


    public IASTListNode<ASTFunctionParNode> getFunctionPars()
    {
        return this.functionPars;
    }

    public void setFunctionPars(IASTListNode<ASTFunctionParNode> newValue)
    {
        this.functionPars = newValue;
    }


    public boolean hasResultClause()
    {
        return this.hasResultClause != null;
    }

    public void setHasResultClause(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasResultClause = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFunctionStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 12;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.prefixSpecList;
        case 2:  return this.hiddenTFunction;
        case 3:  return this.functionName;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.functionPars;
        case 6:  return this.hiddenTRparen;
        case 7:  return this.hasResultClause;
        case 8:  return this.hiddenTLparen2;
        case 9:  return this.name;
        case 10: return this.hiddenTRparen2;
        case 11: return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.prefixSpecList = (IASTListNode<ASTPrefixSpecNode>)value; return;
        case 2:  this.hiddenTFunction = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.functionName = (ASTFunctionNameNode)value; return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.functionPars = (IASTListNode<ASTFunctionParNode>)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hasResultClause = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.hiddenTLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.hiddenTRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 11: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

