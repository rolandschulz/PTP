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

public class ASTFormatsepNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token slashFormatSep; // in ASTFormatsepNode
    org.eclipse.photran.internal.core.lexer.Token colonFormatSep; // in ASTFormatsepNode

    public boolean slashFormatSep()
    {
        return this.slashFormatSep != null;
    }

    public void setSlashFormatSep(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.slashFormatSep = newValue;
    }


    public boolean colonFormatSep()
    {
        return this.colonFormatSep != null;
    }

    public void setColonFormatSep(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.colonFormatSep = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFormatsepNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 2;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.slashFormatSep;
        case 1:  return this.colonFormatSep;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.slashFormatSep = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.colonFormatSep = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

