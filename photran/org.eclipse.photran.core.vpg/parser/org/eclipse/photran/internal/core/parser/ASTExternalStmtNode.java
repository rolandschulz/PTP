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

public class ASTExternalStmtNode extends ASTNode implements ISpecificationStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTExternalStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTExternal; // in ASTExternalStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTExternalStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTExternalStmtNode
    IASTListNode<ASTExternalNameListNode> externalNameList; // in ASTExternalStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTExternalStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTExternalNameListNode> getExternalNameList()
    {
        return this.externalNameList;
    }

    public void setExternalNameList(IASTListNode<ASTExternalNameListNode> newValue)
    {
        this.externalNameList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTExternalStmtNode(this);
        visitor.visitISpecificationStmt(this);
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
        case 1:  return this.hiddenTExternal;
        case 2:  return this.hiddenTColon;
        case 3:  return this.hiddenTColon2;
        case 4:  return this.externalNameList;
        case 5:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hiddenTExternal = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.externalNameList = (IASTListNode<ASTExternalNameListNode>)value;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

