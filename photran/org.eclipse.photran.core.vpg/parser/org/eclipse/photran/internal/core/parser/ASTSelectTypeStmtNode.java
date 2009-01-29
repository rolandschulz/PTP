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

public class ASTSelectTypeStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSelect; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTType; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token associateName; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEqgreaterthan; // in ASTSelectTypeStmtNode
    ISelector selector; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTSelectTypeStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTSelectTypeStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getAssociateName()
    {
        return this.associateName;
    }

    public void setAssociateName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.associateName = newValue;
    }


    public ISelector getSelector()
    {
        return this.selector;
    }

    public void setSelector(ISelector newValue)
    {
        this.selector = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSelectTypeStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 11;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.name;
        case 2:  return this.hiddenTColon;
        case 3:  return this.hiddenTSelect;
        case 4:  return this.hiddenTType;
        case 5:  return this.hiddenTLparen;
        case 6:  return this.associateName;
        case 7:  return this.hiddenTEqgreaterthan;
        case 8:  return this.selector;
        case 9:  return this.hiddenTRparen;
        case 10: return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTSelect = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTType = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.associateName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTEqgreaterthan = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.selector = (ISelector)value; return;
        case 9:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

