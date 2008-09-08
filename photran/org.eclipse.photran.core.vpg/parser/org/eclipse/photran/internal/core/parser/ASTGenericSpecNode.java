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

public class ASTGenericSpecNode extends ASTNode implements IAccessId
{
    org.eclipse.photran.internal.core.lexer.Token isDefinedOperator; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token isAssignmentOperator; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTGenericSpecNode
    IDefinedOperator definedOperator; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token equalsToken; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTGenericSpecNode

    public boolean isDefinedOperator()
    {
        return this.isDefinedOperator != null;
    }

    public void setIsDefinedOperator(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDefinedOperator = newValue;
    }


    public boolean isAssignmentOperator()
    {
        return this.isAssignmentOperator != null;
    }

    public void setIsAssignmentOperator(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAssignmentOperator = newValue;
    }


    public IDefinedOperator getDefinedOperator()
    {
        return this.definedOperator;
    }

    public void setDefinedOperator(IDefinedOperator newValue)
    {
        this.definedOperator = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getEqualsToken()
    {
        return this.equalsToken;
    }

    public void setEqualsToken(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.equalsToken = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTGenericSpecNode(this);
        visitor.visitIAccessId(this);
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
        case 0:  return this.isDefinedOperator;
        case 1:  return this.isAssignmentOperator;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.definedOperator;
        case 4:  return this.equalsToken;
        case 5:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isDefinedOperator = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.isAssignmentOperator = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.definedOperator = (IDefinedOperator)value; return;
        case 4:  this.equalsToken = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

