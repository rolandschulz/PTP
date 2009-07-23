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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTRenameNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isOperator; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token newName; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEqgreaterthan; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTOperator; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen2; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTRenameNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen2; // in ASTRenameNode

    public boolean isOperator()
    {
        return this.isOperator != null;
    }

    public void setIsOperator(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isOperator = newValue;
    }


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
        return 9;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isOperator;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.newName;
        case 3:  return this.hiddenTRparen;
        case 4:  return this.hiddenTEqgreaterthan;
        case 5:  return this.hiddenTOperator;
        case 6:  return this.hiddenTLparen2;
        case 7:  return this.name;
        case 8:  return this.hiddenTRparen2;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isOperator = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.newName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTEqgreaterthan = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTOperator = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.hiddenTLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.hiddenTRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

