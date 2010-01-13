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
public class ASTNamelistGroupsNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTNamelistGroupsNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlash; // in ASTNamelistGroupsNode
    org.eclipse.photran.internal.core.lexer.Token namelistGroupName; // in ASTNamelistGroupsNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlash2; // in ASTNamelistGroupsNode
    org.eclipse.photran.internal.core.lexer.Token variableName; // in ASTNamelistGroupsNode

    public org.eclipse.photran.internal.core.lexer.Token getNamelistGroupName()
    {
        return this.namelistGroupName;
    }

    public void setNamelistGroupName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.namelistGroupName = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getVariableName()
    {
        return this.variableName;
    }

    public void setVariableName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.variableName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTNamelistGroupsNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTComma;
        case 1:  return this.hiddenTSlash;
        case 2:  return this.namelistGroupName;
        case 3:  return this.hiddenTSlash2;
        case 4:  return this.variableName;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTSlash = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.namelistGroupName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTSlash2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.variableName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

