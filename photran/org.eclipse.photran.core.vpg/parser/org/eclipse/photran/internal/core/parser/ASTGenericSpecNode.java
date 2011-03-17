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

import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings("all")
public class ASTGenericSpecNode extends ASTNode implements IAccessId
{
    org.eclipse.photran.internal.core.lexer.Token isDerivedTypeIO; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token isDefinedOperator; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token isAssignmentOperator; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTGenericSpecNode
    IDefinedOperator definedOperator; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token formattingSpec; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token equalsToken; // in ASTGenericSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTGenericSpecNode

    public boolean isDerivedTypeIO()
    {
        return this.isDerivedTypeIO != null;
    }

    public void setIsDerivedTypeIO(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDerivedTypeIO = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isDefinedOperator()
    {
        return this.isDefinedOperator != null;
    }

    public void setIsDefinedOperator(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDefinedOperator = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isAssignmentOperator()
    {
        return this.isAssignmentOperator != null;
    }

    public void setIsAssignmentOperator(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAssignmentOperator = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IDefinedOperator getDefinedOperator()
    {
        return this.definedOperator;
    }

    public void setDefinedOperator(IDefinedOperator newValue)
    {
        this.definedOperator = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getFormattingSpec()
    {
        return this.formattingSpec;
    }

    public void setFormattingSpec(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.formattingSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getEqualsToken()
    {
        return this.equalsToken;
    }

    public void setEqualsToken(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.equalsToken = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTGenericSpecNode(this);
        visitor.visitIAccessId(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 8;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isDerivedTypeIO;
        case 1:  return this.isDefinedOperator;
        case 2:  return this.isAssignmentOperator;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.definedOperator;
        case 5:  return this.formattingSpec;
        case 6:  return this.equalsToken;
        case 7:  return this.hiddenTRparen;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isDerivedTypeIO = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.isDefinedOperator = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.isAssignmentOperator = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.definedOperator = (IDefinedOperator)value; if (value != null) value.setParent(this); return;
        case 5:  this.formattingSpec = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.equalsToken = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

