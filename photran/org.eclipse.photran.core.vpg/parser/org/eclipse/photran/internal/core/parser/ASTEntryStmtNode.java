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
public class ASTEntryStmtNode extends ASTNode implements IBlockDataBodyConstruct, IBodyConstruct, ICaseBodyConstruct, IExecutionPartConstruct, IModuleBodyConstruct, ISpecificationPartConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTEntryStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEntry; // in ASTEntryStmtNode
    org.eclipse.photran.internal.core.lexer.Token entryName; // in ASTEntryStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTEntryStmtNode
    IASTListNode<ASTSubroutineParNode> subroutinePars; // in ASTEntryStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTEntryStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTEntryStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getEntryName()
    {
        return this.entryName;
    }

    public void setEntryName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.entryName = newValue;
    }


    public IASTListNode<ASTSubroutineParNode> getSubroutinePars()
    {
        return this.subroutinePars;
    }

    public void setSubroutinePars(IASTListNode<ASTSubroutineParNode> newValue)
    {
        this.subroutinePars = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEntryStmtNode(this);
        visitor.visitIBlockDataBodyConstruct(this);
        visitor.visitIBodyConstruct(this);
        visitor.visitICaseBodyConstruct(this);
        visitor.visitIExecutionPartConstruct(this);
        visitor.visitIModuleBodyConstruct(this);
        visitor.visitISpecificationPartConstruct(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 7;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTEntry;
        case 2:  return this.entryName;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.subroutinePars;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTEntry = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.entryName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.subroutinePars = (IASTListNode<ASTSubroutineParNode>)value; return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

