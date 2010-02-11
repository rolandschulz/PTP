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
public class ASTFmtSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTFmtSpecNode
    org.eclipse.photran.internal.core.lexer.Token colonFormatSep; // in ASTFmtSpecNode
    org.eclipse.photran.internal.core.lexer.Token slashFormatSep; // in ASTFmtSpecNode
    ASTFormatEditNode formatEdit; // in ASTFmtSpecNode

    public boolean colonFormatSep()
    {
        return this.colonFormatSep != null;
    }

    public void setColonFormatSep(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.colonFormatSep = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean slashFormatSep()
    {
        return this.slashFormatSep != null;
    }

    public void setSlashFormatSep(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.slashFormatSep = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTFormatEditNode getFormatEdit()
    {
        return this.formatEdit;
    }

    public void setFormatEdit(ASTFormatEditNode newValue)
    {
        this.formatEdit = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFmtSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTComma;
        case 1:  return this.colonFormatSep;
        case 2:  return this.slashFormatSep;
        case 3:  return this.formatEdit;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.colonFormatSep = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.slashFormatSep = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.formatEdit = (ASTFormatEditNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

