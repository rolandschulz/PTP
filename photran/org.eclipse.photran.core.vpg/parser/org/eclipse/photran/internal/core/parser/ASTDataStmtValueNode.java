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
public class ASTDataStmtValueNode extends ASTNode
{
    ASTNamedConstantUseNode namedConstKind; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token hasConstIntKind; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token isNull; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTDataStmtValueNode
    ASTConstantNode constant; // in ASTDataStmtValueNode

    public ASTNamedConstantUseNode getNamedConstKind()
    {
        return this.namedConstKind;
    }

    public void setNamedConstKind(ASTNamedConstantUseNode newValue)
    {
        this.namedConstKind = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasConstIntKind()
    {
        return this.hasConstIntKind != null;
    }

    public void setHasConstIntKind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasConstIntKind = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isNull()
    {
        return this.isNull != null;
    }

    public void setIsNull(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isNull = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTConstantNode getConstant()
    {
        return this.constant;
    }

    public void setConstant(ASTConstantNode newValue)
    {
        this.constant = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDataStmtValueNode(this);
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
        case 0:  return this.namedConstKind;
        case 1:  return this.hasConstIntKind;
        case 2:  return this.hiddenTAsterisk;
        case 3:  return this.isNull;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.constant;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.namedConstKind = (ASTNamedConstantUseNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.hasConstIntKind = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.isNull = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.constant = (ASTConstantNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

