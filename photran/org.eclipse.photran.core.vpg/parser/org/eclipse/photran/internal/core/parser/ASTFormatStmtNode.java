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
public class ASTFormatStmtNode extends ASTNodeWithErrorRecoverySymbols implements IBlockDataBodyConstruct, IBodyConstruct, ICaseBodyConstruct, IExecutionPartConstruct, IModuleBodyConstruct, ISpecificationPartConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTFormatStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFormat; // in ASTFormatStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTFormatStmtNode
    IASTListNode<ASTFmtSpecNode> fmtSpec; // in ASTFormatStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTFormatStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTFormatStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTFmtSpecNode> getFmtSpec()
    {
        return this.fmtSpec;
    }

    public void setFmtSpec(IASTListNode<ASTFmtSpecNode> newValue)
    {
        this.fmtSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFormatStmtNode(this);
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
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTFormat;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.fmtSpec;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTFormat = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.fmtSpec = (IASTListNode<ASTFmtSpecNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

