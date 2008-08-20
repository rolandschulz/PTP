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

public class ASTRenameNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token newName; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEqgreaterthan; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTRenameNode

    public org.eclipse.photran.internal.core.lexer.Token getNewName()
    {
        return this.newName;
    }

    public void setNewName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.newName = newValue;
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
        visitor.visitASTRenameNode(this);
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
        case 0:  return this.newName;
        case 1:  return this.hiddenTEqgreaterthan;
        case 2:  return this.name;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.newName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTEqgreaterthan = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

